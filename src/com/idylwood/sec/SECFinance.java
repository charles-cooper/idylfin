package com.idylwood.sec;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SECFinance
{
	public static final String generateURL(final String ticker)
	{
		return "http://www.sec.gov/cgi-bin/browse-edgar?CIK="+ticker+"&action=getcompany";
	}
	// maybe cache the result
	public static final int getCIK(final String ticker)
		throws IOException
	{
		final Document doc = Jsoup.connect(generateURL(ticker)).get();
		final Elements els = doc.select("acronym[title=Central Index Key]:matches(CIK) + a[href*=CIK]");
		if (1!=els.size())
			throw new RuntimeException("regex is not working!");
		// something like <a href=something>0000320193 (see all company filings)</a>
		final String target = els.first().text().replaceAll(" .*","");
		final Integer ret = Integer.parseInt(target);
		if (null==ret)
			throw new RuntimeException("Uh oh you have bug");
		return ret;
	}
	final static String SEC_URI = "http://www.sec.gov";
	// TODO rename this thing
	public static final String quarterly_filing_url(final int CIK)
		throws IOException
	{
		// type=10 will select 10Q and 10K!
		String url = SEC_URI+"/cgi-bin/browse-edgar?action=getcompany&CIK="+CIK+"&type=10";
		Document doc = Jsoup.connect(url).get();
		Elements els = doc.select("a[id=documentsbutton]");
		url = SEC_URI + els.first().attr("href");
		doc = Jsoup.connect(url).get();
		els = doc.select("td[scope=row]:matches(10-.) + td[scope=row] > a[href]");
		return SEC_URI + els.first().attr("href");
	}
	public static void main(String[]args)
		throws IOException
	{
		int CIK = getCIK("AAPL");
		System.out.println(quarterly_report_url(CIK));
	}
}

