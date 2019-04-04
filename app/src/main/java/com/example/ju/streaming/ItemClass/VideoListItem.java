package com.example.ju.streaming.ItemClass;

public class VideoListItem {
    String nickname;
    String image;
    String thumbnail;
    String title;
    String content;
    String video;
    String date;
    String playtime;
    String hit;

    public VideoListItem(String nickname, String image, String thumbnail, String title, String content, String video, String date, String playtime, String hit) {
        this.nickname = nickname;
        this.image = image;
        this.thumbnail = thumbnail;
        this.title = title;
        this.content = content;
        this.video = video;
        this.date = date;
        this.playtime = playtime;
        this.hit = hit;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlaytime() {
        return playtime;
    }

    public void setPlaytime(String playtime) {
        this.playtime = playtime;
    }

    public String getHit() {
        return hit;
    }

    public void setHit(String hit) {
        this.hit = hit;
    }
}
