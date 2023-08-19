package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.user.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:37
 * @Version: 3.4.0
 */
public interface IUserDaoService extends IService<UserInfo> {

    /**
     * get all cluster info.
     * @return
     */
    List<UserInfo> list();

    /**
     * get user info by username.
     * @param username
     * @return
     */
    UserInfo users(String username);

    UserInfo users(String username,String password);

    /**
     * get user info by id.
     * @param id
     * @return
     */
    UserInfo users(Long id);

    /**
     * insert userInfo
     * @param userInfo
     * @return
     */
    boolean insert(UserInfo userInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<UserInfo> pages(Map<String,Object> params);

    /**
     * Update user info by userInfo.
     * @param userInfo
     * @return
     */
    boolean update(UserInfo userInfo);

    boolean reset(UserInfo userInfo);

    boolean delete(UserInfo userInfo);
    boolean delete(Long id);

}
