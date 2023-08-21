package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.alert.AlertChannelInfo;

import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/20 11:51
 * @Version: 3.4.0
 */
public interface IAlertChannelDaoService extends IService<AlertChannelInfo> {

    /**
     * get alert channel info by id.
     *
     * @param id
     * @return
     */
    AlertChannelInfo channel(Long id);

    /**
     * insert alert channel info.
     *
     * @param alertChannelInfo
     * @return
     */
    boolean insert(AlertChannelInfo alertChannelInfo);

    /**
     * Page limit.
     *
     * @param params
     * @return
     */
    Page<AlertChannelInfo> pages(Map<String, Object> params);

    /**
     * Update alert channel info.
     *
     * @param alertChannelInfo
     * @return
     */
    boolean update(AlertChannelInfo alertChannelInfo);

    /**
     * Delete alert channel info by id.
     *
     * @param id
     * @return
     */
    boolean delete(Long id);

}
