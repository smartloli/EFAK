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
package org.smartloli.kafka.eagle.web.dao;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.web.pojo.Signiner;

/**
 * User interface definition
 * 
 * @author smartloli.
 *
 *         Created by May 16, 2017
 */
public interface UserDao {
	
	public Signiner login(Signiner signin);

	public int reset(Signiner signin);

	public List<Signiner> findUserByRtxNo(int rtxno);

	public List<Signiner> findUserBySearch(Map<String, Object> params);

	public int userCounts();

	public int insertUser(Signiner signin);
	
	public int modify(Signiner signin);
	
	public int delete(Signiner signin);
	
	public Signiner findUserById(int id);
	
	public Signiner findUserLimitOne();
}
