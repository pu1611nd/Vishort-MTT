package com.tuan1611pupu.vishort.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuan1611pupu.vishort.Model.Comment;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.databinding.LayoutCommentBinding;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {
    private ArrayList<Comment> list;
    private Context context;
    private FirebaseDatabase database;

    public CommentAdapter(ArrayList<Comment> list, Context context) {
        this.list = list;
        this.context = context;
        this.database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_comment,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Comment comment = list.get(position);
        holder.binding.tvCommentBody.setText(comment.getCommentBody());
        String timeAgo = TimeAgo.using(comment.getCommentedAt());
        holder.binding.tvCommentTime.setText(timeAgo);

        DatabaseReference userRef = database.getReference("User").child(comment.getCommentedBy());

        // Thực hiện truy vấn dữ liệu trên Realtime Database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ snapshot và tạo đối tượng User
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Cập nhật giao diện người dùng với dữ liệu mới
                        Glide.with(holder.binding.getRoot()).load(user.getImage()).into(holder.binding.imageProfile);
                        holder.binding.tvName.setText(user.getUsername());
                    }
                } else {
                    // Không tìm thấy dữ liệu hoặc không có kết quả phù hợp
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        LayoutCommentBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutCommentBinding.bind(itemView);
        }
    }
}
