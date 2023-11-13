package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.Utilities.Validation;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAdminStatistical extends Fragment {

    private RecyclerView rcListVideo;
    private ArrayList<Video> list;
    private ArrayList<Video> listNew;
    private VideoAdapter adapter;

    private TextView startDateText;
    private TextView endDateText;

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    private Button buttonLike;
    private Button buttonComment;
    private Button buttonSave;

    private long starTime;
    private long endTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistical, container, false);

        rcListVideo = view.findViewById(R.id.rvListVideo);
        list = new ArrayList<>();
        listNew = new ArrayList<>();
        adapter = new VideoAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcListVideo.setLayoutManager(linearLayoutManager);
        rcListVideo.setAdapter(adapter);

        adapter.setOnItemClick(new VideoAdapter.OnItemUserClick() {
            @Override
            public void onClick(Video video) {

            }
        });

        buttonLike = view.findViewById(R.id.buttonLike);
        buttonComment = view.findViewById(R.id.buttonComment);
        buttonSave = view.findViewById(R.id.buttonSave);


        startDateText = view.findViewById(R.id.startDateText);
        endDateText = view.findViewById(R.id.endDateText);
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        startDateText.setText(formatCalendar(startDateCalendar));
        // Đặt giờ, phút và giây thành 0:00:00 cho thời điểm bắt đầu
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);
        starTime = convertCalendarToLong(startDateCalendar);

        endDateText.setText(formatCalendar(endDateCalendar));
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        endTime = convertCalendarToLong(endDateCalendar);

        addVideo(starTime,endTime);

        buttonLike.setOnClickListener(v->{
            listNew.addAll(list);
            Collections.sort(listNew, (item1, item2) -> Long.compare(item2.getLikes(), item1.getLikes()));
            adapter.clear();
            adapter.addData(listNew);
        });
        buttonComment.setOnClickListener(v->{
            listNew.addAll(list);
            Collections.sort(listNew, (item1, item2) -> Long.compare(item2.getComments(), item1.getComments()));
            adapter.clear();
            adapter.addData(listNew);
        });
        buttonSave.setOnClickListener(v->{
            listNew.addAll(list);
            Collections.sort(listNew, (item1, item2) -> Long.compare(item2.getSaves(), item1.getSaves()));
            adapter.clear();
            adapter.addData(listNew);
        });

        startDateText.setOnClickListener(view1 -> {
            // Hiển thị DatePickerDialog cho ngày thời gian bắt đầu
            DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view1, int year, int monthOfYear, int dayOfMonth) {
                    startDateCalendar.set(year, monthOfYear, dayOfMonth);
                    // Kiểm tra tính hợp lệ của ngày thời gian bắt đầu
                    if (startDateCalendar.compareTo(endDateCalendar) > 0) {
                        PreferenceManager.showToast(getContext(),Validation.ERR0_TIME_BEGIN);
                    }else if(startDateCalendar.after(Calendar.getInstance())){
                        PreferenceManager.showToast(getContext(),Validation.ERR0_TIME_NOW);
                    }
                    else {
                        startDateText.setText(formatCalendar(startDateCalendar));
                        starTime = convertCalendarToLong(startDateCalendar);
                        addVideo(starTime,endTime);
                    }
                }
            }, startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.DAY_OF_MONTH));
            dpd.show(getFragmentManager(), "StartDatePickerDialog");
        });

        endDateText.setOnClickListener(view12 -> {
            // Hiển thị DatePickerDialog cho ngày thời gian kết thúc
            DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view12, int year, int monthOfYear, int dayOfMonth) {
                    endDateCalendar.set(year, monthOfYear, dayOfMonth);
                    // Kiểm tra tính hợp lệ của ngày thời gian kết thúc
                    if (endDateCalendar.compareTo(startDateCalendar) < 0) {
                        PreferenceManager.showToast(getContext(),Validation.ERR0_TIME_END);
                    }else if(endDateCalendar.after(Calendar.getInstance())){
                        PreferenceManager.showToast(getContext(),Validation.ERR0_TIME_NOW);
                    }
                    else {
                        endDateText.setText(formatCalendar(endDateCalendar));
                        endTime = convertCalendarToLong(endDateCalendar);
                        addVideo(starTime,endTime);
                    }
                }
            }, endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.DAY_OF_MONTH));
            dpd.show(getFragmentManager(), "EndDatePickerDialog");
        });

        return view;
    }




    private String formatCalendar(Calendar calendar) {
        return String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void addVideo(long timeStart , long timeEnd) {
        DataService dataService = APIService.getService();
        Call<List<Video>> call = dataService.getListVideo(timeStart , timeEnd);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                if (response.isSuccessful()) {
                    list.clear();
                    adapter.clear();
                    ArrayList<Video> listVideo = new ArrayList<>();
                    listVideo = (ArrayList<Video>) response.body();
                    list.addAll(listVideo);
                    adapter.addData(list);


                } else {
                    // loi
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {

            }
        });

    }

    // Phương thức chuyển đổi Calendar thành double
    private static long convertCalendarToLong(Calendar calendar) {
        return calendar.getTimeInMillis();
    }


}
