package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuan1611pupu.vishort.Adapter.UserAdapter;
import com.tuan1611pupu.vishort.Adapter.VideoAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.User3;
import com.tuan1611pupu.vishort.Model.Video;
import com.tuan1611pupu.vishort.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAdminUser extends Fragment {

    private RecyclerView rcListUser;
    private ArrayList<User3> list;
    private UserAdapter adapter;
    private TextView textNoti;

    private Spinner spinnerStatus;
    private ArrayAdapter statusAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user, container, false);
        rcListUser = view.findViewById(R.id.rvUser);
        textNoti = view.findViewById(R.id.textList);
        list = new ArrayList<>();
        adapter = new UserAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcListUser.setLayoutManager(linearLayoutManager);
        rcListUser.setAdapter(adapter);

        spinnerStatus = view.findViewById(R.id.status_spinner);
        List<String> options = new ArrayList<>();
        options.add("Tất cả");
        options.add("Theo số follow");
        options.add("Theo số following");
        options.add("Theo số video");

        statusAdapter = new ArrayAdapter(getContext(),R.layout.style_spinner,options);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = options.get(position);

                if (list == null) {
                    return; // Avoid processing if list is null
                }
                    adapter.clear(); // Clear old data before applying new sorting

                if (selectedOption.equals("Tất cả")) {
                    addUser();
                } else if (selectedOption.equals("Theo số follow")) {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getFollowsCount(), item1.getFollowsCount()));
                } else if (selectedOption.equals("Theo số following")) {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getFolloweringCount(), item1.getFolloweringCount()));
                } else {
                    Collections.sort(list, (item1, item2) -> Long.compare(item2.getReelsCount(), item1.getReelsCount()));
                }

                adapter.addData(list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không có mục nào được chọn
            }
        });


        adapter.setOnItemClick(new UserAdapter.OnItemUserClick() {
            @Override
            public void onClick(User3 user) {

            }
        });
        return view;
    }

    private void addUser() {
        DataService dataService = APIService.getService();
        Call<List<User3>> call = dataService.getListUser();
        call.enqueue(new Callback<List<User3>>() {
            @Override
            public void onResponse(Call<List<User3>> call, Response<List<User3>> response) {
                if(response.isSuccessful())
                {
                    list.clear();
                    ArrayList<User3> listUser = new ArrayList<>();
                    listUser = (ArrayList<User3>) response.body();
                    list.addAll(listUser);
                    adapter.addData(list);
                    if(list.size() == 0){
                        rcListUser.setVisibility(View.GONE);
                        textNoti.setVisibility(View.VISIBLE);
                    }


                }else {
                    // loi
                }
            }

            @Override
            public void onFailure(Call<List<User3>> call, Throwable t) {

            }
        });

    }
}
