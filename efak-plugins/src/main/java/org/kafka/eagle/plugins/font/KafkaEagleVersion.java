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
package org.kafka.eagle.plugins.font;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;

import java.io.File;

/**
 * Print kafka eagle system version.
 *
 * @author smartloli.
 * <p>
 * Created by May 23, 2023
 */
@Slf4j
public class KafkaEagleVersion {

    public static void version() {
        try {
            String name = getFontPath();
            String version = KConstants.Common.EFAK_VERSION;
            File file = new File(name);
            String asciiArt = FigletFont.convertOneLine(file, "EFAK");
            System.out.println("Welcome to");
            System.out.println(asciiArt + "( Eagle For Apache Kafka® )\n");
            System.out.println("Version " + version + " -- Copyright 2016-2023");
        } catch (Exception e) {
            log.error("Get kafka eagle version has error, msg is {}", e);
        }

    }

    private static String getFontPath() {
        String path = "";
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows") || osName.contains("Mac")) {
            path = "/Users/smartloli/workspace/kafka-eagle/efak-web/src/main/resources/font/slant.flf";
        } else {
            path = System.getProperty("user.dir") + "/font/slant.flf";
        }
        return path;
    }

}
