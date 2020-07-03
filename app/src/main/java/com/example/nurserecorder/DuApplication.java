package com.example.nurserecorder;


import android.app.Application;
import android.content.Context;

public class DuApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
