package com.tuan1611pupu.vishort.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;
import com.tuan1611pupu.vishort.Model.ChatMessage;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.databinding.ItemContainerRecentConversionBinding;
import com.tuan1611pupu.vishort.listeners.ConversionListener;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage){
            Picasso.get().load(chatMessage.getConversionImage()).into(binding.imageProfile);
            binding.textName.setText(chatMessage.getConversionName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            String timeAgo = TimeAgo.using(chatMessage.getDateObject().getTime());
            binding.textTime.setText(timeAgo);
            binding.getRoot().setOnClickListener(v ->{
                User user = new User();
                user.setId(chatMessage.getConversionId());
                user.setUsername(chatMessage.getConversionName());
                user.setImage(chatMessage.getConversionImage());
                conversionListener.onConversionClicked(user);
            });
        }
    }


}
