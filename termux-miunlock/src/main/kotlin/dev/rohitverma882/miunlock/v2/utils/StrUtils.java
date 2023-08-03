package dev.rohitverma882.miunlock.v2.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class StrUtils {
    static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String randomWord(int len) {
        int clen = CHARS.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            builder.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(0, clen)));
        }
        return builder.toString();
    }

    public static String map2json(Map<?, ?> map, int indent) {
        return map2json(map, indent, indent);
    }

    private static String map2json(Map<?, ?> map, int indent, int sindent) {
        if (!(map instanceof LinkedHashMap)) {
            return new JSONObject(map).toString(indent);
        }
        final String NL = indent > 0 ? "\n" : "";
        String in = indentToString(indent);
        final String IN = in;
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Map.Entry entry : map.entrySet()) {
            String toAdd;
            if (entry.getValue() instanceof Number) {
                toAdd = String.valueOf(entry.getValue());
            } else if (entry.getValue() instanceof String) {
                toAdd = '"' + entry.getValue().toString().replace("\"", "\\\"") + '"';
            } else if (entry.getValue() instanceof Map) {
                toAdd = map2json((Map<?, ?>) entry.getValue(), indent + sindent, sindent);
            } else {
                throw new JSONException("Unknown type: " + entry.getValue().getClass().getSimpleName());
            }
            stringBuilder.append(NL).append(IN).append('"').append(entry.getKey().toString()).append("\" : ").append(toAdd).append(",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1) + NL + indentToString(indent - sindent) + "}" + (indent == sindent ? NL : "");
    }

    private static String indentToString(int indent) {
        if (indent <= 0) {
            return "";
        }
        char[] ic = new char[indent];
        for (int i = 0; i < indent; ++i) {
            ic[i] = ' ';
        }
        return new String(ic);
    }
    
    public static String findJsonStart(String data) {
        char[] d = data.toCharArray();
        for (int i = 0; i < d.length; ++i) {
            if (d[i] == '{') {
                return data.substring(i);
            }
        }
        return null;
    }
}
