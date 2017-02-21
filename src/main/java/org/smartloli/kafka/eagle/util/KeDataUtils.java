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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Kafka Eagle data processing tools.
 * 
 * @author smartloli.
 *
 *         Created by Feb 21, 2017
 */
public class KeDataUtils {

	private final static Logger LOG = LoggerFactory.getLogger(KeDataUtils.class);

	/** Write dataset to storage. */
	public static void write(String name, String data) {
		try {
			String osName = System.getProperties().getProperty("os.name");
			String root = System.getProperty("user.dir");
			String pathname = "";
			if (osName.contains("Mac") || osName.contains("Win")) {
				pathname = root + "/src/main/resources/" + name + ".ke";
			} else {
				pathname = root + "/data/" + name + ".ke";
			}
			File f = new File(pathname);
			if (!f.exists()) {
				f.createNewFile();
			}
			Files.append(EncryptUtils.encode(data) + "\r\n", f, Charsets.UTF_8);
		} catch (Exception ex) {
			LOG.error("Write dataset to kafka eagle storage has error,msg is " + ex.getMessage());
		}
	}

	/** Read dataset from storage. */
	public static String read(String name) {
		List<String> target = new ArrayList<>();
		try {
			String osName = System.getProperties().getProperty("os.name");
			String root = System.getProperty("user.dir");
			String pathname = "";
			if (osName.contains("Mac") || osName.contains("Win")) {
				pathname = root + "/src/main/resources/" + name + ".ke";
			} else {
				pathname = root + "/data/" + name + ".ke";
			}
			List<String> lines = Files.readLines(new File(pathname), Charsets.UTF_8);
			for (String line : lines) {
				target.add(EncryptUtils.decode(line));
			}
		} catch (Exception ex) {
			LOG.error("read dataset to kafka eagle storage has error,msg is " + ex.getMessage());
		}

		return target.toString();
	}

}
