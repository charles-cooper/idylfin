package com.idylytics;

import org.apache.commons.math3.stat.regression.*;
import org.apache.commons.math3.stat.correlation.*;

import com.idylytics.yahoo.YahooFinance;

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

