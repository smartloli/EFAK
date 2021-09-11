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
package org.smartloli.kafka.eagle.factory;

import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Jan 24, 2021
 */
public class TestMx4jServiceImpl {
    private static Mx4jService mx4jService = new Mx4jFactory().create();

    public static void main(String[] args) {
        // MBeanInfo mBeanInfo = mx4jService.bytesInPerSec("cluster1", "127.0.0.1:9999", "k20200326");
        // System.out.println(mBeanInfo.toString());

        System.out.println(new Double(Double.parseDouble("2636")).longValue());
    }
}
