package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.user.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: 用户服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/05 02:41:28
 * @version 5.0.0
 */
public interface UserService {

    /**
     * 获取所有用户信息
     * 
     * @return
     */
    List<UserInfo> list();

    /**
     * 根据用户名获取用户信息
     * 
     * @param username
     * @return
     */
    UserInfo getUserByUsername(String username);

    /**
     * 根据用户名和密码获取用户信息
     * 
     * @param username
     * @param password
     * @return
     */
    UserInfo getUserByUsernameAndPassword(String username, String password);

    /**
     * 根据ID获取用户信息
     * 
     * @param id
     * @return
     */
    UserInfo getUserById(Long id);

    /**
     * 插入用户信息
     * 
     * @param userInfo
     * @return
     */
    boolean insertUser(UserInfo userInfo);

    /**
     * 更新用户信息
     * 
     * @param userInfo
     * @return
     */
    boolean updateUser(UserInfo userInfo);

    /**
     * 重置用户密码
     * 
     * @param userInfo
     * @return
     */
    boolean resetPassword(UserInfo userInfo);

    /**
     * 删除用户
     * 
     * @param userInfo
     * @return
     */
    boolean deleteUser(UserInfo userInfo);

    /**
     * 根据ID删除用户
     * 
     * @param id
     * @return
     */
    boolean deleteUserById(Long id);

    /**
     * 分页查询用户列表
     * 
     * @param page   页码
     * @param size   每页大小
     * @param search 搜索关键词
     * @param status 状态过滤
     * @return 分页结果
     */
    Map<String, Object> getUsersWithPagination(int page, int size, String search, String status);

    /**
     * 验证用户密码
     * 
     * @param userInfo 用户信息
     * @param password 待验证的密码
     * @return 验证结果
     */
    boolean verifyPassword(UserInfo userInfo, String password);

}