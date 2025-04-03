package controller;

import http.HttpRequest;
import http.HttpResponse;

public interface Controller {
    void execute(HttpRequest request, HttpResponse response) throws Exception;
}
