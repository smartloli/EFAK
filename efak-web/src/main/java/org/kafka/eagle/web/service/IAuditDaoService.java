package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.audit.AuditLogInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/7 21:00
 * @Version: 3.4.0
 */
public interface IAuditDaoService extends IService<AuditLogInfo> {

    boolean insert(AuditLogInfo auditLogInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<AuditLogInfo> pages(Map<String,Object> params);

    boolean batch(List<AuditLogInfo> createInfos);

}
