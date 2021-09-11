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
package org.smartloli.kafka.eagle.plugin.util;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

/**
 * Parse XML nodes for custom processing of different XML files.
 *
 * @author smartloli
 *
 *         Created by Nov 17, 2015
 *         Modify by Jul 07, 2020
 */
public class DomUtils {
	
	public static void setTomcatServerXML(String xml, String modifyPort) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(xml));
		Element node = document.getRootElement();
		List<?> tasks = node.elements();
		for (Object task : tasks) {
			Element taskNode = (Element) task;
			String name = taskNode.attributeValue("name");
			if ("Catalina".equals(name)) {
				String protocol = taskNode.element("Connector").attributeValue("protocol");
				if ("HTTP/1.1".equals(protocol)) {
					taskNode.element("Connector").addAttribute("port", modifyPort);
				}
			}
		}

		XMLWriter writer = new XMLWriter(new FileWriter(xml));
		writer.write(document);
		writer.close();
	}

	public static void setMasterQuartzXML(String xml, String quartz) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(xml));
		Element node = document.getRootElement();
		List<?> tasks = node.elements();
		for (Object task : tasks) {
			Element taskNode = (Element) task;
			String id = taskNode.attributeValue("id");
			if ("masterTrigger".equals(id)) {
				for(Object property : taskNode.elements()){
					Element propertyNode = (Element) property;
					String name = propertyNode.attributeValue("name");
					if ("cronExpression".equals(name)) {
						propertyNode.element("value").setText(quartz);
					}
				}
			}
		}

		XMLWriter writer = new XMLWriter(new FileWriter(xml));
		writer.write(document);
		writer.close();
	}
}
