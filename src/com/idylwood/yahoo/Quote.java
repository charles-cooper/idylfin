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
	final String company_name;
	final double bid;
	final double ask;
	final double dividend_yield;
	final double dividend_per_share;
	final Date dividend_pay_date;
	final Date dividend_ex_date;
	final double previous_close;
	final Date open_date;
	final double change;
	final Date last_trade_date;
	final Date trade_date;
	final double change_after_hours;
	final double days_low;
	final double days_high;
	final double change_from_200_day_moving_average;
	final double change_from_50_day_moving_average;
	static final EnumSet<Tag> quoteTags =
			EnumSet.of(
					Tag.BID,
					Tag.ASK,
					Tag.DIVIDEND_YIELD,
					Tag.DIVIDEND_PER_SHARE,
					Tag.DIVIDEND_PAY_DATE,
					Tag.DIVIDEND_EX_DATE,
					Tag.PREVIOUS_CLOSE,
					Tag.OPEN_DATE,
					Tag.CHANGE,
					Tag.LAST_TRADE_DATE,
					Tag.TRADE_DATE,
					Tag.CHANGE_PERCENT,
					Tag.CHANGE_AFTER_HOURS_REALTIME,
					Tag.DAYS_LOW,
					Tag.DAYS_HIGH,
					Tag.CHANGE_FROM_MOVING_AVERAGE_200_DAY,
					Tag.CHANGE_FROM_MOVING_AVERAGE_50_DAY
					);
	Quote(final String symbol, final String[] tokens)
	{
		// this is like the worst invention ever
		time_accessed = System.currentTimeMillis();
		ticker = symbol;
		company_name = tokens[0];
		bid = Double.parseDouble(tokens[indexOf(quoteTags,Tag.BID)]);
		ask = Double.parseDouble(tokens[indexOf(quoteTags,Tag.ASK)]);
		dividend_yield = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DIVIDEND_YIELD)]);
		dividend_per_share = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DIVIDEND_PER_SHARE)]);
		dividend_pay_date = new Date(tokens[indexOf(quoteTags,Tag.DIVIDEND_PAY_DATE)]);
		dividend_ex_date = new Date(tokens[indexOf(quoteTags,Tag.DIVIDEND_EX_DATE)]);
		previous_close = Double.parseDouble(tokens[indexOf(quoteTags,Tag.PREVIOUS_CLOSE)]);
		open_date = new Date(tokens[indexOf(quoteTags,Tag.OPEN_DATE)]);
		change = Double.parseDouble(tokens[indexOf(quoteTags,Tag.CHANGE)]);
		last_trade_date = new Date(tokens[indexOf(quoteTags,Tag.LAST_TRADE_DATE)]);
		change_after_hours = Double.parseDouble(tokens[indexOf(quoteTags,Tag.CHANGE_AFTER_HOURS_REALTIME)]);
		days_low = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DAYS_LOW)]);
		days_high = Double.parseDouble(tokens[indexOf(quoteTags,Tag.DAYS_HIGH)]);
		trade_date = new Date(tokens[indexOf(quoteTags,Tag.TRADE_DATE)]);
		change_from_200_day_moving_average = Double.parseDouble(tokens[indexOf(quoteTags,Tag.CHANGE_FROM_MOVING_AVERAGE_200_DAY)]);
		change_from_50_day_moving_average = Double.parseDouble(tokens[indexOf(quoteTags,Tag.CHANGE_FROM_MOVING_AVERAGE_50_DAY)]);
	}
	private static final int indexOf(EnumSet<Tag> set, Tag target)
	{
		int i = 1;
		for (Tag t : set)
		{
			if (t.equals(target))
				return i;
			i++;
		}
		return -1;
	}

}