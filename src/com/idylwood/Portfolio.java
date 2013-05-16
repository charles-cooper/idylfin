package com.idylwood;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.idylwood.yahoo.Quote;
import com.idylwood.yahoo.YahooFinance;

public class Portfolio {
	class Item
	{
		final String ticker;
		double weight;
		public Item(final String ticker, final double weight)
		{
			this.ticker = ticker; this.weight = weight;
		}
	}
	public final List<Item> items = new ArrayList<Item>();
	private final YahooFinance yf = YahooFinance.getInstance();
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
	public static void main(String[]args)
		throws IOException, FileNotFoundException
	{
		final Reader reader = new FileReader("/media/files/Downloads/QQQHoldings.csv");
		final CSVReader csv = new CSVReader(reader);
		final List<String[]> allLines = csv.readAll();
		allLines.remove(0);
		final Portfolio p = new Portfolio();
		for (final String[] line : allLines)
			p.items.add(p.new Item(line[2], Double.parseDouble(line[4])));
		System.out.println("Market Cap: "+p.marketCap());
		System.out.println("Earnings: "+p.earnings());
		System.out.println("Revenue: "+p.revenue());
	}
}
