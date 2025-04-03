package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import model.User;
import type.*;

import java.io.*;
import java.net.Socket;
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
            HttpRequest request = HttpRequest.from(br);
            HttpResponse response = new HttpResponse(out);

            String url = request.getUrl();

            if (url.equals(UrlPath.USER_LIST.getPath())) {
                if (isLogined(request)) {
                    response.forward("/user/list.html");
                } else {
                    response.redirect(UrlPath.LOGIN_PAGE.getPath());
                }
                return;
            }

            if (request.getMethod() == HttpMethod.POST && url.equals(UrlPath.LOGIN.getPath())) {
                handleLogin(request, response);
                return;
            }

            if (request.getMethod() == HttpMethod.POST && url.equals(UrlPath.SIGNUP.getPath())) {
                handlePostSignUp(request, response);
                return;
            }

            if (url.startsWith(UrlPath.SIGNUP.getPath())) {
                handleSignUp(request, response);
                return;
            }

            if (url.equals(UrlPath.ROOT.getPath())) {
                url = UrlPath.INDEX.getPath();
            }

            response.forward(url);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
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

    private void handleLogin(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getBodyParams();
        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());

        User user = MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.addHeader(HttpHeader.SET_COOKIE.getName(), "logined=true");
            response.redirect(UrlPath.INDEX.getPath());
        } else {
            response.redirect(UrlPath.LOGIN_FAILED.getPath());
        }
    }

    private void handlePostSignUp(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getBodyParams();

        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());
        String name = params.get(UserQueryKey.NAME.getKey());
        String email = params.get(UserQueryKey.EMAIL.getKey());

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response.redirect(UrlPath.INDEX.getPath());
    }


    private void handleSignUp(HttpRequest request, HttpResponse response) throws IOException {
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

        response.redirect(UrlPath.INDEX.getPath());
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
