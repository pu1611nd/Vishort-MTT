package com.tuan1611pupu.vishort.Adapter;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.tuan1611pupu.vishort.Fragment.HomeFragment;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.databinding.ItemPostVideoBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserPostVideoAdapter extends RecyclerView.Adapter<UserPostVideoAdapter.viewHolder> {

    private List<Reels> reels = new ArrayList<>();
    private Context context;

    public UserPostVideoAdapter(List<Reels> reels, Context context) {
        this.reels = reels;
        this.context = context;
    }


    @NonNull
    @Override
    public UserPostVideoAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_video,parent,false);
        return new UserPostVideoAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Reels reel = reels.get(position);
        String videoUrl = reel.getVideo();
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
                    Glide.with(holder.binding.getRoot()).load(thumbnailFile).into(holder.binding.postThumbnail);
                } else {
                    // Xử lý khi có lỗi xảy ra

                }
            }
        });

        holder.binding.bio.setText(reel.getCaption());

        holder.itemView.setOnClickListener(v->{
            HomeFragment fragment = new HomeFragment();
            // Truyền đối tượng Reel vào Fragment1
            Bundle bundle = new Bundle();
            bundle.putSerializable("reel", reel);
            fragment.setArguments(bundle);
            // Thực hiện việc chuyển từ Fragment 4 sang Fragment 1
            FragmentManager fragmentManager =((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemPostVideoBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPostVideoBinding.bind(itemView);
        }
    }
}