package de.illogic.kinetic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class TapService extends Service implements SensorEventListener {

    private static final String TAG = "TapService";
    private SensorManager sensorManager;
    private Sensor accel;
    private long lastUpdate;
    private AudioManager am;
    public Intent viewMediaIntent = new Intent();
    public Intent nextSongIntent = new Intent();
    public Intent togglePauseIntent = new Intent();
    private KeyEvent downEvent;
    private KeyEvent upEvent;
    private KeyEvent downEventTP;
    private KeyEvent upEventTP;

    public TapService() {
        super();
    }

    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                sensorManager.unregisterListener(TapService.this);
                sensorManager.registerListener(TapService.this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // registerReceiver(screenOffBroadcast, filter);

        long eventtime = SystemClock.uptimeMillis();
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        nextSongIntent.setAction(android.content.Intent.ACTION_MEDIA_BUTTON);
        downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        downEventTP = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        upEventTP = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        lastUpdate = System.currentTimeMillis();

        startService();
    }

    private void startService() {

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // handleCommand(intent);
        return START_STICKY;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(TAG, "AccuracyChanged");
    }

    public void onSensorChanged(SensorEvent event) {
        // TODO Check current phone position
        // TODO When the device is at rest, the output of the gravity sensor should be identical to that of the accelerometer.

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
        }
        // TODO Phone being carried? In hand / pocket / bag / armstrap?
        // In pocket perfectly upright: y ~= 9.81
        // upside down: y ~= -9.81

        // In pocket perfectly crosswise: x ~= 9.81
        // on the other side: x ~= -9.81

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            // Movement
            float x = values[0];
            float y = values[1];
            float z = values[2];

            // float accelationSquareRoot = (x * x + y * y + z * z)
            // / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            float accelerationSquareRoot = (z * z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            long actualTime = System.currentTimeMillis();
            if (Math.abs(z) >= 10.0) {
                if (actualTime - lastUpdate < 10000) {
                    return;
                }
                lastUpdate = actualTime;

                Intent i = new Intent("com.android.music.musicservicecommand");
                if (am.isMusicActive()) {
                    // i.putExtra("command", "next");
                    nextSongIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                    TapService.this.sendBroadcast(nextSongIntent);
                    nextSongIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
                    TapService.this.sendBroadcast(nextSongIntent);
                    Log.d(TAG, "Next Song");

                } else {
                    // i.putExtra("command", "play");
                    togglePauseIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                    sendBroadcast(togglePauseIntent);
                    togglePauseIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
                    sendBroadcast(togglePauseIntent);

                    Log.d(TAG, "Play Song");
                }
                Log.d(TAG, "x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
                // startActivity(viewMediaIntent);

                // TODO only if player is active
                sendOrderedBroadcast(nextSongIntent, null);
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        unregisterReceiver(mReceiver);
        Log.d(TAG, "Service stopped");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null; // no IPC used
    }
}