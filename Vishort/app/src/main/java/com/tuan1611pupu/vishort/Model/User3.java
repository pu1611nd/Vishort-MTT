package com.tuan1611pupu.vishort.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User3 extends User{

    @SerializedName("followeringCount")
    @Expose
    private int followeringCount;
    @SerializedName("followsCount")
    @Expose
    private int followsCount;
    @SerializedName("reelsCount")
    @Expose
    private int reelsCount;

    public int getFolloweringCount() {
        return followeringCount;
    }

    public void setFolloweringCount(int followeringCount) {
        this.followeringCount = followeringCount;
    }

    public int getFollowsCount() {
        return followsCount;
    }

    public void setFollowsCount(int followsCount) {
        this.followsCount = followsCount;
    }

    public int getReelsCount() {
        return reelsCount;
    }

    public void setReelsCount(int reelsCount) {
        this.reelsCount = reelsCount;
    }
}
