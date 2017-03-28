/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.smartloli.kafka.eagle.api.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.codehaus.janino.UnicodeUnescapeReader;

import com.alibaba.fastjson.JSONObject;

/**
 * Implements SmsService all method.
 * 
 * @author smartloli.
 *
 *         Created by Mar 27, 2017
 */
public class SmsServiceImpl implements SmsService {

	private String visit(String http, JSONObject params) {
		URL url = null;
		BufferedReader reader = null;
		UnicodeUnescapeReader uur = null;
		HttpURLConnection connection = null;
		try {
			if (params == null || params.size() == 0) {
				url = new URL(http);
			} else {
				StringBuffer queryString = new StringBuffer(http).append("?");
				Iterator<String> it = params.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					queryString.append(key).append("=").append(URLEncoder.encode(params.getString(key), "UTF-8")).append("&");
				}
				url = new URL(queryString.toString());
			}
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			reader = getBufferedReader(connection.getInputStream());
			uur = new UnicodeUnescapeReader(reader);
			return IOUtils.toString(uur);
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (uur != null)
					uur.close();
				if (connection != null)
					connection.disconnect();
			} catch (IOException e) {
			}
		}
		return null;
	}

	private BufferedReader getBufferedReader(InputStream input) throws UnsupportedEncodingException {
		return getBufferedReader(new InputStreamReader(input, "UTF-8"));
	}

	private BufferedReader getBufferedReader(Reader reader) {
		return new BufferedReader(reader);
	}

	/** Send message by sms. */
	@Override
	public String sendSms(String http, JSONObject params) {
		return visit(http, params);
	}

}
