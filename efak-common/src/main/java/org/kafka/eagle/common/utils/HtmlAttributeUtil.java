/**
 * HtmlAttributeUtil.java
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
package org.kafka.eagle.common.utils;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/4 20:58
 * @Version: 3.4.0
 */
public class HtmlAttributeUtil {

    private HtmlAttributeUtil() {

    }

    public static String getClusterStatusHtml(int status) {
        String result = "";
        if (status == 0) {
            result = "<span class='badge bg-danger'>异常</span>";
        } else if (status == 1) {
            result = "<span class='badge bg-success'>健康</span>";
        } else if (status == 2) {
            result = "<span class='badge bg-secondary'>初始化</span>";
        }
        return result;
    }

    public static String getAuthHtml(String auth) {
        String result = "";
        if("Y".equals(auth)){
            result = "<span class='badge bg-primary'>是</span>";
        }else{
            result = "<span class='badge bg-secondary'>否</span>";
        }
        return result;
    }

}
