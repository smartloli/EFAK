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
package org.smartloli.kafka.eagle.common.util;

import java.util.Arrays;
import java.util.List;

/**
 * Define constants in the system.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 3, 2017
 */
public class KConstants {

    /**
     * D3 data plugin size.
     */
    public final class D3 {
        private D3() {
        }

        public static final int SIZE = 10;
        public static final int CHILD_SIZE = 5;
    }

    public final class CharSet {
        private CharSet() {
        }

        public static final String UTF_8 = "utf-8";
    }

    public enum OperateSystem {
        OS_NAME("os.name"),
        LINUX("Linux");

        private String value;

        public String getValue() {
            return value;
        }

        private OperateSystem(String value) {
            this.value = value;
        }

    }

    /**
     * Kafka parameter setting.
     */
    public static final class Kafka {
        private Kafka() {
        }

        public static final String BROKER_IDS_PATH = "/brokers/ids";
        public static final String BROKER = "broker";
        public static final String CONSUMER_OFFSET_TOPIC = "__consumer_offsets";
        public static final String EFAK_SYSTEM_GROUP = "efak.system.group";
        public static final String AUTO_COMMIT = "true";
        public static final String AUTO_COMMIT_MS = "1000";
        public static final String EARLIEST = "earliest";
        public static final String JAVA_SECURITY = "java.security.auth.login.config";
        public static final int TIME_OUT = 100;
        public static final long POSITION = SystemConfigUtils.getLongProperty("efak.sql.topic.records.max") == 0 ? 5000 : SystemConfigUtils.getLongProperty("efak.sql.topic.records.max");
        public static final long PREVIEW = SystemConfigUtils.getLongProperty("efak.sql.topic.preview.records.max") == 0 ? 10 : SystemConfigUtils.getLongProperty("efak.sql.topic.preview.records.max");
        public static final String PARTITION_CLASS = "partitioner.class";
        public static final String KEY_SERIALIZER = "key.serializer";
        public static final String VALUE_SERIALIZER = "value.serializer";
        public static final String UNKOWN = "Unknown";
        public static final int ALL_PARTITION = -1;
        public static final String SASL_PLAINTEXT = "SASL_PLAINTEXT";
        public static final String SSL = "SSL";
    }

    public final class Common {
        private Common() {
        }

        public static final String EFAK_VERSION = "v3.0.1";
        public static final String EFAK_VERSION_DOC = "efakVersion";
    }

    /**
     * Zookeeper session.
     */
    public final class SessionAlias {
        private SessionAlias() {
        }

        public static final String CLUSTER_ALIAS = "clusterAlias";
        public static final String CLUSTER_ALIAS_LIST = "clusterAliasList";
        public static final int CLUSTER_ALIAS_LIST_LIMIT = 5;
    }

    /**
     * EFAK Mode.
     */
    public final class EFAK {
        private EFAK() {
        }

        public static final String MODE_MASTER = "master";
        public static final String MODE_SLAVE = "slave";
        public static final String MODE_STANDALONE = "standalone";
        public static final int THREAD_SLEEP_TIME_SEED = 3000;
        public static final String RUNTIME_ENV_DEV = "dev";
        public static final String RUNTIME_ENV_PRD = "prd";
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
     * Role Administrator.
     */
    public final class Role {
        private Role() {
        }

        public static final String ADMIN = "admin";
        public static final int ADMINISRATOR = 1;
        public static final int ANONYMOUS = 0;
        public static final String WHETHER_SYSTEM_ADMIN = "WHETHER_SYSTEM_ADMIN";
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
        public static final String OSTOTALMEMORY = "os_total_memory";
        public static final String OSFREEMEMORY = "os_free_memory";
        public static final String CPUUSED = "cpu_used";
    }

    public final class ZK {
        private ZK() {
        }

        public static final String ZK_SEND_PACKETS = "zk_packets_sent";
        public static final String ZK_RECEIVEDPACKETS = "zk_packets_received";
        public static final String ZK_NUM_ALIVECONNRCTIONS = "zk_num_alive_connections";
        public static final String ZK_OUTSTANDING_REQUESTS = "zk_outstanding_requests";

    }

    public final class ServerDevice {
        private ServerDevice() {
        }

        public static final int TIME_OUT = 3000;
        public static final int BUFFER_SIZE = 8049;
    }

    public final class CollectorType {
        private CollectorType() {
        }

        public static final String ZK = "zookeeper";
        public static final String KAFKA = "kafka";
    }

    public final class IM {
        private IM() {
        }

        public static final String TITLE = "EFAK Alert";
    }

    public final class WeChat {
        private WeChat() {
        }

        public static final String TOUSER = "@all";
        public static final String TOPARTY = "PartyID1|PartyID2";
        public static final String TOTAG = "TagID1 | TagID2";
        public static final long AGENTID = 1;
    }

    public final class Zookeeper {
        private Zookeeper() {
        }

        public static final String LEADER = "leader";
    }

    public static final class Topic {
        private Topic() {
        }

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

        public static final int BATCH_SIZE = 50;

        public static final long TOPIC_BROKER_SPREAD_ERROR = 60;
        public static final long TOPIC_BROKER_SPREAD_NORMAL = 80;
        public static final long TOPIC_BROKER_SKEW_ERROR = 80;
        public static final long TOPIC_BROKER_SKEW_NORMAL = 30;
        public static final long TOPIC_BROKER_LEADER_SKEW_ERROR = 80;
        public static final long TOPIC_BROKER_LEADER_SKEW_NORMAL = 30;

        public static final int RUNNING = 0;
        public static final int SHUTDOWN = 1;
        public static final int PENDING = 2;

        public static final String RUNNING_STRING = "Running";
        public static final String SHUTDOWN_STRING = "Shutdown";
        public static final String PENDING_STRING = "Pending";

        public static final String PRODUCERS = "producers";
        public static final String CONSUMERS = "consumers";
        public static final String LAG = "lag";
    }

    public final class BrokerSever {
        private BrokerSever() {
        }

        public static final int MEM_NORMAL = 60;
        public static final int MEM_DANGER = 80;

        public static final int CPU_NORMAL = 60;
        public static final int CPU_DANGER = 80;

        public static final String BALANCE_SINGLE = "SINGLE";
        public static final String BALANCE_ALL = "ALL";

        public static final String CONNECT_URI_ALIVE = "Y";
        public static final String CONNECT_URI_SHUTDOWN = "N";
    }

    public final class Component {
        private Component() {
        }

        /**
         * Flink app consumer don't commit consumer info into kafka.
         */
        public static final String UNKNOW = "unknow-host";

    }

    public interface AlarmType {
        public static String[] TYPE = new String[]{"DingDing", "WeChat", "Email","Kafka"};
        public static String[] CLUSTER = new String[]{"Kafka", "Zookeeper", "Topic", "Producer"};
        public static String[] LEVEL = new String[]{"P0", "P1", "P2", "P3"};
        public static int[] MAXTIMES = new int[]{-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        public static String EMAIL = "Email";
        public static String EMAIL_TEST_TITLE = "Kafka Eagle Send Test MSG";
        public static String DingDing = "DingDing";
        public static String WeChat = "WeChat";
        public static String Kafka = "Kafka";
        public static String HTTP_GET = "get";
        public static String HTTP_POST = "post";
        public static String DISABLE = "N";
        public static String TOPIC = "Topic";
        public static String PRODUCER = "Producer";
    }

    public interface AlarmStatus {
        public static String NORMAL = "NORMAL";
        public static String PROBLEM = "PROBLEM";
    }

    public final class AlarmQueue {
        private AlarmQueue() {
        }

        public static final String JOB_PARAMS = "job_params";
    }

    public interface KSQL {
        public static final String ORDER_BY = "ORDER_BY";
        public static final String ORDER_BY_DEFAULT = "ASC";
        public static final String ORDER_BY_DESC = "DESC";
        public static final String ORDER_BY_ASC = ORDER_BY_DEFAULT;
        public static final String COLUMN = "COLUMN";
        public static final String PARTITIONS = "PARTITIONS";
        public static final String LIKE = "LIKE";
        public static final String AND = "AND";
        public static final String OR = "OR";
        public static final String IN = "IN";
        public static final String EQ = "=";
        public static final String GT = ">";
        public static final String GE = ">=";
        public static final String LT = "<";
        public static final String LE = "<=";
        public static final String LB = "(";
        public static final String REG_LB = "\\(";
        public static final String RB = ")";
        public static final String REG_RB = "\\)";
        public static final String[] COMPARE_CONDITIONS = new String[]{">", ">=", "=", "<", "<="};
        public static final int LIMIT = 100;
        public static final String JSON = "JSON";
        public static final String JSONS = "JSONS";
        public static final String INT = "INT";
        public static final String STRING = "STRING";
    }

    public interface Protocol {
        public static final String KEY = "KEY";
        public static final String VALUE = "VALUE";
        public static final String JOB_ID = "JOB_ID";
        public static final String KEY_BY_IP = "KEY_BY_IP";
        public static final String KEY_BY_IP_VIP = "KEY_BY_IP_VIP";
        public static final String KEY_BY_IP_VIP_CLUSTER = "KEY_BY_IP_VIP_CLUSTER";
        public static final String HEART_BEAT = "HEART_BEAT";
        public static final String CLUSTER_NAME = "CLUSTER_NAME";
        public static final String SHARD_TASK = "SHARD_TASK";
        public static final String KSQL_QUERY = "KSQL_QUERY";
        public static final String KSQL_QUERY_LOG = "KSQL_QUERY_LOG";

        public static final String KSQL_PHYSICS = "KSQL_PHYSICS";
        public static final String KSQL_LOGICAL = "KSQL_LOGICAL";
    }

    public interface WorkNode {
        public static final String ALIVE = "Alive";
        public static final String SHUTDOWN = "Shutdown";
        public static final String UNKOWN = "Unkown";
    }

    public interface TopicOffsetReset {
        public static final String[] STRATEGYS = new String[]{"--to-earliest", "--to-latest", "--to-current", "--to-offset", "--shift-by", "--to-datetime", "--by-duration"};
    }

}
