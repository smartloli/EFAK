/**
 * SchedulerConfig.java
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
package org.kafka.eagle.web.quartz.config;

import org.kafka.eagle.web.quartz.manager.AutowiringBeanJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/14 23:11
 * @Version: 3.4.0
 */
@Configuration
public class SchedulerConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AutowiringBeanJobFactory jobFactory;

    @Qualifier("schedulerQuartzFactoryBean")
    private SchedulerFactoryBean schedulerFactoryBean;

    @Bean
    public SchedulerFactoryBean schedulerQuartzFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setSchedulerName("EFAK_SCHEDULER_TASKS");
        factory.setDataSource(dataSource);
        factory.setAutoStartup(true);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        factory.setTaskExecutor(schedulerThreadPool());
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10);// unit seconds
        factory.setJobFactory(jobFactory);
        return factory;
    }

    @Bean
    public Executor schedulerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        executor.setQueueCapacity(Runtime.getRuntime().availableProcessors());
        return executor;
    }

}
