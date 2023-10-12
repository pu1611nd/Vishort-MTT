package com.tuan1611pupu.vishort.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.tuan1611pupu.vishort.Activity.ChatActivity;
import com.tuan1611pupu.vishort.Activity.EditActivity;
import com.tuan1611pupu.vishort.Activity.LogInActivity;
import com.tuan1611pupu.vishort.Activity.MainActivity;
import com.tuan1611pupu.vishort.Adapter.UserPostVideoAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Follower;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.Save;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private PreferenceManager preferenceManager;
    private String userId;
    private User user;
    private ArrayList<Reels> list;
    private ArrayList<Follower> follower;
    private ArrayList<Follower> listFollowing;
    private UserPostVideoAdapter userPostVideoAdapter;

    private RecyclerView rvPostVideo;
    private ImageView back,profilePic,coverImag;
    private RelativeLayout message;
    private Button follow;

    Boolean following = false;
    Boolean followed = false;

    private TextView username,bioUser,txtFollow,txtFollowing;

    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_profile,container,false);
        preferenceManager = new PreferenceManager(getContext());
        database = FirebaseDatabase.getInstance();
        list = new ArrayList<>();
        follower = new ArrayList<>();
        listFollowing = new ArrayList<>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            // Lấy đối tượng Reel từ Bundle
            userId = (String) bundle.getSerializable("userId");
            // Sử dụng đối tượng Reel ở đây
        }

        rvPostVideo = view.findViewById(R.id.rvPostVideo);
        back = view.findViewById(R.id.back);
        message = view.findViewById(R.id.message);
        profilePic = view.findViewById(R.id.profilePic);
        coverImag = view.findViewById(R.id.coverImag);
        username = view.findViewById(R.id.username);
        bioUser = view.findViewById(R.id.bioUser);
        follow = view.findViewById(R.id.btn_follow);
        txtFollow = view.findViewById(R.id.txtFollow);
        txtFollowing = view.findViewById(R.id.txtFollowing);
        userPostVideoAdapter = new UserPostVideoAdapter(list,getContext());
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        rvPostVideo.setLayoutManager(staggeredGridLayoutManager);
        rvPostVideo.setAdapter(userPostVideoAdapter);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            message.setOnClickListener(v->{
                Intent intent1 = new Intent(getContext(), ChatActivity.class);
                intent1.putExtra(Constants.KEY_USER, user);
                startActivity(intent1);
            });
            follow.setOnClickListener(v -> follow());
            checkFollow(userId);

        }

        back.setOnClickListener(v->{
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.popBackStack();

        });

        getUser();
        getListReels();
        getFollow();
        return view;
    }


    private void getUser(){

        DatabaseReference usersRef = database.getReference("User");

        // Đăng ký lắng nghe sự thay đổi của một tài liệu trong Realtime Database
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy dữ liệu từ snapshot và cập nhật thông tin của người dùng (User)
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Cập nhật thông tin người dùng trong ứng dụng của bạn
                        // Ví dụ: user.getUsername(), user.getImage(), user.getBio(), ...
                        Glide.with(getContext()).load(user.getImage()).into(profilePic);
                        Glide.with(getContext()).load(user.getImage_cover()).into(coverImag);
                        username.setText(user.getUsername());
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


    private void getListReels() {

        // Thực hiện truy vấn dữ liệu trên Realtime Database
        DatabaseReference reelsRef = database.getReference("Reels");
        reelsRef.orderByChild("reelsBy").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            list.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Reels reels = snapshot.getValue(Reels.class);
                                if (reels != null) {
                                    reels.setReelsId(snapshot.getKey());
                                    list.add(reels);
                                }
                            }
                            userPostVideoAdapter.notifyDataSetChanged();
                        } else {
                            // Không tìm thấy dữ liệu hoặc không có kết quả phù hợp
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                    }
                });
    }

    private void getFollow(){
        DataService dataService = APIService.getService();

        Call<List<Follower>> call = dataService.getFollow(userId);
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

        Call<List<Follower>> call2 = dataService.getFollowing(userId);
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

    private void follow(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (following) {
                DataService dataService = APIService.getService();
                Call<Void> call = dataService.deleteFollow(preferenceManager.getString(Constants.KEY_USER_ID),userId);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            follow.setText("Follow");
                            follow.setBackgroundResource(R.drawable.shape_lightblue);

                        } else {
                            // Xử lý lỗi khi yêu cầu thất bại

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Xử lý lỗi khi yêu cầu thất bại
                    }
                });


            } else {

                Follower follower = new Follower();
                UUID uuid = UUID.randomUUID();
                follower.setFollowerId(uuid.toString());
                follower.setUserId(preferenceManager.getString(Constants.KEY_USER_ID));
                follower.setFollowerUserId(userId);

                if(followed){
                    DataService dataService = APIService.getService();
                    Call<Void> call = dataService.addFollow(follower);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                follow.setText("Following");
                                follow.setBackgroundResource(R.drawable.following);

                            } else {
                                // Xử lý lỗi khi yêu cầu thất bại

                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Xử lý lỗi khi yêu cầu thất bại
                        }
                    });
                }else {
                    DataService dataService = APIService.getService();
                    Call<Void> call = dataService.addFollow(follower);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                follow.setText("Following");
                                follow.setBackgroundResource(R.drawable.following);
                                addNotification(uuid.toString());

                            } else {
                                // Xử lý lỗi khi yêu cầu thất bại

                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Xử lý lỗi khi yêu cầu thất bại
                        }
                    });
                }



            }
        }else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }


    private void checkFollow(String userId) {
        DatabaseReference followRef = FirebaseDatabase.getInstance()
                .getReference("Follow")
                .child(userId);

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isFollowing = dataSnapshot.child(preferenceManager.getString(Constants.KEY_USER_ID)).getValue(Boolean.class);
                    if (isFollowing != null) {
                        followed = true;
                        if (isFollowing) {
                            // userIdToCheck có giá trị true trong danh sách like
                            // Thực hiện các hành động khi người dùng đã like
                            follow.setText("Following");
                            follow.setBackgroundResource(R.drawable.following);
                            following = true;
                        } else {
                            // userIdToCheck có giá trị false trong danh sách like
                            // Thực hiện các hành động khi người dùng đã unlike
                            follow.setText("Follow");
                            follow.setBackgroundResource(R.drawable.shape_lightblue);
                            following = false;
                        }
                    } else {
                        // userIdToCheck không tồn tại trong danh sách like hoặc có giá trị null
                        // Thực hiện các hành động khi không tìm thấy userId
                        follow.setText("Follow");
                        follow.setBackgroundResource(R.drawable.shape_lightblue);
                        following = false;
                        followed = false;
                    }
                } else {
                    // Node LikeId_1 không tồn tại hoặc không có dữ liệu
                    // Thực hiện các hành động khi không tìm thấy node
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void addNotification(String followId){
        NotificationData notificationData = new NotificationData();
        UUID uuid = UUID.randomUUID();
        notificationData.setNotificationId(uuid.toString());
        notificationData.setUserId(userId);
        notificationData.setActionId(followId);
        notificationData.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
        notificationData.setIsRead(0);
        notificationData.setTimestamp(new Date().getTime());
        notificationData.setActionType("follower");

        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addNotification(notificationData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // gui thong bao
                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });

    }


    }