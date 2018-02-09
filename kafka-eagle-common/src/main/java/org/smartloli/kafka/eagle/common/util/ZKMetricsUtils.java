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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.ZkClusterInfo;

/**
 * Get zookeeper cluster metrics data.
 * 
 * @author smartloli.
 *
 *         Created by Feb 6, 2018
 */
public class ZKMetricsUtils {

	private static Logger LOG = LoggerFactory.getLogger(ZKMetricsUtils.class);

	private static final String zk_avg_latency = "zk_avg_latency";
	private static final String zk_packets_received = "zk_packets_received";
	private static final String zk_packets_sent = "zk_packets_sent";
	private static final String zk_num_alive_connections = "zk_num_alive_connections";
	private static final String zk_outstanding_requests = "zk_outstanding_requests";
	private static final String zk_open_file_descriptor_count = "zk_open_file_descriptor_count";
	private static final String zk_max_file_descriptor_count = "zk_max_file_descriptor_count";

	public static ZkClusterInfo zkClusterInfo(String ip, int port) {
		Process pro = null;
		Runtime rt = Runtime.getRuntime();
		ZkClusterInfo zk = new ZkClusterInfo();
		try {
			String[] command = { "/bin/sh", "-c", "echo mntr | nc " + ip + " " + port };
			pro = rt.exec(command);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			while ((line = buffer.readLine()) != null) {
				String[] rs = line.split("\\s+");
				try {
					switch (rs[0]) {
					case zk_avg_latency:
						zk.setZkAvgLatency(rs[1]);
						break;
					case zk_packets_received:
						zk.setZkPacketsReceived(rs[1]);
						break;
					case zk_packets_sent:
						zk.setZkPacketsSent(rs[1]);
						break;
					case zk_num_alive_connections:
						zk.setZkNumAliveConnections(rs[1]);
						break;
					case zk_outstanding_requests:
						zk.setZkOutstandingRequests(rs[1]);
						break;
					case zk_open_file_descriptor_count:
						zk.setZkOpenFileDescriptorCount(rs[1]);
						break;
					case zk_max_file_descriptor_count:
						zk.setZkMaxFileDescriptorCount(rs[1]);
						break;
					default:
						break;
					}
				} catch (Exception ex) {
					LOG.error("Split zookeeper metrics data has error, msg is " + ex.getMessage());
				}
			}
			buffer.close();
			pro.destroy();
		} catch (Exception e) {
			LOG.error("Collector tcps has error, msg is " + e.getMessage());
		}
		return zk;
	}

}
