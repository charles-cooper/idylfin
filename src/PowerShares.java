import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.idylwood.utils.IOUtils;

public class PowerShares
{
	public static String getFundHoldings(final String ticker)
		throws IOException
	{
    	final String VIEWSTATE, EVENTTARGET, EVENTARGUMENT, EVENTVALIDATION;
    	final String url = "http://www.invescopowershares.com/products/holdings.aspx?ticker="+ticker;
		final Document doc = Jsoup
			.connect(url)
			.get();
		Elements els = doc.select("input[name=__VIEWSTATE]");
		if (1!=els.size())
			throw new RuntimeException("You have bug!");
		VIEWSTATE = els.get(0).attr("value");
		EVENTARGUMENT = "";
		EVENTTARGET = "ctl00$MainPageLeft$MainPageContent$ExportHoldings1$LinkButton1";
		els = doc.select("input[name=__EVENTVALIDATION]");
		if (1!=els.size())
			throw new RuntimeException("You have bug!");
		EVENTVALIDATION = els.get(0).attr("value");

		final String postParams = "__EVENTTARGET="+URLEncoder.encode(EVENTTARGET,"utf-8")
				+ "&__EVENTARGUMENT="+URLEncoder.encode(EVENTARGUMENT,"utf-8")
				+ "&__VIEWSTATE="+URLEncoder.encode(VIEWSTATE,"utf-8")
				+ "&yesno=yes"
				+ "&__EVENTVALIDATION="+URLEncoder.encode(EVENTVALIDATION,"utf-8");
		final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("charset", "utf-8");
		//con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		//con.setRequestProperty("Content-Length", "" + Integer.toString(postParams.length()));
		con.setUseCaches(false);
		con.getOutputStream().write(postParams.getBytes());
		con.getOutputStream().close();
		return IOUtils.fromStream(con.getInputStream(), con.getContentLength());
	}
    public static void main(String[]args)
		throws IOException
	{
    	System.out.println(getFundHoldings("SPHB"));
	}
}

