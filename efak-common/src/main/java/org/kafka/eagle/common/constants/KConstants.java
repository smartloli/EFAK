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

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 15:22
 * @Version: 3.4.0
 */
public class KConstants {

    public static DateTimeFormatter getFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return formatter;
    }

    public final class TableCommon {
        private TableCommon() {
        }

        public static final int TR_LEN = 20;
    }

    public final class ServerDevice {
        private ServerDevice() {
        }

        public static final int TIME_OUT = 3000;
        public static final int BUFFER_SIZE = 8049;
    }

    public static final class Cluster {
        private Cluster() {
        }

        public static final String ENABLE_AUTH = "Y";
        // public static final String DISABLE_AUTH = "N";

        public static final String AUTH_TYPE_SASL = "SASL";

        public static final String AUTH_TYPE_SSL = "SSL";

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
     * Login session.
     */
    public final class Login {
        private Login() {
        }

        public static final String SESSION_USER = "LOGIN_USER_SESSION";
        public static final String SESSION_USER_TIME = "LOGIN_USER_SESSION_TIME";
        public static final String UNKNOW_USER = "__unknow__";
        public static final String ERROR_LOGIN = "error_msg";
    }

    /**
     * Kafka parameter setting.
     */
    public static final class Kafka {
        private Kafka() {
        }

        public static final String EFAK_SYSTEM_GROUP = "efak.system.group";

        public static final int TIME_OUT = 100;

    }

    /**
     * Topic parameter setting.
     */
    public static final class Topic {
        private Topic() {
        }

        public static final String CONSUMER_OFFSET_TOPIC = "__consumer_offsets";

        public static final int PARTITION_LENGTH = 10;

        public static List<String> getTopicConfigKeys() {
            return Arrays.asList("cleanup.policy", "compression.type", "delete.retention.ms", "file.delete.delay.ms", "flush.messages", "flush.ms", "follower.replication.throttled", "index.interval.bytes", "leader.replication.throttled.replicas", "max.message.bytes", "message.downconversion.enable", "message.format.version",
                    "message.timestamp.difference.max.ms", "message.timestamp.type", "min.cleanable.dirty.ratio", "min.compaction.lag.ms", "min.insync.replicas", "preallocate", "retention.bytes", "retention.ms", "segment.bytes", "segment.index.bytes", "segment.jitter.ms", "segment.ms", "unclean.leader.election.enable");
        }

        public static final String ADD = "ADD";
        public static final String DELETE = "DELETE";
        public static final String DESCRIBE = "DESCRIBE";

        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";

        public static final String PRODUCER_THREADS_KEY = "producer_threads_key";
        public static final String PRODUCER_THREADS = "producer_threads";
        public static final String LOGSIZE = "logsize";
        public static final String BYTE_IN = "byte_in";
        public static final String BYTE_OUT = "byte_out";
        public static final String CAPACITY = "capacity";
        public static final String BROKER_SPREAD = "spread";
        public static final String BROKER_SKEWED = "skewed";
        public static final String BROKER_LEADER_SKEWED = "leader_skewed";
        public static final String[] BROKER_PERFORMANCE_LIST = new String[]{BROKER_SPREAD, BROKER_SKEWED, BROKER_LEADER_SKEWED};
        public static final String TRUNCATE = "truncate";// 0:truncating,1:truncated
        public static final String CLEANUP_POLICY_KEY = "cleanup.policy";
        public static final String CLEANUP_POLICY_VALUE = "delete";
        public static final String RETENTION_MS_KEY = "retention.ms";
        public static final String RETENTION_MS_VALUE = "1000";

        public static final String RETENTION_MS_DEFAULT_VALUE = "86400000"; // default 24 hour

        public static final int BATCH_SIZE = 50;

        /**
         * used by batch quartz task.
         */
        public static final int TOPIC_QUARTZ_BATCH_SIZE = 100;

        public static final long TOPIC_BROKER_SPREAD_ERROR = 60;
        public static final long TOPIC_BROKER_SPREAD_NORMAL = 80;
        public static final long TOPIC_BROKER_SKEW_ERROR = 80;
        public static final long TOPIC_BROKER_SKEW_NORMAL = 30;
        public static final long TOPIC_BROKER_LEADER_SKEW_ERROR = 80;
        public static final long TOPIC_BROKER_LEADER_SKEW_NORMAL = 30;

        public static final Short RUNNING = 0;
        public static final Short SHUTDOWN = 1;
        public static final Short PENDING = 2;
        public static final Short ALL = -1;

        public static final String RUNNING_STRING = "Running";
        public static final String SHUTDOWN_STRING = "Shutdown";
        public static final String PENDING_STRING = "Pending";

        public static final String PRODUCERS = "producers";
        public static final String CONSUMERS = "consumers";
        public static final String LAG = "lag";

        public static final Integer PREVIEW_SIZE = 10;
    }

    public final class Common {
        private Common() {
        }

        public static final String EFAK_VERSION = "v3.4.0";
        public static final String EFAK_VERSION_DOC = "efakVersion";
    }

    public static final class Consumer {
        private Consumer() {
        }

        public static final Integer TOPOLOGY_SIZE = 3;

    }

    /**
     * Kafka jmx mbean.
     */
    public final class MBean {
        private MBean() {
        }

        public static final String COUNT = "Count";
        public static final String EVENT_TYPE = "EventType";
        public static final String FIFTEEN_MINUTE_RATE = "FifteenMinuteRate";
        public static final String FIVE_MINUTE_RATE = "FiveMinuteRate";
        public static final String MEAN_RATE = "MeanRate";
        public static final String ONE_MINUTE_RATE = "OneMinuteRate";
        public static final String RATE_UNIT = "RateUnit";
        public static final String VALUE = "Value";

        /**
         * Messages in /sec.
         */
        public static final String MESSAGES_IN = "msg";
        /**
         * Bytes in /sec.
         */
        public static final String BYTES_IN = "ins";
        /**
         * Bytes out /sec.
         */
        public static final String BYTES_OUT = "out";
        /**
         * Bytes rejected /sec.
         */
        public static final String BYTES_REJECTED = "rejected";
        /**
         * Failed fetch request /sec.
         */
        public static final String FAILED_FETCH_REQUEST = "fetch";
        /**
         * Failed produce request /sec.
         */
        public static final String FAILED_PRODUCE_REQUEST = "produce";

        /**
         * MBean keys.
         */
        public static final String MESSAGEIN = "message_in";
        public static final String BYTEIN = "byte_in";
        public static final String BYTEOUT = "byte_out";
        public static final String BYTESREJECTED = "byte_rejected";
        public static final String FAILEDFETCHREQUEST = "failed_fetch_request";
        public static final String FAILEDPRODUCEREQUEST = "failed_produce_request";
        public static final String PRODUCEMESSAGECONVERSIONS = "produce_message_conversions";
        public static final String TOTALFETCHREQUESTSPERSEC = "total_fetch_requests";
        public static final String TOTALPRODUCEREQUESTSPERSEC = "total_produce_requests";
        public static final String REPLICATIONBYTESINPERSEC = "replication_bytes_out";
        public static final String REPLICATIONBYTESOUTPERSEC = "replication_bytes_in";
        public static final String OSUSEDMEMORY = "os_used_memory";
        public static final String OSFREEMEMORY = "os_free_memory";
        public static final String CPUUSED = "cpu_used";
    }

    public static List<String> USER_ROLES_LIST = new ArrayList<String>() {
        {
            add("管理员");
            add("开发");
            add("测试");
        }
    };

    public static List<String> ALERT_CHANNEL_LIST = new ArrayList<String>() {
        {
            add("钉钉");
            add("微信");
            add("邮件");
            // add("自定义");
        }
    };

    public static Map<String, String> ALERT_CHANNEL_MAP = new HashMap<String, String>() {
        {
            put("钉钉", "dingding");
            put("微信", "wechat");
            put("邮件", "email");
        }
    };

    public static Map<String, String> USER_ROLES_MAP = new HashMap<String, String>() {
        {
            put("管理员", "ROLE_ADMIN");
            put("开发", "ROLE_DEV");
            put("测试", "ROLE_TEST");
        }
    };

    public final class AlertChannel {
        public static final String DINGDING = "dingding";
        public static final String DINGDING_NAME = "钉钉";
        public static final String EMAIL = "email";
        public static final String EMAIL_NAME = "邮件";
        public static final String WECHAT = "wechat";
        public static final String WECHAT_NAME = "微信";


    }

}
