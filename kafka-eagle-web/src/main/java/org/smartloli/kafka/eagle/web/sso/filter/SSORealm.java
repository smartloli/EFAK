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
package org.smartloli.kafka.eagle.web.sso.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Login;
import org.smartloli.kafka.eagle.common.util.KConstants.Role;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.AccountService;
import org.smartloli.kafka.eagle.web.service.ResourceService;
import org.smartloli.kafka.eagle.web.sso.pojo.Resources;
import org.smartloli.kafka.eagle.web.sso.pojo.SSOAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verify access requests and their access rights.
 * 
 * @author smartloli.
 *
 *         Created by May 17, 2017
 */
public class SSORealm extends AuthorizingRealm {

	private Logger LOG = LoggerFactory.getLogger(SSORealm.class);

	private Map<Integer, PrincipalCollection> cachePrincipal = new HashMap<Integer, PrincipalCollection>();

	@Autowired
	private ResourceService resourcesService;
	@Autowired
	private AccountService accountService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Signiner signiner = (Signiner) getSession().getAttribute(KConstants.Login.SESSION_USER);
		List<Resources> resources = resourcesService.getUserResources(signiner.getId());

		List<String> permissions = new ArrayList<String>();
		for (Resources resource : resources) {
			permissions.add(resource.getUrl());
		}

		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		simpleAuthorizationInfo.addStringPermissions(permissions);

		cachePrincipal.put(signiner.getId(), principals);

		return simpleAuthorizationInfo;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		SSOAuthenticationToken token = (SSOAuthenticationToken) authcToken;
		Integer rtxno = token.getCode();
		Signiner signin = accountService.findUserByRtxNo(rtxno);
		this.getSession().setAttribute(Login.SESSION_USER, signin);
		List<Integer> roles = resourcesService.findRoleIdByUserId(signin.getId());
		if (roles.contains(Role.ADMINISRATOR)) {
			this.getSession().setAttribute(Role.WHETHER_SYSTEM_ADMIN, Role.ADMINISRATOR);
		}
		return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
	}

	private Session getSession() {
		Subject subject = SecurityUtils.getSubject();
		Session sesison = subject.getSession();
		return sesison;
	}

	public void clearAllCached() {
		Cache<Object, AuthorizationInfo> cache = this.getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
		LOG.info("SSOReaml - Clear all cached principal.");
	}

}
