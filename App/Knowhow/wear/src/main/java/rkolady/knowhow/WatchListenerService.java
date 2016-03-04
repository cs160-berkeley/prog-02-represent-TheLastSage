package rkolady.knowhow;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by RKolady on 3/1/2016.
 */
public class WatchListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d("T", event.getPath());
        String value = new String(event.getData(), StandardCharsets.UTF_8);
        Intent intent;
        if (value.split(",").length == 1) {
            Log.d("VOTE_DEBUG", "sending vote");
            Log.d("VOTE_DEBUG", value);
            intent = new Intent(this, Vote.class);
        } else {
            Log.d("VOTE_DEBUG", "sending names");
            Log.d("VOTE_DEBUG", value);
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("rep", value);
        startActivity(intent);
        super.onMessageReceived(event);
    }
}
