/**
 * Md5Util.java
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
package org.kafka.eagle.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Md5 encryption tool class.
 *
 * @Author: smartloli
 * @Date: 2023/6/21 21:24
 * @Version: 3.4.0
 */
@Slf4j
public class Md5Util {

    /**
     * Generate md5 string 16 bit.
     * @param input
     * @return
     */
    public static String generateMD5(String input) {
        String str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }

            str = sb.toString().substring(8, 24); // 16bit
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.error("Get md5 string error, msg is {}",e);
        }
        return str;
    }

}
