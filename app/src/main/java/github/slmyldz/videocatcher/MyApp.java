package github.slmyldz.videocatcher;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import io.fabric.sdk.android.Fabric;


public class MyApp extends Application {
    public static Bus bus = new Bus(ThreadEnforcer.MAIN);

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
