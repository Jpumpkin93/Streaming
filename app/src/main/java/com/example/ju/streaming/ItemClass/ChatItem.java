package com.example.ju.streaming.ItemClass;

public class ChatItem {
    String id;
    String message;
    String image;

    public ChatItem(String id, String message, String image){
        this.id = id;
        this.message = message;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
