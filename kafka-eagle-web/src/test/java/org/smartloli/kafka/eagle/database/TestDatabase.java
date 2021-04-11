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
package org.smartloli.kafka.eagle.database;

import org.smartloli.kafka.eagle.web.mapper.KeSqlMapperFactory;
import org.smartloli.kafka.eagle.web.mapper.dao.KeMBeanDao;

import java.util.HashMap;
import java.util.Map;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Apr 11, 2021
 */
public class TestDatabase {
    public static void main(String[] args) {
        KeMBeanDao keMBeanDao = ((KeMBeanDao) KeSqlMapperFactory.createMapper(KeMBeanDao.class));
        Map<String, Object> map = new HashMap<>();
        map.put("cluster", "cluster1");
        System.out.println(keMBeanDao.getMBeanOffline(map));
    }
}
