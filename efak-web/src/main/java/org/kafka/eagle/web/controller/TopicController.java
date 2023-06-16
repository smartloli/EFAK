/**
 * WelcomeController.java
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
package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * In the provided example, the TopicController handles the root URL request ("/") and is
 * responsible for displaying the cluster management interface. The welcome() method is
 * annotated with @GetMapping("/") to map the root URL request to this method.
 * It returns the name of the cluster management view template, which will be resolved by
 * the configured view resolver.
 *
 * @Author: smartloli
 * @Date: 2023/6/16 23:45
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/topic")
@Slf4j
public class TopicController {
    /**
     * Handles the root URL request and displays the cluster management interface.
     *
     * @return The name of the cluster management view template.
     */
    @GetMapping("/create")
    public String createView() {
        return "topic/create.html";
    }
}
