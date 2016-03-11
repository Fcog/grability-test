package com.franciscogiraldo.fcog.grability.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by fcog on 9/18/15.
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}