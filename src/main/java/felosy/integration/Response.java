package felosy.integration;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a response from an external API or service
 */
public class Response {
    private int statusCode;
    private String body;
    private Map<String, String> headers;
    private boolean successful;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
        this.successful = (statusCode >= 200 && statusCode < 300);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", successful=" + successful +
                ", body='" + body + '\'' +
                '}';
    }
}