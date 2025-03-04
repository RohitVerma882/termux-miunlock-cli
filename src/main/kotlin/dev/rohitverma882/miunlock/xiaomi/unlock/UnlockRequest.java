package dev.rohitverma882.miunlock.xiaomi.unlock;

import static dev.rohitverma882.miunlock.Consts.SERVICE_NAME;
import static dev.rohitverma882.miunlock.Consts.getUNLOCK_HMAC_KEY;

import org.json.JSONObject;

import java.util.Base64;

import dev.rohitverma882.miunlock.inet.CustomHttpException;
import dev.rohitverma882.miunlock.inet.EasyHttp;
import dev.rohitverma882.miunlock.inet.EasyResponse;
import dev.rohitverma882.miunlock.inet.HttpQuery;
import dev.rohitverma882.miunlock.xiaomi.XiaomiCrypto;
import dev.rohitverma882.miunlock.xiaomi.XiaomiKeystore;
import dev.rohitverma882.miunlock.xiaomi.XiaomiProcedureException;

public class UnlockRequest {
    private HttpQuery params = new HttpQuery();

    private final String path;
    private final String host;

    private String signHmac, signSha;
    private final boolean encrypt;

    public UnlockRequest(String path, String host) {
        this(path, host, true);
    }

    public UnlockRequest(String path, String host, boolean encrypt) {
        this.path = path;
        this.host = host;
        this.encrypt = encrypt;
    }

    public String exec() throws XiaomiProcedureException, CustomHttpException {
        String[] keyToken = XiaomiKeystore.getInstance().requireServiceKeyAndToken(SERVICE_NAME);

        signHmac = XiaomiCrypto.cloudService_signHmac(getUNLOCK_HMAC_KEY(), "POST", path, params.sorted().toString());
        params.put("sign", signHmac);

        String key = keyToken[0];
        String serviceToken = keyToken[1];
        params = params.sorted();

        if (this.encrypt) {
            try {
                XiaomiCrypto.cloudService_encryptRequestParams(params, key);
            } catch (Exception e) {
                throw new XiaomiProcedureException("[UnlockRequest.exec] Cannot encrypt post params: " + e.getMessage());
            }
            signSha = XiaomiCrypto.cloudService_signSha1(key, "POST", path, params.toString());
            params.put("signature", signSha);
        }

        EasyResponse response = new EasyHttp().url(host + path).fields(params).userAgent("XiaomiPCSuite").cookies(XiaomiKeystore.getInstance().requireServiceCookies(SERVICE_NAME)).exec();
        if (!response.isAllRight()) {
            throw new XiaomiProcedureException("[UnlockRequest.exec] Invalid server respose: code: " + response.getCode() + ", lenght: " + response.getBody().length());
        }

        String body = response.getBody();
        if (encrypt) {
            try {
                body = XiaomiCrypto.cloudService_decrypt(body, key);
            } catch (Exception e) {
                throw new XiaomiProcedureException("[UnlockRequest.exec] Cannot decrypt response data: " + e.getMessage());
            }
            try {
                body = new String(Base64.getDecoder().decode(body));
            } catch (Throwable ignored) {
            }
        }
        return body;
    }

    public void addParam(String key, Object value) {
        params.put(key, value);
    }

    public void addNonce() throws XiaomiProcedureException, CustomHttpException {
        String json = UnlockCommonRequests.nonceV2(host);
        try {
            JSONObject obj = new JSONObject(json);
            int code = obj.getInt("code");
            if (code != 0) {
                throw new XiaomiProcedureException("[UnlockRequest.addNonce] Response code of nonce request is not zero: " + code);
            }
            String nonce = obj.getString("nonce");
            params.put("nonce", nonce);
        } catch (Exception e) {
            throw new XiaomiProcedureException("[UnlockRequest.addNonce] Exception while parsing nonce response: " + e.getMessage());
        }
    }
}
