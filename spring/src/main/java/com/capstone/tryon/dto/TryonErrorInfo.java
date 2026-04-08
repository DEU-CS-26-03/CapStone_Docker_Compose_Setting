package com.capstone.tryon.dto;

public class TryonErrorInfo {
    private String code;
    private String message;

    public TryonErrorInfo() {}
    public TryonErrorInfo(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}