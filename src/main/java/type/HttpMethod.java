package type;

public enum HttpMethod {
    GET, POST;

    public static HttpMethod from(String method){
        return HttpMethod.valueOf(method.toUpperCase());
    }
}
