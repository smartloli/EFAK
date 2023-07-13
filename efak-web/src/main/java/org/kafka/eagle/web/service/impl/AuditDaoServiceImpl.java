/**
 * ClusterDaoServiceImpl.java
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
package org.kafka.eagle.web.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.kafka.eagle.pojo.audit.AuditLogInfo;
import org.kafka.eagle.web.dao.mapper.AuditDaoMapper;
import org.kafka.eagle.web.service.IAuditDaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @Author: smartloli
 * @Date: 2023/7/7 21:20
 * @Version: 3.4.0
 */
@Service
public class AuditDaoServiceImpl extends ServiceImpl<AuditDaoMapper, AuditLogInfo> implements IAuditDaoService {

    @Resource
    AuditDaoMapper auditDaoMapper;

    @Override
    public AuditLogInfo auditById(Long id) {
        return new LambdaQueryChainWrapper<>(this.auditDaoMapper).eq(AuditLogInfo::getId, id).one();
    }

    @Override
    public boolean insert(AuditLogInfo auditLogInfo) {
        boolean status = false;
        int code = this.auditDaoMapper.insert(auditLogInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<AuditLogInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<AuditLogInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<AuditLogInfo> queryChainWrapper = new LambdaQueryChainWrapper<AuditLogInfo>(this.auditDaoMapper);
        queryChainWrapper.like(AuditLogInfo::getHost, params.get("search").toString());
        return queryChainWrapper.orderByDesc(AuditLogInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean batch(List<AuditLogInfo> auditLogInfos) {
        boolean status = false;
        int code = this.auditDaoMapper.insertBatchSomeColumn(auditLogInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean delete(String day) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new LambdaUpdateChainWrapper<>(this.auditDaoMapper).lt(AuditLogInfo::getModifyTime, LocalDateTime.parse(day,df)).remove();
    }
}
