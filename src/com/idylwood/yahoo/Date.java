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
package com.idylytics.yahoo;

public class Date implements Comparable<Date> {
	final int year;
	final int month;
	final int day;
	// Dates in format YYYYMMDD
	public Date(int date) {
		year = date / 10000;
		month = (date / 100) % 100;
		day = date % 100;
	}
	// YYYY-MM-DD
	public Date(String date) {
		String [] arr = date.split("-");
		year = Integer.parseInt(arr[0]);
		month = Integer.parseInt(arr[1]);
		day = Integer.parseInt(arr[2]);
	}
	// Copy constructor
	public Date(Date other) {
		this.year = other.year; this.month = other.month; this.day = other.day;
	}
	@SuppressWarnings("deprecation")
	public Date(java.util.Date date) {
		year = date.getYear() + 1900; // java.util.Date year starts at 1900
		month = date.getMonth()+1;
		day = date.getDate();
	}
	// This is consistent with the constructor in that
	// calling new Date(oldDate.toString()) will result
	// in an equivalent Date object.
	@Override public String toString() {
		return year + "-" + month + "-" + day;
	}

	// returns approximate subtraction of other date
	// from this, assuming 365 days to a year and
	// 30 days a month
	public int subtract(Date other)
	{
		return 365 * (this.year - other.year) + 30 * (this.month - other.month) + (this.day - other.day);
	}

	// This is consistent with the constructor in that
	// calling new Date(oldDate.toDate()) will result
	// in an equivalent Date object.
	@SuppressWarnings("deprecation")
	public final java.util.Date toDate() {
		// TODO get the timezone right, probably want UTC
		java.util.Date date = new java.util.Date(0);
		date.setDate(this.day);
		date.setYear(this.year - 1900);
		date.setMonth(this.month - 1);
		return date;
	}

	// This is consistent with the constructor in that
	// calling new Date(oldDate.toInt()) will result
	// in an equivalent Date object
	public final int toInt() {
		return year * 10000 + month * 100 + day;
	}
	@Override public int compareTo(Date other)
	{
		return this.toInt() - other.toInt();
	}
	@Override public int hashCode() { return toInt(); }
	@Override public boolean equals(Object other)
	{
		if (other instanceof Date)
		{
			Date that = (Date) other;
			if (this.year!=that.year) return false;
			if (this.month!=that.month) return false;
			if (this.day != that.day) return false;
			return true;
		}
		return false;
	}
}
