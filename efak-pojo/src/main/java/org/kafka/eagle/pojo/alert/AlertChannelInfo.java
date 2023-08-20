/**
 * AlertChannelInfo.java
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
package org.kafka.eagle.pojo.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/20 11:45
 * @Version: 3.4.0
 */
@Data
@TableName("ke_alert_channel")
public class AlertChannelInfo {

    /**
     * AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Alert channel cluster id.
     */
    private String clusterId;

    /**
     * Alert channel user role, such as 'ROLE_ADMIN'.
     */
    private String channelUserRoles;

    /**
     * Alert channel name, such as 'DingDing Alert'.
     */
    private String channelName;

    /**
     * Alert channel type, such as 'DingDing', 'WeChat', 'Email'.
     */
    private String channelType;

    /**
     * Alert channel url, such as 'https://oapi.dingtalk.com/robot/send?access_token=xxxxxx'.
     */
    private String channelUrl;

    /**
     * Alert channel auth json, such as 'xxxxxx'.
     */
    private String authJson;

    /**
     * Modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();

}
