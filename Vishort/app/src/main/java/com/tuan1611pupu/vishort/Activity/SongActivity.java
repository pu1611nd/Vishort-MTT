package com.tuan1611pupu.vishort.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tuan1611pupu.vishort.Adapter.SongsAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Song;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.Utilities.Validation;
import com.tuan1611pupu.vishort.databinding.ActivitySongBinding;
import com.tuan1611pupu.vishort.workers.FileDownloadWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.changer.audiowife.AudioWife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongActivity extends AppCompatActivity {
    private static final String TAG = "SongActivity";
    private ActivitySongBinding binding;
    private SongsAdapter songsAdapter ;
    private ProgressBar progressBar;
    private BottomSheetBehavior<View> bsb;

    private ImageView play,pause,use;
    private TextView start, end,songName;
    private SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        songsAdapter = new SongsAdapter();

        songsAdapter.setOnSongClickListner(song -> {
            binding.progressbar.setVisibility(View.VISIBLE);
            downloadSelectedSong(song);
            binding.browse.setVisibility(View.GONE);
        });

        binding.rvSongs.setAdapter(songsAdapter);
        initView();

        addSong();


    }

    private void addSong() {
        DataService dataService = APIService.getService();
        Call<List<Song>> call = dataService.getListSong();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if(response.isSuccessful())
                {
                    List<Song> songDummies;
                    songDummies = response.body();

                    List<Song> updatedSongDummies = new ArrayList<>();

                    for (Song s : songDummies) {
                        if (!s.getSongId().equals("01")) {
                            updatedSongDummies.add(s);
                        }
                    }
                    PreferenceManager.showToast(getApplicationContext(), Validation.SONG_SUCCESS);
                    songsAdapter.addData(updatedSongDummies);
                    if(updatedSongDummies.size() == 0){
                        binding.rvSongs.setVisibility(View.GONE);
                        binding.textList.setVisibility(View.VISIBLE);
                    }

                }else {
                    // loi
                    PreferenceManager.showToast(getApplicationContext(), Validation.SONG_FAIL);
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {

            }
        });

    }

    private void initView() {
        binding.browse.setVisibility(View.VISIBLE);
        View sheet = findViewById(R.id.song_preview_sheet);
        bsb = BottomSheetBehavior.from(sheet);
        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View sheet, int state) {
                Log.v(TAG, "Song preview sheet state is: " + state);
                if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                    AudioWife.getInstance().release();
                }
            }

            @Override
            public void onSlide(@NonNull View sheet, float offset) {

            }
        });
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        seekbar = findViewById(R.id.seekbar);
        start = findViewById(R.id.start);
        end = findViewById(R.id.end);
        songName = findViewById(R.id.song);
        use = findViewById(R.id.use);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioWife.getInstance().release();

    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioWife.getInstance().pause();
    }


    public void downloadSelectedSong(Song song) {
        File songs = new File(getFilesDir(), "songs");
        if (!songs.exists() && !songs.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + songs);
        }
        binding.progressbar.setVisibility(View.GONE);

        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait.....")
                .setCancellable(false)
                .show();

        String inputUrl = song.getAudio(); // lấy đường dẫn để tải xuống bài hát
        String outputDir = songs.getAbsolutePath() + File.separator + song.getTitle()+ ".mp3"; // tạo thư mục lưu trữ cho bài hát được chọn
        Data inputData = new Data.Builder()
                .putString(FileDownloadWorker.KEY_INPUT, inputUrl)
                .putString(FileDownloadWorker.KEY_OUTPUT, outputDir)
                .build(); // truyền đường dẫn để tải xuống và lưu trữ vào Data object

        WorkRequest request = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(this, info -> {
                    Log.d(TAG, "downloadSelectedSong: " + info);
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended) {
                        progress.dismiss();
                    }

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
                        songName.setText(song.getTitle());
                        Log.d(TAG, "downloadSelectedSong376772: " + outputDir);
                        Uri songUri = Uri.parse(outputDir);
                        AudioWife.getInstance()
                                .init(this, songUri)
                                .setPlayView(play)
                                .setPauseView(pause)
                                .setSeekBar(seekbar)
                                .setRuntimeView(start)
                                .setTotalTimeView(end);
                        use.setOnClickListener(v->{
                            Intent intent = new Intent(getApplicationContext(),RecorderActivity.class);
                            intent.putExtra("linkAudio",outputDir);
                            intent.putExtra("SongId",song.getSongId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    }
                });
    }



}