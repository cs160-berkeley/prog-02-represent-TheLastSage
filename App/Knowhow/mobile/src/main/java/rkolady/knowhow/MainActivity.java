package rkolady.knowhow;

/* AIzaSyDgEhhHPfIjUeRLb7FJRJ76cYCt_uQrNDk */

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "bwTHVspqbpfF4psTxCh11MWdO";
    private static final String TWITTER_SECRET = "kt65rwYQER0KaIS1HM8AJXJQpGSj7U5lAYXgnfoEkZbDzGoQW0";


    private GoogleApiClient mGoogleApiClient;
    private String api = "AIzaSyDgEhhHPfIjUeRLb7FJRJ76cYCt_uQrNDk";
    private String Lat;
    private String Lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        EditText zip = (EditText) findViewById(R.id.editText);
        zip.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    MainActivity.this.start();
                    return true;
                }
                return false;
            }
        });

        VoteData test = new VoteData(this, "Berkeley");
        String testOut = test.getVotes();
        if (testOut != null) {
            Log.d("voteTest", testOut);
        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int val) {}

    @Override
    public void onConnectionFailed(ConnectionResult res) {}

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("JSON", "connected");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Lat = (String.valueOf(mLastLocation.getLatitude()));
            Lon = (String.valueOf(mLastLocation.getLongitude()));
            Log.d("JSON", Lat + ", " + Lon);
        }
    }

    public void start(View view) {
        start();
    }

    public void start() {
        EditText zip = (EditText) findViewById(R.id.editText);
        Log.d("main", zip.getText().toString());
        final Intent toSend = new Intent(MainActivity.this, Congress.class);
        String zipString = zip.getText().toString();
        if (zipString.length() == 0) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + Lat + "," + Lon + "&key=" + api;
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
                                VoteData election = new VoteData(MainActivity.this, countyToFind);
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
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + zip.getText().toString() + "&key=" + api;
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
                                VoteData election = new VoteData(MainActivity.this, countyToFind);
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
        }
    }

}
