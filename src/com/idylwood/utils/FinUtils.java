/*
 * ====================================================
 * Copyright (C) 2013 by Idylwood Technologies, LLC. All rights reserved.
 *
 * Developed at Idylwood Technologies, LLC.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * The License should have been distributed to you with the source tree.
 * If not, it can be found at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Charles Cooper
 * Date: 2013
 * ====================================================
 */
package com.idylwood.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.idylwood.utils.MathUtils.LinearRegression;
import com.idylwood.yahoo.Date;
import com.idylwood.yahoo.HistRow;
import com.idylwood.yahoo.HistTable;
import com.idylwood.yahoo.Quote;
import com.idylwood.yahoo.YahooFinance;
import com.idylwood.yahoo.YahooFinance.Pair;

public class FinUtils {

	/**
	 * Takes two HistTables, and extracts the adjusted close prices.
	 * Removes rows where the days don't match.
	 * Similar to quantmod merge.
	 * No side effects.
	 * @param table1
	 * @param table2
	 * @return
	 */
	// TODO change this into some sort of 2D matrix.
	public static List<Pair> merge(HistTable table1, HistTable table2)
	{
		final int len = Math.min(table1.size(), table2.size());
		List<Pair> ret = new ArrayList<Pair>(len);
		int idx1 = 0;
		int idx2 = 0;
		//while (true)
		for (; idx1!=table1.size() && idx2!=table2.size(); idx1++,idx2++)
		{
			//if (idx1==table1.data.size()) break;
			//if (idx2==table2.data.size()) break;
			HistRow row1 = table1.get(idx1);
			HistRow row2 = table2.get(idx2);
			if (row1.toInt() > row2.toInt())
			{
				++idx2;
				continue;
			}
			if (row1.toInt() < row2.toInt())
			{
				++idx1;
				continue;
			}
			YahooFinance.Pair pair = new YahooFinance.Pair();
			pair.date = new Date(row1.toInt());
			pair.first = row1.adj_close;
			pair.second = row2.adj_close;
			ret.add(pair);
		}
		return ret;
	}

	final public static double SharpeRatio(final double[] data, double[] benchmark)
	{
		final double[] logReturns = MathUtils.diff(MathUtils.log(data));
		final double[] benchmarkReturns = MathUtils.diff(MathUtils.log(benchmark));
		final double[] difference = MathUtils.subtract(logReturns,benchmarkReturns);
		final double mean = MathUtils.mean(difference);
		return mean / MathUtils.stdev(difference,mean);
	}

	public final static double[] logReturns(final double[] data)
	{
		return MathUtils.diff(MathUtils.log(data));
	}

	/**
	 * Calculates the Sharpe ratio of data with respect to benchmark
	 * @throws IllegalArgumentException if either data or benchmark are not adjusted
	 * @param data
	 * @param benchmark
	 * @return
	 */
	public static final double SharpeRatio(final HistTable data, final HistTable benchmark)
	{
		checkAdjusted(data,benchmark);
		return SharpeRatio(data.CloseArray(), benchmark.CloseArray());
	}

	// assuming the riskFreeRate is constant and stuff
	final public static double SharpeRatio(final double [] data, final double riskFreeRate)
	{
		// risk free rate is in percentage so make it log
		final double logRiskFreeRate = Math.log(1+riskFreeRate);
		final double [] logReturns = MathUtils.diff(MathUtils.log(data));
		final double [] adjustedReturns = MathUtils.shift(logReturns,-logRiskFreeRate);
		final double mean = MathUtils.mean(adjustedReturns);
		return mean / MathUtils.stdev(adjustedReturns,mean);
	}

	public static final double SharpeRatio(final HistTable data, final double riskFreeRate)
	{
		checkAdjusted(data);
		return SharpeRatio(data.CloseArray(), riskFreeRate);
	}

	// the new sharpe ratio is literally identical to the information ratio
	final public static double InformationRatio(final double[] data, double[] benchmark)
	{
		return SharpeRatio(data,benchmark);
	}
	
	final public static double InformationRatio(final HistTable data, final HistTable benchmark)
	{
		checkAdjusted(data,benchmark);
		return InformationRatio(data.CloseArray(),benchmark.CloseArray());
	}

	// TODO
	/*
	final public static double SortinoRatio()
	   {
		   return 0;
	   }
	   */

	final public static double JensensAlpha(final double data[], final double[] benchmark, final double riskFreeRate)
	{
		// TODO double check this math from Wikipedia(!)
		// alpha_J = R_i - [R_f + beta_iM * (R_M - R_f)]
		// alpha_J Jensen's alpha
		// R_i risk free rate
		// R_f portfolio return
		// R_M market return
		// beta_iM beta of portfolio wrt market
		LinearRegression regress = MathUtils.regress(data,benchmark);
		return MathUtils.sum(FinUtils.totalLogReturn(data),-riskFreeRate,-regress.slope * FinUtils.totalLogReturn(benchmark), regress.slope*riskFreeRate);
	}
	final public static double JensensAlpha(final HistTable data, final HistTable benchmark, final double riskFreeRate)
	{
		checkAdjusted(data,benchmark);
		return JensensAlpha(data.CloseArray(), benchmark.CloseArray(), riskFreeRate);
	}

	// numerical precision not guaranteed
	final public static double totalLogReturn(final double data[])
	{
		return Math.log(FinUtils.totalReturn(data));
	}
	final public static double totalLogReturn(final HistTable data)
	{
		return Math.log(FinUtils.totalReturn(data));
	}

	final public static double totalReturn(final double data[])
	{
		return data[data.length-1] / data[0];
	}
	public static final double totalReturn(final HistTable table)
	{
		checkAdjusted(table);
		return table.get(table.size()-1).close / table.get(0).close;
	}

	final public static double TreynorRatio(final double[] data, double [] benchmark)
	{
		final double[] logReturns = MathUtils.diff(MathUtils.log(data));
		final double [] benchmarkReturns = MathUtils.diff(MathUtils.log(data));
		final LinearRegression regress = MathUtils.regress(logReturns, benchmarkReturns);
		return MathUtils.mean(MathUtils.subtract(logReturns,benchmarkReturns)) / regress.slope;
	}
	public static final double TreynorRatio(final HistTable data, final HistTable benchmark)
	{
		if (!data.adjusted()||!benchmark.adjusted())
			throw new IllegalArgumentException("Must be calculated on adjusted data");
		return TreynorRatio(data.CloseArray(), benchmark.CloseArray());
	}

	final public static double CalmarRatio(final double[] data)
	{
		final double averageDailyReturn = MathUtils.mean(MathUtils.diff(MathUtils.log(data)));
		return (averageDailyReturn * 250) / FinUtils.MaximumDrawdown(data);
	}
	public static final double CalmarRatio(final HistTable data)
	{
		checkAdjusted(data);
		return CalmarRatio(data.CloseArray());
	}

	// returns max drawdown in log percent
	final public static double MaximumDrawdown(final double[] data)
	{
		double maxDrawdown = 0;
		double peak = 0;
		for (final double x : data)
		{
			if (peak < x)
			{
				peak = x;
				continue;
			}
			final double drawdown = -Math.log(x / peak);
			if (drawdown < 0) throw new RuntimeException("You have bug!");
			if (drawdown > maxDrawdown) maxDrawdown = drawdown;
		}
		return maxDrawdown;
	}

	/**
	 * Throws IllegalArgumentException if any arguments are not adjusted
	 * @param tables
	 */
	private final static void checkAdjusted(final HistTable... tables)
	{
		for (HistTable ht : tables)
			if (!ht.adjusted())
				throw new IllegalArgumentException("Must be calculated on adjusted data");
	}

	public final static double MaximumDrawdown(final HistTable data)
	{
		checkAdjusted(data);
		return MaximumDrawdown(data.CloseArray());
	}

	// returns empirical VAR as log percentage
	final public static double VAR(final double [] data, final double threshold)
	{
		if (0.0 > threshold || 1.0 < threshold)
			throw new IllegalArgumentException("Bad threshold parameter: "+threshold);
		final double [] logReturns = MathUtils.diff(MathUtils.log(data));
		Arrays.sort(logReturns);
		int idx = (int)(threshold * data.length);
		return data[idx];
	}
	
	final public static double VAR(final HistTable data, final double threshold)
	{
		checkAdjusted(data);
		return VAR(data.CloseArray(), threshold);
	}

	// returns empirical CVAR as log percentage
	// Threshold must be between 0 and 1.
	final public static double CVAR(double [] data, double threshold)
	{
		if (0.0 >= threshold || 1.0 < threshold)
			throw new IllegalArgumentException("Bad threshold parameter: "+threshold);
		final double [] logReturns = MathUtils.sort(MathUtils.diff(MathUtils.log(data)));
		int idx = (int)Math.ceil(threshold * logReturns.length); // ceiling
		final double [] underThreshold = MathUtils.copyOfRange(logReturns,0,idx);
		return MathUtils.sum(underThreshold) / underThreshold.length;
	}

	final public static double CVAR(final HistTable data, double threshold)
		throws IOException
	{
		checkAdjusted(data);
		return CVAR(data.CloseArray(),threshold);
	}

	/**
	 * Finds the Markowitz Portfolio for the given return
	 * Right now it does not handle edge cases, e.g. the tables have
	 * different start and end dates, so it is the responsibility of
	 * the user to make sure the data is clean, otherwise you may get
	 * weird bugs or undefined behavior. Obviously I am planning to
	 * fix this down the line.
	 * @param adjusted_tables
	 * @param portfolio_return
	 * @return
	 */
	public final static double[] MarkowitzPortfolio(final HistTable[] adjusted_tables, final double portfolio_return)
	{
		checkAdjusted(adjusted_tables);
		final double[][] data = new double[adjusted_tables.length][];
		int i = 0;
		for (HistTable ht : adjusted_tables)
			data[i++] = MathUtils.diff(MathUtils.log(ht.CloseArray())); // yay hammering malloc
		final int row_len = data[0].length;

		final double[][] covariance = MathUtils.covariance(data);
		final double[] returns = new double[data.length]; //==tables.size()
		for (i = 0; i < returns.length; ++i)
			returns[i] = data[i][row_len-1]-data[i][0]; //i.e. totalLogReturn(tables.get(i));
		return OptimizationUtils.MarkowitzSolve(covariance,returns,portfolio_return);
	}
	/**
	 * Parameter free Markowitz Portfolio.
	 * Basically calculates Markowitz portfolio for portfolio_return = mean return of constituents
	 * @param adjusted_tables
	 * @return
	 */
	public static final double[] MarkowitzPortfolio(final HistTable[] adjusted_tables)
	{
		checkAdjusted(adjusted_tables);
		final double[] mean_returns = new double[adjusted_tables.length];
		for (int i = 0; i < adjusted_tables.length; i++)
			mean_returns[i] = FinUtils.totalLogReturn(adjusted_tables[i]) / adjusted_tables[i].size();
		final double mean_return = MathUtils.abs(MathUtils.mean(mean_returns));
		return MarkowitzPortfolio(adjusted_tables, mean_return);
	}
	public static final double[] weightByEarnings(List<Quote> quotes)
	{
		final double[] earnings = new double[quotes.size()];
		int i = 0;
		for (final Quote q : quotes)
			earnings[i++] = q.earnings_per_share * q.market_cap / q.last_price;
		return MathUtils.normalize(earnings);
	}
	public static final double[] weightByDividends(List<Quote> quotes)
	{
		final double[] divs = new double[quotes.size()];
		int i = 0;
		for (final Quote q : quotes)
			divs[i++] = q.dividend_per_share * q.market_cap / q.last_price;
		return MathUtils.normalize(divs);
	}
	public static final double[] weightByMarketCap(List<Quote> quotes)
	{
		// this would be so much simpler with closures and better collections!
		final double[] market_caps = new double[quotes.size()];
		int i = 0;
		for (final Quote q : quotes)
			market_caps[i++] = q.market_cap;
		return MathUtils.normalize(market_caps);
	}
	public static final double[] weightByDividendYield(List<Quote> quotes)
	{
		final double[] div_yields = new double[quotes.size()];
		int i = 0;
		for (final Quote q : quotes)
			div_yields[i++] = q.dividend_yield;
		return MathUtils.normalize(div_yields);
	}

	public static void main(String[] args)
		throws java.io.IOException
	{
		YahooFinance.logTime("start");
		YahooFinance.getInstance().HistoricalPrices("AAPL");
		YahooFinance.logTime("downloaded");
		double cvar = 0;
		for (int i = 0; i < 1000; i++)
			cvar = CVAR(YahooFinance.getInstance().HistoricalPrices("AAPL",new Date(20070101),new Date(20130405)),1);
		YahooFinance.logTime("calculated "+cvar);
		YahooFinance.getInstance().HistoricalPrices("MSFT");
	}
}

