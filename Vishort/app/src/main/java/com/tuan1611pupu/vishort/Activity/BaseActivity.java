package com.tuan1611pupu.vishort.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity {

  protected abstract void initUI();
  protected void loadData() {
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      render();
  }

  private void render() {

    initUI();
    loadData();
  }
}
