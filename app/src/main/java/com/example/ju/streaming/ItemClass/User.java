package com.example.ju.streaming.ItemClass;

import com.google.gson.annotations.SerializedName;

public class User {


    @SerializedName("check")
    public String check;
    @SerializedName("image")
    public String image;
    @SerializedName("nickname")
    public String nickname;

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
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
