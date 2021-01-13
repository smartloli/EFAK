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
package org.smartloli.kafka.eagle.plugin.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.plugin.util.DomUtils;
import org.smartloli.kafka.eagle.plugin.util.JConstants;

/**
 * Dynamically modify the master node scheduling time.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 07, 2020
 */
public class MasterQuartzListen {
    private static Logger LOG = LoggerFactory.getLogger(MasterQuartzListen.class);

    public static void main(String[] args) {
        try {
            String xml = "";
            String osName = System.getProperties().getProperty("os.name");
            if (osName.contains(JConstants.WIN)) {
                xml = System.getProperty("user.dir") + "\\kms\\webapps\\ke\\WEB-INF\\classes\\master-quartz.xml";
            } else {
                xml = System.getProperty("user.dir") + "/kms/webapps/ke/WEB-INF/classes/master-quartz.xml";
            }
            String quartz = SystemConfigUtils.getProperty("kafka.eagle.quartz.master.time");
            DomUtils.setMasterQuartzXML(xml, quartz);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Listen master node quartz time has error, msg is " + ex.getMessage());
        }
    }

}
