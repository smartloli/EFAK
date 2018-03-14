/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.sql.ignite.factory;

import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.KConstants.TopicCache;
import org.smartloli.kafka.eagle.core.sql.ignite.domain.TopicX;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Mar 9, 2018
 */
public class KafkaSqlFactory {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaSqlFactory.class);

	private static Ignite ignite = null;

	private static void getInstance() {
		if (ignite == null) {
			ignite = Ignition.start();
		}
	}

	private static IgniteCache<Long, TopicX> processor(List<TopicX> collectors) {
		getInstance();
		CacheConfiguration<Long, TopicX> topicDataCacheCfg = new CacheConfiguration<Long, TopicX>();
		topicDataCacheCfg.setName(TopicCache.NAME);
		topicDataCacheCfg.setCacheMode(CacheMode.PARTITIONED);
		topicDataCacheCfg.setIndexedTypes(Long.class, TopicX.class);
		IgniteCache<Long, TopicX> topicDataCache = ignite.getOrCreateCache(topicDataCacheCfg);
		for (TopicX topic : collectors) {
			topicDataCache.put(topic.getOffsets(), topic);
		}
		return topicDataCache;
	}

	public static String sql(String sql, List<TopicX> collectors) {
		try {
			IgniteCache<Long, TopicX> topicDataCache = processor(collectors);
			SqlFieldsQuery qry = new SqlFieldsQuery(sql);
			QueryCursor<List<?>> cursor = topicDataCache.query(qry);
			for (List<?> row : cursor) {
//				List<TopicDomain> rps = (List<TopicDomain>) row;
//				for(TopicDomain td :rps){
//					System.out.println(td.toString());
//				}
				System.out.println(row.toString());
			}
		} catch (Exception ex) {
			LOG.error("Query kafka topic has error, msg is " + ex.getMessage());
		} finally {
			close();
		}
		return "";
	}

	private static void close() {
		try {
			if (ignite != null) {
				ignite.close();
			}
		} catch (Exception ex) {
			LOG.error("Close Ignite has error, msg is " + ex.getMessage());
		} finally {
			if (ignite != null) {
				ignite.close();
			}
		}
	}

}
