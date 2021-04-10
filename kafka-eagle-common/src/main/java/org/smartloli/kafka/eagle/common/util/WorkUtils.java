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

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for reading text files. Get kafka eagle work node.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 12, 2020
 */
public class WorkUtils {

    public static List<String> getWorkNodes() {
        List<String> lines = new ArrayList<>();
        String osName = System.getProperties().getProperty("os.name");
        String workNodesName = "works";
        try {
            if (osName.contains("Mac") || osName.contains("Win")) {
                String path = SystemConfigUtils.getProperty("kafka.eagle.sql.worknode.server.path");
                if (StrUtils.isNull(path)) {
                    lines = Files.readLines(new File(WorkUtils.class.getClassLoader().getResource(workNodesName).getFile()), Charsets.UTF_8);
                } else {
                    lines = Files.readLines(new File(path), Charsets.UTF_8);
                }
            } else {
                lines = Files.readLines(new File(System.getProperty("user.dir") + "/conf/" + workNodesName), Charsets.UTF_8);
            }
        } catch (Exception ex) {
            ErrorUtils.print(WorkUtils.class).error("Get kafka eagle work node has error, msg is ", ex);
        }
        return lines;
    }

}
