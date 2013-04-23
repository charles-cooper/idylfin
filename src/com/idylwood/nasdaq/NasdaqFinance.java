package com.idylwood.nasdaq;
import java.net.URL;
import java.io.Reader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import au.com.bytecode.opencsv.CSVReader;

public class NasdaqFinance {
	public static class CompanyList
	{
		final long time_accessed;
		final List<CompanyListRow> data;
		public CompanyList(final List<CompanyListRow> data, final long time_accessed)
		{
			this.time_accessed = time_accessed;
			this.data = data;
		}
	}
	public static class CompanyListRow {
		String symbol;
		String name;
		double last_sale;
		double market_cap;
		String adr_tso; // what is this
		int ipo_year;
		String sector;
		String industry;
		URL summary_url;
		public CompanyListRow(final String[] tokens)
		{
			if (10!=tokens.length) // fracking has an extra comma
				throw new IllegalArgumentException("Bad number of tokens!");
			try
			{
				symbol = tokens[0];
				name = tokens[1];
				last_sale = "n/a".equals(tokens[2]) ? 0 : Double.parseDouble(tokens[2]);
				market_cap = "n/a".equals(tokens[3]) ? 0 : Double.parseDouble(tokens[3]);
				adr_tso = tokens[4];
				ipo_year = "n/a".equals(tokens[5]) ? 0 : Integer.parseInt(tokens[5]);
				sector = tokens[6];
				industry = tokens[7];
				summary_url = new URL(tokens[8]);
			}
			catch(MalformedURLException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
	}
	public static void main(String[] args)
		throws MalformedURLException, IOException
	{
		URL url = new URL("http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nyse&render=download");
		Reader reader = new InputStreamReader(url.openStream());
		CSVReader csv = new CSVReader(reader);
		List<String[]> allLines = csv.readAll();
		List<CompanyListRow> data = new ArrayList<CompanyListRow>();
		int i = 0;
		for (String[] line : allLines)
		{
			if (0==i++) continue;
			data.add(new CompanyListRow(line));
		}
		System.out.println(data.size());
		
	}
}

