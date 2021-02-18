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
package org.smartloli.kafka.eagle.web.controller;

import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.plugin.mysql.MySqlRecordSchema;
import org.smartloli.kafka.eagle.plugin.sqlite.SqliteRecordSchema;
import org.smartloli.kafka.eagle.plugin.util.JConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

/**
 * Load kafka consumer internal thread to get offset.
 *
 * @author smartloli.
 * <p>
 * Created by May 22, 2017
 */
@Component
public class StartupListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        ContextSchema context = new ContextSchema();
        context.start();
    }

    public static Object getBean(String beanName) {
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return clazz.cast(getBean(beanName));
    }

    class ContextSchema extends Thread {
        public void run() {
            String jdbc = SystemConfigUtils.getProperty("kafka.eagle.driver");
            if (JConstants.MYSQL_DRIVER_V5.equals(jdbc) || JConstants.MYSQL_DRIVER_V8.equals(jdbc)) {
                MySqlRecordSchema.schema();
            } else if (JConstants.SQLITE_DRIVER.equals(jdbc)) {
                SqliteRecordSchema.schema();
            }
        }
    }

}
