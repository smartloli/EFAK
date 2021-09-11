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
package org.smartloli.kafka.eagle.web.sso.pojo;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Authority and login check entity.
 * 
 * @author smartloli.
 *
 *         Created by May 17, 2017
 */
@SuppressWarnings("unused")
public class SSOAuthenticationToken implements AuthenticationToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int code;
	private String realName = "";
	private String userName = "";
	private String email = "";

	public SSOAuthenticationToken() {
	}

	public SSOAuthenticationToken(int code, String realName, String userName, String email) {
		this.code = code;
		this.realName = realName;
		this.userName = userName;
		this.email = email;
	}

	@Override
	public Object getPrincipal() {
		return code;
	}

	@Override
	public Object getCredentials() {
		return code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
