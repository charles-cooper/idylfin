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

import java.util.EnumSet;

import com.idylwood.yahoo.Date;
import com.idylwood.yahoo.QuoteUrlBuilder.Tag;

// convenience class for getting quote data
// TODO make it work
class Quote {
	final long time_accessed;
	final String ticker;
	//final String company_name;
	final double bid;
	final double ask;
	final double dividend_yield;
	final double dividend_per_share;
	final Date dividend_pay_date;
	final Date dividend_ex_date;
	final double previous_close;
	final double open;
	final Date last_trade_date;
	final double days_low;
	final double days_high;
	final double moving_average_200_day;
	final double moving_average_50_day;
	final double earnings_per_share;
	static final EnumSet<Tag> quoteTags =
			EnumSet.of(
					Tag.BID_REALTIME,
					Tag.ASK_REALTIME,
					Tag.DIVIDEND_YIELD,
					Tag.DIVIDEND_PER_SHARE,
					Tag.DIVIDEND_PAY_DATE,
					Tag.DIVIDEND_EX_DATE,
					Tag.PREVIOUS_CLOSE,
					Tag.OPEN,
					Tag.LAST_TRADE_DATE,
					Tag.DAYS_LOW,
					Tag.DAYS_HIGH,
					Tag.MOVING_AVERAGE_200_DAY,
					Tag.MOVING_AVERAGE_50_DAY,
					Tag.EARNINGS_PER_SHARE
				  );
	Quote(final String symbol, final String[] tokens)
	{
		try
		{
			// this is like the worst invention ever
			time_accessed = System.currentTimeMillis();
			ticker = symbol;
			//company_name = tokens[0];
			bid = Double.parseDouble(tokens[indexOf(quoteTags,Tag.BID_REALTIME)]);
			ask = Double.parseDouble(tokens[indexOf(quoteTags,Tag.ASK_REALTIME)]);
			dividend_yield = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DIVIDEND_YIELD)]);
			dividend_per_share = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DIVIDEND_PER_SHARE)]);
			previous_close = Double.parseDouble(tokens[indexOf(quoteTags,Tag.PREVIOUS_CLOSE)]);
			open = Double.parseDouble(tokens[indexOf(quoteTags,Tag.OPEN)]);
			days_low = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DAYS_LOW)]);
			days_high = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DAYS_HIGH)]);
			moving_average_200_day = Double.parseDouble(tokens[indexOf(quoteTags,Tag.MOVING_AVERAGE_200_DAY)]);
			moving_average_50_day = Double.parseDouble(tokens[indexOf(quoteTags,Tag.MOVING_AVERAGE_50_DAY)]);
			earnings_per_share = Double.parseDouble(tokens[indexOf(Tag.EARNINGS_PER_SHARE)]);

			// dates are tricky so they get their own block
			final java.util.Date todaysDate = new java.util.Date(time_accessed);
			java.text.DateFormat df = new java.text.SimpleDateFormat("MMM dd"); // eg Jun 28
			java.util.Date parsed = df.parse(tokens[indexOf(quoteTags,Tag.DIVIDEND_PAY_DATE)]);
			parsed.setYear(todaysDate.getYear());
			dividend_pay_date = new Date(parsed);
			parsed = df.parse(tokens[indexOf(quoteTags,Tag.DIVIDEND_EX_DATE)]);
			parsed.setYear(todaysDate.getYear());
			dividend_ex_date = new Date(parsed);
			df = new java.text.SimpleDateFormat("mm/dd/yyyy");
			last_trade_date = new Date(df.parse(tokens[indexOf(quoteTags,Tag.LAST_TRADE_DATE)]));
		}
		catch (java.text.ParseException e)
		{
			throw new RuntimeException("You have bug!", e);
		}
	}
	private static final int indexOf(Tag target)
	{
		return indexOf(quoteTags,target);
	}
	private static final int indexOf(final EnumSet<Tag> set, final Tag target)
	{
		int i = 0;
		for (Tag t : set)
		{
			if (t.equals(target))
				return i;
			i++;
		}
		return -1;
	}

}
