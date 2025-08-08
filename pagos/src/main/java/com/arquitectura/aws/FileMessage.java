package com.arquitectura.aws;

public class FileMessage {

    private String message;

    public FileMessage(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
