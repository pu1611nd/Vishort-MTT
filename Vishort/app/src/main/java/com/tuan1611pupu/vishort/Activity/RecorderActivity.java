package com.tuan1611pupu.vishort.Activity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.AudioCodec;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.VideoCodec;
import com.otaliastudios.cameraview.filter.Filters;
import com.tuan1611pupu.vishort.Adapter.FilterRecordAdapter;
import com.tuan1611pupu.vishort.Model.Song;
import com.tuan1611pupu.vishort.databinding.ActivityRecorderBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter;

public class RecorderActivity extends AppCompatActivity {
    private ActivityRecorderBinding binding;

    private boolean flash = false;
    private boolean isRecording = false;

    private CountDownTimer countDownTimer;
    private File videoFile = null;
    private FilterRecordAdapter adapterFilter;

    private MediaPlayer mediaPlayer;


    private static final int SELECT_VIDEO = 3;

    private String linkAudio ;
    private String SongId ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecorderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent1 = getIntent();
        linkAudio = intent1.getStringExtra("linkAudio");
        SongId = intent1.getStringExtra("SongId");
        mediaPlayer = new MediaPlayer();

        if(linkAudio == null){
            binding.camera.setAudio(Audio.ON);
        }else {
            binding.camera.setAudio(Audio.OFF);
            binding.camera.setVideoCodec(VideoCodec.H_264);
            binding.camera.setAudioCodec(AudioCodec.AAC);


            try {
                loadAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        adapterFilter = new FilterRecordAdapter(this);

        binding.camera.setMode(Mode.PICTURE);
        // close
        binding.close.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        binding.filter.setOnClickListener(v->{
            binding.filters.setVisibility(View.VISIBLE);
            binding.closeFilter.setVisibility(View.VISIBLE);
            binding.filters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.filters.setAdapter(adapterFilter);

        });
        binding.closeFilter.setOnClickListener(v->{
            binding.filters.setVisibility(View.GONE);
            binding.closeFilter.setVisibility(View.GONE);
        });

        adapterFilter.setListener(filter -> {
            switch (filter) {
                case NONE:
                    binding.camera.setFilter(Filters.NONE.newInstance());
                    break;
                case BRIGHTNESS: {
                    binding.camera.setFilter(Filters.BRIGHTNESS.newInstance());
                    break;
                }
                case EXPOSURE:
                    binding.camera.setFilter(Filters.SEPIA.newInstance());
                    break;
                case GAMMA: {
                    binding.camera.setFilter(Filters.GAMMA.newInstance());
                    break;
                }
                case GRAYSCALE:
                    binding.camera.setFilter(Filters.GRAYSCALE.newInstance());
                    break;
                case HAZE: {
                    binding.camera.setFilter(Filters.SHARPNESS.newInstance());
                    break;
                }
                case INVERT:
                    binding.camera.setFilter(Filters.INVERT_COLORS.newInstance());
                    break;

                case MONOCHROME:
                    binding.camera.setFilter(Filters.LOMOISH.newInstance());
                    break;
                case PIXELATED: {
                    binding.camera.setFilter(Filters.AUTO_FIX.newInstance());
                    break;
                }
                case POSTERIZE:
                    binding.camera.setFilter(Filters.POSTERIZE.newInstance());
                    break;
                case SEPIA:
                    binding.camera.setFilter(Filters.SEPIA.newInstance());
                    break;
                case SHARP: {
                    GPUImageSharpenFilter glf = new GPUImageSharpenFilter();
                    glf.setSharpness(1f);
                    binding.camera.setFilter(Filters.SHARPNESS.newInstance());
                    break;
                }
                case SOLARIZE:
                    binding.camera.setFilter(Filters.SATURATION.newInstance());
                    break;
                case VIGNETTE:
                    binding.camera.setFilter(Filters.VIGNETTE.newInstance());
                    break;
                default:
                    break;
            }
        });

        // bat flash
        binding.flash.setOnClickListener(v->{
            if(flash){
                binding.camera.setFlash(Flash.OFF);
                flash = false;
            }else {
                binding.camera.setFlash(Flash.TORCH);
                flash = true;
            }
        });
        // chuyen cam
        binding.flip.setOnClickListener(view -> {
            // Lấy trạng thái hiện tại của camera (trước hay sau)
            Facing currentFacing = binding.camera.getFacing();

            // Chuyển đổi trạng thái camera
            Facing newFacing = currentFacing == Facing.BACK ? Facing.FRONT : Facing.BACK;
            binding.camera.setFacing(newFacing);
        });

        binding.record.setOnClickListener(v->{
            if(!isRecording){
                startRecordingVideo();
            }else {
                stopRecordingVideo();
            }
        });

        binding.upload.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select a Video "), SELECT_VIDEO);
        });

        binding.sound.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),SongActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });



    }

    public void loadAudio() throws IOException {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(linkAudio);
        mediaPlayer.prepare();

    }

    public void startRecordingVideo(){
        // Nếu đang quay phim hoặc còn đang đếm ngược, không bắt đầu ghi
        if (isRecording || countDownTimer != null) {
            return;
        }

        // Hiển thị đếm ngược trước khi ghi
        int countdownTime = 4; // Thời gian đếm ngược
        binding.countdown.setVisibility(View.VISIBLE);
        binding.count.setText(String.valueOf(countdownTime));
        countDownTimer = new CountDownTimer(countdownTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.count.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                binding.countdown.setVisibility(View.GONE);
                startCapturingVideo();
            }
        }.start();
    }

    // Bắt đầu ghi lại video
    private void startCapturingVideo() {

        // Xác định đường dẫn tới tệp video
        String filename = "video_" + System.currentTimeMillis() + ".mp4";
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        videoFile = new File(outputDir, filename);

        int timeSong = mediaPlayer.getDuration();
        mediaPlayer.start();
        binding.camera.takeVideoSnapshot(videoFile);
        isRecording = true;
        // Bắt đầu đếm ngược đến khi hoàn thành quay phim
        int recordingTime = timeSong; // Thời gian quay tối đa là 15 giây
        countDownTimer = new CountDownTimer(recordingTime , 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật đếm ngược cho người dùng biết
                binding.circularProgress.setProgress(millisUntilFinished,timeSong);
            }

            @Override
            public void onFinish() {
                stopRecordingVideo();
            }
        }.start();
    }

    // Kết thúc quay phim
    public void stopRecordingVideo() {
        // Kiểm tra nếu đang quay, không thì return
        if (!isRecording) {
            return;
        }

        // Dừng và giải phóng đối tượng MediaRecorder và CameraView
        binding.camera.stopVideo();
        binding.camera.clearAnimation();
        mediaPlayer.pause();

        isRecording = false;

        // Hủy bỏ đếm ngược và ẩn danh sách CameraListener để xử lý khi hoàn thành quay phim
        if (countDownTimer != null) {
        countDownTimer.cancel();
        countDownTimer = null;
        }
        binding.countdown.setVisibility(View.GONE);

        binding.camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                Uri uriVideo = null;
                if (linkAudio != null) {
                    UUID id = UUID.randomUUID();
                    String filename =  id + ".mp4";
                    File outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    File videoFile1 = new File(outputDir, filename);
                    String[] cmd = {"-i", result.getFile().getAbsolutePath(), "-i", linkAudio, "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", "-shortest",videoFile1.getAbsolutePath()};
                    FFmpeg.execute(cmd);
                    videoFile.delete();
                    uriVideo =Uri.fromFile(videoFile1);
                }else {
                    uriVideo =Uri.fromFile(videoFile);
                }
                Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                intent.putExtra("uriVideo",uriVideo);
                intent.putExtra("SongId", SongId);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            String path = getFilePathFromUri(this,selectedVideoUri);
            if(path!=null){
                File file = new File(path);
                Uri uriVideo =Uri.fromFile(file);
                Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                intent.putExtra("uriVideo",uriVideo);
                intent.putExtra("SongId", "01");
                startActivity(intent);
            }
        }
    }

    public String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    public String getRealPathFromURI(Context context, Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(column_index);
            cursor.close();
            return filePath;
        }

        return uri.getPath();
    }



    @Override
    protected void onResume() {
        super.onResume();
        binding.camera.open();

    }
    @Override
    protected void onPause() {
        super.onPause();
        binding.camera.close();
    }



}