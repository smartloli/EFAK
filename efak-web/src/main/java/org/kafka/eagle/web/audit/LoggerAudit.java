/**
 * LoggerAudit.java
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
package org.kafka.eagle.web.audit;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.kafka.eagle.pojo.audit.AuditLogInfo;
import org.kafka.eagle.web.service.IAuditDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Used to record audit log information of user access to the system.
 *
 * @Author: smartloli
 * @Date: 2023/7/7 19:43
 * @Version: 3.4.0
 */
@Component
@Aspect
@Slf4j
public class LoggerAudit {


    @Autowired
    private IAuditDaoService auditDaoService;

    @Pointcut("execution(* org.kafka.eagle.web.controller.*.*(..))")
    public void audit() {
    }

    @Around("audit()")
    public Object loggerAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        long stime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(attributes, true);
        if (attributes == null) {
            return false;
        }
        HttpServletRequest request = attributes.getRequest();
        try {
            Object result = joinPoint.proceed();
            String uri = request.getRequestURI();
            String method = request.getMethod();
            String host = request.getRemoteAddr();
            String params = JSON.toJSONString(request.getParameterMap());
            Integer status = attributes.getResponse().getStatus();
            long etime = System.currentTimeMillis();

            // record audit log
            AuditLogInfo auditLogInfo = new AuditLogInfo();
            auditLogInfo.setHost(host);
            auditLogInfo.setCode(status);
            auditLogInfo.setParams(params);
            auditLogInfo.setMethod(method);
            auditLogInfo.setSpentTime(etime - stime);
            auditLogInfo.setUri(uri);
            auditDaoService.insert(auditLogInfo);

            return result;

        } catch (Throwable e) {
            log.error("Get audit log has error, msg is {}", e);
            throw e;
        }

    }

}
