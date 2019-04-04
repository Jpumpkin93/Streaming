package com.example.ju.streaming.ItemClass;

public class ChatListItem {

    String youimage;
    String younickname;
    String lastmessage;
    int hit;


    public ChatListItem(String youimage, String younickname, String lastmessage, int hit) {
        this.youimage = youimage;
        this.younickname = younickname;
        this.lastmessage = lastmessage;
        this.hit = hit;
    }

    public String getYouimage() {
        return youimage;
    }

    public void setYouimage(String youimage) {
        this.youimage = youimage;
    }

    public String getYounickname() {
        return younickname;
    }

    public void setYounickname(String younickname) {
        this.younickname = younickname;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }
}
