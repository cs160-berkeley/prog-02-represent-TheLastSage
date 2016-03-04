package rkolady.knowhow;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private GestureDetector mDetector;
    private String[] names;
    private String zip;
    private int index;
    private TextView display;
    private float scrollDist;

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
                int rand = (int) Math.ceil(Math.random() * 100);
                String toPass = "\n" + Integer.toString(rand);
                Intent sendZip = new Intent(MainActivity.this, Vote.class);
                sendZip.putExtra("rep", toPass);
                startActivity(sendZip);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        names = new String[]{"", "name2", "name3"};
        index = 0;
        display = (TextView) findViewById(R.id.text);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String name = extras.getString("rep");
            names = name.split("\n")[0].split(",");
            zip = name.split("\n")[1];
        }
        display.setText(names[0]);
        scrollDist = 0;
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float x, float y) {
                Log.d("gesture", "true");
                scrollDist += y;
                if (scrollDist > 250) {
                    Log.d("gesture", "up");
                    index++;
                    if (index >= names.length) {index = 0;}
                    scrollDist = 0;
                } else if (scrollDist < -250) {
                    Log.d("gesture", "down");
                    index--;
                    if (index < 0) {index = names.length - 1;}
                    scrollDist = 0;
                }
                display.setText(names[index]);
                return true;
            }

            public boolean onDown(MotionEvent event) {
                return true;
            }

            public void onLongPress(MotionEvent event) {
                Log.d("gesture", "long");
                Intent sendIntent = new Intent(MainActivity.this, WatchToPhoneService.class);
                sendIntent.putExtra("name", names[index] + "\n" + zip);
                startService(sendIntent);
            }
        });
        display.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("gesture", "got here");
                return mDetector.onTouchEvent(event);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

}
