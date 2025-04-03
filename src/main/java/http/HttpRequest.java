package http;

import http.util.HttpRequestUtils;
import type.HttpHeader;
import type.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String url;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequest(HttpMethod method, String url, String version,
                        Map<String, String> headers, String body) {
        this.method = method;
        this.url = url;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        String startLine = br.readLine();
        if (startLine == null || startLine.isEmpty()) throw new IllegalArgumentException("Invalid start line");

        String[] tokens = startLine.split(" ");
        HttpMethod method = HttpMethod.from(tokens[0]);
        String url = tokens[1];
        String version = tokens[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            int index = line.indexOf(":");
            if (index != -1) {
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                headers.put(key, value);
            }
        }

        int contentLength = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH.getName(), "0"));
        String body = contentLength > 0 ? http.util.IOUtils.readData(br, contentLength) : "";

        return new HttpRequest(method, url, version, headers, body);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getBodyParams() {
        return HttpRequestUtils.parseQueryParameter(body);
    }

    public Map<String, String> getQueryParams() {
        int queryIndex = url.indexOf("?");
        if (queryIndex == -1) return new HashMap<>();
        String queryString = url.substring(queryIndex + 1);
        return HttpRequestUtils.parseQueryParameter(queryString);
    }
}
