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
package org.smartloli.kafka.eagle.common.protocol.offsets;

import org.apache.kafka.common.protocol.types.Schema;
import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * K&V schema storage offset in Kafka topic.
 * 
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class KeyAndValueSchemasInfo extends BaseProtocol{

	private Schema keySchema;
	private Schema valueSchema;

	public Schema getKeySchema() {
		return keySchema;
	}

	public void setKeySchema(Schema keySchema) {
		this.keySchema = keySchema;
	}

	public Schema getValueSchema() {
		return valueSchema;
	}

	public void setValueSchema(Schema valueSchema) {
		this.valueSchema = valueSchema;
	}

}
