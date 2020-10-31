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
package org.smartloli.kafka.eagle.core.task.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.task.rpc.MasterNodeClient;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.List;

/**
 * Construct a task schedule class of worker task.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 31, 2020
 */
public class WorkerScheduleTask {

    /**
     * Get worknode evaluate result.
     */
    public static List<JSONArray> evaluate(KSqlStrategy input, WorkNodeStrategy worknode) {
        JSONObject object = new JSONObject();
        object.put(KConstants.Protocol.KEY, KConstants.Protocol.KSQL_QUERY);
        object.put(KConstants.Protocol.VALUE, input);
        String result = MasterNodeClient.getResult(worknode.getHost(), worknode.getPort(), object);
        try {
            if (!StrUtils.isNull(result)) {
                return JSON.parseArray(result, JSONArray.class);
            }
        } catch (Exception e) {
            ErrorUtils.print(WorkerScheduleTask.class).error("Deserialize result by [" + worknode.toString() + "] has error, msg is ", e);
        }
        return null;
    }

}
