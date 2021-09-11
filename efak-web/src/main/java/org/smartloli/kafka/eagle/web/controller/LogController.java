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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

/**
 * Add topic export task and submit.
 *
 * @author smartloli.
 * <p>
 * Created by Nov 21, 2020
 */
@Controller
public class LogController {

    /**
     * Topic common viewer.
     */
    @RequestMapping(value = "/log/add", method = RequestMethod.GET)
    public ModelAndView topicLogView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/log/add");
        return mav;
    }

    /**
     * Topic common viewer.
     */
    @RequestMapping(value = "/log/tasks", method = RequestMethod.GET)
    public ModelAndView topicTasksView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/log/tasks");
        return mav;
    }

}
