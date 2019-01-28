package com.example.donxing.starnote;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;
    public static Context getContext(){
        return context;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

    }
}
