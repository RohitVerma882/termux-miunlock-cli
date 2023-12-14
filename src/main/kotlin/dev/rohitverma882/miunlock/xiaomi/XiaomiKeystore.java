package dev.rohitverma882.miunlock.xiaomi;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.UUID;

import dev.rohitverma882.miunlock.inet.CustomHttpException;

public class XiaomiKeystore {
    private final HashMap<String, XiaomiServiceEntry> serviceMap = new HashMap<>();

    private String userId;
    private String passToken;
    private String deviceId;
    private String pcId;

    private static XiaomiKeystore instance;
    public static XiaomiKeystore getInstance() {
        if (instance == null) {
            instance = new XiaomiKeystore();
        }
        return instance;
    }

    public static void clear() {
        instance = new XiaomiKeystore();
    }

    public String getUserId() {
        return userId;
    }

    public String getPassToken() {
        return passToken;
    }

    public String getDeviceId() {
        if (this.deviceId == null) {
            this.deviceId = generateDeviceId();
        }
        return this.deviceId;
    }

    public static String generateDeviceId() {
        return "wb_" + UUID.randomUUID();
    }

    public String getPcId() {
        if (pcId == null) {
            pcId = DigestUtils.md5Hex(getDeviceId());
        }
        return pcId;
    }

    public void setDeviceId(String pcId) {
        this.pcId = pcId;
    }

    public void setCredentials(String userId, String passToken, String deviceId) {
        this.userId = userId;
        this.passToken = passToken;
        this.deviceId = deviceId;
    }

    public void setCredentials(String userId, String passToken) {
        setCredentials(userId, passToken, generateDeviceId());
    }

    public boolean isLogged() {
        return this.getUserId() != null && this.getPassToken() != null;
    }

    public String[] requireServiceKeyAndToken(String sid) throws XiaomiProcedureException, CustomHttpException {
        XiaomiServiceEntry entry = serviceMap.get(sid);
        if (entry == null) {
            entry = new XiaomiServiceEntry(sid, this);
            serviceMap.put(sid, entry);
        }
        return entry.requireSSandST();
    }

    public HashMap<String, String> requireServiceCookies(String sid) throws XiaomiProcedureException, CustomHttpException {
        XiaomiServiceEntry entry = serviceMap.get(sid);
        if (entry == null) {
            entry = new XiaomiServiceEntry(sid, this);
            entry.requireSSandST();
            serviceMap.put(sid, entry);
        }
        return entry.getCookies();
    }
}
