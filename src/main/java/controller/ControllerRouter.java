package controller;

import java.util.HashMap;
import java.util.Map;

public class ControllerRouter {
    private final Map<String, Controller> routes = new HashMap<>();
    private final Controller defaultController = new ForwardController();

    public ControllerRouter() {
        init();
    }

    private void init() {
        routes.put("/", new HomeController());
        routes.put("/user/signup", new SignUpController());
        routes.put("/user/login", new LoginController());
        routes.put("/user/userList", new ListController());
    }

    public Controller getController(String path) {
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }
        return routes.getOrDefault(path, defaultController);
    }
}
