package rkolady.knowhow;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by RKolady on 3/1/2016.
 */
public class PhoneListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("Testerino", "got message");
        String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
        Intent sendIntent = new Intent(PhoneListenerService.this, Congress.class);
        sendIntent.putExtra("rep", value);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendIntent);
        super.onMessageReceived(messageEvent);
    }
}
