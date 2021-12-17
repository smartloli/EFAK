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
package org.smartloli.kafka.eagle.web.quartz.shard.task.sub;

import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

/**
 * Clean chart expired data.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 10, 2021
 */
public class CleanChartSubTask extends Thread {

    @Override
    public void run() {
        try {
            this.cleanCharts();
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Clean efak charts dataset has error, msg is ", e);
        }
    }

    private synchronized void cleanCharts() {
        if (SystemConfigUtils.getBooleanProperty("efak.metrics.charts")) {
            MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
            int retain = SystemConfigUtils.getIntProperty("efak.metrics.retain");
            metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanTopicLogSize(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanBScreenConsumerTopic(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanTopicSqlHistory(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
        }
    }
}
