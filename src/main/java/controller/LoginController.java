package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import type.HttpHeader;
import type.UserQueryKey;

import java.util.Map;

public class LoginController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, String> params = request.getBodyParams();
        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());

        User user = MemoryUserRepository.getInstance().findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response.addHeader(HttpHeader.SET_COOKIE.getName(), "logined=true");
            response.redirect("/index.html");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }
}
