package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tuan1611pupu.vishort.Adapter.UserPostVideoAdapter;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import java.util.ArrayList;

public class Fragment_myVideo extends Fragment {

    private ArrayList<Reels> list;
    private UserPostVideoAdapter userPostVideoAdapter;
    private RecyclerView rcListVideo;
    private  FirebaseDatabase database;
    private PreferenceManager preferenceManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myvideo, container, false);
        list = new ArrayList<>();
        userPostVideoAdapter = new UserPostVideoAdapter(list,getContext());
        preferenceManager = new PreferenceManager(getContext());

        database = FirebaseDatabase.getInstance();

        rcListVideo = view.findViewById(R.id.rvPostVideo);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        rcListVideo.setLayoutManager(staggeredGridLayoutManager);
        rcListVideo.setAdapter(userPostVideoAdapter);
        getListReels();
        return view;

    }

    private void getListReels(){
        list.clear();
        // Thực hiện truy vấn dữ liệu trên Realtime Database
        DatabaseReference reelsRef = database.getReference("Reels");
        reelsRef.orderByChild("reelsBy").equalTo(preferenceManager.getString(Constants.KEY_USER_ID))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            list.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Lấy dữ liệu từ snapshot và tạo đối tượng Reels
                                Reels reels = snapshot.getValue(Reels.class);
                                if (reels != null) {
                                    reels.setReelsId(snapshot.getKey()); // Lấy mã ID của reels
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

}
