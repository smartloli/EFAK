/**
 * AutowiringBeanJobFactory.java
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
package org.kafka.eagle.web.quartz.manager;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/7/15 20:18
 * @Version: 3.4.0
 */
@Component
public class AutowiringBeanJobFactory extends AdaptableJobFactory {
    @Autowired
    private AutowireCapableBeanFactory autowiringBeanJobFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // create job instance
        Object jobInstance = super.createJobInstance(bundle);
        // add bean into spring
        autowiringBeanJobFactory.autowireBean(jobInstance);

        return jobInstance;
    }

}