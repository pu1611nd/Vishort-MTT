package com.tuan1611pupu.vishort;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import iknow.android.utils.BaseUtils;
import nl.bravobit.ffmpeg.FFmpeg;

public class ZApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    BaseUtils.init(this);
    initFFmpegBinary(this);
  }

  private void initFFmpegBinary(Context context) {
    if (!FFmpeg.getInstance(context).isSupported()) {
      Log.e("ZApplication","Android cup arch not supported!");
    }
  }
}
