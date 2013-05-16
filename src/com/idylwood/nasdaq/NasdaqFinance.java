package com.idylwood.nasdaq;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idylwood.nasdaq.NasdaqFinance.CompanyList.Exchange;

import au.com.bytecode.opencsv.CSVReader;

public class NasdaqFinance {
	private NasdaqFinance() {}

	private static final NasdaqFinance sNasdaqFinance = new NasdaqFinance();
	public static final NasdaqFinance getInstance()
	{
		return sNasdaqFinance;
	}

	private final Map<Exchange,CompanyList> mCompanies = new HashMap<Exchange,CompanyList>();
	public final CompanyList companyList(Exchange exchange)
		throws IOException
	{
		CompanyList ret;
		synchronized(mCompanies)
		{
			ret = mCompanies.get(exchange);
			final long TwentyFourHours = 1000*60*60*24;
			if (null==ret || System.currentTimeMillis() - ret.time_accessed > TwentyFourHours)
			{
				ret = CompanyList.get(exchange);
				mCompanies.put(exchange,ret);
			}
		}
		return ret;
	}

	public final List<Company> allCompanies()
		throws IOException
	{
		final List<Company> ret = new ArrayList<Company>();
		for (final Exchange ex : Exchange.values())
			ret.addAll(companyList(ex).data);
		return ret;
	}

	// this is probably not necessary
	public final Set<String> sectors(final CompanyList companies)
	{
		final Set<String> ret = new HashSet<String>();
		for (final Company company : companies.data)
			ret.add(company.sector);
		return ret;
	}
	public final Set<String> sectors()
		throws IOException
	{
		final Set<String> ret = new HashSet<String>();
		for (Exchange ex : Exchange.values())
			ret.addAll(sectors(companyList(ex)));
		return ret;
	}

	public static class CompanyList
	{
		final long time_accessed;
		final List<Company> data;
		private CompanyList(final List<Company> data, final long time_accessed)
		{
			this.time_accessed = time_accessed;
			this.data = Collections.unmodifiableList(data);
		}
		public enum Exchange
		{
			NYSE,NASDAQ,AMEX
		}
		static CompanyList get(Exchange ex)
			throws IOException, MalformedURLException
		{
			URL url = new URL("http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange="+ex+"&render=download");
			Reader reader = new InputStreamReader(url.openStream());
			CSVReader csv = new CSVReader(reader);
			List<String[]> allLines = csv.readAll();
			csv.close();
			List<Company> data = new ArrayList<Company>();
			int i = 0;
			for (String[] line : allLines)
			{
				if (0==i++) continue;
				data.add(new Company(line));
			}
			return new CompanyList(data,System.currentTimeMillis());
		}
	}
	public static class Company {
		final String ticker;
		final String name;
		final double last_sale;
		final double market_cap;
		final String adr_tso; // what is this
		final int ipo_year;
		final String sector;
		final String industry;
		final URL summary_url;
		public Company(final String[] tokens)
		{
			if (10!=tokens.length) // fracking has an extra comma
				throw new IllegalArgumentException("Bad number of tokens!");
			try
			{
				// intern the crap out of all strings
				// it is going to make queries faster
				ticker = tokens[0].intern();
				name = tokens[1].intern();
				last_sale = "n/a".equals(tokens[2]) ? 0 : Double.parseDouble(tokens[2]);
				market_cap = "n/a".equals(tokens[3]) ? 0 : Double.parseDouble(tokens[3]);
				adr_tso = tokens[4].intern();
				ipo_year = "n/a".equals(tokens[5]) ? 0 : Integer.parseInt(tokens[5]);
				sector = tokens[6].intern();
				industry = tokens[7].intern();
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
		System.out.println(NasdaqFinance.getInstance().sectors());
	}
}

