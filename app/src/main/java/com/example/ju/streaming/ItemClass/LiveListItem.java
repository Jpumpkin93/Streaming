package com.example.ju.streaming.ItemClass;

public class LiveListItem {
    String id;
    String image;
    String title;
    String nickname;
    String thumbnail;
    String liveaddress;

    public LiveListItem(String id, String image, String title, String nickname, String thumbnail, String liveaddress) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.nickname = nickname;
        this.thumbnail = thumbnail;
        this.liveaddress = liveaddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLiveaddress() {
        return liveaddress;
    }

    public void setLiveaddress(String liveaddress) {
        this.liveaddress = liveaddress;
    }
}
