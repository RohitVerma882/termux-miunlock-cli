package dev.rohitverma882.miunlock.utility.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import dev.rohitverma882.miunlock.inet.CustomHttpException;
import dev.rohitverma882.miunlock.inet.EasyHttp;
import dev.rohitverma882.miunlock.inet.EasyResponse;

public class InetUtils {
    public static String urlEncode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
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
