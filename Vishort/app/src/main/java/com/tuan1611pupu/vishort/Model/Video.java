package com.tuan1611pupu.vishort.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Video extends Reels{
    @SerializedName("username")
    @Expose
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
