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
package org.smartloli.kafka.eagle.web.service;

import java.util.Map;

/**
 * Big screen service interface.
 * 
 * @author smartloli.
 *
 *         Created by Aug 29, 2019
 */
public interface BScreenService {

	/** Get producer and consumer real rate data . */
	public String getProducerAndConsumerRate(String clusterAlias);

	/** Get topic total logsize data . */
	public String getTopicTotalLogSize(String clusterAlias);

	/** Get producer history data. */
	public String getProducerOrConsumerHistory(String clusterAlias, String type);

	/** Get today or last 7 day consumer and producer data . */
	public String getTodayOrHistoryConsumerProducer(String clusterAlias, String type);

	/** Get topic total capacity. */
	public String getTopicCapacity(Map<String, Object> params);

}
