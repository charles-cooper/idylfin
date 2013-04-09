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

// low level convenience class abstracting the yahoo url flags (so you don't have to memorize them)
public final class HistoricalUrlBuilder extends UrlBuilder
{
	public enum Type {
		WEEKLY("w"),
		DAILY("d"),
		MONTHLY("m"),
		DIVIDEND("v");
		private final String flag;
		private Type(final String flag) { this.flag = flag; }
		@Override public String toString() { return flag; }
	}

	private Type type = null;
	private String symbol = null; // TODO maybe this is not needed

	@Override public String baseUrl() {
		// for dividends/splits, baseUrl is http://ichart.yahoo.com/x.csv?
		if (Type.DIVIDEND==this.type)
			return "http://ichart.yahoo.com/x";
		return "http://ichart.yahoo.com/table.csv";
	}

	public HistoricalUrlBuilder(String symbol) {
		setSymbol(symbol);
	}
	public HistoricalUrlBuilder(String symbol, Date start, Date end, Type type) {
		this(symbol,type);
		setStartDate(start); setEndDate(end);
	}
	public HistoricalUrlBuilder(String symbol, Type type)
	{
		this(symbol);
		setType(type);
		set("ignore", ".csv"); // TODO check if this is needed
	}
	public static final String SYMBOL_FLAG="s";
	public HistoricalUrlBuilder setSymbol(String s) {
		set(SYMBOL_FLAG,s);
		this.symbol = s;
		return this;
	}

	public HistoricalUrlBuilder setStartDate(Date d)
	{
		// TODO maybe don't need setday/month/year to be public?
		return this
			.setStartDay(d.day)
			.setStartMonth(d.month-1) // January == 0
			.setStartYear(d.year);
	}
	
	public HistoricalUrlBuilder setEndDate(Date d)
	{
		return this
			.setEndDay(d.day)
			.setEndMonth(d.month-1) // January == 0
			.setEndYear(d.year);
	}
	
	static final String START_DAY_FLAG = "b";
	public HistoricalUrlBuilder setStartDay(int day) {
		set(START_DAY_FLAG,Integer.toString(day));
		return this;
	}
	// recall january==0
	public static final String START_MONTH_FLAG = "a";
	public HistoricalUrlBuilder setStartMonth(int month) {
		set(START_MONTH_FLAG,Integer.toString(month));
		return this;
	}
	public static final String START_YEAR_FLAG = "c";
	public HistoricalUrlBuilder setStartYear(int year) {
		set(START_YEAR_FLAG,Integer.toString(year));
		return this;
	}
	public static final String END_DAY_FLAG = "e";
	public HistoricalUrlBuilder setEndDay(int day) {
		set(END_DAY_FLAG,Integer.toString(day));
		return this;
	}
	// recall january==0
	public static final String END_MONTH_FLAG = "d";
	public HistoricalUrlBuilder setEndMonth(int month) {
		set(END_MONTH_FLAG,Integer.toString(month));
		return this;
	}
	public static final String END_YEAR_FLAG = "f";
	public HistoricalUrlBuilder setEndYear(int year) {
		set(END_YEAR_FLAG,Integer.toString(year));
		return this;
	}
	public static final String TYPE_FLAG = "g";
	public HistoricalUrlBuilder setType(Type t) {
		this.type = t;
		set(TYPE_FLAG,t.toString());
		return this;
	}
}

