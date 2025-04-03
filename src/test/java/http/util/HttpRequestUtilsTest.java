package http.util;

import http.HttpRequest;
import org.junit.jupiter.api.Test;
import type.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestUtilsTest {

    @Test
    void parseQuery() {
        Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter("userId=1");
        assertEquals("1", queryParameter.get("userId"));
    }

    @Test
    void parseQueryMore() {
        Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter("userId=1&password=1");
        assertEquals("1", queryParameter.get("userId"));
        assertEquals("1", queryParameter.get("password"));
    }

    @Test
    void parseQueryZero() {
        Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter("");
    }

    private BufferedReader bufferedReaderFromFile(String filename) throws IOException {
        return new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(filename)
        ));
    }

    @Test
    void httpRequest_should_parse_post_request_correctly() throws IOException {
        // Given
        BufferedReader br = bufferedReaderFromFile("HttpRequestMessageTest.txt");

        // When
        HttpRequest request = HttpRequest.from(br);

        // Then
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/create", request.getUrl());
        assertEquals("localhost:8080", request.getHeader("Host"));
        assertEquals("keep-alive", request.getHeader("Connection"));

        Map<String, String> bodyParams = request.getBodyParams();
        assertEquals("jw", bodyParams.get("userId"));
        assertEquals("password", bodyParams.get("password"));
        assertEquals("jungwoo", bodyParams.get("name"));
    }
}