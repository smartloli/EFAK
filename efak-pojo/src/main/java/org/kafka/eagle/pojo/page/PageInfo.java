/**
 * PageInfo.java
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
package org.kafka.eagle.pojo.page;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for assisting with pagination of collection data.
 * This class provides methods to simplify the implementation of pagination functionality
 * by dividing a given collection into smaller pages of data.
 *
 * @Author: smartloli
 * @Date: 2023/8/16 21:04
 * @Version: 3.4.0
 */
@Data
public class PageInfo<T> {

    private Integer total = 0;
    private List<Object> list = new ArrayList<Object>();

    public PageInfo(List<T> list, Integer pageNum, Integer pageSize) {
        this.total = list.size();
        int offset = 0;
        for (Integer i=0;i<list.size();i++) {
            if (offset >= pageNum && offset < (pageNum + pageSize)) {
                Object object = list.get(i);
                this.list.add(object);
            }
            offset++;
        }

    }

//    public static <T> PageInfo<T> of(List<? extends T> list) {
//        return new PageInfo(list);
//    }
//
//    public PageInfo(List<Object> list) {
//        this.list = list;
//    }


}
