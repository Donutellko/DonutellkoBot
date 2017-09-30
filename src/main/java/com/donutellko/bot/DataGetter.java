package com.donutellko.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 *
 */

public class DataGetter {

	public static String getDataFromUrl (String url_s) throws Exception {
		String result;
		BufferedReader reader = null;
		URLConnection uc;
		String encoding;

		try {
			URL url = new URL(url_s);
			uc = url.openConnection();
			encoding = uc.getContentEncoding();
			uc.setConnectTimeout(1000);
			uc.connect();
//			reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), Charset.forName("utf-8")));
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

//		result = new String(result.getBytes(), Charset.forName("utf-8"));
//		result = new String(result.getBytes(), Charset.defaultCharset());
//		result = new String(result.getBytes(), Charset.forName(encoding));
		return result;
	}
}
