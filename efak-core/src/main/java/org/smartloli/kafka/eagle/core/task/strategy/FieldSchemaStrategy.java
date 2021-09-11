/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.task.strategy;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Kafka sql field schema parser.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 13, 2020
 */
public class FieldSchemaStrategy extends BaseProtocol {
    private String key;
    private String type;
    private String value;
    private String valueType;
    private boolean isJsonUdf = false;
    private boolean isJsonsUdf = false;

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public boolean isJsonsUdf() {
        return isJsonsUdf;
    }

    public void setJsonsUdf(boolean jsonsUdf) {
        isJsonsUdf = jsonsUdf;
    }

    public boolean isJsonUdf() {
        return isJsonUdf;
    }

    public void setJsonUdf(boolean jsonUdf) {
        isJsonUdf = jsonUdf;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
