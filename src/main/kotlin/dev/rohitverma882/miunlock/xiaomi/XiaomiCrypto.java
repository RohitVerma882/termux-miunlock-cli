package dev.rohitverma882.miunlock.xiaomi;

import static dev.rohitverma882.miunlock.Consts.DEFAULT_IV;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import dev.rohitverma882.miunlock.crypto.AES;
import dev.rohitverma882.miunlock.crypto.Hash;
import dev.rohitverma882.miunlock.inet.HttpQuery;

public class XiaomiCrypto {
    public static String cloudService_encrypt(String data, String key) throws Exception {
        byte[] bkey = Base64.decodeBase64(key);
        return Base64.encodeBase64String(AES.aes128cbc_encrypt(bkey, DEFAULT_IV.getBytes(StandardCharsets.ISO_8859_1), data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String cloudService_decrypt(String data, String key) throws Exception {
        byte[] bkey = Base64.decodeBase64(key);
        byte[] bdata = Base64.decodeBase64(data);
        return new String(AES.aes128cbc_decrypt(bkey, DEFAULT_IV.getBytes(StandardCharsets.ISO_8859_1), bdata), StandardCharsets.UTF_8);
    }

    public static void cloudService_encryptRequestParams(HttpQuery params, String key) throws Exception {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            e.setValue(cloudService_encrypt(e.getValue().toString(), key));
        }
    }

    public static String cloudService_signHmac(byte[] hmacKey, String method, String path, String query) {
        String hmacData = method + "\n" + path + "\n" + query;
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_1, hmacKey).hmacHex(hmacData.getBytes(StandardCharsets.UTF_8));
    }

    public static String cloudService_signSha1(String key, String method, String path, String query) {
        String shaData = method + "&" + path + "&" + query + "&" + key;
        return Hash.sha1Base64(shaData);
    }
}