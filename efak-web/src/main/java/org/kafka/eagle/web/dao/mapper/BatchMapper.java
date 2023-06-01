package org.kafka.eagle.web.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/1 22:28
 * @Version: 3.4.0
 */
public interface BatchMapper<T> extends BaseMapper<T> {
    Integer insertBatchSomeColumn(List<T> entityList);
}
