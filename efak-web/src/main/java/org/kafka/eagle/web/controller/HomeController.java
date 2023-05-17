/**
 * DashboardController.java
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *  The DashboardController handles requests for dashboard pages. This controller handles the following requests:
 *  - /dashboard: returns the main dashboard page.
 *  - /dashboard/{id}: returns a specific dashboard page with the given ID.
 *  - /dashboard/create: returns a page for creating a new dashboard.
 *  - /dashboard/save: handles POST requests to save a dashboard.
 *
 *  This controller uses the DashboardService to manage the creation, editing, and saving of dashboards. When
 *  handling requests, this controller also uses the Thymeleaf template engine to render pages and pass necessary data
 *  to the pages.
 *
 * @Author: smartloli
 * @Date: 2023/5/13 23:38
 * @Version: 3.4.0
 */
@Controller
public class HomeController {

    @GetMapping("")
    public String index() {
        return "dashboard.html";
    }

}
