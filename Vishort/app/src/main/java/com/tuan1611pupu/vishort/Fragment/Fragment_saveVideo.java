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

import com.tuan1611pupu.vishort.Adapter.UserPostVideoAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_saveVideo extends Fragment {

    private ArrayList<Reels> list;
    private UserPostVideoAdapter userPostVideoAdapter;
    private RecyclerView rcListVideo;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savevideo, container, false);

        list = new ArrayList<>();
        userPostVideoAdapter = new UserPostVideoAdapter(list,getContext());
        preferenceManager = new PreferenceManager(getContext());

        rcListVideo = view.findViewById(R.id.rvSaveVideo);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        rcListVideo.setLayoutManager(staggeredGridLayoutManager);
        rcListVideo.setAdapter(userPostVideoAdapter);
        getListReels();

        return view;
    }


    private void getListReels(){
        list.clear();
        DataService dataService = APIService.getService();
        Call<List<Reels>> call = dataService.getSaveVideo(preferenceManager.getString(Constants.KEY_USER_ID));
        call.enqueue(new Callback<List<Reels>>() {
            @Override
            public void onResponse(Call<List<Reels>> call, Response<List<Reels>> response) {
                if (response.isSuccessful()) {
                    List<Reels> newReelsList = response.body();
                    if (newReelsList != null) {
                        list.clear(); // Xóa danh sách cũ
                        list.addAll(newReelsList); // Thêm danh sách mới
                        userPostVideoAdapter.notifyDataSetChanged(); // Cập nhật adapter
                    } else {
                        // Xử lý trường hợp dữ liệu null
                    }

                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<List<Reels>> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });
    }
}
