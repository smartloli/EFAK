/**
 * ConsumerGroupTopicDaoServiceImpl.java
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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.kafka.eagle.pojo.cluster.KafkaMBeanInfo;
import org.kafka.eagle.web.dao.mapper.KafkaMBeanDaoMapper;
import org.kafka.eagle.web.service.IKafkaMBeanDaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/5 21:33
 * @Version: 3.4.0
 */
@Service
public class KafkaMBeanDaoServiceImpl extends ServiceImpl<KafkaMBeanDaoMapper, KafkaMBeanInfo> implements IKafkaMBeanDaoService {

    @Resource
    private KafkaMBeanDaoMapper kafkaMBeanDaoMapper;

    @Override
    public List<KafkaMBeanInfo> kafkaMBeanList(String clusterId) {
        return null;
    }

    @Override
    public KafkaMBeanInfo kafkaMBean(KafkaMBeanInfo kafkaMetricInfo) {
        return null;
    }

    @Override
    public boolean insert(KafkaMBeanInfo kafkaMetricInfo) {
        return false;
    }

    @Override
    public boolean update(KafkaMBeanInfo kafkaMetricInfo) {
        return false;
    }

    @Override
    public boolean batch(List<KafkaMBeanInfo> kafkaMetricInfos) {
        boolean status = false;
        int code = this.kafkaMBeanDaoMapper.insertBatchSomeColumn(kafkaMetricInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public boolean delete(List<Long> kafkaMBeanIds) {
        return false;
    }

    @Override
    public List<KafkaMBeanInfo> pages(Map<String, Object> params) {

        String cid = params.get("cid").toString();
        List<String> mbeans = (List<String>) params.get("modules");
        String stime = params.get("stime").toString();
        String etime = params.get("etime").toString();

        return new LambdaQueryChainWrapper<>(this.kafkaMBeanDaoMapper).eq(KafkaMBeanInfo::getClusterId, cid).in(KafkaMBeanInfo::getMbeanKey, mbeans).between(KafkaMBeanInfo::getDay, stime, etime).list();
    }
}
