package rkolady.knowhow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by RKolady on 3/1/2016.
 */
public class WatchListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d("T", event.toString());
        HashMap<String, Object> data = new HashMap<>();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(event.getData());

        try {
            ObjectInputStream in = new ObjectInputStream(byteIn);
            data = (HashMap<String, Object>) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String value = (String) data.get("reps");
        if (value != null) {
            Log.d("images debug", Boolean.toString(data.containsKey("images")));
            Intent intent;

            if (data.containsKey("images")) {
                byte[][] images = (byte[][]) data.get("images");
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("images", images);
            } else {
                intent = new Intent(this, Vote.class);
            }


            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("rep", value);
            startActivity(intent);
        }
        super.onMessageReceived(event);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        Log.d("data change", "success");

        for (DataEvent event: dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals("/images")) {
                DataMapItem dataMap = DataMapItem.fromDataItem(event.getDataItem());
                String nameString = dataMap.getDataMap().getString("reps");
                String[] names = nameString.split("\n")[0].split("_");
                byte[][] images = new byte[names.length][];
                for (int i = 0; i < names.length; i++) {
                    Asset pic = dataMap.getDataMap().getAsset(names[i]);
                    Log.d("picture", pic.toString());
                    Bitmap picbmp = loadBitmapFromAsset(pic);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    picbmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] picArray = stream.toByteArray();
                    images[i] = picArray;
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("images", images);
                intent.putExtra("rep", nameString);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
//            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
