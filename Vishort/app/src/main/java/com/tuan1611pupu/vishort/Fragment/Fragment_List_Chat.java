package com.tuan1611pupu.vishort.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tuan1611pupu.vishort.Activity.ChatActivity;
import com.tuan1611pupu.vishort.Adapter.RecentConversationAdapter;
import com.tuan1611pupu.vishort.Model.ChatMessage;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.listeners.ConversionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Fragment_List_Chat extends Fragment implements ConversionListener {

    private RecyclerView recyclerView;
    private TextView textNoti;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fagment_list_chat,container,false);
        preferenceManager = new PreferenceManager(getContext());
        recyclerView = view.findViewById(R.id.conversationRecyclerView);
        progressBar = view.findViewById(R.id.progressBarListChat);
        textNoti = view.findViewById(R.id.textList);
        init();
        listenConversation();
        return view;
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations,this);
        recyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversation(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference conversationsReference = firebaseDatabase.getReference().child(Constants.KEY_COLLECTION_CONVERSATIONS);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Thực hiện xử lý khi có dữ liệu mới được thêm vào nút con của conversationsReference
                String senderId = snapshot.child(Constants.KEY_SENDER_ID).getValue(String.class);
                String receiverId = snapshot.child(Constants.KEY_RECEIVER_ID).getValue(String.class);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderId(senderId);
                chatMessage.setReceiverId(receiverId);

                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                    chatMessage.setConversionImage(snapshot.child(Constants.KEY_RECEIVER_IMAGE).getValue(String.class));
                    chatMessage.setConversionName(snapshot.child(Constants.KEY_RECEIVER_NAME).getValue(String.class));
                    chatMessage.setConversionId(receiverId);
                } else {
                    chatMessage.setConversionImage(snapshot.child(Constants.KEY_SENDER_IMAGE).getValue(String.class));
                    chatMessage.setConversionName(snapshot.child(Constants.KEY_SENDER_NAME).getValue(String.class));
                    chatMessage.setConversionId(senderId);
                }

                chatMessage.setMessage(snapshot.child(Constants.KEY_LAST_MESSAGE).getValue(String.class));
                chatMessage.setDateObject(new Date(snapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class)));

                conversations.add(chatMessage);

                Collections.sort(conversations, (obj1, obj2) -> obj2.getDateObject().compareTo(obj1.getDateObject()));
                conversationAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if(conversations.size() == 0){
                    recyclerView.setVisibility(View.GONE);
                    textNoti.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Xử lý khi có dữ liệu bị thay đổi (nếu cần)
                String senderId = snapshot.child(Constants.KEY_SENDER_ID).getValue(String.class);
                String receiverId = snapshot.child(Constants.KEY_RECEIVER_ID).getValue(String.class);
                String lastMessage = snapshot.child(Constants.KEY_LAST_MESSAGE).getValue(String.class);
                Date timestamp = new Date(snapshot.child(Constants.KEY_TIMESTAMP).getValue(long.class));

                for (int i = 0; i < conversations.size(); i++) {
                    if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                        conversations.get(i).setMessage(lastMessage);
                        conversations.get(i).setDateObject(timestamp);
                        break;
                    }
                }

                Collections.sort(conversations, (obj1, obj2) -> obj2.getDateObject().compareTo(obj1.getDateObject()));
                conversationAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Xử lý khi có dữ liệu bị xóa (nếu cần)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Xử lý khi có dữ liệu di chuyển (nếu cần)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi xảy ra trong quá trình lắng nghe
            }
        };

        // Đăng ký lắng nghe sự kiện thay đổi trên conversationsReference cho từng trường hợp sender và receiver
        conversationsReference.orderByChild(Constants.KEY_SENDER_ID)
                .equalTo(preferenceManager.getString(Constants.KEY_USER_ID))
                .addChildEventListener(childEventListener);

        conversationsReference.orderByChild(Constants.KEY_RECEIVER_ID)
                .equalTo(preferenceManager.getString(Constants.KEY_USER_ID))
                .addChildEventListener(childEventListener);

    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}
