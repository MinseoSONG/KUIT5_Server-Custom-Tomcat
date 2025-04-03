package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import type.HttpMethod;
import type.UserQueryKey;

import java.util.Map;

public class SignUpController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, String> params = request.getMethod() == HttpMethod.GET? request.getQueryParams(): request.getBodyParams();

        String userId = params.get(UserQueryKey.USER_ID.getKey());
        String password = params.get(UserQueryKey.PASSWORD.getKey());
        String name = params.get(UserQueryKey.NAME.getKey());
        String email = params.get(UserQueryKey.EMAIL.getKey());

        User user = new User(userId, password, name, email);
        MemoryUserRepository.getInstance().addUser(user);

        response.redirect("/index.html");
    }
}
