package com.example.liftlog;

import android.app.Application;

import com.example.liftlog.data.AppDatabase;

public class LiftLogApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.getInstance(this);
    }
}
