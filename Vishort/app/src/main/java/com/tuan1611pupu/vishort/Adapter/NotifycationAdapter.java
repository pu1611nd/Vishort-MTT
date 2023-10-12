package com.tuan1611pupu.vishort.Adapter;

import android.content.Context;
import android.text.Html;
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
import com.squareup.picasso.Picasso;
import com.tuan1611pupu.vishort.Model.ChatMessage;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.databinding.ItemContainerRecentConversionBinding;
import com.tuan1611pupu.vishort.databinding.ItemNotifycationBinding;
import com.tuan1611pupu.vishort.databinding.ItemSearchUserBinding;

import java.util.ArrayList;
import java.util.List;

public class NotifycationAdapter extends RecyclerView.Adapter<NotifycationAdapter.NotiHolder> {
    List<NotificationData> notilist = new ArrayList<>();

    NotifycationAdapter.OnItemUserClick onItemClick;

    public NotifycationAdapter.OnItemUserClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(NotifycationAdapter.OnItemUserClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemUserClick {
        void onClick(NotificationData noti);
    }


    @NonNull
    @Override
    public NotifycationAdapter.NotiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotifycationAdapter.NotiHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifycation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotifycationAdapter.NotiHolder  holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return notilist.size();
    }

    public List<NotificationData> getList() {
        return notilist;
    }

    public void addData(List<NotificationData> noti) {
        this.notilist.addAll(noti);
        notifyItemRangeInserted(this.notilist.size(), noti.size());
    }

    public void clear() {
        notilist.clear();
        notifyDataSetChanged();
    }

    public class NotiHolder extends RecyclerView.ViewHolder {
        ItemNotifycationBinding binding;
        Context context;

        public NotiHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNotifycationBinding.bind(itemView);
            this.context = itemView.getContext();;
        }

        public void setData(int position) {
            NotificationData noti = notilist.get(position);

            if(noti.getIsRead() == 1){
                binding.layoutNoti.setBackgroundColor(context.getResources().getColor(R.color.app_color, null));
            }

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User");
            // Đăng ký lắng nghe sự thay đổi của một tài liệu trong Realtime Database
            usersRef.child(noti.getNotificationBy()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Lấy dữ liệu từ snapshot và cập nhật thông tin của người dùng (User)
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            Glide.with(binding.getRoot()).load(user.getImage()).into(binding.imageProfile);
                            if(noti.getActionType().equals("follower")){
                                String formattedText = "<b>" + user.getUsername() + "</b> đã follow bạn.";
                                binding.textName.setText(Html.fromHtml(formattedText));
                            }else if (noti.getActionType().equals("comment")){
                                String formattedText = "<b>" + user.getUsername() + "</b> đã comment 1 video của bạn.";
                                binding.textName.setText(Html.fromHtml(formattedText));

                            }else {
                                String formattedText = "<b>" + user.getUsername() + "</b> đã like 1 video của bạn.";
                                binding.textName.setText(Html.fromHtml(formattedText));

                            }

                            String timeAgo = TimeAgo.using(noti.getTimestamp());
                            binding.textRecentMessage.setText(timeAgo);
                        }
                    } else {
                        // Nếu tài liệu không tồn tại hoặc bị xóa, bạn có thể xử lý ở đây
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Xử lý lỗi nếu có
                }
            });

            itemView.setOnClickListener(v ->
                    onItemClick.onClick(noti));

        }


    }


}

