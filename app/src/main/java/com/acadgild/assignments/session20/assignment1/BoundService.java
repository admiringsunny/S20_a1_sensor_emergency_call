package com.acadgild.assignments.session20.assignment1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BoundService extends Service {

    public static final String TAG = "bound";
    public static final int NOTIFICATION_ID = 100;
    private final IBinder myBinder = new MyLocalBinder();
    private Thread backgroundThread;

    //    client is binding to the service with bindService()
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  myBinder;
    }

    //    Called when the service is being created.
    @Override
    public void onCreate() {
        super.onCreate();

        // do the work in a separate thread so main thread is not blocked
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "backgroundThread running");
                /*
                steps
                */

                MainActivity mainActivity = new MainActivity();

                if ((!mainActivity.shakeInitiated) && mainActivity.ifAccelarationChanged()) {
                    mainActivity.shakeInitiated = true;
                } else if (mainActivity.shakeInitiated && mainActivity.ifAccelarationChanged()) {
                    mainActivity.executeShakeAction();
                } else if (mainActivity.shakeInitiated && (!mainActivity.ifAccelarationChanged())) {
                    mainActivity.shakeInitiated = false;
                }

            }
        });
        backgroundThread.start();
    }

    //    the class used for the client Binder
    public class MyLocalBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }
}
