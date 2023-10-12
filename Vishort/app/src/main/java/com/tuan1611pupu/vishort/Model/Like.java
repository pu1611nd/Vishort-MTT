package com.tuan1611pupu.vishort.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Like {
    @SerializedName("likeId")
    @Expose
    private String likeId;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("reelsId")
    @Expose
    private String reelsId;

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReelsId() {
        return reelsId;
    }

    public void setReelsId(String reelsId) {
        this.reelsId = reelsId;
    }
}
