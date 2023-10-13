package com.tuan1611pupu.vishort.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.databinding.ActivityLoginWithPhoneBinding;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginWithPhoneActivity extends AppCompatActivity {
    private ActivityLoginWithPhoneBinding binding;

    private String verificationId1;
    private PreferenceManager preferenceManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    private String linkImage = "https://firebasestorage.googleapis.com/v0/b/vishort-ef364.appspot.com/o/image%2Fuser.png?alt=media&token=acb88c4a-34fb-45a2-9bf8-0345a1b8a8da";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginWithPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        sendOTP();

    }


    private void sendOTP(){
        binding.buttonGetOTP.setOnClickListener(v -> {
            if(binding.inputMobile.getText().toString().trim().isEmpty()){
                Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                Log.d("Message_Phone","Message_Phone_erro");
                return;
            }else {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.buttonGetOTP.setVisibility(View.INVISIBLE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+84"+binding.inputMobile.getText().toString(),
                        60,
                        TimeUnit.SECONDS,
                        LoginWithPhoneActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.buttonGetOTP.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("aaaa",e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                GetOTP(binding.inputMobile.getText().toString(),verificationId);
                            }
                        }

                );

            }
        });
    }

    private void LogIn(FirebaseUser user) {

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputMobile.getText().toString())
                .whereEqualTo(Constants.KEY_PROVIDER,Constants.PROVIDER_PHONE)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,user.getUid());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                       SignIn(user);
                    }
                });
    }

    private void SignIn(FirebaseUser user){
        User user1 = new User();
        user1.setId(user.getUid());
        user1.setUsername("user" + user.getPhoneNumber());
        user1.setEmail(user.getPhoneNumber());
        user1.setImage(linkImage);
        user1.setProvider(Constants.PROVIDER_PHONE);
        user1.setImage_cover(Constants.IMAGE_COVER);
        user1.setBio(" ");

        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addUser(user1);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại
                    try {
                        String errorBody = response.errorBody().string();
                        String code = String.valueOf(response.code());
                        // Xử lý thông tin lỗi ở đây
                        Toast.makeText(getApplicationContext(), "them user that bai", Toast.LENGTH_SHORT).show();
                        Log.d("Akkkk",errorBody+code);
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

    private void GetOTP(String mobile,String verificationId){

        verificationId1 = verificationId;

        setupOTPInput();

        binding.buttonverify.setOnClickListener(v ->{
            if(binding.inputCode1.getText().toString().trim().isEmpty() ||
                    binding.inputCode2.getText().toString().trim().isEmpty() ||
                    binding.inputCode3.getText().toString().trim().isEmpty() ||
                    binding.inputCode4.getText().toString().trim().isEmpty() ||
                    binding.inputCode5.getText().toString().trim().isEmpty() ||
                    binding.inputCode6.getText().toString().trim().isEmpty() ){
                Toast.makeText(getApplicationContext(), "nhap du code", Toast.LENGTH_SHORT).show();
                return;
            }
            String code = binding.inputCode1.getText().toString() +
                    binding.inputCode2.getText().toString() +
                    binding.inputCode3.getText().toString() +
                    binding.inputCode4.getText().toString() +
                    binding.inputCode5.getText().toString() +
                    binding.inputCode6.getText().toString() ;
            if(verificationId1 != null) {
                binding.progressBar2.setVisibility(View.VISIBLE);
                binding.buttonverify.setVisibility(View.INVISIBLE);
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                        verificationId1,
                        code
                );
                firebaseAuth.signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(task -> {
                            binding.progressBar2.setVisibility(View.GONE);
                            binding.buttonverify.setVisibility(View.VISIBLE);
                            if(task.isSuccessful()){
                                FirebaseUser user = task.getResult().getUser();
                                // kiem tra xem co dang ky chua neu chua thi hien man hinh dang ky
                                LogIn(user);
                            }else{
                                Toast.makeText(getApplicationContext(), "code khong dung", Toast.LENGTH_SHORT).show();
                                Log.d("Message_OTP","Message_OTP_Info");
                            }
                        });

            }

        });
        binding.textResendOTP.setOnClickListener(v ->{
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+84"+mobile,
                    60,
                    TimeUnit.SECONDS,
                    LoginWithPhoneActivity.this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("Message_Resend","Message_Resend_Info");
                        }

                        @Override
                        public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            verificationId1 = newVerificationId;
                            Toast.makeText(getApplicationContext(), "OTP sent", Toast.LENGTH_SHORT).show();
                        }
                    }

            );
        });


    }





    //////////
    private void setupOTPInput(){
        binding.inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}
