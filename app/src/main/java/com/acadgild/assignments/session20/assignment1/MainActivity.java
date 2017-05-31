package com.acadgild.assignments.session20.assignment1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private float curX, curY, curZ;
    private float lastX, lastY, lastZ;
    private boolean firstUpdate = true;
    private final float shakeThreshold = 1.5f;
    boolean shakeInitiated = false;
    private static final String TAG = "bound";
    private boolean isBound;
    private BoundService serviceReference;
    public static final int NOTIFICATION_ID = 100;
    public static final int REQUEST_CODE = 101;


    EditText etMbNo;
    CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        etMbNo = (EditText) findViewById(R.id.etMobileNo);

        checkBox = (CheckBox) findViewById(R.id.chkBoxStartService);

        // start/intent service
        Log.i(TAG, "bounding service");
        Intent intent = new Intent(this, BoundService.class);
        startService(intent);
        sendNotification();

    }

// check sensor type
// implement shaking device function
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateAccelParameters(sensorEvent.values[0], sensorEvent.values[1],
                    sensorEvent.values[2]);
            if ((!shakeInitiated) && ifAccelarationChanged()) {
                shakeInitiated = true;
            } else if (shakeInitiated && ifAccelarationChanged()) {
                executeShakeAction();
            } else if (shakeInitiated && (!ifAccelarationChanged())) {
                shakeInitiated = false;
            }


        }
    }

//    call intent -> make call
    void executeShakeAction() {

        if (checkBox.isChecked()) {
            String uri = "tel:" + etMbNo.getText().toString().trim();


            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));

            startActivity(intent);


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor
                (Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_NORMAL);

    }

    private void updateAccelParameters(float newX, float newY, float newZ) {
        if (firstUpdate) {
            lastX = newX;
            lastY = newY;
            lastZ = newZ;
            firstUpdate = false;
        } else {
            lastX = curX;
            lastY = curY;
            lastZ = curZ;
        }
        curX = newX;
        curY = newY;
        curZ = newZ;
    }

    boolean ifAccelarationChanged() {

        float diffX = Math.abs(lastX - curX);
        float diffY = Math.abs(lastY - curY);
        float diffZ = Math.abs(lastZ - curZ);


        return (diffX > shakeThreshold && diffY > shakeThreshold)
                || (diffX > shakeThreshold && diffZ > shakeThreshold)
                || (diffY > shakeThreshold && diffZ > shakeThreshold);
    }

//    the activity is being destroyed -> onDestroy() -> stop service
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroying service");
        if (isFinishing()) {
            Log.i(TAG, "activity is finishing");

//            stop service as activity being destroyed and we won't use it any more
            Intent stopServiceIntent = new Intent(this, BoundService.class);
            stopService(stopServiceIntent);
        }
    }

    //    activity starting -> onStart() -> bind to service
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "MainActivity - onStart - binding...");
        // bind to service
        doBindToService();
    }

    //    bind to the service
    private void doBindToService() {
        Toast.makeText(this, "binding....", Toast.LENGTH_SHORT).show();
        if (!isBound) {
            Intent boundIntent = new Intent(this, BoundService.class);
            isBound = bindService(boundIntent, myConnection, Context.BIND_AUTO_CREATE);
        }
    }

    //    activity stopping -> onStop() -> unbind from the service
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop -> Unbinding From Service");
//        unbind from the service
        doUnbindFromService();
    }

    //        unbind from the service
    private void doUnbindFromService() {
        Toast.makeText(this, "unbinding...", Toast.LENGTH_SHORT).show();
        unbindService(myConnection);
        isBound = false;
    }

    //    interface for monitoring the state of the service
    private ServiceConnection myConnection = new ServiceConnection() {

        // when the connection with the service has been established
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "bound service connected");
            serviceReference = ((BoundService.MyLocalBinder) service).getService();
            isBound = true;
        }

        // when crash happen
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "bound service disconnected");
            serviceReference = null;
            isBound = false;
        }
    };

    //      sends an ongoing notification notifying that service is running.
//      it's only dismissed when the service is destroyed
    private void sendNotification() {
//        we use the compatibility library
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Service Running")
                .setTicker("Emergency Active")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);

//        notify
        Intent startIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


}