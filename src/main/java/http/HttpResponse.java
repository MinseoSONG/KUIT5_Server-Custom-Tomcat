package http;

import type.HttpHeader;
import type.HttpStatus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream outputStream) {
        this.dos = new DataOutputStream(outputStream);
    }

    public void forward(String path) throws IOException {
        byte[] body = Files.readAllBytes(Paths.get("./webapp" + path));
        String contentType = getContentType(path);

        addHeader(HttpHeader.CONTENT_TYPE.getName(), contentType + ";charset=utf-8");
        addHeader(HttpHeader.CONTENT_LENGTH.getName(), String.valueOf(body.length));

        writeStartLine(HttpStatus.OK);
        writeHeaders();
        writeBody(body);
    }

    public void redirect(String path) throws IOException {
        addHeader(HttpHeader.LOCATION.getName(), path);
        writeStartLine(HttpStatus.FOUND);
        writeHeaders();
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    private void writeStartLine(HttpStatus status) throws IOException {
        dos.writeBytes("HTTP/1.1 " + status.getStatus() + "\r\n");
    }

    private void writeHeaders() throws IOException {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");
    }

    private void writeBody(byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private String getContentType(String url) {
        if (url.endsWith(".css")) return "text/css";
        if (url.endsWith(".js")) return "application/javascript";
        if (url.endsWith(".png")) return "image/png";
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return "image/jpeg";
        if (url.endsWith(".svg")) return "image/svg+xml";
        return "text/html";
    }
}
