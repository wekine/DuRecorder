package com.example.nurserecorder.utils;

import android.os.Handler;
import android.os.Looper;

public class DuThreadUtil {
    private static final Object handlerLock = new Object();
    private static Handler handler = null;

    /**
     * @param runnable
     */
    public static void post(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * @param runnable
     * @param uptimeMillis
     */
    public static void postDelayed(Runnable runnable, long uptimeMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, uptimeMillis);
    }

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void runOnUiThread(Runnable runnable) {
        handler().post(runnable);
    }

    /**
     * handler cache
     */
    public static Handler handler() {
        if (handler == null) {
            synchronized (handlerLock) {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

}
