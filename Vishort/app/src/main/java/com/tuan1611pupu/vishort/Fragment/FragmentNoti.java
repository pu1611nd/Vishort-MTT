package com.tuan1611pupu.vishort.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuan1611pupu.vishort.Activity.MainActivity;
import com.tuan1611pupu.vishort.Adapter.NotifycationAdapter;
import com.tuan1611pupu.vishort.Adapter.SearchAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Like;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentNoti extends Fragment {

    private RecyclerView rcListNoti;
    private ArrayList<NotificationData> list;
    private NotifycationAdapter adapter;
    private PreferenceManager preferenceManager;
    private TextView notiNull;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noti, container, false);

        rcListNoti = view.findViewById(R.id.rvNoti);
        notiNull = view.findViewById(R.id.notiNull);
        preferenceManager = new PreferenceManager(getContext());
        list = new ArrayList<>();
        adapter = new NotifycationAdapter();
        adapter.setOnItemClick(new NotifycationAdapter.OnItemUserClick() {
            @Override
            public void onClick(NotificationData noti) {
                DataService dataService = APIService.getService();
                Call<Void> call = dataService.updateNotification(noti.getNotificationId(),1,noti.getUserId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Xử lý khi cập nhật thành công
                            if(noti.getActionType().equals("follower")){
                                UserProfileFragment fragment = new UserProfileFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("userId",noti.getNotificationBy());
                                fragment.setArguments(bundle);

                                FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.container, fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }else if (noti.getActionType().equals("comment")){
                                // Đường dẫn tới bảng comment trong Realtime Database
                                DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comment");
                                commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                                            String reelId = commentSnapshot.getKey(); // Lấy reelId tương ứng với nút comment
                                            if (commentSnapshot.child(noti.getActionId()).exists()) {
                                                findReel(reelId);
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Xử lý lỗi nếu có
                                    }
                                });

                            }else {
                                DataService data = APIService.getService();
                                Call<Like> call1 = data.getLike(noti.getActionId());
                                call1.enqueue(new Callback<Like>() {
                                    @Override
                                    public void onResponse(Call<Like> call, Response<Like> response) {
                                        Like like = response.body();
                                        findReel(like.getReelsId());
                                    }

                                    @Override
                                    public void onFailure(Call<Like> call, Throwable t) {

                                    }
                                });
                            }

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
        });
        getData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcListNoti.setLayoutManager(linearLayoutManager);
        return view;
    }

    private void getData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = firebaseDatabase.getReference("Notification").child(preferenceManager.getString(Constants.KEY_USER_ID));

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Đọc dữ liệu của từng user từ DataSnapshot và chuyển đổi thành đối tượng User
                        NotificationData noti = userSnapshot.getValue(NotificationData.class);
                        list.add(noti);
                    }
                    if(list.size() != 0){
                        notiNull.setVisibility(View.GONE);
                    }
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getTimestamp(), item1.getTimestamp()));
                    adapter.addData(list);
                    rcListNoti.setAdapter(adapter);

                    // Ở đây, userList chứa danh sách các đối tượng User
                    // Tiếp tục xử lý dữ liệu theo nhu cầu của bạn
                } else {
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình truy vấn
            }
        });


    }

    private void findReel(String reelId){
        // Đường dẫn tới bảng Reels trong Realtime Database
        DatabaseReference reelsRef = FirebaseDatabase.getInstance().getReference("Reels");
        reelsRef.child(reelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Reels reel = dataSnapshot.getValue(Reels.class);
                    HomeFragment fragment = new HomeFragment();
                    // Truyền đối tượng Reel vào Fragment1
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("reel", reel);
                    fragment.setArguments(bundle);
                    // Thực hiện việc chuyển từ Fragment 4 sang Fragment 1
                    FragmentManager fragmentManager =((AppCompatActivity) getContext()).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    // Không tìm thấy dữ liệu của reel có reelId tương ứng
                    // Xử lý tình huống này nếu cần
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });

    }

}
