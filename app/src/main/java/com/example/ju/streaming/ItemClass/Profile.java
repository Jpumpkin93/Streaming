package com.example.ju.streaming.ItemClass;

import com.google.gson.annotations.SerializedName;

public class Profile {


    @SerializedName("check")
    public String check;
    @SerializedName("nickname")
    public String nickname;
    @SerializedName("image")
    public String image;
    @SerializedName("introduce")
    public String introduce;
    @SerializedName("email")
    public String email;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
