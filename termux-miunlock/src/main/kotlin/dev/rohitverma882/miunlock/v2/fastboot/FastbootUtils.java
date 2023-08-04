package dev.rohitverma882.miunlock.v2.fastboot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastbootUtils {
    public static String parseFastbootVar(String var, String output) {
        if (output == null || var == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\s*" + var + "\\s*:\\s*([^\\n]+)");
        Matcher m = pattern.matcher(output);
        if (!m.find()) {
            return null;
        }
        return m.group(1).trim();
    }
}
