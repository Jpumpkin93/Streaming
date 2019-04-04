package com.example.ju.streaming.ItemClass;


import com.google.gson.annotations.SerializedName;

public class Check{

    @SerializedName("check")
    public String check;
    public int error;
    public boolean bool;

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }
}
