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
package org.smartloli.kafka.eagle.common.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Use link hash map to store data, and remove special data.
 *
 * @author smartloli.
 *
 *         Aug Mar 24, 2016
 */
public class LRUCacheUtils<K, V> extends AbstractMap<K, V> implements Serializable {
	/**
	 * Version ID.
	 */
	private static final long serialVersionUID = 1L;
	private final int MAX_CACHE_SIZE;
	private final float DEFAULT_LOAD_FACTOR = 0.75f;
	LinkedHashMap<K, V> map;

	/**
	 * Calculate the capacity of the hashmap by using the cacheSize and load
	 * factor. + 1 ensures that the hashmap is not triggered when the cacheSize
	 * is reached.
	 * 
	 * @param cacheSize
	 */
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	public LRUCacheUtils(int cacheSize) {
		MAX_CACHE_SIZE = cacheSize;
		int capacity = (int) Math.ceil(MAX_CACHE_SIZE / DEFAULT_LOAD_FACTOR) + 1;
		map = new LinkedHashMap(capacity, DEFAULT_LOAD_FACTOR, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};
	}

	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	public LRUCacheUtils() {
		MAX_CACHE_SIZE = 100000;
		int capacity = (int) Math.ceil(MAX_CACHE_SIZE / DEFAULT_LOAD_FACTOR) + 1;
		map = new LinkedHashMap(capacity, DEFAULT_LOAD_FACTOR, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};
	}

	public synchronized V put(K key, V value) {
		return map.put(key, value);
	}

	public synchronized V get(Object key) {
		return map.get(key);
	}

	public synchronized V remove(Object key) {
		return map.remove(key);
	}

	public synchronized Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	public synchronized int size() {
		return map.size();
	}

	public synchronized void clear() {
		map.clear();
	}

	public synchronized boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry entry : map.entrySet()) {
			sb.append(String.format("%s:%s ", entry.getKey(), entry.getValue()));
		}
		return sb.toString();
	}
}
