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
package org.smartloli.kafka.eagle.common.util;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request http client,such as get or post etc.
 * 
 * @author smartloli.
 *
 *         Created by Jan 1, 2019
 */
public class HttpClientUtils {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtils.class);

	/**
	 * Send request by get method.
	 * 
	 * @param uri:
	 *            http://ip:port/demo?httpcode=200&name=smartloli
	 */
	public static String doGet(String uri) {
		String result = "";
		try {
			CloseableHttpClient client = null;
			CloseableHttpResponse response = null;
			try {
				HttpGet httpGet = new HttpGet(uri);

				client = HttpClients.createDefault();
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			} finally {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Do get request has error, msg is " + e.getMessage());
		}
		return result;
	}

	/**
	 * Send request by post method.
	 * 
	 * @param uri:
	 *            http://ip:port/demo
	 * @param parames:
	 *            new BasicNameValuePair("code", "200")
	 * 
	 *            new BasicNameValuePair("name", "smartloli")
	 */
	public static String doPostForm(String uri, List<BasicNameValuePair> parames) {
		String result = "";
		try {
			CloseableHttpClient client = null;
			CloseableHttpResponse response = null;
			try {

				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(new UrlEncodedFormEntity(parames, "UTF-8"));
				client = HttpClients.createDefault();
				response = client.execute(httpPost);
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			} finally {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Do post form request has error, msg is " + e.getMessage());
		}
		return result;
	}

	/**
	 * Send request by post method.
	 * 
	 * @param uri:
	 *            http://ip:port/demo
	 */
	public static String doPostJson(String uri, String data) {
		String result = "";
		try {
			CloseableHttpClient client = null;
			CloseableHttpResponse response = null;
			try {
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
				httpPost.setEntity(new StringEntity(data, ContentType.create("text/json", "UTF-8")));
				client = HttpClients.createDefault();
				response = client.execute(httpPost);
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			} finally {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Do post json request has error, msg is " + e.getMessage());
		}

		return result;
	}

	public static void main(String[] args) {
		testDingDing();

	}

	private static void testDingDing() {
		String uri = SystemConfigUtils.getProperty("kafka.eagle.im.dingding.url");
		String data = "{\"markdown\":\"{\\\"text\\\":\\\"<font color=\\\\\\\"#FF0000\\\\\\\"Kafka Eagle Alert</font>\n"
				+ "> **Information** \n "
				+ "> **Type:** <font color=\\\\\\\"#008000\\\\\\\">Zookeeper</font> \\\\n > **ClusterID:** cluster2 \\\\n>**Owners:** @example1@email.com> \\\\n>**Time:** <font color=\\\\\\\"#008000\\\\\\\">2019-01-01 15:45:02</font> \\\\n>Describer:\\\\n><font color=\\\\\\\"#FFA500\\\\\\\">Telnet server[127.0.0.1:2188] is not available.</font>\\\\n\\\",\\\"title\\\":\\\"Kafka Eagle Alert\\\"}\",\"msgtype\":\"markdown\"}";
		String res = doPostJson(uri, data);
		System.out.println(res);
	}

}
