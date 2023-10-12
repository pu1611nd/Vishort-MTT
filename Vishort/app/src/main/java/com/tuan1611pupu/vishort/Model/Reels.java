package com.tuan1611pupu.vishort.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Reels implements Serializable {
    @SerializedName("reelsId")
    @Expose
    private String reelsId;
    @SerializedName("reelsBy")
    @Expose
    private String reelsBy;
    @SerializedName("reelsAt")
    @Expose
    private long reelsAt;
    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("video")
    @Expose
    private String video;
    @SerializedName("songId")
    @Expose
    private String songId;
    @SerializedName("likeCount")
    @Expose
    private int likes;
    @SerializedName("saveCount")
    @Expose
    private int saves;
    @SerializedName("commentCount")
    @Expose
    private int comments;

    public String getReelsId() {
        return reelsId;
    }

    public void setReelsId(String reelsId) {
        this.reelsId = reelsId;
    }

    public String getReelsBy() {
        return reelsBy;
    }

    public void setReelsBy(String reelsBy) {
        this.reelsBy = reelsBy;
    }

    public long getReelsAt() {
        return reelsAt;
    }

    public void setReelsAt(long reelsAt) {
        this.reelsAt = reelsAt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        this.saves = saves;
    }
}
