/**
 * ErrorPageConf.java
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
package org.kafka.eagle.web.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * When requesting status 400, 401, 404, 500, jump to the specified page.
 * @Author: smartloli
 * @Date: 2023/6/10 19:48
 * @Version: 3.4.0
 */
@Configuration
public class ErrorPageConf implements ErrorPageRegistrar {
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage page400 = new ErrorPage(HttpStatus.BAD_REQUEST, "/errors/404");
        ErrorPage page401 = new ErrorPage(HttpStatus.UNAUTHORIZED, "/errors/404");
        ErrorPage page404 = new ErrorPage(HttpStatus.NOT_FOUND, "/errors/404");
        ErrorPage page500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errors/500");

        registry.addErrorPages(page400,page401,page404,page500);
    }

}
