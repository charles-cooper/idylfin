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

package com.idylwood.yahoo;

// TODO does this really need to implement Comparable<Date>?
public class HistRow implements Comparable<Date>
{
	public final Date date;
	public final double open;
	public final double high;
	public final double low;
	public final double close;
	public final int volume;
	public final double adj_close;

	// This is consistent with the constructor, as in new HistRow(this.toString())
	// will return an equivalent HistRow
	@Override public String toString()
	{
		return date + "," + open + "," + high 
			+ "," + low + "," + close + "," +volume + "," + adj_close;
	}
	@Override public int compareTo(final Date date)
	{
		return this.date.compareTo(date);
	}

	public HistRow(final Date date, final double open, final double high,
			final double low, final double close, final int volume,
			final double adj_close)
	{
		this.date = date; this.open = open; this.high = high; this.low = low;
		this.close = close; this.volume = volume; this.adj_close = adj_close;
	}
	// Copy constructor
	public HistRow(final HistRow other)
	{
		this(new Date(other.date),other.open,other.high,other.low,other.close,
				other.volume,other.adj_close);
		// equivalent to this(other.toString());
	}

	public HistRow(final String row)
	{
		final String [] elems = row.split(",");
		date = new Date(elems[0]);
		open = Double.parseDouble(elems[1]);
		high = Double.parseDouble(elems[2]);
		low = Double.parseDouble(elems[3]);
		close = Double.parseDouble(elems[4]);
		volume = Integer.parseInt(elems[5]);
		adj_close = Double.parseDouble(elems[6]);
	}
}
