package controller;

import http.HttpRequest;
import http.HttpResponse;
import type.HttpHeader;

public class ListController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        String cookieHeader = request.getHeader(HttpHeader.COOKIE.getName());
        boolean isLoggedIn = false;

        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=");
                if (pair.length == 2 && pair[0].equals("logined") && pair[1].equals("true")) {
                    isLoggedIn = true;
                    break;
                }
            }
        }

        if (isLoggedIn) {
            response.forward("/user/list.html");
        } else {
            response.redirect("/user/login.html");
        }
    }
}
