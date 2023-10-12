package com.tuan1611pupu.vishort.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.iammert.library.readablebottombar.ReadableBottomBar;
import com.tuan1611pupu.vishort.Fragment.HomeFragment;
import com.tuan1611pupu.vishort.Fragment.NotificationFragment;
import com.tuan1611pupu.vishort.Fragment.ProfileFragment;
import com.tuan1611pupu.vishort.Fragment.SearchUserFragment;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(getResources().getColor(R.color.black_main));
        }

        preferenceManager = new PreferenceManager(getApplicationContext());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getToken();

        }else {
            preferenceManager.putString(Constants.KEY_USER_ID, "");
        }
        setSupportActionBar(binding.toolbar);
        MainActivity.this.setTitle("My Profile");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        binding.toolbar.setVisibility(View.GONE);
        transaction.replace(R.id.container,new HomeFragment());
        transaction.commit();

        binding.readableBottomBar.setOnItemSelectListener(i -> {
            FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();

            switch (i){
                case 0:
                    binding.toolbar.setVisibility(View.GONE);
                    transaction1.replace(R.id.container,new HomeFragment());
                    break;
                case 1:
                    binding.toolbar.setVisibility(View.GONE);
                    transaction1.replace(R.id.container,new SearchUserFragment());
                    break;
                case 2:
                    binding.toolbar.setVisibility(View.GONE);
                    Intent intent = new Intent(getApplicationContext(),RecorderActivity.class);
                    startActivity(intent);
                    break;
                case 3:
                    binding.toolbar.setVisibility(View.GONE);
                    transaction1.replace(R.id.container,new NotificationFragment());
                    break;
                case 4:
                    binding.toolbar.setVisibility(View.VISIBLE);
                    transaction1.replace(R.id.container,new ProfileFragment());
                    break;
            }
            transaction1.commit();
        });

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.ic_setting) {
                    Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                    startActivity(intent);
                    return true;
                }else {

                    return false;
                }
            }

        });
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        Log.d("token",token+preferenceManager.getString(Constants.KEY_USER_ID));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("User");
        usersRef.child(preferenceManager.getString(Constants.KEY_USER_ID)).child(Constants.KEY_FCM_TOKEN).setValue(token)
                .addOnSuccessListener(aVoid -> {
                    //showMessage("them token thanh cong");
                })
                .addOnFailureListener(e -> showMessage("them token that bai"));


    }

    private void showMessage (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);

        return true;
    }

}