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

// TODO change package
package com.idylwood.yahoo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.idylwood.utils.MathUtils;
import com.idylwood.yahoo.YahooFinance.DivSplitTable;
import com.idylwood.yahoo.YahooFinance.DivTable;
import com.idylwood.yahoo.YahooFinance.Single;
import com.idylwood.yahoo.YahooFinance.SplitTable;


public class HistTable
{
	final public String symbol;
	final public long dateAccessed;
	// Note: this is not thread safe! assume there are no methods which modify this!
	// TODO make this an immutable list. note that a lot of these methods could be optimized better if the data structures were immutable.
	final public List<HistRow> data;
	public boolean splitAdjusted; // TODO do something with these two things
	public boolean dividendAdjusted;
	public HistTable(String symbol, long dateAccessed,
			List<HistRow> data)
	{
		this.symbol=symbol; this.dateAccessed = dateAccessed; this.data = data;
	}

	// Returns a newly allocated HistTable. Not just a 'view'.
	// If the start and end date are not within the table's start and end,
	// it will return a newly allocated HistTable with not very much (nothing) in it
	public HistTable SubTable(Date start, Date end)
	{
		final List<HistRow> list = new ArrayList<HistRow>(end.subtract(start));
		HistTable ret = new HistTable(this.symbol,this.dateAccessed,list);
		for (HistRow row : this.data)
			if (row.date.compareTo(start)>=0 && row.date.compareTo(end)<= 0)
				ret.data.add(new HistRow(row));
		return ret;
	}

	public Date StartDate()
	{
		return data.get(0).date;
	}

	public Date EndDate()
	{
		return data.get(data.size()-1).date;
	}

	public List<Date> Dates()
	{
		List<Date> ret = new ArrayList<Date>(data.size());
		for (HistRow row : data)
			ret.add(row.date);
		return ret;
	}

	// Extracts the closing prices as list of Double objects.
	public List<Double> Close()
	{
		List<Double> ret = new ArrayList<Double>(data.size());
		for (HistRow row : data)
			ret.add(row.close);
		return ret;
	}
	// Extracts the closing prices as array of primitive doubles.
	// This should be used if speed is needed. Its List counterpart
	// Close() is both slower to create (four times slower by my benchmark)
	// and slower to manipulate.
	public double[] CloseArray()
	{
		double[] ret = new double[data.size()];
		// iterating backwards is faster
		for (int i = ret.length; i--!=0;)
			ret[i] = data.get(i).close;
		return ret;
	}
	public List<Double> High()
	{
		List<Double> ret = new ArrayList<Double>(data.size());
		for (HistRow row : data)
			ret.add(row.high);
		return ret;
	}
	public double[] HighArray()
	{
		double[] ret = new double[data.size()];
		for (int i = ret.length; 0!=i--; )
			ret[i] = data.get(i).high;
		return ret;
	}
	public List<Double> Low()
	{
		List<Double> ret = new ArrayList<Double>(data.size());
		for (HistRow row : data)
			ret.add(row.low);
		return ret;
	}
	public double[] LowArray()
	{
		double[] ret = new double[data.size()];
		for (int i = ret.length; 0!=i--;)
			ret[i] = data.get(i).low;
		return ret;
	}
	public List<Double> Open()
	{
		List<Double> ret = new ArrayList<Double>(data.size());
		for (HistRow row : data)
			ret.add(row.open);
		return ret;
	}
	public double[] OpenArray()
	{
		double[] ret = new double[data.size()];
		for (int i = ret.length; i--!=0; )
			ret[i] = data.get(i).open;
		return ret;
	}
	public List<Double> AdjustedClose()
	{
		List<Double> ret = new ArrayList<Double>(data.size());
		for (HistRow row : data)
			ret.add(row.adj_close);
		return ret;
	}
	public double[] AdjustedCloseArray()
	{
		double[] ret = new double[data.size()];
		for (int i = ret.length; 0!=i--; )
			ret[i] = data.get(i).adj_close;
		return ret;
	}
	public List<Integer> Volume()
	{
		List<Integer> ret = new ArrayList<Integer>(data.size());
		for (HistRow row : data)
			ret.add(row.volume);
		return ret;
	}
	public int[] VolumeArray()
	{
		int[] ret = new int[data.size()];
		for (int i = ret.length; 0!=i--; )
			ret[i] = data.get(i).volume;
		return ret;
	}

	// Pre: All the tables have the same starting and ending dates. otherwise
	// the result will be undefined.
	// This function calculates the result of reinvesting dividends
	HistTable AdjustReinvestedDividends(DivTable dividends)
	{
		HistTable ret = new HistTable(this.symbol,this.dateAccessed,new ArrayList<HistRow>(this.data.size()));
		List<YahooFinance.Single> ratios = new ArrayList<YahooFinance.Single>(dividends.data.size()+1);
		ratios.add(new YahooFinance.Single(this.data.get(0).date,1));

		int idx = 0;
		for (YahooFinance.Single s : dividends.data)
		{
			// calculate the running ratio (this represents the number of shares
			// held after reinvesting dividends)
			HistRow row = null;
			while (true)
			{
				row = this.data.get(idx++);
				if (0==row.date.compareTo((Date)s)) break;
			}
			double ratio = 1 + (s.data / row.close); // the percentage + 1.
			ratios.add(new YahooFinance.Single(s,ratio)); // recall the constructor Single(Date, double)
		}
		// running ratio
		for (int i = 1; i < ratios.size(); ++i)
			ratios.get(i).data *= ratios.get(i-1).data;

		int j = 0;
		for (int i = 0; i < this.data.size(); i++)
		{
			HistRow row = this.data.get(i);
			// find the correct ex-date
			if (j < ratios.size() - 1 && ratios.get(j+1).toInt()==row.date.toInt())
				++j;
			final double ratio = ratios.get(j).data;
			ret.data.add(new HistRow(row.date,
						MathUtils.roundToCent(row.open * ratio),
						MathUtils.roundToCent(row.high * ratio),
						MathUtils.roundToCent(row.low * ratio),
						MathUtils.roundToCent(row.close * ratio),
						row.volume,
						0 // adj_close undefined.
						));
		}
		return ret;
	}

	// This function calculates the effect of dividends. This is more correct
	// than the adjusted close price given by Yahoo Finance
	HistTable AdjustDividends(DivTable arg)
	{
		HistTable ret = new HistTable(this.symbol, this.dateAccessed, new ArrayList<HistRow>(this.data.size()));
		List<YahooFinance.Single> dividends = new ArrayList<YahooFinance.Single>(arg.data.size()+1);
		dividends.add(new YahooFinance.Single(this.data.get(0).date, 0)); // add a zero dividend
		// calculate the running sum of the dividends
		for (int i = 0; i < arg.data.size(); ++i)
		{
			YahooFinance.Single s = new YahooFinance.Single(arg.data.get(i));
			if (0!=i)
				s.data += dividends.get(i-1).data;
			s.data = MathUtils.roundToCent(s.data);
			dividends.add(s);
		}

		// TODO refactor this to get rid of repeated complicated code?
		int j = 0;
		for (int i = 0; i < this.data.size(); ++i)
		{
			HistRow row = this.data.get(i);
			// figure out if need to move to the next ex-date.
			// TODO double check correctness
			if (j < dividends.size() - 1 && row.date.toInt()==dividends.get(j+1).toInt())
				++j;
			final double dividend = dividends.get(j).data;
			ret.data.add(new HistRow(row.date,
						MathUtils.roundToCent(row.open + dividend),
						MathUtils.roundToCent(row.high + dividend),
						MathUtils.roundToCent(row.low + dividend),
						MathUtils.roundToCent(row.close + dividend),
						row.volume,
						0 // adj_close undefined
						));
		}
		return ret;
	}

	private HistTable AdjustSplits(SplitTable splits)
	{
		HistTable ret = new HistTable(this.symbol,
				this.dateAccessed,
				new ArrayList<HistRow>(this.data.size()));

		List<Single> ratios = splits.RunningRatios(this.StartDate());

		// TODO refactor this to get rid of repeated code.
		int j = 0;
		for (int i = 0; i < this.data.size(); i++)
		{
			HistRow row = this.data.get(i);

			if (j < ratios.size() - 1 && ratios.get(j+1).toInt()==row.date.toInt())
				++j;

			// adjust splits forwards in time instead of backwards. TODO comment more clearly
			final double finalRatio = ratios.get(ratios.size()-1).data;
			final double ratio = ratios.get(j).data;
			ret.data.add(new HistRow(row.date,
						MathUtils.roundToCent((row.open / ratio) * finalRatio),
						MathUtils.roundToCent((row.high / ratio) * finalRatio),
						MathUtils.roundToCent((row.low / ratio) * finalRatio),
						MathUtils.roundToCent((row.close / ratio) * finalRatio),
						row.volume,
						0 // adj_close undefined.
						));
		}
		return ret;
	}

	public HistTable AdjustReinvestedDividends()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(this.symbol,this.StartDate(),this.EndDate());
		return this.AdjustReinvestedDividends(dst.DivTable());
	}

	public HistTable AdjustDividends()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(this.symbol,this.StartDate(),this.EndDate());
		return this.AdjustDividends(dst.DivTable());
	}

	public HistTable AdjustSplits()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(symbol, this.StartDate(), this.EndDate());
		return this.AdjustSplits(dst.SplitTable());
	}

	// Adjust OHLC for dividends and splits
	// Returns newly allocated table with no side effects.
	public HistTable AdjustOHLC()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		// retarded way of checking if dividends are already split adjusted.
		if (yf.HistoricalDividends(symbol, this.StartDate(), this.EndDate()).splitAdjusted)
			return this.AdjustSplits().AdjustDividends();

		return this.AdjustDividends().AdjustSplits();
	}

	public HistTable AdjustOHLCWithReinvestment()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		// retarded way of checking if dividends are already split adjusted.
		if (yf.HistoricalDividends(symbol, this.StartDate(), this.EndDate()).splitAdjusted)
			return this.AdjustSplits().AdjustReinvestedDividends();

		return this.AdjustReinvestedDividends().AdjustSplits();
	}
}
