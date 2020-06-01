package com.rifcode.camchat.models;

public class Messaging {

    private String username,thumb_image;

    public Messaging() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public Messaging(String username, String thumb_image) {
        this.username = username;
        this.thumb_image = thumb_image;
    }

}
