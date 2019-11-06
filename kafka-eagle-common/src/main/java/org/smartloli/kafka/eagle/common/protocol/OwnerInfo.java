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
package org.smartloli.kafka.eagle.common.protocol;

import java.util.HashSet;
import java.util.Set;

/**
 * Active topic schema and owners.
 * 
 * @author smartloli.
 *
 *         Created by Aug 2, 2019
 */
public class OwnerInfo extends BaseProtocol {

	private int activeSize = 0;
	private Set<String> topicSets = new HashSet<>();

	public int getActiveSize() {
		return activeSize;
	}

	public void setActiveSize(int activeSize) {
		this.activeSize = activeSize;
	}

	public Set<String> getTopicSets() {
		return topicSets;
	}

	public void setTopicSets(Set<String> topicSets) {
		this.topicSets = topicSets;
	}

}
