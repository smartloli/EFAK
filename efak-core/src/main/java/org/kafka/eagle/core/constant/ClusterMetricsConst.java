/**
 * ClusterMetricsConst.java
 * <p>
 * Copyright 2025 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.core.constant;

/**
 * <p>
 * 与集群指标相关的常量
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 22:34:47
 * @version 5.0.0
 */
public class ClusterMetricsConst {

    /**
     * 集群指标常量枚举。
     */
    public enum Cluster {

        ENABLE_AUTH("Y"),
        DISABLE_AUTH("N"),
        AUTH_TYPE_SASL("SASL"),
        AUTH_TYPE_SSL("SSL"),
        EFAK_SYSTEM_GROUP("efak.system.group"),
        CONSUMER_OFFSET_TOPIC("__consumer_offsets");

        private final String key;

        Cluster(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }


}
