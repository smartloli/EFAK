/**
 * KeAuthenctiationSuccessHandler.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.web.security.handle;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Custom login success processing class.
 *
 * @Author: smartloli
 * @Date: 2023/7/2 18:53
 * @Version: 3.4.0
 */
@Slf4j
@Component
public class KeAuthenctiationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");

        RequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        // If url is null then redirect to index page.
        String url = "";
        if ((savedRequest == null)) {
            url = "/";
        } else {
            url = savedRequest.getRedirectUrl();
        }
        log.info("We are successful login, redirect to url: {}", url);

        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        HttpSession session = request.getSession();
        session.removeAttribute(clusterAlias);
        Long cid = 0L;
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        if (clusterInfos != null && clusterInfos.size() > 0) {
            cid = clusterInfos.get(0).getId();
        }
        session.setAttribute(clusterAlias, cid);
        log.info("Get remote[{}] clusterAlias from session md5 = {}, set default cid[{}]", remoteAddr, clusterAlias, cid);

        response.sendRedirect(url);
    }

}
