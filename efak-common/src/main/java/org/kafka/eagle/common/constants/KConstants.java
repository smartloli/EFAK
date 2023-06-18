/**
 * KConstants.java
 * <p>
 * Copyright 2023 smartloli
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
package org.kafka.eagle.common.constants;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/6/7 15:22
 * @Version: 3.4.0
 */
public class KConstants {

    public final class ServerDevice {
        private ServerDevice() {
        }

        public static final int TIME_OUT = 3000;
        public static final int BUFFER_SIZE = 8049;
    }

    /**
     * Schudle job submit to mysql default size.
     */
    public static final int MYSQL_BATCH_SIZE = 500;

    public final class SessionClusterId {
        private SessionClusterId() {
        }

        public static final String CLUSTER_ID = "clusterId";
        public static final String CLUSTER_ID_LIST = "clusterIdList";
        public static final int CLUSTER_ID_LIST_LIMIT = 5;
    }

    /**
     * Kafka parameter setting.
     */
    public static final class Kafka {
        private Kafka() {
        }

        public static final String EFAK_SYSTEM_GROUP = "efak.system.group";
    }

}
