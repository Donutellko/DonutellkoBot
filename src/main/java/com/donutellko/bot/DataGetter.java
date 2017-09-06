package com.donutellko.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */

public class DataGetter {

	public static String getDataFromUrl (String url_s) throws Exception {
		String result;
		BufferedReader reader = null;
		URLConnection uc;

		try {
			URL url = new URL(url_s);
			uc = url.openConnection();
			uc.setConnectTimeout(1000);
			uc.connect();
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuilder buffer = new StringBuilder();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);
			result = buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
		return result;
	}
}
