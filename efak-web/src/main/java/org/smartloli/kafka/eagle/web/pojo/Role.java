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
package org.smartloli.kafka.eagle.web.pojo;

import com.google.gson.Gson;

/**
 * The role entity corresponds to the role table in the database.
 * 
 * @author smartloli.
 *
 *         Created by May 24, 2017
 */
public class Role {

	private int id;
	private String roleName;
	private String roleDescriber;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescriber() {
		return roleDescriber;
	}

	public void setRoleDescriber(String roleDescriber) {
		this.roleDescriber = roleDescriber;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
