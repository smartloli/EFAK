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
package org.smartloli.kafka.eagle.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Use link hash map to store data, and remove special data.
 *
 * @author smartloli.
 *
 *         Created by Mar 24, 2016
 */
public class LRUCacheUtils<K, V> {
	private final int MAX_CACHE_SIZE;
	private final float DEFAULT_LOAD_FACTOR = 0.75f;
	LinkedHashMap<K, V> map;

	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	public LRUCacheUtils(int cacheSize) {
		MAX_CACHE_SIZE = cacheSize;
		// CacheSize and loading factor calculated by capactiy HashMap, +1 to
		// ensure that when the cacheSize does not trigger the HashMap limit
		// will trigger the expansion
		int capacity = (int) Math.ceil(MAX_CACHE_SIZE / DEFAULT_LOAD_FACTOR) + 1;
		map = new LinkedHashMap(capacity, DEFAULT_LOAD_FACTOR, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};
	}

	public synchronized void put(K key, V value) {
		map.put(key, value);
	}

	public synchronized V get(K key) {
		return map.get(key);
	}

	public synchronized void remove(K key) {
		map.remove(key);
	}

	public synchronized Set<Map.Entry<K, V>> getAll() {
		return map.entrySet();
	}

	public synchronized int size() {
		return map.size();
	}

	public synchronized void clear() {
		map.clear();
	}

	public synchronized boolean containsKey(K key) {
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
