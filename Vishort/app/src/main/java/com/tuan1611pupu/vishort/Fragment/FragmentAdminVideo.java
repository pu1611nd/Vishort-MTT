package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuan1611pupu.vishort.Adapter.VideoAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Video;
import com.tuan1611pupu.vishort.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAdminVideo extends Fragment {

    private RecyclerView rcListVideo;
    private ArrayList<Video> list;
    private VideoAdapter adapter;
    private Spinner spinnerStatus;
    private ArrayAdapter statusAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_video, container, false);
        rcListVideo = view.findViewById(R.id.rvListVideo);
        list = new ArrayList<>();
        adapter = new VideoAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcListVideo.setLayoutManager(linearLayoutManager);
        rcListVideo.setAdapter(adapter);
        spinnerStatus = view.findViewById(R.id.status_spinner);
        List<String> options = new ArrayList<>();
        options.add("Tất cả");
        options.add("Theo số like");
        options.add("Theo số comment");
        options.add("Theo số save");

        statusAdapter = new ArrayAdapter(getContext(), R.layout.style_spinner, options);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = options.get(position);
                if (list == null) {
                    return; // Avoid processing if list is null
                }
                adapter.clear();
                if (selectedOption.equals("Tất cả")) {
                    addVideo();
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getReelsAt(), item1.getReelsAt()));

                } else if (selectedOption.equals("Theo số like")) {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getLikes(), item1.getLikes()));

                } else if (selectedOption.equals("Theo số comment")) {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getComments(), item1.getComments()));

                } else {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getSaves(), item1.getSaves()));

                }
                adapter.addData(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không có mục nào được chọn
            }
        });

        adapter.setOnItemClick(new VideoAdapter.OnItemUserClick() {
            @Override
            public void onClick(Video video) {

            }
        });

        return view;
    }

    private void addVideo() {
        DataService dataService = APIService.getService();
        Call<List<Video>> call = dataService.getListVideo();
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                if (response.isSuccessful()) {
                    list.clear();
                    ArrayList<Video> listVideo = new ArrayList<>();
                    listVideo = (ArrayList<Video>) response.body();
                    list.addAll(listVideo);
                    adapter.addData(list);


                } else {
                    // loi
                    Log.d("VALIDATION_ADMIN","VALIDATION_ADMIN_THONGKE1");
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {

            }
        });

    }

}
