package controller;

import http.HttpRequest;
import http.HttpResponse;

public class ForwardController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        String url = request.getUrl();

        if (url.equals("/")) {
            url = "/index.html";
        }

        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            url = url.substring(0, queryIndex);
        }

        response.forward(url);
    }
}
