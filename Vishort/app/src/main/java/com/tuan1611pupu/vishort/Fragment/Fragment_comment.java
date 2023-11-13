package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tuan1611pupu.vishort.Adapter.CommentAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.ChatMessage;
import com.tuan1611pupu.vishort.Model.Comment;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.Utilities.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_comment extends BottomSheetDialogFragment {


    private ImageView imgExit;
    private RecyclerView rcListComment;
    private EditText inputComment;
    private FrameLayout layoutSend;
    private TextView commentNull;

    private ArrayList<Comment> list;
    private CommentAdapter adapter;
    private PreferenceManager preferenceManager;

    private String reelsId;
    private String reelsBy;

    private int commentCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            reelsId = bundle.getString("reelId"); // Lấy giá trị từ Bundle
            reelsBy = bundle.getString("reelBy");
        }
        commentNull = view.findViewById(R.id.commentNull);
        imgExit = view.findViewById(R.id.imgExit);
        rcListComment = view.findViewById(R.id.rc_comment);
        inputComment = view.findViewById(R.id.inputMessage);
        layoutSend = view.findViewById(R.id.layoutSend);
        preferenceManager = new PreferenceManager(getContext());
        list = new ArrayList<>();
        adapter = new CommentAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcListComment.setLayoutManager(linearLayoutManager);
        rcListComment.setNestedScrollingEnabled(false);
        rcListComment.setAdapter(adapter);

        imgExit.setOnClickListener(v -> {
            dismiss();
        });
        layoutSend.setOnClickListener(v -> {
            if (inputComment.getText().toString().isEmpty()) {
                preferenceManager.showToast(getContext(), Validation.VALIDATION_COMMENT_INFO);
            } else {

                Comment comment = new Comment();
                UUID uuid = UUID.randomUUID();
                comment.setCommentId(uuid.toString());
                comment.setReelId(reelsId);
                comment.setCommentBody(inputComment.getText().toString().trim());
                comment.setCommentedAt(new Date().getTime());
                comment.setCommentedBy(preferenceManager.getString(Constants.KEY_USER_ID));

                DatabaseReference reelsRef = FirebaseDatabase.getInstance()
                        .getReference("Reels")
                        .child(reelsId);

                DataService dataService = APIService.getService();
                Call<Void> call = dataService.addComment(comment);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Xử lý khi cập nhật thành công
                            Map<String, Object> updates = new HashMap<>();
                            commentCount++;
                            updates.put("comments", commentCount);
                            reelsRef.updateChildren(updates)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công số lượng like
                                        inputComment.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật số lượng like thất bại
                                    });
                            addNotification(uuid.toString(), reelsBy);

                        } else {
                            // Xử lý lỗi khi yêu cầu thất bại
                            PreferenceManager.showToast(getContext(),Validation.ERR0_EX);

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Xử lý lỗi khi yêu cầu thất bại
                        PreferenceManager.showToast(getContext(),Validation.ERR0_EX);
                    }
                });

            }
        });

        getComment();

        return view;

    }

    private void getComment() {

        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("Comment")
                .child(reelsId);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Comment> comments = new ArrayList<>();
                    for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                        Comment comment = commentSnapshot.getValue(Comment.class);
                        comments.add(comment);
                    }

                    // Xóa list hiện tại và thêm danh sách comment vào list
                    list.clear();
                    list.addAll(comments);
                    commentCount = list.size();
                    if (commentCount != 0) {
                        commentNull.setVisibility(View.GONE);
                    }

                    // Báo cho adapter biết rằng dữ liệu đã thay đổi
                    adapter.notifyDataSetChanged();


                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void addNotification(String commentId, String userId) {
        NotificationData notificationData = new NotificationData();
        UUID uuid = UUID.randomUUID();
        notificationData.setNotificationId(uuid.toString());
        notificationData.setUserId(userId);
        notificationData.setActionId(commentId);
        notificationData.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
        notificationData.setIsRead(0);
        notificationData.setTimestamp(new Date().getTime());
        notificationData.setActionType("comment");

        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addNotification(notificationData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

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
}
