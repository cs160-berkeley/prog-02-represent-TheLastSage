package rkolady.knowhow;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Created by RKolady on 3/1/2016.
 */
public class PhoneListenerService extends WearableListenerService {

    private String api = "AIzaSyDgEhhHPfIjUeRLb7FJRJ76cYCt_uQrNDk";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("Testerino", "got message");
        if (messageEvent.getPath().equals("/rand")) {
            VoteData vote = new VoteData(this, "RANDOM_COUNTY");
            String county = vote.getRandom().split("\n")[0];
            StringBuilder countyBuild = new StringBuilder();
            for (String part: county.split(" ")) {
                countyBuild.append("+");
                countyBuild.append(part);
            }
            String state = vote.getRandom().split("\n")[1];
            StringBuilder stateBuild = new StringBuilder();
            for (String part: state.split(" ")) {
                stateBuild.append("+");
                stateBuild.append(part);
            }
//            String toSend = "fill\nfill\n" + voteString;
//            Intent send = new Intent(PhoneListenerService.this, PhoneToWatchService.class);
//            send.putExtra("rep", toSend);
//            startService(send);

            final Intent toSend = new Intent(PhoneListenerService.this, Congress.class);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + county +",+USA&key=" + api;
            Log.d("JSON", url);
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject res) {
                    try {
                        JSONArray address = (JSONArray) res.get("results");
                        JSONObject components = (JSONObject) address.get(0);
                        JSONArray temp1 = (JSONArray) components.get("address_components");
                        for (int i = 0; i < temp1.length(); i++) {
                            JSONObject temp2 = (JSONObject) temp1.get(i);
                            JSONArray types = (JSONArray) temp2.get("types");
                            boolean zipField = false;
                            boolean county = false;
                            for (int j = 0; j < types.length(); j++) {
                                String type = (String) types.get(j);
                                if (type.equals("postal_code")) {
                                    zipField = true;
                                }

                                if (type.equals("administrative_area_level_2")) {
                                    county = true;
                                }
                            }
                            if (county) {
                                String countyValue = temp2.getString("long_name");
                                Log.d("inAction", "county: " + countyValue);
                                String countyToFind = countyValue.split(" ")[0];
                                VoteData election = new VoteData(PhoneListenerService.this, countyToFind);
                                String countyVotes = election.getVotes();
                                Log.d("inAction", countyVotes);
                                toSend.putExtra("votes", countyVotes);
                            }

                            if (zipField) {
                                Log.d("JSON", temp2.getString("long_name"));
                                String zipValue = temp2.getString("long_name");
                                toSend.putExtra("zip", zipValue);
                            }

                            if (toSend.hasExtra("zip") && toSend.hasExtra("votes")) {
                                Log.d("voteTest", toSend.getExtras().toString());
                                toSend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(toSend);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError err) {
                    // Meh
                }
            });
            queue.add(req);
        } else {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent sendIntent = new Intent(PhoneListenerService.this, Congress.class);
            sendIntent.putExtra("rep", value);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(sendIntent);
            super.onMessageReceived(messageEvent);
        }

    }
}
