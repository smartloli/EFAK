/**
 * JMXInitializeInfo.java
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
package org.kafka.eagle.pojo.kafka;

import lombok.Data;

import javax.management.remote.JMXServiceURL;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/6/7 14:10
 * @Version: 3.4.0
 */
@Data
public class JMXInitializeInfo {

    /**
     * ClusterID
     */
    private String clusterId;

    private String host;

    private int port;

    private String brokerId;

    /**
     * JMX connect url
     */
    private JMXServiceURL url;

    /**
     * Default uri
     */
    private String uri = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    /**
     * Default timeout 30s
     */
    private Long timeout = Long.valueOf(30);

    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private boolean acl = false;

    private String jmxUser;

    private String jmxPass;

    private boolean ssl = false;

    /**
     * Default key store path
     */
    private String keyStorePath;

    private String keyStorePassword;

    /**
     * Get value from jmx result
     */
    private String objectName;



}
