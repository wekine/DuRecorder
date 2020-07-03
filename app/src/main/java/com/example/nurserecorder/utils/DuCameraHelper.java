package com.example.nurserecorder.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.example.nurserecorder.DuApplication;
import com.example.nurserecorder.common.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class DuCameraHelper {

    private static final String TAG = "DuCameraHelper";

    public static Camera openCamera() {
        return openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public static Camera openCamera(int cameraId) {
        if (!haveFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e(TAG, "no camera!");
            return null;
        }

        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT
                && !haveFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Log.e(TAG, "no front camera!");
            return null;
        }

        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
            if (camera == null) {
                Log.e(TAG, "openCamera failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return camera;
    }

    private static boolean haveFeature(String name) {
        return DuApplication.getContext().getPackageManager().hasSystemFeature(name);
    }

    public static void setFocusMode(Camera camera, String focusMode) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(focusMode)) {
            parameters.setFocusMode(focusMode);
        }
        camera.setParameters(parameters);
    }

    public static void printSupportVideoSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();

        int size = mSupportedVideoSizes.size();
        for (int index = 0; index < size; index++) {
            Camera.Size sizeObj = mSupportedVideoSizes.get(index);
            Log.i("DuCameraHelper", "width is : " + sizeObj.width + "height is : " + sizeObj.height);
        }

        int size2 = mSupportedPreviewSizes.size();
        for (int index = 0; index < size2; index++) {
            Camera.Size sizeObj = mSupportedPreviewSizes.get(index);
            Log.i("DuCameraHelper", "preview-width is : " + sizeObj.width + "height is : " + sizeObj.height);
        }
    }

    public static void setDisplayOrientation(Activity activity, Camera camera, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = ((WindowManager)
                activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        // 前置
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public static boolean isFacingBack(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public static void releaseCamera(Camera camera) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
    }

    public static void setOptimalSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = DuCameraHelper.chooseOptimalSize(parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(size.width, size.height);
        Log.d(TAG, "output size: (" + size.width + ", " + size.height + ")");
        camera.setParameters(parameters);
    }

    public static Camera.Size chooseOptimalSize(List<Camera.Size> options) {
        List<Camera.Size> alternative = new ArrayList<>();
        for (Camera.Size option : options) {
            if (option.height == option.width * Constant.ASPECT_RATIO_ARRAY[1]) {
                alternative.add(option);
            }
        }

        if (alternative.size() > 0) {
            return Collections.max(alternative, new CompareSizesByArea());
        }

        return options.get(0);
    }

    public String getSurfaceViewSize(int width, int height) {
        if (equalRate(width, height, 1.33f)) {
            return "4:3";
        } else {
            return "16:9";
        }
    }

    public boolean equalRate(int width, int height, float rate) {
        float r = (float)width /(float) height;
        if (Math.abs(r - rate) <= 0.2) {
            return true;
        } else {
            return false;
        }
    }


    private static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // 转型 long 是为了确保乘法运算不会溢出
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    }
}
