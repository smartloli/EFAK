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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Oct 25, 2019
 */
public class TestJob implements Job {
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		BaseJobContext bjc = (BaseJobContext) arg0.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
		System.out.println("kafka eagle quartz job, date : [" + new Date().toString() + "],[" + bjc + "]");
	}
}
