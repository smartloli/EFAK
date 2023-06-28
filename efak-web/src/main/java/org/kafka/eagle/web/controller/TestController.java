/**
 * TestController.java
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

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:51
 * @Version: 3.4.0
 */
@RestController
public class TestController {

    @Secured(value = "ROLE_ADMIN")
    @GetMapping("/admin")
    public String admin() {
        return "获取信息....";
    }

    @Secured(value = "ROLE_DEV")
    @GetMapping("/dev")
    public String dev() {
        return "dev.... ";
    }

    @Secured(value = "ROLE_TEST")
    @GetMapping("/test")
    public String test() {
        return "test.... ";
    }
}
