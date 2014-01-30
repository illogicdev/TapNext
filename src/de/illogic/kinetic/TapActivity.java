package de.illogic.kinetic;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public class TapActivity extends Activity {
    final String TAG = "TapActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTapService();
    }

    private void startTapService() {
        final Intent intentService = new Intent(this, TapService.class);
        Toast.makeText(getBaseContext(), R.string.starting_service, Toast.LENGTH_LONG).show();
        startService(intentService);
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onStop();
    }
}