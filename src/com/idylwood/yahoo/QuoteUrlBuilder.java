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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class QuoteUrlBuilder extends UrlBuilder {
	final Collection<String> mSymbols;
	final Set<Tag> mTags = EnumSet.noneOf(Tag.class);
	// not really sure what these 'realtime' things are
	public enum Tag
	{
		ASK("a"),
		BID("b"),
		ASK_REALTIME("b2"),
		BID_REALTIME("b3"),
		DIVIDEND_YIELD("y"),
		DIVIDEND_PER_SHARE("d"),
		DIVIDEND_PAY_DATE("r1"),
		DIVIDEND_EX_DATE("q"),
		PREVIOUS_CLOSE("p"),
		OPEN_DATE("o"),
		CHANGE("c1"),
		LAST_TRADE_DATE("d1"),
		//CHANGE_PERCENT("c"),
		TRADE_DATE("d2"),
		CHANGE_REALTIME("c6"),
		LAST_TRADE_TIME("t1"),
		CHANGE_PERCENT_REALTIME("k2"),
		CHANGE_PERCENT("p2"),
		CHANGE_AFTER_HOURS_REALTIME("c8"),
		DAYS_LOW("g"),
		DAYS_HIGH("h"),
		CHANGE_FROM_MOVING_AVERAGE_200_DAY("m5"),
		CHANGE_PERCENT_FROM_MOVING_AVERAGE_200_DAY("m6"),
		CHANGE_FROM_MOVING_AVERAGE_50_DAY("m7"),
		CHANGE_PERCENT_FROM_MOVING_AVERAGE_50_DAY("m8"),
		MOVING_AVERAGE_50_DAY("m3"),
		MOVING_AVERAGE_200_DAY("m4"),
		LAST_TRADE_REALTIME_WITH_TIME("k1"),
		LAST_TRADE_WITH_TIME("l"),
		LAST_TRADE_PRICE_ONLY("l1"),
		ONE_YEAR_TARGET_PRICE("t8"),
		DAYS_VALUE_CHANGE("w1"),
		DAYS_VALUE_CHANGE_REALTIME("w4"),
		DAYS_RANGE("m"),
		DAYS_RANGE_REALTIME("m2"),
		FIFTY_TWO_WEEK_HIGH("k"),
		MORE_INFO("v"),
		FIFTY_TWO_WEEK_LOW("j"),
		MARKET_CAPITALIZATION("j1"),
		CHANGE_FROM_FIFTY_TWO_WEEK_LOW("j5"),
		MARKET_CAPITALIZATION_REALTIME("j3"),
		CHANGE_FROM_FIFTY_TWO_WEEK_HIGH("k4"),
		SHARES_FLOATING("f6"),
		CHANGE_PERCENT_FROM_FIFTY_TWO_WEEK_LOW("j6"),
		NAME("n"),
		CHANGE_PERCENT_FROM_FIFTY_TWO_WEEK_HIGH("k5"),
		NOTES("n4"),
		FIFTY_TWO_WEEK_RANGE("w"),
		SYMBOL("s"),
		VOLUME("v"),
		EXCHANGE_("x"),
		ASK_SIZE("a5"),
		BID_SIZE("b6"),
		LAST_TRADE_SIZE("k3"),
		TICKER_TREND("t7"),
		DAILY_VOLUME_AVERAGE("a2"),
		ORDER_BOOK_REALTIME("i5"),
		HIGH_LIMIT("l2"),
		EARNINGS_PER_SHARE("e"),
		LOW_LIMIT("l3"),
		EARNINGS_PER_SHARE_ESTIMATE_CURRENT_YEAR("e7"),
		EARNINGS_PER_SHARE_ESTIMATE_NEXT_YEAR("e8"),
		EARNINGS_PER_SHARE_ESTIMATE_NEXT_QUARTER("e9"),
		BOOK_VALUE("b4"),
		EBITDA("j4"),
		PRICE_OVER_SALES_RATIO("p5"),
		PRICE_OVER_BOOK_RATIO("p6"),
		PRICE_OVER_EARNINGS_RATIO("r"),
		PRICE_OVER_EARNINGS_RATIO_REALTIME("r2"),
		PRICE_OVER_EARNINGS_TO_GROWTH_RATIO("r5"),
		PRICE_OVER_EARNINGS_PER_SHARE_ESTIMATE_CURRENT_YEAR("r6"),
		PRICE_OVER_EARNINGS_PER_SHARE_ESTIMATE_NEXT_YEAR("r7"),
		SHORT_RATIO("s7"),
		SHARES_OUTSTANDING("j2"),
		REVENUE("s6");

		private final String flag;
		private Tag(final String flag) { this.flag = flag; }
		@Override public String toString() { return flag; }
	}

	public QuoteUrlBuilder()
	{
		mSymbols = new ArrayList<String>();
	}
	public QuoteUrlBuilder(String symbol)
	{
		this(); mSymbols.add(symbol);
	}
	public QuoteUrlBuilder(Collection<String> symbols)
	{
		this.mSymbols = symbols;
	}
	public QuoteUrlBuilder(List<String> symbols)
	{
		this((Collection<String>)symbols);
	}
	public QuoteUrlBuilder addSymbol(String s)
	{
		mSymbols.add(s);
		return this;
	}
	public QuoteUrlBuilder addSymbols(Collection<String> symbols)
	{
		mSymbols.addAll(symbols);
		return this;
	}
	public QuoteUrlBuilder addTag(Tag t)
	{
		mTags.add(t);
		return this;
	}
	public QuoteUrlBuilder addTags(Collection<Tag> tags)
	{
		mTags.addAll(tags);
		return this;
	}
	private void setTags(final String tags)
	{
		set("f",tags);
	}
	private void setSymbols(final String symbols)
	{
		set("s",symbols);
	}
	// enumset iteration order is guaranteed, uses bitvector internally

	@Override protected String baseUrl()
	{
		return "http://finance.yahoo.com/d/quotes.csv";
	}
	@Override protected UrlBuilder prepare()
	{
		String tags = "";
		for (Tag t : mTags)
			tags += t;
		setTags(tags);
		String symbols = "";
		for (String s : mSymbols)
		{
			if (0!=s.length())
				symbols += "+";
			symbols += s;
		}
		setSymbols(symbols);
		return this;
	}
}
