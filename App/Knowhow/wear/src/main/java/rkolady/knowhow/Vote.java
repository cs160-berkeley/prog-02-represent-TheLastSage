package rkolady.knowhow;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class Vote extends WearableActivity {

    /* put this into your activity class */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter
            if (mAccel > 12) {
                Intent random = new Intent(Vote.this, WatchToPhoneService.class);
                random.putExtra("rand", "yes");
                startService(random);
//                int rand = (int) Math.ceil(Math.random() * 100);
//                String toPass = "\n" + Integer.toString(rand);
//                Intent sendZip = new Intent(MainActivity.this, Vote.class);
//                sendZip.putExtra("rep", toPass);
//                startActivity(sendZip);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("VOTE_DEBUG", "arrived at vote");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        TextView voteData = (TextView) findViewById(R.id.text);
        Intent intent = getIntent();
        String[] value = intent.getStringExtra("rep").split("\n");
        Log.d("VOTE_DEBUG", value[2]);
        if (value.length > 1 && value[1] != null) {
            String[] votes = value[2].split("-");
            for (String vote: votes) {
                Log.d("votes", vote);
            }

            String ob = votes[0];
            String rom = votes[1];
            String county = votes[2];
            voteData.setText("2012 Vote Data\n" + county+ "\nObama: " + ob + "%\nRomney: " + rom + "%");
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

}
