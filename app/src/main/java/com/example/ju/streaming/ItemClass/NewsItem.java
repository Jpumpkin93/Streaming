package com.example.ju.streaming.ItemClass;

public class NewsItem {
    String newsimage;
    String newstext;
    String newsurl;

    public NewsItem(String newsimage, String newstext, String newsurl) {
        this.newsimage = newsimage;
        this.newstext = newstext;
        this.newsurl = newsurl;
    }

    public String getNewsimage() {
        return newsimage;
    }

    public void setNewsimage(String newsimage) {
        this.newsimage = newsimage;
    }

    public String getNewstext() {
        return newstext;
    }

    public void setNewstext(String newstext) {
        this.newstext = newstext;
    }

    public String getNewsurl() {
        return newsurl;
    }

    public void setNewsurl(String newsurl) {
        this.newsurl = newsurl;
    }
}
