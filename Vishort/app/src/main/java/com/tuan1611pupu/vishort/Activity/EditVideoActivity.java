package com.tuan1611pupu.vishort.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.android.exoplayer2.util.Log;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.databinding.ActivityEditVideoBinding;
import com.tuan1611pupu.vishort.listeners.VideoTrimListener;

import java.io.File;

public class EditVideoActivity extends BaseActivity implements VideoTrimListener {

    private static final String TAG = "jason";
    private static final String VIDEO_PATH_KEY = "video-file-path";
    private static final String COMPRESSED_VIDEO_FILE_NAME = "compress.mp4";
    public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
    private ActivityEditVideoBinding mBinding;
    private Uri videoUri = null;
    private String songId;
    private ProgressDialog mProgressDialog;

    public static void call(FragmentActivity from, Uri videoUri,String songId) {
        if (videoUri != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(VIDEO_PATH_KEY, videoUri);
            bundle.putString("songId",songId);

            Intent intent = new Intent(from, EditVideoActivity.class);
            intent.putExtras(bundle);

            from.startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
        }
    }

    @Override
    public void initUI() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_video);
        Bundle bd = getIntent().getExtras();
        if (bd != null) {
            videoUri = bd.getParcelable(VIDEO_PATH_KEY);
            songId = bd.getString("songId");
            Log.d("videoUri23",videoUri.toString());
        }
        if (mBinding.trimmerView != null && videoUri != null) {
            mBinding.trimmerView.setOnTrimVideoListener(this);
            mBinding.trimmerView.initVideoByURI(videoUri);
        }
    }


    @Override public void onResume() {
        super.onResume();
    }

    @Override public void onPause() {
        super.onPause();
        mBinding.trimmerView.onVideoPause();
        mBinding.trimmerView.setRestoreState(true);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.trimmerView.onDestroy();
    }

    @Override public void onStartTrim() {
        buildDialog(getResources().getString(R.string.trimming)).show();
    }

    @Override public void onFinishTrim(String in) {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        File file = new File(in);
        Uri uriVideo =Uri.fromFile(file);
        Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
        intent.putExtra("uriVideo",uriVideo);
        intent.putExtra("SongId", songId);
        startActivity(intent);
        finish();
        //TODO: please handle your trimmed video url here!!!
        //String out = StorageUtil.getCacheDir() + File.separator + COMPRESSED_VIDEO_FILE_NAME;
        //buildDialog(getResources().getString(R.string.compressing)).show();
        //VideoCompressor.compress(this, in, out, new VideoCompressListener() {
        //  @Override public void onSuccess(String message) {
        //  }
        //
        //  @Override public void onFailure(String message) {
        //  }
        //
        //  @Override public void onFinish() {
        //    if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        //    finish();
        //  }
        //});
    }

    @Override public void onCancel() {
        mBinding.trimmerView.onDestroy();
        Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
        intent.putExtra("uriVideo",videoUri);
        intent.putExtra("SongId", songId);
        startActivity(intent);
        finish();
    }

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }
}
