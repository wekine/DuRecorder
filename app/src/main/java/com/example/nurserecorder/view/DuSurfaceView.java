package com.example.nurserecorder.view;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.nurserecorder.utils.DuCameraHelper;

import java.io.IOException;

public class DuSurfaceView extends SurfaceView {

    private Camera mCamera;
    private PreviewCallback mPreviewCallback;

    public DuSurfaceView(Context context) {
        super(context);
    }

    public DuSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void openCamera() {
        openCamera(getHolder());
    }

    private void openCamera(SurfaceHolder holder) {
        if (mCamera != null) {
            return;
        }
        int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera = DuCameraHelper.openCamera(mCameraId);
        if (mCamera == null) {
            return;
        }

        DuCameraHelper.printSupportVideoSize(mCamera);

        DuCameraHelper.setOptimalSize(mCamera);
        DuCameraHelper.setDisplayOrientation((Activity) getContext(), mCamera, mCameraId);
        startPreview(holder);
    }

    private void startPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            doStartPreview();
        } catch (IOException e) {
            Log.e("CameraSurfaceView", "startPreview open camera failed: " + e.getMessage());
            releaseCamera();
            if (mPreviewCallback != null) {
                mPreviewCallback.onPreviewFailed();
            }
        }
    }

    private void doStartPreview() {
        mCamera.startPreview();
        if (mPreviewCallback != null) {
            mPreviewCallback.onPreviewStarted(mCamera);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }
}
