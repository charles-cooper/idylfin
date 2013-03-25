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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.idylwood.utils.MathUtils.LinearRegression;
import com.idylwood.yahoo.Date;
import com.idylwood.yahoo.HistRow;
import com.idylwood.yahoo.HistTable;
import com.idylwood.yahoo.YahooFinance;
import com.idylwood.yahoo.YahooFinance.DivTable;
import com.idylwood.yahoo.YahooFinance.Pair;
import com.idylwood.yahoo.YahooFinance.SplitTable;
import com.idylwood.yahoo.YahooFinance.Single;

public class FinUtils {
	
	// Takes two HistTables, and extracts the adjusted close prices
	// Removes rows where the days don't match
	// Similar to quantmod merge.
	// No side effects.
	public static List<Pair> merge(HistTable table1, HistTable table2)
	{
		final int len = Math.min(table1.data.size(), table2.data.size());
		List<Pair> ret = new ArrayList<Pair>(len);
		int idx1 = 0;
		int idx2 = 0;
		while (true)
		{
			if (idx1==table1.data.size()) break;
			if (idx2==table2.data.size()) break;
			HistRow row1 = table1.data.get(idx1);
			HistRow row2 = table2.data.get(idx2);
			if (row1.date.toInt() > row2.date.toInt())
			{
				++idx2;
				continue;
			}
			if (row1.date.toInt() < row2.date.toInt())
			{
				++idx1;
				continue;
			}
			YahooFinance.Pair pair = new YahooFinance.Pair();
			pair.date = new Date(row1.date.toInt());
			pair.first = row1.adj_close;
			pair.second = row2.adj_close;
			ret.add(pair);
	
			++idx1;
			++idx2;
		}
		return ret;
	}

	final public static double SharpeRatio(final double[] data, double[] benchmark)
	{
		final double[] logReturns = MathUtils.diff(MathUtils.log(data));
		final double[] benchmarkReturns = MathUtils.diff(MathUtils.log(benchmark));
		final double [] difference = MathUtils.subtract(logReturns,benchmarkReturns);
		final double mean = MathUtils.mean(difference);
		return mean / MathUtils.stdev(difference,mean);
	}

	public final static double[] logReturns(final double[] data)
	{
		return MathUtils.diff(MathUtils.log(data));
	}
	
	public static final double SharpeRatio(final HistTable data, final HistTable benchmark)
	{
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
		return SharpeRatio(data.CloseArray(), riskFreeRate);
	}

	// the new sharpe ratio is literally identical to the information ratio
	final public static double InformationRatio(final double[] data, double[] benchmark)
	{
		return SharpeRatio(data,benchmark);
	}
	
	final public static double InformationRatio(final HistTable data, final HistTable benchmark)
	{
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
	final public static double JensensAlpha(final HistTable data,
			final HistTable benchmark, final double riskFreeRate)
	{
		return JensensAlpha(data.CloseArray(), benchmark.CloseArray(), riskFreeRate);
	}

	// numerical precision not guaranteed
	final public static double totalLogReturn(final double data[])
	{
		return Math.log(FinUtils.totalReturn(data));
	}
	final public static double totalLogReturn(final HistTable data)
	{
		return totalLogReturn(data.CloseArray());
	}

	final public static double totalReturn(final double data[])
	{
		return data[data.length-1] / data[0];
	}
	public static final double totalReturn(final HistTable data)
	{
		return totalReturn(data.CloseArray());
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
		return TreynorRatio(data.CloseArray(), benchmark.CloseArray());
	}

	final public static double CalmarRatio(final double[] data)
	{
		final double averageDailyReturn = MathUtils.mean(MathUtils.diff(MathUtils.log(data)));
		return (averageDailyReturn * 250) / FinUtils.MaximumDrawdown(data);
	}
	public static final double CalmarRatio(final HistTable data)
	{
		return CalmarRatio(data.CloseArray());
	}

	// returns max drawdown in log percent
	final public static double MaximumDrawdown(final double[] data)
	{
		double maxDrawdown = 0;
		double peak = 0;
		for (double x : data)
		{
			if (peak < x)
			{
				peak = x;
				continue;
			}
			double drawdown = -Math.log(x / peak);
			if (drawdown < 0) throw new RuntimeException("You have bug!");
			if (drawdown > maxDrawdown) maxDrawdown = drawdown;
		}
		return maxDrawdown;
	}
	
	public final static double MaximumDrawdown(final HistTable data)
	{
		return MaximumDrawdown(data.CloseArray());
	}

	// returns empirical VAR as log percentage
	final public static double VAR(final double [] data, final double threshold)
	{
		if (0.0 > threshold || 1.0 < threshold)
			throw new ArithmeticException("Bad threshold parameter: "+threshold);
		final double [] logReturns = MathUtils.diff(MathUtils.log(data));
		Arrays.sort(logReturns);
		int idx = (int)(threshold * data.length);
		return data[idx];
	}
	
	final public static double VAR(final HistTable data, final double threshold)
	{
		return VAR(data.CloseArray(), threshold);
	}

	// returns empirical CVAR as log percentage
	// Threshold must be between 0 and 1.
	final public static double CVAR(double [] data, double threshold)
	{
		if (0.0 > threshold || 1.0 < threshold)
			throw new ArithmeticException("Bad threshold parameter: "+threshold);
		final double [] logReturns = MathUtils.diff(MathUtils.log(data));
		Arrays.sort(logReturns);
		int idx = (int)(threshold * data.length);
		double [] underThreshold = Arrays.copyOfRange(logReturns,0,idx+1);
		return MathUtils.sum(underThreshold);
	}
	final public static double CVAR(final HistTable data, double threshold)
	{
		return CVAR(data.CloseArray(),threshold);
	}

	// Pre: all the tables have the same start and end date and have the same
	// number of rows.
	// TODO make this public
	final static double[] MarkowitzPortfolio(final java.util.List<HistTable> tables)
	{
		final double[][] data = new double[tables.size()][];
		int i = 0;
		for (HistTable ht : tables)
			data[i++] = MathUtils.diff(MathUtils.log(ht.CloseArray())); // yay hammering malloc

		final double[][] covariance = MathUtils.covariance(data);
		final double[] returns = new double[data.length];
		for (i = 0; i < returns.length; ++i)
			returns[i] = totalLogReturn(data[i]);
		final double riskTolerance = 0.15;
		return OptimizationUtils.cvxSolve(covariance,returns,riskTolerance);
	}
}
