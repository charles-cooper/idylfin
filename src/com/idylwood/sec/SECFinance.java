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
		Document doc = Jsoup.connect(generateURL(ticker)).get();
		Elements els = doc.select("acronym[title=Central Index Key]:matches(CIK) + a[href*=CIK]");
		if (1!=els.size())
			throw new RuntimeException("regex is not working!");
		// something like <a href=something>0000320193 (see all company filings)</a>
		final String target = els.first().text().replaceAll(" .*","");
		final Integer ret = Integer.parseInt(target);
		if (null==ret)
			throw new RuntimeException("Uh oh you have bug");
		return ret;
	}
	public static void main(String[]args)
		throws IOException
	{
		System.out.println(getCIK("AAPL"));
	}
}

