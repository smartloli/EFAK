package org.kafka.eagle.web.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolCallArgumentsTest {

    @Test
    public void normalizeConcatenatedClusterIdObjects() {
        String stutter = "{\"cluster_id\": \"5uTuNNn5dKvh0ZvF\"}"
                + "{\"cluster_id\": \"5uTuNNn5dKvh0ZvF\"}"
                + "{\"cluster_id\": \"5uTuNNn5dKvh0ZvF\"}"
                + "{\"cluster_id\": \"5uTuNNn5dKvh0ZvF\"}";
        Map<String, Object> parsed = ToolCallArguments.parseMap(stutter);
        assertEquals("5uTuNNn5dKvh0ZvF", parsed.get("cluster_id"));
        assertEquals(1, parsed.size());
    }

    @Test
    public void accumulateStutteredCompleteObjects() {
        StringBuilder buffer = new StringBuilder();
        String once = "{\"cluster_id\":\"5uTuNNn5dKvh0ZvF\"}";
        ToolCallArguments.accumulate(buffer, once);
        ToolCallArguments.accumulate(buffer, once);
        ToolCallArguments.accumulate(buffer, once + once);
        Map<String, Object> parsed = ToolCallArguments.parseMap(buffer.toString());
        assertEquals("5uTuNNn5dKvh0ZvF", parsed.get("cluster_id"));
    }

    @Test
    public void accumulateFragmentsThenComplete() {
        StringBuilder buffer = new StringBuilder();
        ToolCallArguments.accumulate(buffer, "{\"cluster_id\":");
        ToolCallArguments.accumulate(buffer, "\"abc\"}");
        Map<String, Object> parsed = ToolCallArguments.parseMap(buffer.toString());
        assertEquals("abc", parsed.get("cluster_id"));
    }

    @Test
    public void accumulateGrowingObjectReplaces() {
        StringBuilder buffer = new StringBuilder();
        ToolCallArguments.accumulate(buffer, "{\"cluster_id\":\"c1\"}");
        ToolCallArguments.accumulate(buffer, "{\"cluster_id\":\"c1\",\"topic\":\"orders\"}");
        Map<String, Object> parsed = ToolCallArguments.parseMap(buffer.toString());
        assertEquals("c1", parsed.get("cluster_id"));
        assertEquals("orders", parsed.get("topic"));
    }

    @Test
    public void stringifyIdHandlesNumbers() {
        assertEquals("12", ToolCallArguments.stringifyId(12));
        assertTrue(ToolCallArguments.stringifyId(null).isEmpty());
    }
}
