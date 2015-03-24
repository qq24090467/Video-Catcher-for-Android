package com.mobojobo.vivideodownloader;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by pc on 23.03.2015.
 */
public class MyApp extends Application {
    public static Bus bus = new Bus(ThreadEnforcer.MAIN);

    @Override
    public void onCreate() {
        super.onCreate();


    }
}
