package jjocenio.rosey.service.http;

import jjocenio.rosey.service.ProcessConfig;

public class HttpProcessConfig extends ProcessConfig {

    private String url;
    private HttpMethod httpMethod;
    private String body;
    private String headers;
    private long wait;

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getWait() {
        return wait;
    }

    public void setWait(long wait) {
        this.wait = wait;
    }

    public enum HttpMethod {
        GET, POST, PUT, DELETE;
    }
}
