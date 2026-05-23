package com.kmedical.http;

import java.util.*;
import java.util.regex.*;

/** 외부 라이브러리 없이 동작하는 최소 JSON 직렬화/역직렬화 유틸. */
public final class JsonUtil {

    private JsonUtil() {}

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            appendValue(sb, e.getValue());
        }
        return sb.append("}").toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            sb.append("\"").append(escape((String) v)).append("\"");
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof List) {
            sb.append("[");
            List<?> list = (List<?>) v;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                appendValue(sb, list.get(i));
            }
            sb.append("]");
        } else if (v instanceof Map) {
            sb.append(toJson((Map<String, Object>) v));
        } else {
            sb.append("\"").append(escape(v.toString())).append("\"");
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    /** 평탄한 JSON 객체를 Map<String, String>으로 파싱 (중첩 객체/배열 미지원). */
    public static Map<String, String> parse(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return result;
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+(?:\\.\\d+)?|true|false|null))");
        Matcher m = p.matcher(json);
        while (m.find()) {
            result.put(m.group(1), m.group(2) != null ? m.group(2) : m.group(3));
        }
        return result;
    }
}
