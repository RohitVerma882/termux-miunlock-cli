package dev.rohitverma882.miunlock.v2.fastboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.rohitverma882.miunlock.v2.process.FastbootRunner;
import dev.rohitverma882.miunlock.v2.process.ProcessRunner;

public class FastbootCommons {
    private static final int DEFAULT_TIMEOUT = Integer.MAX_VALUE;
    private static final HashMap<String, String> LAST_ERROR_MAP = new HashMap<>();

    public static String command_string(String cmd) {
        return command_string(cmd, null);
    }

    public static String command_string(String cmd, String device) {
        ProcessRunner runner = command_fast(cmd, device, DEFAULT_TIMEOUT);
        if (runner == null) {
            return "";
        }
        return runner.getOutputString();
    }

    public static List<String> command_list(String cmd) {
        return command_list(cmd, null);
    }

    public static List<String> command_list(String cmd, String device) {
        ProcessRunner runner = command_fast(cmd, device, DEFAULT_TIMEOUT);
        if (runner == null) {
            return new ArrayList<>();
        }
        return runner.getOutputLines();
    }

    public static FastbootRunner command_fast(String cmd, String device, int timeout) {
        FastbootRunner runner = new FastbootRunner();
        if (device != null) {
            runner.setDeviceSerial(device);
        }
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find()) {
            list.add(m.group(1));
        }
        for (String arg : list) {
            runner.addArgument(arg);
        }
        try {
            runner.runWait(timeout);
        } catch (IOException e) {
            return null;
        }
        return runner;
    }

    public static List<String> devices() {
        return command_list("devices");
    }

    public static String getUnlockToken(String device) {
        String token = getvar("token", device);
        if (token != null) {
            return token;
        } else {
            return oemGetToken(device);
        }
    }

    public static String getvar(String var, String device) {
        FastbootRunner runner = command_fast("getvar " + var, device, DEFAULT_TIMEOUT);
        if (runner == null) {
            return null;
        }
        if (runner.getExitValue() != 0) {
            return "";
        }
        return FastbootUtils.parseFastbootVar(var, runner.getOutputString());
    }

    public static String oemGetToken(String device) {
        FastbootRunner runner = command_fast("oem get_token", device, DEFAULT_TIMEOUT);
        if (runner == null) {
            return null;
        }
        if (runner.getExitValue() != 0) {
            return "";
        }
        return FastbootUtils.parseOemToken(runner.getOutputString());
    }

    public static boolean oemUnlock(String device, String token) {
        FastbootRunner runner = command_fast(device, 14, "oem-unlock", token);
        if (Objects.requireNonNull(runner).getExitValue() != 0) {
            return false;
        } else {
            String output = runner.getOutputString();
            if (output.contains("OKAY") && !output.contains("FAIL")) {
                return true;
            }
            return true;
        }
    }

    private static FastbootRunner command_fast(String device, int timeout, String... args) {
        FastbootRunner runner = new FastbootRunner();
        runner.setDeviceSerial(device);
        for (String arg : args) {
            runner.addArgument(arg);
        }
        try {
            runner.runWait(timeout);
        } catch (IOException e) {
            return null;
        }
        int exitCode = runner.getExitValue();
        if (exitCode == 0) {
            LAST_ERROR_MAP.put(device, null);
        } else {
            List<String> outlines = runner.getOutputLines();
            String output = outlines != null && !outlines.isEmpty() ? outlines.get(outlines.size() - 1) : "unknown error";
            LAST_ERROR_MAP.put(device, output);
        }
        return runner;
    }

    public static String getLastError(String serial) {
        return String.valueOf(LAST_ERROR_MAP.get(serial));
    }
}
