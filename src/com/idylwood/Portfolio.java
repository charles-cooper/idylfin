package com.idylwood;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.idylwood.misc.PowerShares;
import com.idylwood.utils.FinUtils;
import com.idylwood.utils.MathUtils;
import com.idylwood.yahoo.Date;
import com.idylwood.yahoo.HistTable;
import com.idylwood.yahoo.HistRow;
import com.idylwood.yahoo.YahooFinance;

class Portfolio
{
	class Item
	{
		final String ticker;
		final double weight;
		public Item(final String ticker, final double weight)
		{
			this.ticker = ticker; this.weight = weight;
		}
	}
	public final List<Item> items = new ArrayList<Item>();
	private static final YahooFinance yf = YahooFinance.getInstance();
	private String[] tickers()
	{
		final String []ret = new String[items.size()];
		int i = 0;
		for (Item item : items) ret[i++] = item.ticker;
		return ret;
	}
	public double marketCap()
		throws IOException
	{
		yf.Quotes(tickers()); // force it to batch download them. yay side effects!
		double ret = 0;
		for (final Item it : items)
			ret += it.weight * yf.getQuote(it.ticker).market_cap;
		return ret;
	}
	public double earnings()
		throws IOException
	{
		yf.Quotes(tickers());
		double ret = 0;
		for (final Item it : items)
			ret += it.weight * yf.getQuote(it.ticker).earnings();
		return ret;
	}
	public double revenue()
		throws IOException
	{
		yf.Quotes(tickers());
		double ret = 0;
		for (final Item it : items)
			ret += it.weight * yf.getQuote(it.ticker).revenue;
		return ret;
	}
	HistTable[] tables()
		throws IOException
	{
		final HistTable[] tables = new HistTable[items.size()];
		for (int i = 0 ; i < items.size(); i++)
			tables[i] = yf.HistoricalPrices(items.get(i).ticker);
		return tables;
	}
	public double totalLogReturn()
		throws IOException
	{
		final HistTable[] tables = HistTable.merge(tables());
		for (int i = 0; i < tables.length; i++)
			tables[i] = tables[i]
				.AdjustOHLCWithReinvestment();
		return totalLogReturn(tables);
	}
	public double totalLogReturn(final Date begin, final Date end)
		throws IOException
	{
		final HistTable[] tables = HistTable.merge(tables());
		// TODO check that the begin, end is actually valid.
		for (int i = 0; i < tables.length; i++)
			tables[i] = tables[i]
				.SubTable(begin,end)
				.AdjustOHLCWithReinvestment();
		return totalLogReturn(tables);
	}
	private Portfolio reassignWeights(final double[] weights)
	{
		return Portfolio.reassignWeights(this,weights);
	}
	private static Portfolio reassignWeights(final Portfolio arg, final double[] weights)
	{
		final Portfolio ret = new Portfolio();
		int i = 0;
		for (final Item it : arg.items)
			ret.items.add(ret.new Item(it.ticker, weights[i++]));
		return ret;
	}
	// assume tables are clean.
	private Portfolio markowitzPortfolio(final HistTable[] tables)
	{
		return reassignWeights(FinUtils.MarkowitzPortfolio(tables, Math.abs(totalLogReturn(tables))));
	}
	private Portfolio markowitzPortfolio(final Date begin, final Date end)
		throws IOException
	{
		final HistTable data[] = HistTable.merge(tables());
		for (int i = 0; i < data.length; i++)
			data[i] = data[i].SubTable(begin,end).AdjustOHLCWithReinvestment();
		return markowitzPortfolio(data);
	}
	// frackin hate all this boilerplate
	public final double[] weights()
	{
		final double[] ret = new double[items.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = items.get(i).weight;
		return ret;
	}
	// start of backtesting interface
	private void doBacktest()
		throws IOException
	{
		final HistTable data[] = HistTable.merge(tables());
		final Date today = data[0].get(data[0].size()-1);
		for (int i = 0; i < data.length; i++)
			data[i] = data[i].SubTable(today.subtractYears(2),today).AdjustOHLCWithReinvestment();
		final int len = data[0].size();
		final int incr_sz = 40; // rebalance every incr_sz days.
		final List<Date> splits = new ArrayList<Date>(len/incr_sz + 1);
		for (int i = 0; i < len; i+=incr_sz)
			splits.add(data[0].get(i));
		// last incr is larger.
		splits.set(splits.size()-1,data[0].get(len-1));
		Portfolio p = this.weightByPrice(data, splits.get(0));
		double total_return = 0;
		System.out.println("begin weights: "+Arrays.toString(p.weights()));
		for (int i = 0; i < splits.size()-1; i++)
		{
			final Date period_begin = splits.get(i);
			final Date period_end = splits.get(i+1);
			System.out.println(period_begin+" --> "+period_end);
			final HistTable[] period_data = new HistTable[data.length];
			for (int j = 0; j < data.length; j++)
				period_data[j] = data[j].SubTable(period_begin, period_end);
			final double period_return = p.totalLogReturn(period_data);
			total_return += period_return;
			// introduce data dependency, rebalance according to return of last period.
			System.out.println("Period return: "+period_return);
			p = p
				.markowitzPortfolio(data)//, begin,end)
				.sanitize(1);
				//.weightByPrice(period_data, period_end);
			System.out.println("Optimize weights to: "+Arrays.toString(p.weights()));
		}
		System.out.println("total return: "+total_return);
	}
	private Portfolio weightByPrice(final HistTable[] tables, final Date date)
		throws IOException
	{
		final int len = items.size();
		final double[] weights = new double[len];
		//final HistTable[] tables = tables();
		for (int i = 0; i < tables.length; i++)
			weights[i] = tables[i].getFloor(date).close; // if we have bug getFloor will throw exception
		return reassignWeights(this,weights).normalizeWeights();

	}
	// max weight should be +- 1.
	private Portfolio sanitize(final double max_weight)
	{
		if (max_weight <= 1 / (double)items.size())
			throw new IllegalArgumentException("Bug!");
		final double[] weights = weights();
		for (int i = 0; i < weights.length; i++)
			if (MathUtils.abs(weights[i]) > max_weight)
				weights[i] = MathUtils.copySign(max_weight, weights[i]);
		return this.reassignWeights(weights).normalizeWeights();
	}
	private Portfolio normalizeWeights()
	{
		return reassignWeights(this, MathUtils.normalize(weights()));
	}
	private double totalLogReturn(final HistTable[] tables)
		//throws IOException
	{
		// assume dividends reinvestment strategy is just
		// each symbol that pays a dividend is reinvested
		// in that company.
		final double returns[] = new double[items.size()];
		final double weights[] = new double[items.size()];
		// fwahaha abusing the final keyword
		for (int i = 0; i < items.size(); i++)
		{
			final Item it = items.get(i);
			//ret += FinUtils.totalReturn(table.CloseArray()) * it.weight;
			//sanity_check += it.weight;
			returns[i] = FinUtils.totalLogReturn(tables[i].CloseArray());
			weights[i] = it.weight;
		}
		final double epsilon = 1e-3;
		System.out.println(MathUtils.sumSlow(weights));
		return MathUtils.linearCombinationSlow(returns,weights);
	}
	public static void main(String[]args)
		throws IOException, FileNotFoundException
	{
		final Reader reader = //new StringReader(PowerShares.getFundHoldings("QQQ"));
		//new FileReader("/media/files/Downloads/QQQHoldings.csv");
			new FileReader("/media/files/Downloads/DIA_All_Holdings.csv");
		final CSVReader csv = new CSVReader(reader);
		final List<String[]> allLines = csv.readAll();
		csv.close();
		allLines.remove(0);
		//final HistTable dia = yf.HistoricalPrices("DIA");
		//final HistTable dia_sub = dia.SubTable(dia.EndDate().subtractYears(1), dia.EndDate());
		final Portfolio p = new Portfolio();
		for (final String[] line : allLines)
			//p.items.add(p.new Item(line[2], Double.parseDouble(line[4]) / 100));
			p.items.add(p.new Item(line[1], Double.parseDouble(line[2])/100));
		final HistTable table = HistTable.merge(p.tables())[0];
		final Date end = table.EndDate();
		final Date begin = end.subtractYears(1);
		System.out.println(table.SubTable(begin,end).size());
		final HistTable sub = table.SubTable(begin,end).AdjustOHLC();
		System.out.println(sub.size());
		System.out.println(FinUtils.totalLogReturn(table.SubTable(begin,end).AdjustOHLC()));
		//final HistTable foo = table.SubTable(begin,end);
		//for (final HistRow row : foo)
		//	System.out.println(row);
		System.out.println(p.totalLogReturn(begin,end));
		p.doBacktest();
		/*
		System.out.println("Market Cap: "+p.marketCap());
		System.out.println("Earnings: "+p.earnings());
		System.out.println("Revenue: "+p.revenue());
		*/
	}
}

