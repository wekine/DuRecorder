package com.example.nurserecorder.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DuFileUtils {
    private static final String TAG = "DuFileUtils";
    // 图片
    public static final int MEDIA_TYPE_IMAGE = 1;
    // 视频
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static String getOutputMediaDir(int type) {
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "DuRecorder");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        String dir;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            dir = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        } else if (type == MEDIA_TYPE_VIDEO) {
            dir = mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4";
        } else {
            dir = mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4";
        }

        return dir;
    }
}
