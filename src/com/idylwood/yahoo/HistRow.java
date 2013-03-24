package com.idylytics.yahoo;

// TODO does this really need to implement Comparable<Date>?
public class HistRow implements Comparable<Date>
{
	public final Date date;
	public final double open;
	public final double high;
	public final double low;
	public final double close;
	public final int volume;
	public final double adj_close;

	// This is consistent with the constructor, as in new HistRow(this.toString())
	// will return an equivalent HistRow
	@Override public String toString()
	{
		return date + "," + open + "," + high 
			+ "," + low + "," + close + "," +volume + "," + adj_close;
	}
	@Override public int compareTo(Date date)
	{
		return this.date.compareTo(date);
	}

	public HistRow(final Date date, final double open, final double high,
			final double low, final double close, final int volume,
			final double adj_close)
	{
		this.date = date; this.open = open; this.high = high; this.low = low;
		this.close = close; this.volume = volume; this.adj_close = adj_close;
	}
	// Copy constructor
	public HistRow(HistRow other)
	{
		this(new Date(other.date),other.open,other.high,other.low,other.close,
				other.volume,other.adj_close);
		// equivalent to this(other.toString());
	}

	public HistRow(String row)
	{
		String [] elems = row.split(",");
		date = new Date(elems[0]);
		open = Double.parseDouble(elems[1]);
		high = Double.parseDouble(elems[2]);
		low = Double.parseDouble(elems[3]);
		close = Double.parseDouble(elems[4]);
		volume = Integer.parseInt(elems[5]);
		adj_close = Double.parseDouble(elems[6]);
	}
}