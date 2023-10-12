package com.tuan1611pupu.vishort.Adapter;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.tuan1611pupu.vishort.Model.Video;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.databinding.ItemVideoBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    List<Video> videoList = new ArrayList<>();

    VideoAdapter.OnItemUserClick onItemClick;

    public VideoAdapter.OnItemUserClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(VideoAdapter.OnItemUserClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemUserClick {
        void onClick(Video video);
    }


    @NonNull
    @Override
    public VideoAdapter.VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoAdapter.VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public List<Video> getList() {
        return videoList;
    }

    public void addData(List<Video> videoList) {
        this.videoList.addAll(videoList);
        notifyItemRangeInserted(this.videoList.size(), videoList.size());
    }

    public void clear() {
        videoList.clear();
        notifyDataSetChanged();
    }

    public class VideoHolder extends RecyclerView.ViewHolder {
        ItemVideoBinding binding;
        Context context;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemVideoBinding.bind(itemView);
            this.context = itemView.getContext();
        }

        public void setData(int position) {
            Video video = videoList.get(position);
            String videoUrl = video.getVideo();
            UUID uuid = UUID.randomUUID();
            File thumbnailFile = new File(context.getCacheDir(), uuid+"image.png");
            String thumbnailPath = thumbnailFile.getAbsolutePath();
            Uri videoUri = Uri.parse(videoUrl);
            String[] cmd = {"-i", videoUri.toString(), "-ss", "00:00:01", "-frames:v", "1", thumbnailPath};
            FFmpeg.executeAsync(cmd, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        // Xử lý khi lấy được thành công hình ảnh đại diện của video trực tuyến
                        Glide.with(binding.getRoot()).load(thumbnailFile).into(binding.imageVideo);
                    } else {
                        // Xử lý khi có lỗi xảy ra

                    }
                }
            });
            Date date = new Date(video.getReelsAt());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDateTime = dateFormat.format(date);
            binding.textCaption.setText("Caption: "+video.getCaption());
            binding.textUser.setText("User: "+video.getUserName());
            binding.textTime.setText("Time: "+formattedDateTime);
            binding.textLike.setText("Like: "+video.getLikes());
            binding.textComment.setText("Comment: "+video.getComments());
            binding.textSave.setText("Save: "+video.getSaves());

            itemView.setOnClickListener(v ->
                    onItemClick.onClick(video));

        }


    }
}
