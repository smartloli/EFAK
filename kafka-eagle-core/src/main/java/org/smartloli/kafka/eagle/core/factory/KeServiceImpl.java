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
package org.smartloli.kafka.eagle.core.factory;

import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KeDataUtils;

/**
 * Implements KeService all method.
 * 
 * @author smartloli.
 *
 *         Created by Feb 21, 2017
 */
public class KeServiceImpl implements KeService {

	/** Read dataset from storage. */
	public String read(String... args) {
		String name = args[0];
		return KeDataUtils.read(name);
	}

	/** Write dataset to storage. */
	public void write(String name, String data) {
		String suffix = CalendarUtils.getCustomDate("yyyyMMdd");
		KeDataUtils.write(name + "_" + suffix, data);
	}

}
