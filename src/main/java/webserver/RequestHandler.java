package webserver;

import controller.Controller;
import controller.ControllerRouter;
import http.HttpRequest;
import http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final ControllerRouter router = new ControllerRouter();

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

            Controller controller = router.getController(url);
            controller.execute(request, response);
            response.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
