package com.tuan1611pupu.vishort.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.tuan1611pupu.vishort.Adapter.ChatAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.ChatMessage;
import com.tuan1611pupu.vishort.Model.Conversation;
import com.tuan1611pupu.vishort.Model.Message;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.databinding.ActivityChatBinding;
import com.tuan1611pupu.vishort.databinding.ActivityLogInBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;

    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private  FirebaseDatabase database;
    private User myUser;
    private String conversionId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        myUser = new User();
        loadReceiverDetails();
        init();
        getUser();
        listenMessages1();
        setListeners();

    }
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.getImage(),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    private void getUser(){
        DatabaseReference usersRef = database.getReference("User");
        // Đăng ký lắng nghe sự thay đổi của một tài liệu trong Realtime Database
        usersRef.child(preferenceManager.getString(Constants.KEY_USER_ID)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ snapshot và cập nhật thông tin của người dùng (User)
                    myUser = dataSnapshot.getValue(User.class);

                } else {
                    // Nếu tài liệu không tồn tại hoặc bị xóa, bạn có thể xử lý ở đây
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });

    }

    private void sendMessage(){
        Message message = new Message();
        UUID uuid = UUID.randomUUID();
        message.setChatId(uuid.toString());
        message.setSenderId(preferenceManager.getString(Constants.KEY_USER_ID));
        message.setReceiverId(receiverUser.getId());
        message.setContent(binding.inputMessage.getText().toString());
        message.setTimestamp(new Date().getTime());

        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addChat(message);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("chat","da gui thanh cong");
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });

        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            Conversation conversation = new Conversation();
            UUID uuid1 = UUID.randomUUID();
            conversation.setConversationID(uuid1.toString());
            conversation.setSenderId(preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.setSenderName(myUser.getUsername());
            conversation.setSenderImage(myUser.getImage());
            conversation.setReceiverId(receiverUser.getId());
            conversation.setReceiverName(receiverUser.getUsername());
            conversation.setReceiverImage(receiverUser.getImage());
            conversation.setLastMessage(binding.inputMessage.getText().toString());
            conversation.setTimestamp(new Date().getTime());
            addConversion(conversation);
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        DatabaseReference databaseReference = database.getReference(Constants.KEY_COLLECTION_CHAT);
        // Sử dụng phương thức addListenerForSingleValueEvent để lấy dữ liệu một lần duy nhất
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int count = chatMessages.size();
                    chatMessages.clear();
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setSenderId(chatSnapshot.child(Constants.KEY_SENDER_ID).getValue(String.class));
                        chatMessage.setReceiverId(chatSnapshot.child(Constants.KEY_RECEIVER_ID).getValue(String.class));
                        chatMessage.setMessage(chatSnapshot.child(Constants.KEY_MESSAGE).getValue(String.class));
                        chatMessage.setDateTime(getReadableDateTime(new Date(chatSnapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class))));
                        chatMessage.setDateObject(new Date(chatSnapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class)));
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(chatMessage.getSenderId())
                                && receiverUser.getId().equals(chatMessage.getReceiverId()) || preferenceManager.getString(Constants.KEY_USER_ID).equals(chatMessage.getReceiverId())
                                && receiverUser.getId().equals(chatMessage.getSenderId()) ) {
                            chatMessages.add(chatMessage);
                        }
                        Collections.sort(chatMessages, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));
                        if (count == 0) {
                            chatAdapter.notifyDataSetChanged();
                        } else {
                            chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                            //tim sau lai nha
                            binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        }
                        binding.chatRecyclerView.setVisibility(View.VISIBLE);
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    if(conversionId == null){
                        checkForConversion();
                    }


                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void listenMessages1() {
        DatabaseReference databaseReference = database.getReference(Constants.KEY_COLLECTION_CHAT);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int count = chatMessages.size();
                    chatMessages.clear();
                    List<ChatMessage> newMessages = new ArrayList<>();

                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setSenderId(chatSnapshot.child(Constants.KEY_SENDER_ID).getValue(String.class));
                        chatMessage.setReceiverId(chatSnapshot.child(Constants.KEY_RECEIVER_ID).getValue(String.class));
                        chatMessage.setMessage(chatSnapshot.child(Constants.KEY_MESSAGE).getValue(String.class));
                        chatMessage.setDateTime(getReadableDateTime(new Date(chatSnapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class))));
                        chatMessage.setDateObject(new Date(chatSnapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class)));
                        if (chatMessage == null) {
                            continue;
                        }

                        String senderId = chatMessage.getSenderId();
                        String receiverId = chatMessage.getReceiverId();
                        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                        if ((currentUserId.equals(senderId) && receiverUser.getId().equals(receiverId)) ||
                                (currentUserId.equals(receiverId) && receiverUser.getId().equals(senderId))) {
                            newMessages.add(chatMessage);
                        }
                    }

                    chatMessages.addAll(newMessages);
                    Collections.sort(chatMessages, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));
                    if(count == 0){
                        chatAdapter.notifyDataSetChanged();
                    }else {
                        chatAdapter.notifyDataSetChanged();
                        int lastVisibleItemPosition = chatMessages.size() - 1;
                        binding.chatRecyclerView.smoothScrollToPosition(Math.min(lastVisibleItemPosition, chatMessages.size() - 1));
                    }
                    binding.chatRecyclerView.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);


                    if (conversionId == null) {
                        checkForConversion();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled if needed
            }
        });
    }



    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.getUsername());

    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void addConversion(Conversation conversation){
        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addConversation(conversation);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("conver","da gui thanh cong");
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });



    }

    private void updateConversion(String message) {
        DataService dataService = APIService.getService();
        Call<Void> call = dataService.updateConversation(conversionId,message,new Date().getTime());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("update", "thanh cong");

                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });
    }



    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.getId()
            );
            checkForConversionRemotely(
                    receiverUser.getId(),
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Conversations");

        reference.orderByChild("senderId").equalTo(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String conversationSenderId = snapshot.child("senderId").getValue(String.class);
                    String conversationReceiverId = snapshot.child("receiverId").getValue(String.class);

                    if (conversationSenderId.equals(senderId) && conversationReceiverId.equals(receiverId)) {
                        conversionId = snapshot.child("conversationId").getValue(String.class);
                        // Đây là cuộc trò chuyện bạn đang tìm kiếm (senderId và receiverId khớp)
                        // Tiếp tục xử lý dữ liệu ở đây...
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình lắng nghe dữ liệu
            }
        });
    }




}