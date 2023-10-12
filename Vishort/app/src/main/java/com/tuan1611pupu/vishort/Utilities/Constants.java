package com.tuan1611pupu.vishort.Utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "User";
    public static final String KEY_COLLECTION_REELS = "Reels";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_PREFERENCE_NAME1 = "chatAppPreference";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "Chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "content";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String PROVIDER_FACEBOOK = "facebook";
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_PHONE = "phone";
    public static  final String KEY_PROVIDER = "provider";



    public static final String KEY_COLLECTION_CONVERSATIONS = "Conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";

    public static final String KEY_PREFERENCE_NAME = "videoMeetingPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_IS_ADMIN = "isAdmin";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";


    public static final String IMAGE_COVER ="https://firebasestorage.googleapis.com/v0/b/vishort-ef364.appspot.com/o/image%2Fcove_image.jpeg?alt=media&token=3cd9b385-4033-4037-91ea-e7eb3352dbbd";

    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String,String> getRemoteMessageHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAw21X72I:APA91bHNjwMSf-gGSM6mHO8KF2Ae2zk8Qb2PzEbVAl8Rf-pbnB2p0Ta7CLX8_r6Sx2K4LHkPEnw33ZQGrRu6B-3YWVRnXfvXQMh80qwEQEWI_CEiaOKQKNmzDQAGd6TU2oNo731zTqHX"
            );
            remoteMsgHeaders.put(Constants.REMOTE_MSG_CONTENT_TYPE,"application/json");
        }

        return remoteMsgHeaders;
    }
}
