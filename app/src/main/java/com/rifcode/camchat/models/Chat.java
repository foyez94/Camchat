package com.rifcode.camchat.models;

public class Chat {
    public String message;
    public String from;


    public Chat() {
    }

    public Chat(String message, String from) {
        this.message = message;
        this.from = from;


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {this.from = from;}


}
