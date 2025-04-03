package webserver;

import controller.Controller;
import controller.ControllerRouter;
import http.HttpRequest;
import http.HttpResponse;

public class RequestMapper {
    private final HttpRequest request;
    private final HttpResponse response;
    private final ControllerRouter router = new ControllerRouter();

    public RequestMapper(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public void proceed() throws Exception {
        String path = request.getUrl();
        Controller controller = router.getController(path);
        controller.execute(request, response);
        response.close();
    }
}
