/**
 * ClusterManageTask.java
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
package org.kafka.eagle.web.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 14:35
 * @Version: 3.4.0
 */
@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class ClusterManageTask {

    @Async
    // @Scheduled(cron = "0/2 * * * * ? ")
    @Scheduled(fixedRate = 10000)
    public void test2() {
        log.info("test2, {}", Thread.currentThread().getName());
    }

    @Async
    // @Scheduled(cron = "0/10 * * * * ? ")
    @Scheduled(fixedRate = 5000)
    public void test1() {
        log.info("test1, {}", Thread.currentThread().getName());
    }

}
