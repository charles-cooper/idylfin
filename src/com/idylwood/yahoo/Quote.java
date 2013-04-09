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

import com.idylwood.yahoo.Date;

public class Quote {
	double bid;
	double ask;
	double dividend_yield;
	double dividend_per_share;
	Date dividend_pay_date;
	Date dividend_ex_date;
	double previous_close;
	Date open_date;
	double change;
	Date last_trade_date;
	Date trade_date;
	double change_percent;
	double change_after_hours;
	double days_low;
	double days_high;
	double change_from_200_day_moving_average;
	double change_percent_from_200_day_moving_average;
	double change_from_50_day_moving_average;
	double change_percent_from_50_day_moving_average;
}

