package com.example.confessionwall.dto;

public class CsrfTokenResponse {
    private String token;
    private String headerName;
    private String cookieName;

    public CsrfTokenResponse() {}

    public CsrfTokenResponse(String token, String headerName, String cookieName) {
        this.token = token;
        this.headerName = headerName;
        this.cookieName = cookieName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }
    public String getCookieName() { return cookieName; }
    public void setCookieName(String cookieName) { this.cookieName = cookieName; }
}
