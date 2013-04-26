/*
 * ====================================================
 * Copyright (C) 2013 by Idylwood Technologies, LLC. All rights reserved.
 *
 * Developed at Idylwood Technologies, LLC.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * The License should have been distributed to you with the source tree.
 * If not, it can be found at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Charles Cooper
 * Date: 2013
 * ====================================================
 */
package com.idylwood.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	/**
	 * Reads the InputStream into a String and returns it.
	 * @param is
	 * @param len 'hint' parameter for allocating the internal buffer
	 * @return
	 * @throws IOException
	 * Side Effects: Consumes the InputStream
	 */
	public final static String fromStream(final InputStream is, final int len)
		throws IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
		Transfer(is,baos);
		return baos.toString();
	}

	/**
	 * Wrapper which calls fromStream(is,0)
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public final static String fromStream(final InputStream is)
		throws IOException
	{
		return fromStream(is,0);
	}

	// Flushes the stream
	/**
	 * Transfers the bytes, 1K at a time, from src to dest
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public final static void Transfer(final InputStream src, final OutputStream dest)
		throws IOException
	{
		final byte [] ba = new byte[1024];
		int read = -1;
		while ( -1 < (read = src.read(ba)) )
		{
			dest.write(ba,0,read);
		}
		dest.flush();
	}
}

