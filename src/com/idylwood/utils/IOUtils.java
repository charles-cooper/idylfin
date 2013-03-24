package com.idylytics.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

	// Takes len as a 'hint' parameter when allocating the buffer
	public final static String fromStream(InputStream is, int len)
		throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
		IOUtils.Transfer(is,baos);
		return baos.toString();
	}

	public final static String fromStream(InputStream is)
		throws IOException
	{
		return fromStream(is,0);
	}

	public final static void Transfer(InputStream is, OutputStream os)
		throws IOException
	{
		byte [] ba = new byte[1024];
		int read = -1;
		while ( -1 < (read = is.read(ba)) )
		{
			os.write(ba,0,read);
		}
	}

	/**
	 * Copyright (C) 2010  Tobias Domhan
	 * This method was taken from AndObjViewer
	 */
	public final static List<String> fastSplit(final String text, final char separator, int lenHint) {
		final List<String> result = new ArrayList<String>(lenHint);
	
		if (text != null && text.length() > 0) {
			int index1 = 0;
			int index2 = text.indexOf(separator);
			while (index2 >= 0) {
				String token = text.substring(index1, index2);
				result.add(token);
				index1 = index2 + 1;
				index2 = text.indexOf(separator, index1);
			}
	
			if (index1 < text.length() - 1) {
				result.add(text.substring(index1));
			}
		}//else: input unavailable
	
		return result;
	}

}
