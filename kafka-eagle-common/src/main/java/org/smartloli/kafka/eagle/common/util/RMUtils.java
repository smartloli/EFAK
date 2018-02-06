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
import org.smartloli.kafka.eagle.common.util.KConstants.Linux;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Collect Linux operating system memory, CPU, IO and other index data.
 * 
 * @author smartloli.
 *
 *         Created by Dec 8, 2017
 */
public class RMUtils {

	private static final Logger LOG = LoggerFactory.getLogger(RMUtils.class);

	/** Collector system cpu. */
	public static float cpu(String hostname) {

		float cpuUsage = 0.0f;
		Process sProcess, eProcess;
		Runtime rt = Runtime.getRuntime();
		String[] command = { "/bin/sh", "-c", "ssh " + hostname + " 'cat /proc/stat'" };
		try {
			sProcess = rt.exec(command);
			BufferedReader sBuffer = new BufferedReader(new InputStreamReader(sProcess.getInputStream()));
			String line = null;
			long sIdleCpuTime = 0, sTotalCpuTime = 0;
			while ((line = sBuffer.readLine()) != null) {
				if (line.startsWith(Linux.CPU)) {
					line = line.trim();
					String[] temp = line.split("\\s+");
					sIdleCpuTime = Long.parseLong(temp[4]);
					for (String str : temp) {
						if (!str.equals(Linux.CPU)) {
							sTotalCpuTime += Long.parseLong(str);
						}
					}
					LOG.debug("[IdleCpuTime First] - " + sIdleCpuTime + ",[TotalCpuTime First] - " + sTotalCpuTime);
					break;
				}
			}
			sBuffer.close();
			sProcess.destroy();

			try {
				Thread.sleep(Linux.SLEEP);
			} catch (Exception e) {
				LOG.error("Cpu sleep has error, msg is " + e.getMessage());
			}

			eProcess = rt.exec(command);
			BufferedReader eBuffer = new BufferedReader(new InputStreamReader(eProcess.getInputStream()));
			long eIdleCpuTime = 0, eTotalCpuTime = 0;
			while ((line = eBuffer.readLine()) != null) {
				if (line.startsWith(Linux.CPU)) {
					line = line.trim();
					LOG.debug("[CPU Second Info] - " + line);
					String[] temp = line.split("\\s+");
					eIdleCpuTime = Long.parseLong(temp[4]);
					for (String str : temp) {
						if (!str.equals(Linux.CPU)) {
							eTotalCpuTime += Long.parseLong(str);
						}
					}
					LOG.debug("[IdleCpuTime Second] - " + eIdleCpuTime + ",[TotalCpuTime Second] - " + eTotalCpuTime);
					break;
				}
			}

			if (sIdleCpuTime != 0 && sTotalCpuTime != 0 && eIdleCpuTime != 0 && eTotalCpuTime != 0) {
				cpuUsage = 1 - ((float) (eIdleCpuTime - sIdleCpuTime)) / ((float) (eTotalCpuTime - sTotalCpuTime));
				LOG.debug("[CPU Usage] - " + cpuUsage);
			}
		} catch (Exception e) {
			LOG.error("Collector system cpu has error, msg is " + e.getMessage());
		}
		return cpuUsage;
	}

	/** Transmission speed of multi network gateway, unit is byte. */
	public static JSONArray networks(String hostname) {
		JSONArray nets = new JSONArray();
		try {
			long sTime = System.currentTimeMillis();
			JSONArray sNetWorks = network(hostname);
			Thread.sleep(Linux.SLEEP);
			long eTime = System.currentTimeMillis();
			JSONArray eNetWorks = network(hostname);

			for (Object eObject : eNetWorks) {
				JSONObject eNetWork = (JSONObject) eObject;
				String eFace = eNetWork.getString("face");
				long eOut = eNetWork.getLongValue("out");
				long eIn = eNetWork.getLongValue("in");
				for (Object sObject : sNetWorks) {
					JSONObject sNetWork = (JSONObject) sObject;
					String sFace = sNetWork.getString("face");
					if (eFace.equals(sFace)) {
						long sOut = sNetWork.getLongValue("out");
						long sIn = sNetWork.getLongValue("in");
						JSONObject net = new JSONObject();
						float netRate = (float) (eIn - sIn + eOut - sOut) * 8 / (eTime - sTime);
						// unit is bps, such as 20bps
						net.put("net", netRate);
						net.put("face", eFace);
						nets.add(net);
						break;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Collector networks speed has error, msg is " + e.getMessage());
		}
		LOG.debug("[Nets] - " + nets);
		return nets;
	}

	/** Collector system network. */
	private static JSONArray network(String hostname) {
		JSONArray nets = new JSONArray();
		Process process;
		Runtime r = Runtime.getRuntime();
		try {
			String[] command = { "/bin/sh", "-c", "ssh " + hostname + " 'cat /proc/net/dev'" };
			process = r.exec(command);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			int count = 0;
			while ((line = buffer.readLine()) != null) {
				line = line.trim();
				if (++count > 2 && !line.startsWith(Linux.LO)) {
					String[] temp = line.split("\\s+");
					String face = temp[0].replaceAll(":", "");
					long in = Long.parseLong(temp[1]);
					long out = Long.parseLong(temp[9]);
					JSONObject net = new JSONObject();
					net.put("face", face);
					net.put("in", in);
					net.put("out", out);
					nets.add(net);
				}

			}
			buffer.close();
			process.destroy();
		} catch (Exception e) {
			LOG.error("Collecrot network has error, msg is " + e.getMessage());
		}
		LOG.debug("[NetWork] - " + nets.toJSONString());
		return nets;
	}

	/** Collector system io. */
	public static JSONArray io(String hostname) {
		JSONArray ios = new JSONArray();
		Process pro = null;
		Runtime rt = Runtime.getRuntime();
		try {
			// String command = "iostat -d -x";
			String[] command = { "/bin/sh", "-c", "ssh " + hostname + " 'iostat -d -k -x'" };
			pro = rt.exec(command);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			int count = 0;
			while ((line = buffer.readLine()) != null) {
				if (++count >= 4 && line.contains(Linux.DEVICE)) {
					String[] temp = line.split("\\s+");
					if (temp.length > 1) {
						// unit is %, such 0.03%
						float util = Float.parseFloat(temp[temp.length - 1]);
						String device = temp[0];
						// unit is kb/seconds, such as 30.32kb/s
						float rkb = Float.parseFloat(temp[5]);
						float wkb = Float.parseFloat(temp[6]);
						JSONObject io = new JSONObject();
						io.put("util", util);
						io.put("device", device);
						io.put("rkb", rkb);
						io.put("wkb", wkb);
						ios.add(io);
					}
				}
			}
			LOG.debug("[IO] - " + ios.toJSONString());
			buffer.close();
			pro.destroy();
		} catch (Exception e) {
			LOG.error("Collector io has error, msg is " + e.getMessage());
		}
		return ios;
	}

	/** Collector system memory. */
	public static float memory(String hostname) {
		float memUsage = 0.0f;
		Process pro = null;
		Runtime rt = Runtime.getRuntime();
		try {
			String[] command = { "/bin/sh", "-c", "ssh " + hostname + " 'cat /proc/meminfo'" };
			pro = rt.exec(command);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			int count = 0;
			long totalMem = 0, freeMem = 0;
			while ((line = buffer.readLine()) != null) {
				String[] memInfo = line.split("\\s+");
				if (memInfo[0].startsWith(Linux.MemTotal)) {
					totalMem = Long.parseLong(memInfo[1]);
				}
				if (memInfo[0].startsWith(Linux.MemFree)) {
					freeMem = Long.parseLong(memInfo[1]);
				}
				memUsage = 1 - (float) freeMem / (float) totalMem;
				LOG.debug("[MemUsage] - " + memUsage);
				if (++count == 2) {
					break;
				}
			}
			buffer.close();
			pro.destroy();
		} catch (Exception e) {
			LOG.error("Collector memory has error, msg is " + e.getMessage());
		}
		return memUsage;
	}

	/** Collector system tcps. */
	public static long tcp(String hostname) {
		long tcpNums = 0L;
		Process pro = null;
		Runtime rt = Runtime.getRuntime();
		try {
			String[] command = { "/bin/sh", "-c", "ssh " + hostname + " 'cat /proc/net/snmp'" };
			pro = rt.exec(command);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			while ((line = buffer.readLine()) != null) {
				String[] tcps = line.split("\\s+");
				if (tcps[0].startsWith(Linux.TCP) && !line.contains(Linux.CurrEstab)) {
					tcpNums = Long.parseLong(tcps[9]);
				}
			}
			buffer.close();
			pro.destroy();
		} catch (Exception e) {
			LOG.error("Collector tcps has error, msg is " + e.getMessage());
		}
		return tcpNums;
	}

}
