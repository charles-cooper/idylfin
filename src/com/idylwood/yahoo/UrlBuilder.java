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

package com.idylwood.yahoo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.idylwood.utils.IOUtils;

public abstract class UrlBuilder {

	protected abstract String baseUrl();
	// maybe subclasses need to do some preparation
	protected abstract UrlBuilder prepare();

	// want guaranteed traversal order.
	// TODO figure out some way of making this immutable?
	private final Map<String,String> map;

	UrlBuilder()
	{
		this(new HashMap<String,String>());
	}
	UrlBuilder(Map<String,String> arg)
	{
		this.map = arg;
	}

	// set to public so that everybody can access this at a low level
	public final UrlBuilder set(String K, String V) {
		map.put(K, V);
		return this;
	}
	protected final String get(String K)
	{
		return map.get(K);
	}

	// returns entry set with unguaranteed traversal order
	public final java.util.Set<Map.Entry<String,String>> entrySet()
	{
		return map.entrySet();
	}

	// Removes the key and value from the url
	public final UrlBuilder unset(String K)
	{
		map.remove(K);
		return this;
	}

	// convenience wrapper method
	public final String download()
			throws MalformedURLException, IOException
	{
		return IOUtils.fromStream(toURL().openStream());
	}

	public final java.net.URL toURL()
			throws java.net.MalformedURLException
	{
		return new java.net.URL(this.toString());
	}

	@Override public final String toString()
	{
		String ret = baseUrl();
		int i = 0;
		for (Map.Entry<String,String> entry : map.entrySet())
		{
			if (i++==0)
				ret += "?";
			else
				ret += "&";
			ret += entry.getKey()+"="+entry.getValue();
		}
		return ret;
	}

}
