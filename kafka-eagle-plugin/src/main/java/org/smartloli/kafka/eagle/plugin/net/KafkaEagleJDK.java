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
package org.smartloli.kafka.eagle.plugin.net;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Check the operate system JDK encoding environment.
 * 
 * @author smartloli.
 *
 *         Created by Jul 27, 2019
 */
public class KafkaEagleJDK {
	public static void main(String[] args) {
		System.out.println("===================== Kafka Eagle Check OS JDK Encoding  ====================");
		System.out.println("* Default Charset=" + Charset.defaultCharset());
		System.out.println("* file.encoding=" + System.getProperty("file.encoding"));
		System.out.println("* Default Charset=" + Charset.defaultCharset());
		System.out.println("* Default Charset in Use=" + getDefaultCharSet());
		System.out.println("*******************************************************************");
		System.out.println("* If the charset is not 'UTF-8',\n* Please set it in the environment variable\n* export JAVA_TOOL_OPTIONS=\"-Dfile.encoding=UTF-8\" into ~/.bash_profile file.");
		System.out.println("===================== End  ==================================================");
	}

	/** Get default charset. */
	private static String getDefaultCharSet() {
		OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
		String enc = writer.getEncoding();
		return enc;
	}

}
