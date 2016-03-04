package rkolady.knowhow;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class Vote extends WearableActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("VOTE_DEBUG", "arrived at vote");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        TextView voteData = (TextView) findViewById(R.id.text);
        Intent intent = getIntent();
        String[] value = intent.getStringExtra("rep").split("\n");
        Log.d("VOTE_DEBUG", value[1]);
        if (value.length > 1 && value[1] != null) {
            String zip = value[1];
            Log.d("what", zip);
            Log.d("what", zip);
            int dig = Integer.parseInt(zip) % 100;
            String rom = Integer.toString(dig);
            String ob = Integer.toString(100 - dig);
            voteData.setText("2012 Vote Data\nObama: " + ob + "%\nRomney: " + rom + "%");
        }
    }

}
