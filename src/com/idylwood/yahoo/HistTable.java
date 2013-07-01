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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import com.idylwood.utils.MathUtils;
import com.idylwood.yahoo.YahooFinance.DivSplitTable;
import com.idylwood.yahoo.YahooFinance.DivTable;
import com.idylwood.yahoo.YahooFinance.Single;
import com.idylwood.yahoo.YahooFinance.SplitTable;


public class HistTable implements Iterable<HistRow>
{
	final public String symbol;
	final public long dateAccessed;
	// Note: this is not thread safe! assume there are no methods which modify this!
	// TODO make this an immutable list. note that a lot of these methods could be optimized better
	// if the data structures were immutable.
	final private List<HistRow> data; // TODO this should become private.
	public boolean splitAdjusted = false; // TODO declare these final.
	public boolean dividendAdjusted = false;
	public HistTable(final String symbol, final long dateAccessed, final List<HistRow> data)
	{
		this.symbol=symbol; this.dateAccessed = dateAccessed;
		this.data = Collections.unmodifiableList(data);
	}
	/**
	 * Returns true iff it has been both split and dividend adjusted.
	 * Side effects: none
	 * @return
	 */
	final public boolean adjusted()
	{
		return splitAdjusted && dividendAdjusted;
	}

	/**
	 * Returns a newly allocated HistTable whose internal data is a view of the parent's data.
	 * If the start and end date are not within the table's start and end,
	 * it will return a newly allocated HistTable with not very much (nothing) in it
	 * Note that you should be careful when adjusting for splits and dividends; i.e.
	 * table#SubTable#AdjustOHLC() is NOT the same as table#AdjustOHLC()#SubTable
	 * since #AdjustOHLC() always adjusts to the first element in the table.
	 * Side Effects: allocation of new HistTable
	 * @param start
	 * @param end
	 * @return
	 */
	public HistTable SubTable(final Date start, final Date end)
	{
		int start_idx = ceil_idx(start);
		int end_idx = floor_idx(end);
		return new HistTable(this.symbol, this.dateAccessed, this.data.subList(start_idx, end_idx));
	}
	/**
	 * Returns the date of the first entry.
	 * Side Effects: none
	 * @return
	 */
	public Date StartDate()
	{
		return data.get(0).date;
	}
	/**
	 * Returns the date of the last entry.
	 * Side Effects: none
	 * @return
	 */
	public Date EndDate()
	{
		return data.get(data.size()-1).date;
	}

	// if it's not in the list, returns ~insertion_point as in Collections#binarySearch
	public int idx(final Date date)
	{
		return Collections.binarySearch(this.data, date);
	}
	// if it's not in the list, returns the first date before it.
	private int floor_idx(final Date date)
	{
		final int idx = idx(date);
		if (idx < 0)
			return ~idx - 1;
		return idx;
	}
	// if it's not in the list, returns the first date after it.
	private int ceil_idx(final Date date)
	{
		final int idx = idx(date);
		if (idx < 0)
			return ~idx;
		return idx;
	}
	// if it's not in the list, returns null. for something more robust, use getCeiling or getFloor
	public HistRow get(final Date date)
	{
		final int idx = idx(date);
		if (idx < 0)
			return null;
		return get(idx);
	}
	public int size()
	{
		return data.size();
	}
	public HistRow getCeiling(final Date date)
	{
		final int idx = ceil_idx(date);
		if (idx < -1)
			throw new RuntimeException("You have bug!");
		if (idx < 0)
			return null;
		return get(idx);
	}
	public HistRow getFloor(final Date date)
	{
		final int idx = floor_idx(date);
		if (idx < -1)
			throw new RuntimeException("You have bug!");
		if (idx < 0)
			return null;
		return get(idx);
	}
	public HistRow get(final int idx)
	{
		return data.get(idx);
	}
	@Override public Iterator<HistRow> iterator()
	{
		return data.iterator();
	}

	/**
	 * Returns newly allocated list with the dates of the rows of the table.
	 * @sideeffects Allocation of new List
	 * @return
	 */
	public List<Date> Dates()
	{
		final List<Date> ret = new ArrayList<Date>(data.size());
		for (final HistRow row : data)
			ret.add(row.date);
		return ret;
	}

	// Extracts the closing prices as list of Double objects.
	public List<Double> Close()
	{
		final List<Double> ret = new ArrayList<Double>(data.size());
		for (final HistRow row : data)
			ret.add(row.close);
		return ret;
	}
	// Extracts the closing prices as array of primitive doubles.
	// This should be used if speed is needed. Its List counterpart
	// Close() is both slower to create (four times slower by my benchmark)
	// and slower to manipulate.
	public double[] CloseArray()
	{
		final double[] ret = new double[size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = data.get(i).close;
		return ret;
	}
	public List<Double> High()
	{
		final List<Double> ret = new ArrayList<Double>(data.size());
		for (final HistRow row : data)
			ret.add(row.high);
		return ret;
	}
	public double[] HighArray()
	{
		final double[] ret = new double[data.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = data.get(i).high;
		return ret;
	}
	public List<Double> Low()
	{
		final List<Double> ret = new ArrayList<Double>(data.size());
		for (final HistRow row : data)
			ret.add(row.low);
		return ret;
	}
	public double[] LowArray()
	{
		final double[] ret = new double[data.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = get(i).low;
		return ret;
	}
	public List<Double> Open()
	{
		final List<Double> ret = new ArrayList<Double>(data.size());
		for (final HistRow row : data)
			ret.add(row.open);
		return ret;
	}
	public double[] OpenArray()
	{
		final double[] ret = new double[data.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = get(i).open;
		return ret;
	}
	public List<Double> AdjustedClose()
	{
		final List<Double> ret = new ArrayList<Double>(data.size());
		for (final HistRow row : data)
			ret.add(row.adj_close);
		return ret;
	}
	public double[] AdjustedCloseArray()
	{
		final double[] ret = new double[data.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = data.get(i).adj_close;
		return ret;
	}
	public List<Integer> Volume()
	{
		final List<Integer> ret = new ArrayList<Integer>(data.size());
		for (final HistRow row : data)
			ret.add(row.volume);
		return ret;
	}
	public int[] VolumeArray()
	{
		final int[] ret = new int[data.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = data.get(i).volume;
		return ret;
	}

	// Pre: All the tables have the same starting and ending dates. otherwise
	// the result will be undefined.
	// This function calculates the result of reinvesting dividends
	private HistTable AdjustReinvestedDividends(final DivTable dividends)
	{
		if (this.dividendAdjusted)
			throw new IllegalArgumentException("This is already dividend adjusted!");
		final List<HistRow> ret_data = new ArrayList<HistRow>(this.size());

		final List<YahooFinance.Single> ratios = new ArrayList<YahooFinance.Single>(dividends.data.size()+1);
		ratios.add(new YahooFinance.Single(this.get(0).date,1));

		int idx = 0;
		for (YahooFinance.Single s : dividends.data)
		{
			// calculate the running ratio (this represents the number of shares
			// held after reinvesting dividends)
			HistRow row = null;
			while (true)
			{
				row = get(idx++);
				if (row.compareTo((Date)s) >= 0) break;
			}
			final double ratio = 1 + (s.data / row.close); // the percentage + 1.
			ratios.add(new YahooFinance.Single(s,ratio)); // recall the constructor Single(Date, double)
		}
		// running ratio
		for (int i = 1; i < ratios.size(); ++i)
			ratios.get(i).data *= ratios.get(i-1).data;

		int j = 0;
		for (int i = 0; i < this.size(); i++)
		{
			final HistRow row = get(i);
			// find the correct ex-date
			if (j < ratios.size() - 1 && ratios.get(j+1).toInt()==row.date.toInt())
				++j;
			final double ratio = ratios.get(j).data;
			ret_data.add(new HistRow(row.date,
						MathUtils.roundToCent(row.open * ratio),
						MathUtils.roundToCent(row.high * ratio),
						MathUtils.roundToCent(row.low * ratio),
						MathUtils.roundToCent(row.close * ratio),
						row.volume,
						0 // adj_close undefined.
						));
		}
		final HistTable ret = new HistTable(this.symbol,this.dateAccessed,ret_data);
		ret.dividendAdjusted = true;
		ret.splitAdjusted = this.splitAdjusted;
		return ret;
	}

	// This function calculates the effect of dividends. This is more correct
	// than the adjusted close price given by Yahoo Finance
	private HistTable AdjustDividends(final DivTable arg)
	{
		if (this.dividendAdjusted)
			throw new IllegalArgumentException("This is already dividend adjusted!");
		final List<HistRow> ret_data = new ArrayList<HistRow>(this.size());

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
		for (int i = 0; i < this.size(); ++i)
		{
			final HistRow row = this.get(i);
			// figure out if need to move to the next ex-date.
			// TODO double check correctness
			if (j < dividends.size() - 1 && 0==row.compareTo(dividends.get(j+1)))
				//	row.toInt()==dividends.get(j+1).toInt())
				++j;
			final double dividend = dividends.get(j).data;
			ret_data.add(new HistRow(row.date,
						MathUtils.roundToCent(row.open + dividend),
						MathUtils.roundToCent(row.high + dividend),
						MathUtils.roundToCent(row.low + dividend),
						MathUtils.roundToCent(row.close + dividend),
						row.volume,
						0 // adj_close undefined
						));
		}
		final HistTable ret = new HistTable(this.symbol, this.dateAccessed, ret_data);
		ret.dividendAdjusted = true;
		ret.splitAdjusted = this.splitAdjusted;
		return ret;
	}

	/**
	 * Adjusts for splits according to arg. If already split adjusted will throw
	 * an IllegalArgumentException
	 * @param splits
	 * @return newly allocated HistTable. No side effects.
	 */
	private HistTable AdjustSplits(SplitTable splits)
	{
		if (this.splitAdjusted)
			throw new IllegalArgumentException("This is already dividend adjusted!");
		final List<HistRow> ret_data = new ArrayList<HistRow>(this.size());

		final List<Single> ratios = splits.RunningRatios(this.StartDate());

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
			ret_data.add(new HistRow(row.date,
						MathUtils.roundToCent((row.open / ratio) * finalRatio),
						MathUtils.roundToCent((row.high / ratio) * finalRatio),
						MathUtils.roundToCent((row.low / ratio) * finalRatio),
						MathUtils.roundToCent((row.close / ratio) * finalRatio),
						row.volume,
						0 // adj_close undefined.
						));
		}
		final HistTable ret = new HistTable(this.symbol, this.dateAccessed, ret_data);
		ret.splitAdjusted = true;
		ret.dividendAdjusted = this.dividendAdjusted;
		return ret;
	}

	/**
	 * Adjusts for reinvested dividends. Adjusts forward, not backwards
	 * like most other adjusting algorithms.
	 * Will throw IllegalArgumentException if has already been dividend adjusted
	 * @return
	 * @throws IOException
	 */
	public HistTable AdjustReinvestedDividends()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(this.symbol,this.StartDate(),this.EndDate());
		return this.AdjustReinvestedDividends(dst.DivTable());
	}

	/**
	 * Adjusts for dividends without reinvestment. Adjusts forward, not backwards
	 * like most other adjusting algorithms.
	 * Will throw IllegalArgumentException if has already been dividend adjusted
	 * @return
	 * @throws IOException
	 */
	public HistTable AdjustDividends()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(this.symbol,this.StartDate(),this.EndDate());
		return this.AdjustDividends(dst.DivTable());
	}

	/**
	 * Adjusts for splits by downloading the split table from yahoo finance.
	 * If already split adjusted will throw an IllegalArgumentException
	 * @param splits
	 * @return
	 */
	public HistTable AdjustSplits()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		DivSplitTable dst = yf.HistoricalDivSplits(symbol, this.StartDate(), this.EndDate());
		return this.AdjustSplits(dst.SplitTable());
	}

	/**
	 * Adjust for dividends and splits. Returns newly allocated table with
	 * no side effects besides possibly downloading and caching data from Yahoo Finance.
	 * May throw an IllegalArgumentException if already adjusted for dividends or splits.
	 * @return
	 * @throws IOException
	 */
	public HistTable AdjustOHLC()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		// order of adjust splits and dividends matters depending on whether
		// dividends are split adjusted
		// retarded way of checking if dividends are already split adjusted.
		if (yf.HistoricalDividends(symbol, this.StartDate(), this.EndDate()).splitAdjusted)
			return this.AdjustSplits().AdjustDividends();

		return this.AdjustDividends().AdjustSplits();
	}

	/**
	 * Adjust for reinvested dividends and splits. Returns newly allocated table with
	 * no side effects besides possibly downloading and caching data from Yahoo Finance.
	 * May throw an IllegalArgumentException if already adjusted for dividends or splits.
	 * @return
	 * @throws IOException
	 */
	public HistTable AdjustOHLCWithReinvestment()
		throws IOException
	{
		YahooFinance yf = YahooFinance.getInstance();
		// retarded way of checking if dividends are already split adjusted.
		if (yf.HistoricalDividends(symbol, this.StartDate(), this.EndDate()).splitAdjusted)
			return this.AdjustSplits().AdjustReinvestedDividends();

		return this.AdjustReinvestedDividends().AdjustSplits();
	}
	public static final HistTable[] merge(final HistTable... raw_tables)
	{
		// complicated thing to avoid n^2 or n log n search.
		final int len = raw_tables.length;
		final int[] idx = new int[len];
		Arrays.fill(idx,0);
		//final HistTable[] ret = new HistTable[len];
		final List<HistRow>[] ret_lists = new List[len];
		for (int i = 0; i < len; i++)
			//ret[i] = new HistTable(raw_tables[i].symbol, raw_tables[i].dateAccessed, new ArrayList<HistRow>(raw_tables[i].data.size()));
			ret_lists[i] = new ArrayList<HistRow>(raw_tables[i].size());
		boolean exit = false;
		while (!exit)
		{
			Date min_date = raw_tables[0].get(idx[0]);
			boolean all_have = true;
			for (int i = 0; i < len; i++)
			{
				// min_date - other.date > 0 ==> min_date > other.date
				if (min_date.compareTo(raw_tables[i].get(idx[i])) > 0)
					min_date = raw_tables[i].get(idx[i]);
				if (0!=min_date.compareTo(raw_tables[i].get(idx[i])))
					all_have = false;
			}
			if (all_have)
				for (int i = 0; i < len; i++)
					ret_lists[i].add(raw_tables[i].get(idx[i]));
			// increment the laggards
			for (int i = 0; i < len; i++)
				if (0==min_date.compareTo(raw_tables[i].get(idx[i])))
					if (++idx[i] == raw_tables[i].size())
						exit = true;
		}
		final HistTable[] ret = new HistTable[len];
		for (int i = 0; i < ret.length; i++)
			ret[i] = new HistTable(raw_tables[i].symbol, raw_tables[i].dateAccessed, ret_lists[i]);
		return ret;
	}
}

