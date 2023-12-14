package dev.rohitverma882.miunlock.xiaomi.unlock;

import static dev.rohitverma882.miunlock.Consts.AHAUNLOCKV3;
import static dev.rohitverma882.miunlock.Consts.CLIENT_VERSION;
import static dev.rohitverma882.miunlock.Consts.DEVICECLEARV3;
import static dev.rohitverma882.miunlock.Consts.NONCEV2;
import static dev.rohitverma882.miunlock.Consts.SID;
import static dev.rohitverma882.miunlock.Consts.USERINFOV3;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;

import dev.rohitverma882.miunlock.inet.CustomHttpException;
import dev.rohitverma882.miunlock.utility.utils.StrUtils;
import dev.rohitverma882.miunlock.xiaomi.XiaomiKeystore;
import dev.rohitverma882.miunlock.xiaomi.XiaomiProcedureException;

public class UnlockCommonRequests {
    private static final HashMap<Integer, String> UNLOCK_CODE_MEANING = buildUnlockCodeMeaning();

    public static String getUnlockCodeMeaning(int code, JSONObject object) {
        String code_meaning = UNLOCK_CODE_MEANING.get(code);
        if (code_meaning == null) {
            return String.format("Unknown error: %1$d", code);
        }
        String toReturn;
        if (code == 20036) {
            int hours = -1;
            {
                try {
                    hours = object.getJSONObject("data").getInt("waitHour");
                } catch (Throwable ignored) {
                }
                if (hours >= 0) {
                    int days = hours / 24;
                    int leftHours = hours % 24;
                    toReturn = String.format(code_meaning, days, leftHours);
                } else {
                    toReturn = ("You have to wait some days before you can unlock your device");
                }
            }
        } else {
            toReturn = code_meaning;
        }
        return toReturn;
    }

    private static HashMap<Integer, String> buildUnlockCodeMeaning() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(-1, "Unknown error: %1$d");
        map.put(10000, "10000:Request parameter error, this can be caused by entering invalid token or product");
        map.put(10001, "10001:Signature verification failed");
        map.put(10002, "10002:The same IP request too often (Too many tries)");
        map.put(10003, "10003:Internal server error");
        map.put(10004, "10004:Request has expired");
        map.put(10005, "10005:Invalid Nonce request");
        map.put(10006, "10006:Client version is too low");
        map.put(20030, "You have already unlocked a device recently\nYou should wait at least 30 days from the first unlock to unlock another device");
        map.put(20031, "This device is not bound to your account\nTurn on your device and bind your account to the device by going in MIUI's Settings > Developer options > Mi Unlock status");
        map.put(20032, "Failed to generate signature value required to unlock");
        map.put(20033, "User portrait scores too low or black");
        map.put(20034, "Current account cannot unlock this device");
        map.put(20035, "This tool is outdated, contact the developers.");
        map.put(20036, "Your account has been bound to this device for not enough time\nYou have to wait %1$d days and %2$d hours before you can unlock this device");
        map.put(20037, "Unlock number has reached the upper limit");
        map.put(20041, "Your Xiaomi account isn't associated with a phone number\nGo to account.xiaomi.com and associate it with your phone number");
        return map;
    }

    public static String nonceV2(String host) throws XiaomiProcedureException, CustomHttpException {
        UnlockRequest request = new UnlockRequest(NONCEV2, host);
        request.addParam("r", new String(StrUtils.randomWord(16).toLowerCase().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String userInfo(String host) throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();

        UnlockRequest request = new UnlockRequest(USERINFOV3, host);
        HashMap<String, String> pp = new LinkedHashMap<>();
        pp.put("clientId", "1");
        pp.put("clientVersion", CLIENT_VERSION);
        pp.put("language", "en");
        pp.put("pcId", keystore.getPcId());
        pp.put("region", "");
        pp.put("uid", keystore.getUserId());
        String data = new JSONObject(pp).toString(3);
        data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String deviceClear(String host, String product) throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();

        UnlockRequest request = new UnlockRequest(DEVICECLEARV3, host);
        HashMap<String, String> pp = new LinkedHashMap<>();
        pp.put("clientId", "1");
        pp.put("clientVersion", CLIENT_VERSION);
        pp.put("language", "en");
        pp.put("pcId", keystore.getPcId());
        pp.put("product", product);
        pp.put("region", "");
        String data = new JSONObject(pp).toString(3);
        data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        request.addParam("appId", "1");
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String ahaUnlock(String host, String token, String product, String boardVersion, String deviceName, String socId) throws XiaomiProcedureException, CustomHttpException {
        if (product == null || product.isEmpty()) {
            throw new XiaomiProcedureException("Invalid input argument: null product");
        }
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();

        UnlockRequest request = new UnlockRequest(AHAUNLOCKV3, host);
        HashMap<String, String> p2 = new LinkedHashMap<>();
        p2.put("boardVersion", boardVersion);
        p2.put("deviceName", deviceName);
        p2.put("product", product);
        p2.put("socId", socId);
        HashMap<String, Object> pp = new LinkedHashMap<>();
        pp.put("clientId", "2");
        pp.put("clientVersion", CLIENT_VERSION);
        pp.put("deviceInfo", p2);
        pp.put("deviceToken", token);
        pp.put("language", "en");
        pp.put("operate", "unlock");
        pp.put("pcId", keystore.getPcId());
        pp.put("region", "");
        pp.put("uid", keystore.getUserId());
        String data = StrUtils.map2json(pp, 3);
        data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        request.addParam("appId", "1");
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }
}
