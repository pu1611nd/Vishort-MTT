package com.tuan1611pupu.vishort.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.databinding.ActivitySettingBinding;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        logout();

        binding.back.setOnClickListener(v->{
            onBackPressed();
            finish();
        });
    }


    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        binding.logout.setOnClickListener(v -> {
            showMessage("Signing out ....");

            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.KEY_COLLECTION_USERS);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                DatabaseReference currentUserRef = usersReference.child(currentUser.getUid());

                currentUserRef.child(Constants.KEY_FCM_TOKEN).setValue(null)
                        .addOnSuccessListener(aVoid -> {
                            FirebaseAuth.getInstance().signOut();
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .build();
                            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                            googleSignInClient.signOut().addOnCompleteListener(task -> {
                                // Xử lý sau khi đăng xuất khỏi Google thành công
                            });

                            LoginManager.getInstance().logOut();
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "loi dang xuat", Toast.LENGTH_SHORT).show());
            }
        });
    }




}