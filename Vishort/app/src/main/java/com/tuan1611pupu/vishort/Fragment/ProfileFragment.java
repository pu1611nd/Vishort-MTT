package com.tuan1611pupu.vishort.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.tuan1611pupu.vishort.Activity.EditActivity;
import com.tuan1611pupu.vishort.Activity.LogInActivity;
import com.tuan1611pupu.vishort.Adapter.NotificationPagerAdapter;
import com.tuan1611pupu.vishort.Adapter.UserPostVideoAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Follower;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView imageProfile,edit;
    private TextView userName,bioUser,txtFollow,txtFollowing;
    private  FirebaseDatabase database;
    private ArrayList<Follower> follower;
    private ArrayList<Follower> listFollowing;
    private PreferenceManager preferenceManager;
    private User user;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        database = FirebaseDatabase.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        follower = new ArrayList<>();
        listFollowing = new ArrayList<>();
        imageProfile = view.findViewById(R.id.profile_image);
        userName =view.findViewById(R.id.username);
        bioUser = view.findViewById(R.id.bioUser1);
        txtFollow = view.findViewById(R.id.txtFollow1);
        txtFollowing = view.findViewById(R.id.txtFollowing1);
        viewPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tablayout);
        edit = view.findViewById(R.id.edit);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            edit.setOnClickListener(v->{
                Intent intent = new Intent(getContext(), EditActivity.class);
                intent.putExtra("user",user);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
            init();
            getData();
            getFollow();
        }else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        return  view;
    }

    private void init(){
        NotificationPagerAdapter mainViewPagerAdapter = new NotificationPagerAdapter(getChildFragmentManager(),getLifecycle());
        mainViewPagerAdapter.addFragment(new Fragment_myVideo());
        mainViewPagerAdapter.addFragment(new Fragment_saveVideo());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(mainViewPagerAdapter);
        tabLayout.addTab(tabLayout.newTab().setText("My Reels"));
        tabLayout.addTab(tabLayout.newTab().setText("Save Video"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private void getData() {

        DatabaseReference usersRef = database.getReference("User");
    // Đăng ký lắng nghe sự thay đổi của một tài liệu trong Realtime Database
        usersRef.child(preferenceManager.getString(Constants.KEY_USER_ID)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ snapshot và cập nhật thông tin của người dùng (User)
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Cập nhật thông tin người dùng trong ứng dụng của bạn
                        // Ví dụ: user.getUsername(), user.getImage(), user.getBio(), ...
                        //Picasso.get().load(user.getImage()).into(imageProfile);
                        Glide.with(getContext()).load(user.getImage()).into(imageProfile);
                        userName.setText(user.getUsername());
                        bioUser.setText(user.getBio());
                    }
                } else {
                    // Nếu tài liệu không tồn tại hoặc bị xóa, bạn có thể xử lý ở đây
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });

    }


    private void getFollow(){
        DataService dataService = APIService.getService();

        Call<List<Follower>> call = dataService.getFollow(preferenceManager.getString(Constants.KEY_USER_ID));
        call.enqueue(new Callback<List<Follower>>() {
            @Override
            public void onResponse(Call<List<Follower>> call, Response<List<Follower>> response) {
                if (response.isSuccessful()) {
                    follower = (ArrayList<Follower>) response.body();
                    txtFollow.setText(follower.size()+"");
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<List<Follower>> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });

        Call<List<Follower>> call2 = dataService.getFollowing(preferenceManager.getString(Constants.KEY_USER_ID));
        call2.enqueue(new Callback<List<Follower>>() {
            @Override
            public void onResponse(Call<List<Follower>> call2, Response<List<Follower>> response) {
                if (response.isSuccessful()) {
                    listFollowing = (ArrayList<Follower>) response.body();
                    txtFollowing.setText(listFollowing.size()+"");

                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<List<Follower>> call2, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });
    }
}
