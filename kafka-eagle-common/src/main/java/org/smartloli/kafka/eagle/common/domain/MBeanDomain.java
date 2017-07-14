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
package org.smartloli.kafka.eagle.common.domain;

import com.google.gson.Gson;

/**
 * Mapper kafka jmx data to MBeanDomain.
 * 
 * @author smartloli.
 *
 *         Created by Jul 14, 2017
 */
public class MBeanDomain {

	private long count;
	private String eventType;
	private String fifteenMinute;
	private String fiveMinute;
	private String meanRate;
	private String oneMinute;
	private String rateUnit;

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getFifteenMinute() {
		return fifteenMinute;
	}

	public void setFifteenMinute(String fifteenMinute) {
		this.fifteenMinute = fifteenMinute;
	}

	public String getFiveMinute() {
		return fiveMinute;
	}

	public void setFiveMinute(String fiveMinute) {
		this.fiveMinute = fiveMinute;
	}

	public String getMeanRate() {
		return meanRate;
	}

	public void setMeanRate(String meanRate) {
		this.meanRate = meanRate;
	}

	public String getOneMinute() {
		return oneMinute;
	}

	public void setOneMinute(String oneMinute) {
		this.oneMinute = oneMinute;
	}

	public String getRateUnit() {
		return rateUnit;
	}

	public void setRateUnit(String rateUnit) {
		this.rateUnit = rateUnit;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
