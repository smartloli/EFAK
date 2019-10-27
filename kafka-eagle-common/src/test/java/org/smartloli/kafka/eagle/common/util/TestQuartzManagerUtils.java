/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.util;

import java.util.Date;

import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Oct 25, 2019
 */
public class TestQuartzManagerUtils {

	public static void main(String[] args) {
		System.out.println("Send msg, date : [" + new Date().toString() + "]");
		String jobName = "ke_job_id_" + new Date().getTime();
		String jobName2 = "ke_job2_id_" + new Date().getTime();
		BaseJobContext bjc = new BaseJobContext();
		bjc.setData("test");
		bjc.setUrl("http://www.kafka-eagle.org");
		QuartzManagerUtils.addJob(bjc,jobName, TestJob.class, QuartzManagerUtils.getCron(new Date(), 5));
		QuartzManagerUtils.addJob(bjc,jobName2, TestJob.class, QuartzManagerUtils.getCron(new Date(), 10));
		// QuartzManagerUtils.addJob("ke_job_id_" + new Date().getTime(),
		// TestJob.class, QuartzManagerUtils.getCron(new Date(), 10));
	}

}
