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
package org.smartloli.kafka.eagle.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Test Kafka sql clazz.
 * 
 * @author smartloli.
 *
 *         Created by Feb 27, 2017
 */
public class TestKafkaSQL {

	public static void main(String[] args) {
		try {
			String sql = "select * from [admin] where aa=1 and cc='b' group by aa order by aa desc "; 			
			Matcher matcher = null; 
			if( sql.startsWith("select") ){
		            matcher = Pattern.compile("select\\s.+from\\s(.+)where\\s(.*)").matcher(sql);
		            if(matcher.find()){
		                System.out.println(matcher.group(1).trim());
		                System.out.println(matcher.group(2).trim());
		            }
		        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
