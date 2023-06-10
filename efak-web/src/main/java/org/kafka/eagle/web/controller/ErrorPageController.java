/**
 * ErrorPageController.java
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
 * Error page controller to viewer data.
 * @Author: smartloli
 * @Date: 2023/6/3 22:20
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/errors")
@Slf4j
public class ErrorPageController {

    @GetMapping("/404")
    public String e404() {
        return "errors/404.html";
    }

    @GetMapping("/500")
    public String e500() {
        return "errors/500.html";
    }

}
