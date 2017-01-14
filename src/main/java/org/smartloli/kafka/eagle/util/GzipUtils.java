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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP compress & uncompress class utils.
 *
 * @author smartloli.
 * 
 *         Created by Mar 24, 2016
 */
public class GzipUtils {

	private static final String UTF_16 = "UTF-16";

	/**
	 * Strings compress to bytes.
	 * 
	 * @param str
	 * @return byte[]
	 */
	public static byte[] compressToByte(String str) {
		return compressToByte(str, UTF_16);
	}

	// public static byte[] compressToByte(String str) {
	// if (str == null || str.length() == 0) {
	// return null;
	// }
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// GZIPOutputStream gzip;
	// try {
	// gzip = new GZIPOutputStream(out);
	// gzip.write(str.getBytes(UTF_16));
	// gzip.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return out.toByteArray();
	// }

	/**
	 * Strings compress to bytes.
	 * 
	 * @param str
	 * @param encoding
	 * @return byte[]
	 */
	public static byte[] compressToByte(String str, String encoding) {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(encoding));
			gzip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}