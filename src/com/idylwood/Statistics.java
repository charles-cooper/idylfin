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
package com.idylwood;

import org.apache.commons.math3.stat.regression.*;
import org.apache.commons.math3.stat.correlation.*;

import com.idylwood.yahoo.YahooFinance;

import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataSource;

import java.util.List;

// TODO get rid of this class
public class Statistics
{
	private static double[][] data(List<YahooFinance.Pair> pairs)
	{
		double[][] data = new double[pairs.size()-1][2];
		for (int i = 0; i < pairs.size() - 1; i++)
		{
			data[i][0] = Math.log(pairs.get(i+1).first / pairs.get(i).first);
			data[i][1] = Math.log(pairs.get(i+1).second / pairs.get(i).second);
		}
		return data;
	}

	public static SimpleRegression regress(List<YahooFinance.Pair> pairs)
	{
		SimpleRegression regress = new SimpleRegression();
		regress.addData(data(pairs));

		return regress;
	}

	public static double[][] covariance(List<YahooFinance.Pair> pairs)
	{
		Covariance cov = new Covariance(data(pairs));
		return cov.getCovarianceMatrix().getData();
	}

	public static double mean(DataSource data, int col)
	{
		Column column = data.getColumn(col);
		return column.getStatistics(de.erichseifert.gral.data.statistics.Statistics.MEAN);
	}

	public static double max(DataSource data, int col)
	{
		Column column = data.getColumn(col);
		return column.getStatistics(de.erichseifert.gral.data.statistics.Statistics.MAX);
	}

	public static double min(DataSource data, int col)
	{
		Column column = data.getColumn(col);
		return column.getStatistics(de.erichseifert.gral.data.statistics.Statistics.MIN);
	}

	public static double variance(DataSource data, int col)
	{
		Column column = data.getColumn(col);
		return column.getStatistics(de.erichseifert.gral.data.statistics.Statistics.VARIANCE);
	}

	public static double population_variance(DataSource data, int col)
	{
		Column column = data.getColumn(col);
		return column.getStatistics(de.erichseifert.gral.data.statistics.Statistics.POPULATION_VARIANCE);
	}
}

