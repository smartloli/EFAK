package com.webank.cms.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Date Mar 24, 2016
 *
 * @Author smartloli
 *
 * @Note Use link hash map to store data, and remove special data.
 */
public class LRUCacheUtils<K, V> {
	private final int MAX_CACHE_SIZE;
	private final float DEFAULT_LOAD_FACTOR = 0.75f;
	LinkedHashMap<K, V> map;

	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	public LRUCacheUtils(int cacheSize) {
		MAX_CACHE_SIZE = cacheSize;
		// 通过cacheSize和加载因子计算hashmap的capactiy，+1确保当达到cacheSize上限时不会触发hashmap的扩容，
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
