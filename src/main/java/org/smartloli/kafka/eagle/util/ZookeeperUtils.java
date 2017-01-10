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
package org.smartloli.kafka.eagle.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZK tools class.
 *
 * @author smartloli.
 *
 *         Created by Sep 9, 2016
 */
public class ZookeeperUtils {
	private final static Logger LOG = LoggerFactory.getLogger(ZookeeperUtils.class);

	public static String serverStatus(String host, String port) {
		String ret = "";
		Socket sock = null;
		try {
			String tmp = "";
			if (port.contains("/")) {
				tmp = port.split("/")[0];
			} else {
				tmp = port;
			}
			sock = new Socket(host, Integer.parseInt(tmp));
		} catch (Exception e) {
			LOG.error("Socket[" + host + ":" + port + "] connect refused");
			return "death";
		}
		BufferedReader reader = null;
		try {
			OutputStream outstream = sock.getOutputStream();
			outstream.write("stat".getBytes());
			outstream.flush();
			sock.shutdownOutput();

			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("Mode: ") != -1) {
					ret = line.replaceAll("Mode: ", "").trim();
				}
			}
		} catch (Exception ex) {
			LOG.error("Read ZK buffer has error,msg is " + ex.getMessage());
			return "death";
		} finally {
			try {
				sock.close();
				if (reader != null) {
					reader.close();
				}
			} catch (Exception ex) {
				LOG.error("Close read has error,msg is " + ex.getMessage());
			}
		}
		return ret;
	}

}
