package org.kafka.eagle.web.scheduler;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Ketama-style hash ring. Adding or removing one node remaps about 1/N of keys
 * instead of reshuffling the whole set the way {@code i % N} does.
 */
final class ConsistentHashRing {

    private static final int VIRTUAL_NODES = 64;

    private ConsistentHashRing() {
    }

    static String owner(Object item, List<String> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        NavigableMap<Integer, String> ring = build(nodes);
        if (ring.isEmpty()) {
            return nodes.get(0);
        }
        int hash = mix(String.valueOf(item));
        var entry = ring.ceilingEntry(hash);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    private static NavigableMap<Integer, String> build(List<String> nodes) {
        NavigableMap<Integer, String> ring = new TreeMap<>();
        for (String node : nodes) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                ring.put(mix(node + "#" + i), node);
            }
        }
        return ring;
    }

    private static int mix(String value) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            h ^= value.charAt(i);
            h *= 0x100000001b3L;
        }
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        return (int) h;
    }
}
