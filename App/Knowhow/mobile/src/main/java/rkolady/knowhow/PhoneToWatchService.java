package rkolady.knowhow;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by RKolady on 3/1/2016.
 */
public class PhoneToWatchService extends Service {

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){
                    @Override
                    public void onConnected(Bundle connectionHint) {}

                    @Override
                    public void onConnectionSuspended(int cause) {}
                })
                .build();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            final String nameString;
            final String[] names = extras.getStringArray("rep");
            if (names == null) {
                nameString = extras.getString("rep");
            } else {
                StringBuilder combine = new StringBuilder();
                for (int i = 0; i < names.length - 2; i++) {
                    combine.append(names[i]);
                    combine.append("_");
                }
                combine.append("\n" + names[names.length - 2]);
                combine.append("\n" + names[names.length - 1]);
                nameString = combine.toString();
                Log.d("test", "this:" + combine);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Object> toSend = new HashMap<>();
                    String[] images = extras.getStringArray("img");
                    if (images != null) {
                        byte[][] imgData = new byte[images.length][];
                        int i = 0;
                        for (String url : images) {
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            InputStream is = null;
                            try {
                                URL link = new URL(url);
                                is = link.openStream();
                                byte[] byteChunk = new byte[4096];
                                int n;

                                while ((n = is.read(byteChunk)) > 0) {
                                    os.write(byteChunk, 0, n);
                                }

                                if (is != null) {
                                    is.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            imgData[i] = os.toByteArray();
                            i++;
                        }

//                        PutDataMapRequest dataMap = PutDataMapRequest.create("/images");
//                        dataMap.getDataMap().putLong("date", new Date().getTime());
//
//                        dataMap.getDataMap().putString("reps", nameString);
//                        for (i = 0; i < imgData.length; i++) {
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(imgData[i], 0, imgData[i].length);
//                            Asset asset = createAssetFromBitmap(bitmap);
//                            dataMap.getDataMap().putAsset(names[i], asset);
//                        }
//
//                        PutDataRequest request = dataMap.asPutDataRequest();
//                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient, request);

                        toSend.put("images", imgData);
                    }

                    toSend.put("reps", nameString);


                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream out = new ObjectOutputStream(byteOut);
                        out.writeObject(toSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d("images debug", toSend.toString());
                    Log.d("images debug", byteOut.toByteArray().toString());

                    mApiClient.connect();
                    sendMessage("/data", byteOut.toByteArray());



                }
            }).start();

            return START_STICKY;
        }

        return 0;
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    private void sendMessage(final String repPath, final byte[] reps) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node: nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), repPath, reps ).await();
                }
            }
        }).start();
    }
}
