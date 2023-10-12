package com.tuan1611pupu.vishort.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tuan1611pupu.vishort.databinding.ActivityPreviewBinding;

public class PreviewActivity extends AppCompatActivity {

    private ActivityPreviewBinding binding;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        Uri videoUri = intent.getParcelableExtra("uriVideo");
        String SongId = intent.getStringExtra("SongId");
        player = new SimpleExoPlayer.Builder(this).build();
        binding.player.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(videoUri));
        player.prepare();
        player.setPlayWhenReady(true);

        binding.editVideo.setOnClickListener(v->{
            EditVideoActivity.call(this, videoUri,SongId);
            player.release();
        });



        binding.back.setOnClickListener(v->{
            Intent intent1 = new Intent(getApplicationContext(),RecorderActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
            finish();
        });

        binding.post.setOnClickListener(v->{
            Intent intent1= new Intent(getApplicationContext(),UploadVideoActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.putExtra("uriVideo",videoUri);
            intent1.putExtra("SongId",SongId);
            startActivity(intent1);
            finish();
        });

    }





    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }
}