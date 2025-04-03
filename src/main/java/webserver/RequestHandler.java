package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.util.HttpRequestUtils;
import model.User;
import type.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest request = HttpRequest.from(br);
            String url = request.getUrl();

            if (url.equals(UrlPath.USER_LIST.getPath())) {
                if (isLogined(request)) {
                    Path filePath = Paths.get("./webapp/user/list.html");
                    if (Files.exists(filePath)) {
                        byte[] body = Files.readAllBytes(filePath);
                        String contentType = getContentType(url);
                        response200Header(dos, body.length, contentType);
                        responseBody(dos, body);
                    } else {
                        response404Header(dos);
                    }
                } else {
                    response302Header(dos, UrlPath.LOGIN_PAGE.getPath());
                }
                return;
            }

            if (request.getMethod() == HttpMethod.POST && url.equals(UrlPath.LOGIN.getPath())) {
                handleLogin(request, dos);
                return;
            }

            if (request.getMethod() == HttpMethod.POST && url.equals(UrlPath.SIGNUP.getPath())) {
                handlePostSignUp(request, dos);
                return;
            }

            if (url.startsWith(UrlPath.SIGNUP.getPath())) {
                handleSignUp(request, dos);
                return;
            }

            if (url.equals(UrlPath.ROOT.getPath())) {
                url = UrlPath.INDEX.getPath();
            }

            Path filePath = Paths.get("./webapp" + url);

            if (Files.exists(filePath)) {
                byte[] body = Files.readAllBytes(filePath);
                String contentType = getContentType(url);
                response200Header(dos, body.length, contentType);
                responseBody(dos, body);
            } else {
                response404Header(dos);
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private String getContentType(String url) {
        if (url.endsWith(".css")) return "text/css";
        if (url.endsWith(".js")) return "application/javascript";
        if (url.endsWith(".png")) return "image/png";
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return "image/jpeg";
        if (url.endsWith(".svg")) return "image/svg+xml";
        return "text/html";
    }

    private boolean isLogined(HttpRequest request) {
        String cookieHeader = request.getHeader(HttpHeader.COOKIE.getName());
        if (cookieHeader == null) return false;

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] pair = cookie.trim().split("=");
            if (pair.length == 2 && pair[0].equals("logined") && pair[1].equals("true")) {
                return true;
            }
        }
        return false;
    }

    private void handleLogin(HttpRequest request, DataOutputStream dos) throws IOException {
        Map<String, String> params = request.getBodyParams();
        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());

        User user = MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response302WithLoginCookie(dos);
        } else {
            response302Header(dos, UrlPath.LOGIN_FAILED.getPath());
        }
    }

    private void handlePostSignUp(HttpRequest request, DataOutputStream dos) throws IOException {
        Map<String, String> params = request.getBodyParams();

        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());
        String name = params.get(UserQueryKey.NAME.getKey());
        String email = params.get(UserQueryKey.EMAIL.getKey());

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response302Header(dos, UrlPath.INDEX.getPath());
    }


    private void handleSignUp(HttpRequest request, DataOutputStream dos) throws IOException {
        String url = request.getUrl();
        int queryIndex = url.indexOf("?");
        if (queryIndex == -1) return;

        String queryString = url.substring(queryIndex + 1);
        Map<String, String> params = HttpRequestUtils.parseQueryParameter(queryString);

        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());
        String name = params.get(UserQueryKey.NAME.getKey());
        String email = params.get(UserQueryKey.EMAIL.getKey());

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response302Header(dos, UrlPath.INDEX.getPath());
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 " + HttpStatus.FOUND.getStatus() + "\r\n");
            dos.writeBytes(HttpHeader.LOCATION.getName() + ": " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 " + HttpStatus.NOT_FOUND.getStatus() + "\r\n");
            dos.writeBytes(HttpHeader.CONTENT_TYPE.getName() + ": text/html;charset=utf-8\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 " + HttpStatus.OK.getStatus() + "\r\n");
            dos.writeBytes(HttpHeader.CONTENT_TYPE.getName() + ": " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes(HttpHeader.CONTENT_LENGTH.getName() + ": " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private void response302WithLoginCookie(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 " + HttpStatus.FOUND.getStatus() + "\r\n");
            dos.writeBytes(HttpHeader.LOCATION.getName() + ": " + UrlPath.INDEX.getPath() + "\r\n");
            dos.writeBytes(HttpHeader.SET_COOKIE.getName() + ": logined=true\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
