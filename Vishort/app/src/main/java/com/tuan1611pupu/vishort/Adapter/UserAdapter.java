package com.tuan1611pupu.vishort.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tuan1611pupu.vishort.Model.User3;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    List<User3> userList = new ArrayList<>();

    UserAdapter.OnItemUserClick onItemClick;

    public UserAdapter.OnItemUserClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(UserAdapter.OnItemUserClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemUserClick {
        void onClick(User3 user);
    }


    @NonNull
    @Override
    public UserAdapter.UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserAdapter.UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<User3> getList() {
        return userList;
    }

    public void addData(List<User3> userList) {
        this.userList.addAll(userList);
        notifyItemRangeInserted(this.userList.size(),userList.size());
    }

    public void clear() {
        userList.clear();
        notifyDataSetChanged();
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;
        Context context;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemUserBinding.bind(itemView);
            this.context = itemView.getContext();
        }

        public void setData(int position) {
            User3 user = userList.get(position);
            Glide.with(binding.getRoot()).load(user.getImage()).into(binding.imageUser);
            binding.textUserName.setText("Username: "+user.getUsername());
            binding.textBio.setText("Bio: "+user.getBio());
            binding.textEmail.setText("Email: "+user.getEmail());
            binding.textFollow.setText("Follows: "+user.getFollowsCount());
            binding.textFollowing.setText("Following: "+user.getFolloweringCount());
            binding.textReel.setText("Reels: "+user.getReelsCount());

            itemView.setOnClickListener(v ->
                    onItemClick.onClick(user));

        }


    }
}