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

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Change quartz time.
 *
 * @author smartloli.
 *
 *         Created by Jul 06, 2020
 */

@Controller
@RequestMapping("/quartz")
public class QuartzController {

    @RequestMapping(value = "/update/{date}/", method = RequestMethod.GET)
    public void refreshScheduleJob(@PathVariable("date") int date) {
        try {
            System.out.println("Dynamic[" + date + "]s");
            WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
            SchedulerFactoryBean keSchedulerFactoryBean = (SchedulerFactoryBean)ctx.getBean("masterSchedule");
            SimpleTriggerFactoryBean keSimpleTriggerFactoryBean = (SimpleTriggerFactoryBean)ctx.getBean("masterTrigger");
            TriggerBuilder<SimpleTrigger> tb = keSimpleTriggerFactoryBean.getObject().getTriggerBuilder();
            Trigger trigger = tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(date)).build();
            //Trigger trigger = tb.withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?")).build();
            keSchedulerFactoryBean.getObject().rescheduleJob(keSimpleTriggerFactoryBean.getObject().getKey(), trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
