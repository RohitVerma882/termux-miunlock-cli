package dev.rohitverma882.miunlock.inet;

import java.util.LinkedHashMap;
import java.util.Map;

public class EasyHttp {
    private final Map<String, String> cookies = new LinkedHashMap<>();
    protected CustomHttpRequest request = new CustomHttpRequest();

    private boolean headOnly = false;

    public static EasyResponse get(String url) throws CustomHttpException {
        return (new EasyHttp()).url(url).exec();
    }

    public EasyHttp url(String url) {
        request.setUrl(url);
        return this;
    }

    public EasyHttp setHeadOnly() {
        headOnly = true;
        return this;
    }

    public EasyHttp header(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    public EasyHttp headers(Map<String, String> headers) {
        request.addHeaders(headers);
        return this;
    }

    public EasyHttp field(String key, String value) {
        request.addPostField(key, value);
        return this;
    }

    public EasyHttp fields(Map<String, ?> fields) {
        request.addPostFields(fields);
        return this;
    }

    public EasyHttp proxy(String host, int port) {
        request.setProxy(host, port);
        return this;
    }

    public EasyHttp cookie(String key, String value) {
        this.cookies.put(key, value);
        return this;
    }

    public EasyHttp cookies(Map<String, String> cookies) {
        this.cookies.putAll(cookies);
        return this;
    }

    public CustomHttpRequest getHttpRequestObj() {
        return request;
    }

    public EasyHttp referer(String referer) {
        return this.header("Referer", referer);
    }

    public EasyResponse exec() throws CustomHttpException {
        if (cookies.size() > 0) {
            StringBuilder cookieString = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                cookieString.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            request.addHeader("Cookie", cookieString.toString());
        }
        if (headOnly) {
            request.setHeadRequest();
        }
        request.execute();
        return new EasyResponse(request.getResponseHeaders(), request.getResponseBody(), request.getResponseCode());
    }

    public EasyHttp userAgent(String userAgent) {
        return this.header("User-Agent", userAgent);
    }
}
