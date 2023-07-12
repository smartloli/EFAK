/**
 * ConsumerController.java
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
 * The controller class that handles consumer-related operations.
 * This class is responsible for handling requests and providing responses related to consumers.
 *
 * @Author: smartloli
 * @Date: 2023/7/12 21:39
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    @GetMapping("/summary")
    public String consumerSummaryView() {
        return "consumer/summary.html";
    }

}
