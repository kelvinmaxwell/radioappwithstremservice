package com.karimax.radioappwithstremservice;

import android.app.Application;
import android.util.Log;



public class NetworkMonitoringApplication extends Application {
    public static final String TAG = NetworkMonitoringApplication.class.getSimpleName();

    public NetworkMonitoringUtil mNetworkMonitoringUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");

        mNetworkMonitoringUtil = new NetworkMonitoringUtil(getApplicationContext());
        mNetworkMonitoringUtil.checkNetworkState();
        mNetworkMonitoringUtil.registerNetworkCallbackEvents();
    }
}
