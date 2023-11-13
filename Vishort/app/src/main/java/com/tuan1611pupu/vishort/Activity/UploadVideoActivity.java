package com.tuan1611pupu.vishort.Activity;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.Utilities.Validation;
import com.tuan1611pupu.vishort.databinding.ActivityUploadVideoBinding;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadVideoActivity extends AppCompatActivity {

    private ActivityUploadVideoBinding binding;
    private FirebaseStorage storage;
    private ProgressDialog dialog;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        Uri videoUri = intent.getParcelableExtra("uriVideo");
        String SongId = intent.getStringExtra("SongId");
        binding.back.setOnClickListener(v -> {
            onBackPressed();
        });

        UUID uuid = UUID.randomUUID();
        File thumbnailFile = new File(getCacheDir(), uuid + "image.png");
        String thumbnailPath = thumbnailFile.getAbsolutePath();
        String[] cmd = {"-i", videoUri.toString(), "-ss", "00:00:01", "-frames:v", "1", thumbnailPath};
        FFmpeg.executeAsync(cmd, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    // Xử lý khi lấy được thành công hình ảnh đại diện của video trực tuyến
                    Glide.with(binding.getRoot()).load(thumbnailFile).into(binding.imageview);
                } else {
                    // Xử lý khi có lỗi xảy ra

                }
            }
        });

        storage = FirebaseStorage.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        dialog = new ProgressDialog(UploadVideoActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please wait .....");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        binding.textPost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String description = binding.textPost.getText().toString().trim();
                if (!description.isEmpty()) {
                    binding.post.setVisibility(View.VISIBLE);
                } else {
                    binding.post.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.post.setOnClickListener(v -> {
            // Sử dụng MediaMetadataRetriever để lấy thông tin về video
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);

            // Lấy thời lượng video (đơn vị: milliseconds)
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMillis = Long.parseLong(duration);

            // Chuyển đổi thời lượng thành giây
            float totalSeconds = durationMillis / 1000.0f;

            // Kiểm tra nếu thời gian video lớn hơn 90 giây
            if (totalSeconds > 95) {
                PreferenceManager.showToast(getApplicationContext(), Validation.VALIDATION_VIDEO_TIME);
                // Thêm mã để hiển thị thông báo ở đây
            } else {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    dialog.show();
                    postVideo(videoUri, SongId);
                } else {
                    Intent intent1 = new Intent(getApplicationContext(), LogInActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                }
            }
        });

    }

    public Uri convertSystemUriToMediaUri(Context context, Uri systemUri) {
        String systemPath = systemUri.getPath(); // Lấy phần path từ Uri hệ thống
        Uri mediaUri = Uri.parse("content://" + systemPath); // Tạo Uri mới với scheme "content"
        return mediaUri;
    }


    private void postVideo(Uri uri, String SongId) {
        final StorageReference reference = storage.getReference().child("videos")
                .child(preferenceManager.getString(Constants.KEY_USER_ID))
                .child(new Date().getTime() + "");
        reference.putFile(uri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri1 -> {
            Reels reel = new Reels();
            reel.setVideo(uri1.toString());
            reel.setCaption(binding.textPost.getText().toString().trim());
            reel.setComments(0);
            reel.setLikes(0);
            reel.setSaves(0);
            reel.setReelsBy(preferenceManager.getString(Constants.KEY_USER_ID));
            reel.setReelsAt(new Date().getTime());
            reel.setSongId(SongId);
            Log.d("songId", reel.getSongId() + " ");
            UUID uuid = UUID.randomUUID();
            reel.setReelsId(uuid.toString());

            DataService dataService = APIService.getService();
            Call<Void> call = dataService.addReel(reel);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Xử lý khi cập nhật thành công
                        dialog.dismiss();
                        PreferenceManager.showToast(getApplicationContext(),Validation.POST_VIDEO_SUCCESS);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Xử lý lỗi khi yêu cầu thất bại
                        PreferenceManager.showToast(getApplicationContext(),Validation.POST_VIDEO_FAIL);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Xử lý lỗi khi yêu cầu thất bại
                    PreferenceManager.showToast(getApplicationContext(),Validation.POST_VIDEO_FAIL);
                }
            });


        }));


    }


}