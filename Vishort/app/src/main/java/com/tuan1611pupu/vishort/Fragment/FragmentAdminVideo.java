package com.tuan1611pupu.vishort.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class FragmentAdminVideo extends Fragment {

    private RecyclerView rcListVideo;
    private ArrayList<Video> list;
    private VideoAdapter adapter;

    private TextView textTime;

    private Calendar dateCalendar;

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
        adapter.setOnItemClick(new VideoAdapter.OnItemUserClick() {
            @Override
            public void onClick(Video video) {

            }
        });
        dateCalendar = Calendar.getInstance();
        // Đặt giờ, phút và giây thành 0:00:00 cho thời điểm bắt đầu
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dateCalendar.set(Calendar.MINUTE, 0);
        dateCalendar.set(Calendar.SECOND, 0);

        long selectedStartTime = convertCalendarToLong(dateCalendar);

// Đặt giờ, phút và giây thành 23:59:59 cho thời điểm kết thúc
        dateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        dateCalendar.set(Calendar.MINUTE, 59);
        dateCalendar.set(Calendar.SECOND, 59);

        long selectedEndTime = convertCalendarToLong(dateCalendar);
        addVideo(selectedStartTime, selectedEndTime);
        textTime = view.findViewById(R.id.textTime);
        textTime.setText(formatCalendar(dateCalendar));
        textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiển thị DatePickerDialog cho ngày thời gian bắt đầu
                DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        // Tạo một Calendar cho ngày thời gian được chọn
                        Calendar selectedDateCalendar = Calendar.getInstance();
                        selectedDateCalendar.set(Calendar.YEAR, year);
                        selectedDateCalendar.set(Calendar.MONTH, monthOfYear);
                        selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                        // Kiểm tra xem ngày thời gian được chọn có lớn hơn ngày hiện tại hay không
                        if (selectedDateCalendar.after(Calendar.getInstance())) {
                            // Ngày thời gian được chọn lớn hơn ngày hiện tại, hiển thị thông báo lỗi
                            PreferenceManager.showToast(getContext(), Validation.ERR0_TIME_NOW);
                        } else {
                            // Ngày thời gian hợp lệ, cập nhật TextView và biến dateCalendar
                            dateCalendar = selectedDateCalendar;
                            textTime.setText(formatCalendar(dateCalendar));
                            // Đặt giờ, phút và giây thành 0:00:00 cho thời điểm bắt đầu
                            dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            dateCalendar.set(Calendar.MINUTE, 0);
                            dateCalendar.set(Calendar.SECOND, 0);

                            long selectedStartTime = convertCalendarToLong(dateCalendar);

                            // Đặt giờ, phút và giây thành 23:59:59 cho thời điểm kết thúc
                            dateCalendar.set(Calendar.HOUR_OF_DAY, 23);
                            dateCalendar.set(Calendar.MINUTE, 59);
                            dateCalendar.set(Calendar.SECOND, 59);

                            long selectedEndTime = convertCalendarToLong(dateCalendar);
                            Log.d("Time",selectedStartTime +" "+ selectedEndTime +"");
                            addVideo(selectedStartTime,selectedEndTime);
                        }

                    }
                }, dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH));
                dpd.show(getFragmentManager(), "DatePickerDialog");
            }
        });

        return view;
    }

    private void addVideo(long timeStart , long timeEnd) {
        DataService dataService = APIService.getService();
        Call<List<Video>> call = dataService.getListVideo(timeStart,timeEnd);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                if (response.isSuccessful()) {
                    list.clear();
                    adapter.clear();
                    ArrayList<Video> listVideo = new ArrayList<>();
                    listVideo = (ArrayList<Video>) response.body();
                    list.addAll(listVideo);
                    Log.d("Time123",list.size() +"");
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

    private String formatCalendar(Calendar calendar) {
        return String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }
    // Phương thức chuyển đổi Calendar thành double
    private static long convertCalendarToLong(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

}
