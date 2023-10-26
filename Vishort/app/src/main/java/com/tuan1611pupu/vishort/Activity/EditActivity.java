package com.tuan1611pupu.vishort.Activity;

import static android.provider.MediaStore.MediaColumns.DATA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.Utilities.Validation;
import com.tuan1611pupu.vishort.databinding.ActivityEditBinding;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditActivity extends AppCompatActivity {

    private ActivityEditBinding binding;
    private ProgressDialog dialog;

    static final int GALLERY_COVER_CODE = 1002;
    static final int PERMISSION_REQUEST_CODE = 101;
    static final int GALLERY_CODE = 1001;
    Uri selectedImage;
    String picturePath;
    Uri selectedCoverImage;
    User user;
    String image = "";
    String image_cover = "";
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(EditActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please wait .....");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");

        storage = FirebaseStorage.getInstance();

        iniView();
    }
    private void iniView() {
        openEditSheet();

        binding.coverImag.setVisibility(View.VISIBLE);
        binding.coverLay.setOnClickListener(v -> chooseCoverPhoto());

        Glide.with(this).load(user.getImage_cover()).into(binding.coverImag);
        Glide.with(this).load(user.getImage()).into(binding.userImg);
        binding.etEmail.setText(user.getUsername());
        binding.etBio.setText(user.getBio());

        binding.close.setOnClickListener(v -> finish());
        binding.done.setOnClickListener(v -> {
            dialog.show();
            if (selectedImage != null && selectedCoverImage == null) {
                // luu
                final StorageReference reference = storage.getReference().child("avatar").child(user.getId());
                reference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                image = uri.toString();
                                saveData(image,user.getImage_cover());
                            }
                        });
                    }
                });
            }
            if(selectedCoverImage != null && selectedImage == null){
                final StorageReference reference = storage.getReference().child("cover_photo").child(user.getId());
                reference.putFile(selectedCoverImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                image_cover = uri.toString();
                                saveData(user.getImage(),image_cover);
                            }
                        });
                    }
                });
            }
            if(selectedImage != null && selectedCoverImage != null){
                final StorageReference reference = storage.getReference().child("avatar").child(user.getId());
                reference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                image = uri.toString();
                                final StorageReference reference = storage.getReference().child("cover_photo").child(user.getId());
                                reference.putFile(selectedCoverImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                image_cover = uri.toString();
                                                saveData(image,image_cover);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
            if(selectedImage == null && selectedCoverImage == null){
                saveData(user.getImage(),user.getImage_cover());
            }

        });
    }

    private void saveData (String image , String image_cover){
        user.setImage_cover(image_cover);
        user.setImage(image);
        user.setBio(binding.etBio.getText().toString().trim());
        user.setUsername(binding.etEmail.getText().toString().trim());
        DataService dataService = APIService.getService();
        Call<Void> call = dataService.editUser(user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    finish();
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại
                    try {
                        String errorBody = response.errorBody().string();
                        String code = String.valueOf(response.code());
                        // Xử lý thông tin lỗi ở đây
                        PreferenceManager.showToast(getApplicationContext(), Validation.SAVE_INFO_FAIL);
                    } catch (IOException e) {

                        e.printStackTrace();

                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });

    }


    private void openEditSheet() {
        binding.lytimg.setOnClickListener(v -> choosePhoto());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {

            selectedImage = data.getData();

            Log.d("TAG", "onActivityResult: " + selectedImage);

            Glide.with(this).load(selectedImage).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(false).into(binding.userImg);

            String[] filePathColumn = {DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            Log.d("TAG", "picpath:2 " + picturePath);
            Log.d("TAG", "onActivityResultpicpath: " + picturePath);
        }

        if (requestCode == GALLERY_COVER_CODE && resultCode == RESULT_OK && null != data) {
            binding.coverLay.setVisibility(View.GONE);

            selectedCoverImage = data.getData();
            Log.d("TAG", "onActivityResult: " + selectedCoverImage);
            Glide.with(this).load(selectedCoverImage).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(false).into(binding.coverImag);

            String[] filePathColumn = {DATA};

            Cursor cursor = getContentResolver().query(selectedCoverImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String pictureCoverPath = cursor.getString(columnIndex);
            cursor.close();

            Log.d("TAG", "picpath:2 " + pictureCoverPath);
            Log.d("TAG", "onActivityResultpicpath: " + pictureCoverPath);
        }

    }


    private void chooseCoverPhoto() {
        if (checkPermission()) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_COVER_CODE);
        } else {
            requestPermission();
        }
    }

    private void choosePhoto() {

        if (checkPermission()) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_CODE);
        } else {
            requestPermission();
        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PreferenceManager.showToast(getApplicationContext(),Validation.PERMISSION_APP_SETTING);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


}