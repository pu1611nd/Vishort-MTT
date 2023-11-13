package com.tuan1611pupu.vishort.Api;


import com.tuan1611pupu.vishort.Model.Comment;
import com.tuan1611pupu.vishort.Model.Conversation;
import com.tuan1611pupu.vishort.Model.Follower;
import com.tuan1611pupu.vishort.Model.Like;
import com.tuan1611pupu.vishort.Model.Message;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.Save;
import com.tuan1611pupu.vishort.Model.Song;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.Model.User2;
import com.tuan1611pupu.vishort.Model.User3;
import com.tuan1611pupu.vishort.Model.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataService {

    @POST("add-user.php") // Đường dẫn API để thêm người dùng
    Call<Void> addUser(@Body User user);

    @FormUrlEncoded
    @POST("check-user.php") // Đường dẫn API để lấy thông tin người dùng
    Call<User2> getUserById(@Field("id") String userId);

    @POST("edit_user.php") // Đường dẫn API để thêm người dùng
    Call<Void> editUser(@Body User user);

    @GET("listSong.php")
    Call<List<Song>> getListSong();

    @FormUrlEncoded
    @POST("get_song.php")
    Call<Song> getSong(@Field("songId") String songId);

    @POST("add-video.php") //
    Call<Void> addReel(@Body Reels reels);

    @POST("add_like.php") //
    Call<Void> addLike(@Body Like like);

    @POST("add_saveVideo.php") //
    Call<Void> saveVideo(@Body Save save);

    @FormUrlEncoded
    @POST("delete_like.php")
    Call<Void> deleteLike(@Field("userId") String userId, @Field("reelsId") String reelsId);

    @FormUrlEncoded
    @POST("delete_saveVideo.php")
    Call<Void> deleteSave(@Field("userId") String userId, @Field("reelsId") String reelsId);

    @POST("add_comment.php") //
    Call<Void> addComment(@Body Comment comment);

    @FormUrlEncoded
    @POST("update_conver.php")
    Call<Void> updateConversation(@Field("conversationID") String conversationID, @Field("lastMessage") String lastMessage, @Field("timestamp") long timestamp);


    @POST("add_chat.php") //
    Call<Void> addChat(@Body Message message);

    @POST("add_Conversation.php") //
    Call<Void> addConversation(@Body Conversation conversation);

    @POST("add_follow.php") //
    Call<Void> addFollow(@Body Follower follower);

    @FormUrlEncoded
    @POST("delete_follow.php")
    Call<Void> deleteFollow(@Field("userId") String userId, @Field("follower") String follower);

    @FormUrlEncoded
    @POST("get_follow.php")// ai dang fl mk
    Call<List<Follower>> getFollow(@Field("userId") String userId);

    @FormUrlEncoded
    @POST("get_following.php")// mk dang fl ai
    Call<List<Follower>> getFollowing(@Field("userId") String userId);


    @FormUrlEncoded
    @POST("get_saveVideo.php")
    Call<List<Reels>> getSaveVideo(@Field("userId") String userId);


    @POST("add_NotificationData.php") //noti
    Call<Void> addNotification(@Body NotificationData notificationData);

    @FormUrlEncoded
    @POST("update_NotificationData.php")
    Call<Void> updateNotification(@Field("notificationId") String notificationId, @Field("isRead") int isRead, @Field("userId") String userId);

    @FormUrlEncoded
    @POST("get_like.php")
    Call<Like> getLike(@Field("likeId") String likeId);

    @FormUrlEncoded
    @POST("get_listVideo.php")
    Call<List<Video>> getListVideo(@Field("timeStart")long timeStart , @Field("timeEnd")long timeEnd);

    @GET("get_listUser.php")
    Call<List<User3>> getListUser();


}
