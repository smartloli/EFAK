package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.user.UserInfo;

import java.util.List;

/**
 * <p>
 * User 数据访问层接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/05 01:51:02
 * @version 5.0.0
 */
@Mapper
public interface UserMapper {

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info WHERE username = #{username}")
        UserInfo findByUsername(String username);

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info WHERE username = #{username} AND password = #{password}")
        UserInfo findByUsernameAndPassword(String username, String password);

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info WHERE id = #{id}")
        UserInfo findById(Long id);

        @Update("UPDATE ke_users_info SET username = #{username}, roles = #{roles}, status = #{status}, modify_time = #{modifyTime} WHERE id = #{id}")
        int updateUser(UserInfo userInfo);

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info ORDER BY id DESC LIMIT #{offset}, #{size}")
        List<UserInfo> findUsersWithPagination(@Param("offset") int offset, @Param("size") int size);

        @Select("SELECT COUNT(*) FROM ke_users_info")
        int countTotalUsers();

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info WHERE username LIKE CONCAT('%', #{search}, '%') OR roles LIKE CONCAT('%', #{search}, '%') ORDER BY id DESC LIMIT #{offset}, #{size}")
        List<UserInfo> findUsersWithSearch(@Param("search") String search, @Param("offset") int offset,
                        @Param("size") int size);

        @Select("SELECT COUNT(*) FROM ke_users_info WHERE username LIKE CONCAT('%', #{search}, '%') OR roles LIKE CONCAT('%', #{search}, '%')")
        int countUsersWithSearch(@Param("search") String search);

        @Select("SELECT id, username, password, origin_password as originPassword, roles, status, modify_time as modifyTime FROM ke_users_info WHERE status = #{status} ORDER BY id DESC LIMIT #{offset}, #{size}")
        List<UserInfo> findUsersByStatus(@Param("status") String status, @Param("offset") int offset,
                                         @Param("size") int size);

        @Select("SELECT COUNT(*) FROM ke_users_info WHERE status = #{status}")
        int countUsersByStatus(@Param("status") String status);

        @Insert("INSERT INTO ke_users_info (username, password, origin_password, roles, status, modify_time) VALUES (#{username}, #{password}, #{originPassword}, #{roles}, #{status}, #{modifyTime})")
        int insertUser(UserInfo userInfo);

        @Delete("DELETE FROM ke_users_info WHERE id = #{id}")
        int deleteUserById(Long id);

        @Update("UPDATE ke_users_info SET password = #{password}, origin_password = #{originPassword}, modify_time = #{modifyTime} WHERE id = #{id}")
        int updatePassword(UserInfo userInfo);
}