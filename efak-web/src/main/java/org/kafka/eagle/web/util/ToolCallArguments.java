package org.kafka.eagle.web.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalize streamed tool-call arguments. OpenAI-compatible models often
 * stutter a complete JSON object several times, producing
 * {@code {...}{...}{...}} which Fastjson rejects with "input not end".
 */
public final class ToolCallArguments {

    private ToolCallArguments() {
    }

    public static void accumulate(StringBuilder buffer, String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        if (buffer.length() == 0) {
            buffer.append(chunk);
            compactBuffer(buffer);
            return;
        }
        String current = buffer.toString();
        if (chunk.startsWith(current)) {
            buffer.setLength(0);
            buffer.append(chunk);
            compactBuffer(buffer);
            return;
        }
        if (current.startsWith(chunk)) {
            return;
        }
        String normalizedChunk = normalize(chunk);
        if (isObject(normalizedChunk) && !isObject(current)) {
            buffer.setLength(0);
            buffer.append(normalizedChunk);
            return;
        }
        if (isObject(normalizedChunk) && isObject(current)) {
            buffer.setLength(0);
            buffer.append(mergeJson(current, normalizedChunk));
            return;
        }
        buffer.append(chunk);
        compactBuffer(buffer);
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "{}";
        }
        String text = raw.trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return "{}";
        }
        if (isObject(text)) {
            return compact(text);
        }
        List<String> objects = splitObjects(text);
        if (objects.isEmpty()) {
            return text;
        }
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String object : objects) {
            Map<String, Object> parsed = parseMapOrNull(object);
            if (parsed != null) {
                merged.putAll(parsed);
            }
        }
        return merged.isEmpty() ? objects.get(objects.size() - 1) : JSON.toJSONString(merged);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String raw) {
        Map<String, Object> parsed = parseMapOrNull(normalize(raw));
        return parsed != null ? parsed : new LinkedHashMap<>();
    }

    public static String stringifyId(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static void compactBuffer(StringBuilder buffer) {
        String normalized = normalize(buffer.toString());
        if (!normalized.equals(buffer.toString()) && isObject(normalized)) {
            buffer.setLength(0);
            buffer.append(normalized);
        }
    }

    private static String mergeJson(String left, String right) {
        Map<String, Object> merged = new LinkedHashMap<>();
        Map<String, Object> leftMap = parseMapOrNull(left);
        Map<String, Object> rightMap = parseMapOrNull(right);
        if (leftMap != null) {
            merged.putAll(leftMap);
        }
        if (rightMap != null) {
            merged.putAll(rightMap);
        }
        return merged.isEmpty() ? right : JSON.toJSONString(merged);
    }

    private static String compact(String json) {
        Map<String, Object> parsed = parseMapOrNull(json);
        return parsed == null ? json : JSON.toJSONString(parsed);
    }

    private static boolean isObject(String text) {
        return parseMapOrNull(text) != null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseMapOrNull(String text) {
        if (text == null) {
            return null;
        }
        try {
            JSONObject object = JSON.parseObject(text);
            if (object == null) {
                return null;
            }
            return new LinkedHashMap<>(object);
        } catch (Exception e) {
            return null;
        }
    }

    static List<String> splitObjects(String text) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(text.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }
}
