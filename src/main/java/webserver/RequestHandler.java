package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import model.User;

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

            String line = br.readLine(); // ex) GET /index.html HTTP/1.1
            if (line == null) return;

            String[] tokens = line.split(" ");
            String method = tokens[0];
            String url = tokens[1];

            if (method.equals("POST") && url.equals("/user/login")){
                handleLogin(br, dos);
                return;
            }

            if (method.equals("POST") && url.equals("/user/signup")) {
                handlePostSignUp(br, dos);
                return;
            }

            if (url.startsWith("/user/signup")) {
                handleSignUp(url, dos);
                return;
            }

            if (url.equals("/")) {
                url = "/index.html";
            }

            Path filePath = Paths.get("./webapp" + url);

            if (Files.exists(filePath)) {
                byte[] body = Files.readAllBytes(filePath);
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                response404Header(dos);
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void handleLogin(BufferedReader br, DataOutputStream dos) throws IOException {
        int requestContentLength = 0;

        while (true) {
            final String line = br.readLine();
            if (line.equals("")) {
                break;
            }

            if (line.startsWith("Content-Length")) {
                requestContentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }

        String body = http.util.IOUtils.readData(br, requestContentLength);
        Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

        String userId = params.get("userId");
        String password = params.get("password");

        User user = MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response302WithLoginCookie(dos, "/index.html");
        } else {
            response302Header(dos, "/user/login_failed.html");
        }
    }

    private void handlePostSignUp(BufferedReader br, DataOutputStream dos) throws IOException {
        int requestContentLength = 0;

        while (true) {
            final String line = br.readLine();
            if (line.equals("")) {
                break;
            }

            if (line.startsWith("Content-Length")) {
                requestContentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }

        String body = http.util.IOUtils.readData(br, requestContentLength);

        Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

        String userId = params.get("userId");
        String password = params.get("password");
        String name = params.get("name");
        String email = params.get("email");

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response302Header(dos, "/index.html");
    }


    private void handleSignUp(String url, DataOutputStream dos) throws IOException {
        int queryIndex = url.indexOf("?");
        if (queryIndex == -1) {
            return;
        }

        String queryString = url.substring(queryIndex + 1);
        Map<String, String> params = HttpRequestUtils.parseQueryParameter(queryString);

        String userId = params.get("userId");
        String password = params.get("password");
        String name = params.get("name");
        String email = params.get("email");

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response302Header(dos, "/index.html");
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302WithLoginCookie(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }


}
