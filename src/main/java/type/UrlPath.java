package type;

public enum UrlPath {
    ROOT("/"),
    INDEX("/index.html"),
    LOGIN("/user/login"),
    LOGIN_FAILED("/user/login_failed.html"),
    LOGIN_PAGE("/user/login.html"),
    SIGNUP("/user/signup"),
    USER_LIST("/user/userList");

    private final String path;

    UrlPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
