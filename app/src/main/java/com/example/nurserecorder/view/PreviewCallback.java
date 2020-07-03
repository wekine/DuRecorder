package com.example.nurserecorder.view;

import android.hardware.Camera;

public interface PreviewCallback {
    void onPreviewStarted(Camera camera);

    void onPreviewStopped();

    void onPreviewFailed();
}