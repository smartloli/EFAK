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
package org.smartloli.kafka.eagle.common.util;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Encode or decode text.
 *
 * @author smartloli.
 * <p>
 * Created by Nov 13, 2020
 */
public class UnicodeUtils {

    public static String encode(String text) {
        String msg = "";
        try {
            msg = URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            ErrorUtils.print(UnicodeUtils.class).error("Encode [" + text + "] has error, msg is ", e);
        }
        return msg;
    }

    public static String decode(String text) {
        String msg = "";
        try {
            msg = URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
            ErrorUtils.print(UnicodeUtils.class).error("Decode [" + text + "] has error, msg is ", e);
        }
        return msg;
    }

    public static String encodeSql(String sql) {

    }
}
