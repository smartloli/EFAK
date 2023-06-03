/**
 * BaseController.java
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Return backend data to the frontend in byte format.
 * @Author: smartloli
 * @Date: 2023/6/3 21:27
 * @Version: 3.4.0
 */
public class BaseController {
    private BaseController() {

    }

    /** Response data to request url. */
    public static void response(byte[] output, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Charset", "utf-8");
        response.setHeader("Cache-Control", "no-cache");

        output = output == null ? "".getBytes() : output;
        response.setContentLength(output.length);
        OutputStream out = response.getOutputStream();
        out.write(output);

        out.flush();
        out.close();
    }
}
