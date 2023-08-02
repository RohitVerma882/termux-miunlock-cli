package dev.rohitverma882.miunlock.v2.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import dev.rohitverma882.miunlock.v2.inet.CustomHttpException;
import dev.rohitverma882.miunlock.v2.inet.EasyHttp;
import dev.rohitverma882.miunlock.v2.inet.EasyResponse;

public class InetUtils {
    public static String urlEncode(String data) {
        return URLEncoder.encode(data, StandardCharsets.UTF_8);
    }

    public static String getRedirectUrl(String url) throws CustomHttpException {
        return getRedirectUrl(url, null);
    }

    public static String getRedirectUrl(String url, String referer) throws CustomHttpException {
        EasyHttp request = new EasyHttp().url(url).setHeadOnly();
        if (referer != null) {
            request = request.referer(referer);
        }
        EasyResponse response = request.exec();
        List<String> list = response.getHeaders().get("location");
        if (list == null) {
            return null;
        }
        return list.get(0);
    }
}
