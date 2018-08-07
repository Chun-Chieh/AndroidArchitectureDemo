package com.chunchiehliang.androidarchitecureexample;

import android.app.Application;
import android.content.Context;

/**
 * @author Chun-Chieh Liang on 8/6/18.
 */
public class BaseApp extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
