package com.example.ju.streaming.ItemClass;

public class SubscribItem {

    String bjnickname;
    String bjimage;

    public SubscribItem(String bjnickname, String bjimage) {
        this.bjnickname = bjnickname;
        this.bjimage = bjimage;
    }

    public String getBjnickname() {
        return bjnickname;
    }

    public void setBjnickname(String bjnickname) {
        this.bjnickname = bjnickname;
    }

    public String getBjimage() {
        return bjimage;
    }

    public void setBjimage(String bjimage) {
        this.bjimage = bjimage;
    }
}
