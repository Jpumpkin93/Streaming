package com.example.ju.streaming.ItemClass;

public class UserListItem {

    String image;
    String nickname;

    public UserListItem(String image, String nickname) {
        this.image = image;
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
