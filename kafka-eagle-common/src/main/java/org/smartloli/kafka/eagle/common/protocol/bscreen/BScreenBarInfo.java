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
package org.smartloli.kafka.eagle.common.protocol.bscreen;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Big screen producer data
 * 
 * @author smartloli.
 *
 *         Created by Aug 29, 2019
 */
public class BScreenBarInfo extends BaseProtocol {

	private long value;
	private String tm;

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getTm() {
		return tm;
	}

	public void setTm(String tm) {
		this.tm = tm;
	}

}
