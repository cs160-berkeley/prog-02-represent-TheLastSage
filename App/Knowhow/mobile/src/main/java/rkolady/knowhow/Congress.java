package rkolady.knowhow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Congress extends AppCompatActivity implements ThreadListener{

    private String[] names;
    private boolean started = false;
    private String zip;
    private String votes;
    private String sunlight = "7cbd0df256ab4fb692b92e90478dcd17";
    final private ArrayList<HashMap> repHolder = new ArrayList<HashMap>();
    private Intent restart;

    private static final String TWITTER_KEY = "bwTHVspqbpfF4psTxCh11MWdO";
    private static final String TWITTER_SECRET = "kt65rwYQER0KaIS1HM8AJXJQpGSj7U5lAYXgnfoEkZbDzGoQW0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final HashMap<String, Boolean> reqTracker = new HashMap<>();

        int reqs = 0;

        final Intent rec = getIntent();
        if (rec.hasExtra("zip")) {
            zip = rec.getStringExtra("zip");
            votes = rec.getStringExtra("votes");
            Log.d("zip", zip);
        } else {
            String temp = rec.getStringExtra("rep");
            zip = temp.split("\n")[1];
            votes = temp.split("\n")[2];
        }

        // Get Rep Data
        reqTracker.put("reps", false);
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://congress.api.sunlightfoundation.com/legislators/locate?zip=" + zip + "&apikey=" + sunlight;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject res) {
                try {

                    JSONArray results = (JSONArray) res.get("results");
                    for (int i = 0; i < res.getInt("count"); i++) {
                        JSONObject repData = (JSONObject) results.get(i);
                        HashMap<String, Object> repMap = new HashMap<String, Object>();
                        String name = repData.getString("title") + ". " + repData.getString("first_name") + " " + repData.getString("last_name")
                                + " (" + repData.getString("party") + ")";
                        repMap.put("name", name);
                        repMap.put("end", repData.getString("term_end"));
                        repMap.put("party", repData.getString("party"));
                        repMap.put("email", repData.getString("oc_email"));
                        repMap.put("site", repData.getString("website"));
                        repMap.put("ID", repData.getString("bioguide_id"));
                        repMap.put("twitterID", repData.getString("twitter_id"));
                        repHolder.add(repMap);
                        Log.d("timer", "this before");
                    }

                    for (final HashMap repMap1: repHolder) {
                        String url = "https://congress.api.sunlightfoundation.com/committees?member_ids=" + repMap1.get("ID") + "&apikey=" + sunlight;
                        JsonObjectRequest req = new JsonObjectRequest(
                                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject res) {
                                try {
                                    JSONArray results = (JSONArray) res.get("results");
                                    int count = 4;
                                    if (res.getInt("count") < 4) {
                                        count = res.getInt("count");
                                    }
                                    ArrayList<String> comms = new ArrayList<String>();
                                    for (int i = 0; i < count; i++) {
                                        JSONObject repData = (JSONObject) results.get(i);
                                        comms.add(repData.getString("name"));
                                    }
                                    repMap1.put("comm", comms);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError err) {
                                // wut
                            }
                        });

                        queue.add(req);
                    }

                    for (final HashMap repMap2: repHolder) {
                        String url = "https://congress.api.sunlightfoundation.com/bills?sponsor_id=" + repMap2.get("ID") + "&apikey=" + sunlight;
                        JsonObjectRequest req = new JsonObjectRequest(
                                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject res) {
                                try {
                                    JSONArray results = (JSONArray) res.get("results");
                                    int count = 4;
                                    if (res.getInt("count") < 4) {
                                        count = res.getInt("count");
                                    }
                                    ArrayList<String> bills = new ArrayList<String>();
                                    for (int i = 0; i < count; i++) {
                                        JSONObject repData = (JSONObject) results.get(i);
                                        bills.add(repData.getString("short_title") + " | " + repData.getString("introduced_on"));
                                    }
                                    repMap2.put("bills", bills);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError err) {
                                // wut
                            }
                        });

                        queue.add(req);
                    }

                    String url = "www.dum.com";

                    JsonObjectRequest dummy = new JsonObjectRequest(
                            Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {}
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError err) {
                            NotifyingThread twitterThread = new NotifyingThread() {
                                @Override
                                public void doRun() {
                                    for (final HashMap repMap3: repHolder) {
                                        ConfigurationBuilder cb = new ConfigurationBuilder();
                                        cb.setDebugEnabled(true)
                                                .setOAuthConsumerKey(TWITTER_KEY)
                                                .setOAuthConsumerSecret(TWITTER_SECRET)
                                                .setOAuthAccessToken("347878220-SuwqqXzbguaLDulxzZLGHbkN92DnAH772hps76fy")
                                                .setOAuthAccessTokenSecret("7Uh2nQYvERTx6PNUZz4WGU1wNfJP89otvc45eE9p4Y8Ax");
                                        TwitterFactory tf = new TwitterFactory(cb.build());
                                        Twitter twitter = tf.getInstance();
                                        try {
                                            User user = twitter.showUser(repMap3.get("twitterID").toString());
                                            twitter4j.Status status = user.getStatus();
                                            String tweet = status.getText();
                                            repMap3.put("tweet", tweet);
                                            Log.d("twitter", user.getStatus().toString());
                                            repMap3.put("img", user.getOriginalProfileImageURL());
                                            repMap3.put("smallimg", user.getBiggerProfileImageURL());
                                        } catch (TwitterException te) {
                                            te.printStackTrace();
                                            System.exit(-1);
                                        }
                                    }

                                    Log.d("ErrTest", "started from the bottom");
                                }
                            };

                            twitterThread.addListener(Congress.this);
                            twitterThread.start();
                        }

                    });

                    queue.add(dummy);

                    reqTracker.clear();
                    reqTracker.put("reps", true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError err) {
                // wut
            }
        });

        queue.add(req);

        Log.d("timer", "that");

        // Populating tweets and picture. Look for "status" and "profile_image_url" https://api.twitter.com/1.1/users/show.json?screen_name=
//
//        for (final HashMap repMap: repHolder) {
//            url = "https://api.twitter.com/1.1/users/show.json?screen_name=" + repMap.get("twitterID");
//            Log.d("twitterURL", url);
//            req = new JsonObjectRequest(
//                    Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                @Override
//                public void onResponse(JSONObject res) {
////                    try {
//                        Log.d("twitter", res.toString());
////                    } catch (JSONException e) {
////                        throw new RuntimeException(e);
////                    }
//                }
//            }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError err) {
//                    // wut
//                }
//            });
//
//            queue.add(req);
//        }

//        HashMap<String, String> map1 = new HashMap<String, String>();
//        map1.put("name", "Rep. Barbara Lee (D)");
//        map1.put("email", "lee@house.gov");
//        map1.put("site", "lee.house.gov");
//        map1.put("comm", "that one comm,the other comm");
//        map1.put("bills", "that one bill,the other bill");
//        map1.put("img", "barb");
//        map1.put("tweet", "@RepBarbaraLee: Don’t forget - this Sunday, I’m hosting a screening & discussion of @findingsamlowe w/ @pamadison. See you there!");
//        HashMap<String, String> map2 = new HashMap<String, String>();
//        map2.put("name", "Sen. Barbara Boxer (D)");
//        map2.put("email", "sen.boxer@opencongress.org");
//        map2.put("site", "boxer.senate.gov");
//        map2.put("comm", "House Committee on Appropriations,Subcommittee on Labor, Health and Human Services, Education and Related Agencies,Subcommittee on" +
//                " Military Construction-Veterans Affairs,Subcommittee on State, Foreign Operations, and Related Programs,House Committee on The Budget,House Democratic Steering and Policy Committee");
//        map2.put("bills", "more biils!,weee");
//        map2.put("img", "barb2");
//        map2.put("tweet", "@SenatorBoxer: Democrats have 3 words for the Senate #GOP: Do. Your. Job. bit.ly/1Lujvf0 pic.twitter.com/q4CMDUpfwj");
//        HashMap<String, String> map3 = new HashMap<String, String>();
//        map3.put("name", "Dianne Feinstein (D)");
//        map3.put("email", "sen.feinstein@opencongress.org");
//        map3.put("site", "feinstein.senate.gov");
//        map3.put("comm", "plz,no,more");
//        map3.put("bills", "can't,think");
//        map3.put("img", "dianne");
//        map3.put("tweet", "@SenFeinstein: Important discussion on combating sex trafficking with Alameda County DA @NancyOMalley—she’s doing a great job. pic.twitter.com/2pcEaAFAhL");
//        reps[0] = new Rep(map1);
//        names[0] = reps[0].getName();
//        reps[1] = new Rep(map2);
//        names[1] = reps[1].getName();
//        reps[2] = new Rep(map3);
//        names[2] = reps[2].getName();
//        reps[3] = new Rep(map2);
//        names[3] = reps[3].getName();

    }

    public void complete() {
        int numReps = repHolder.size();
        final Rep[] reps = new Rep[numReps];
        String[] images = new String[numReps];
        names = new String[numReps + 2];
        names[numReps] = zip;
        names[numReps + 1] = votes;


        int i = 0;
        for (HashMap repMap: repHolder) {
            names[i] = (String) repMap.get("name");
            reps[i] = new Rep(repMap);
            images[i] = reps[i].getSmallImg();
            i++;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_congress);
                updateView(reps);
            }
        });

        Intent rec = getIntent();
        Bundle extras = rec.getExtras();
        if (rec.hasExtra("rep")) {
            String input = extras.getString("rep");
            votes = input.split("\n")[2];
            input = input.split("\n")[0];
            for (Rep rep : reps) {
                if (input.equals(rep.getName())) {
                    Log.d("VOTE_DEBUG", "sending vote");
                    Intent toSend = new Intent(Congress.this, Detail.class);
                    toSend.putExtra("rep", rep);
                    Intent toWatch = new Intent(Congress.this, PhoneToWatchService.class);
                    toWatch.putExtra("rep", rep.getName() + "\n" + zip + "\n" + votes);
                    startService(toWatch);
                    startActivity(toSend);
                    this.finish();
                }
            }
        } else {
            Log.d("VOTE_DEBUG", "sending names");
            Intent intent = new Intent(Congress.this, PhoneToWatchService.class);
            intent.putExtra("rep", names);
            intent.putExtra("img", images);
            Log.d("VOTE_DEBUG", intent.toString());
            restart = intent;
            startService(intent);
        }
    }

    @Override
    public void notifyOnComplete(final Thread thread) {
        Log.d("ErrTest", "we got here");
        Congress.this.complete();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent check = getIntent();
        if (restart != null) {
            startService(restart);
        }
    }

    public abstract class NotifyingThread extends Thread {
        private final Set<ThreadListener> listeners = new CopyOnWriteArraySet<ThreadListener>();

        public final void addListener(final ThreadListener listener) {
            listeners.add(listener);
        }

        public final void removeListener(final ThreadListener listener) {
            listeners.remove(listener);
        }

        private final void notifyListeners() {
            for (ThreadListener listener: listeners) {
                listener.notifyOnComplete(this);
            }
        }

        @Override
        public final void run() {
            try {
                doRun();
            } finally {
                notifyListeners();
            }
        }

        public abstract void doRun();
    }


    public static class Rep implements Parcelable {
        private String name;
        private String party;
        private String email;
        private String site;
        private ArrayList<String> comm;
        private ArrayList<String> bills;
        private String imgPath;
        private String smallImg;
        private String tweet;
        private String handle;
        private String termEnd;
        private HashMap<String, Object> storage;

        public Rep(HashMap<String, Object> values) {
            storage = values;
            name = (String) values.get("name");
            party = (String) values.get("party");
            email = (String) values.get("email");
            site = (String) values.get("site");
//            String commString = values.get("comm");
            comm = (ArrayList<String>) values.get("comm");
//            String billsString = values.get("bills");
            bills = (ArrayList<String>) values.get("bills");
            imgPath = (String) values.get("img");
            smallImg = (String) values.get("smallimg");
            tweet = (String) values.get("tweet");
            handle = (String) values.get("twitterID");
            termEnd = (String) values.get("end");
        }

        public Rep(Parcel in) {
            HashMap<String, Object> values = new HashMap<String, Object>();

            in.readMap(values, null);
            storage = values;
            name = (String) values.get("name");
            party = (String) values.get("party");
            email = (String) values.get("email");
            site = (String) values.get("site");
//            String commString = values.get("comm");
            comm = (ArrayList<String>) values.get("comm");
//            String billsString = values.get("bills");
            bills = (ArrayList<String>) values.get("bills");
            imgPath = (String) values.get("img");
            smallImg = (String) values.get("smallimg");
            tweet = (String) values.get("tweet");
            handle = (String) values.get("twitterID");
            termEnd = (String) values.get("end");
        }

        public String getName() {
            return this.name;
        }

        public String getParty() { return this.party;}

        public String getMail() {
            return this.email;
        }

        public String getSite() {
            return this.site;
        }

        public ArrayList<String> getComm() {
            return this.comm;
        }

        public ArrayList<String> getBills() {
            return this.bills;
        }

        public String getImgPath() {
            return this.imgPath;
        }

        public String getSmallImg() { return this.smallImg;}

        public String getTweet() {
            return this.tweet;
        }

        public String getHandle() { return this.handle;}

        public String getTermEnd() { return this.termEnd;};

        @Override
        public void writeToParcel(Parcel parcel, int id) {
            parcel.writeMap(new HashMap(storage));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public Rep createFromParcel(Parcel in) {
                return new Rep(in);
            }

            public Rep[] newArray(int size) {
                return new Rep[size];
            }
        };
    }

    public void updateView(Rep[] reps) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_congress, null);

        TextView place = (TextView) v.findViewById(R.id.textView6);
        place.setText(votes.split("-")[2]);

        ScrollView sv = (ScrollView) v.findViewById(R.id.scrollView);

        RelativeLayout outside = new RelativeLayout(this);

        ImageView botBorder = null;

        RelativeLayout prev = null;

        int id = 9000;

        for (final Rep rep: reps) {
            if (rep == null) {
                continue;
            }

            final RelativeLayout rl = new RelativeLayout(this);
            //noinspection ResourceType
            rl.setId(id);
            id++;

            ImageView border = new ImageView(this);
            //noinspection ResourceType
            border.setId(id);
            id++;
            border.setImageResource(R.drawable.line);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);

            border.setLayoutParams(params);
            rl.addView(border);

            TextView name = new TextView(this);
            //noinspection ResourceType
            name.setId(id);
            id++;
            name.setText(rep.getName());
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            name.setTextColor(Color.BLACK);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, border.getId());
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.setMargins(0, 20, 0, 0);

            name.setLayoutParams(params);
            rl.addView(name);
//
            TextView email = new TextView(this);
            //noinspection ResourceType
            email.setId(id);
            id++;
            email.setText(rep.getMail());
//            name.setTextSize(android.R.style.TextAppearance_Large);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, name.getId());
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.setMargins(0, 0, 0, 0);

            email.setLinksClickable(true);
            Linkify.addLinks(email, Linkify.ALL);

            email.setLayoutParams(params);
            rl.addView(email);
//
            TextView site = new TextView(this);
            site.setText(rep.getSite());
            //noinspection ResourceType
            site.setId(id);
            id++;
//            name.setTextSize(android.R.style.TextAppearance_Large);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, email.getId());
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.setMargins(0, 0, 0, 0);

            site.setLinksClickable(true);
            Linkify.addLinks(site, Linkify.ALL);

            site.setLayoutParams(params);
            rl.addView(site);
//
            ImageView pic = new ImageView(this);
            //noinspection ResourceType
            pic.setId(id);
            id++;
            String path = rep.getImgPath();
            if (path == null) {
                path = "barb";
                int imageId = getResources().getIdentifier("rkolady.knowhow:drawable/" + path, null, null);
                pic.setImageResource(imageId);
            }
            Log.d("path", path);

            CircleTransform circleMaker = new CircleTransform();
            String party = rep.getParty();
            Log.d("party", party);

            if (party.equals("D")) {
                Log.d("party", "yup");
//                rl.setBackgroundColor(Color.parseColor("#80bfff"));
                circleMaker.setColor("#80bfff");
            } else if (party.equals("R")) {
//                rl.setBackgroundColor(Color.parseColor("#ff4d4d"));
                circleMaker.setColor("#ff4d4d");
            }

            Picasso.with(this).load(path).resize(400, 400).transform(circleMaker).into(pic);

            params = new RelativeLayout.LayoutParams(420, 420);
            params.addRule(RelativeLayout.BELOW, site.getId());
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.setMargins(0, 30, 0, 0);

            pic.setLayoutParams(params);
            rl.addView(pic);

            ImageView bird = new ImageView(this);
            //noinspection ResourceType
            bird.setId(id);
            id++;
            bird.setImageResource(R.drawable.twitter_low);
            bird.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("http://www.twitter.com/" + rep.getHandle()));
                    startActivity(intent);
                }
            });

            params = new RelativeLayout.LayoutParams(200, 200);
            params.addRule(RelativeLayout.BELOW, pic.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(10, 30, 20, 0);

            bird.setLayoutParams(params);
            rl.addView(bird);
//
            TextView tweet = new TextView(this);
            //noinspection ResourceType
            tweet.setId(id);
            id++;
            if (rep.getTweet() == null) {
                tweet.setText("filler");
            } else {
                tweet.setText("@" + rep.getHandle() + ": '" + rep.getTweet() + "'");
            }
//            name.setTextSize(android.R.style.TextAppearance_Large);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, pic.getId());
            params.addRule(RelativeLayout.RIGHT_OF, bird.getId());
            params.setMargins(20, 30, 0, 40);

            tweet.setLayoutParams(params);
            tweet.setElevation(2);
            tweet.setTranslationZ(4);
//            tweet.setBackgroundResource(R.drawable.myrect);
            rl.addView(tweet);

            ImageView border2 = new ImageView(this);
            //noinspection ResourceType
            border2.setId(id);
            id++;
            border2.setImageResource(R.drawable.line);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            params.setMargins(0, 100, 0, 0);

            border2.setLayoutParams(params);
//            rl.addView(border2);

//            botBorder = border2;

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (prev != null) {
                params.addRule(RelativeLayout.BELOW, prev.getId());
            }

            rl.setLayoutParams(params);

            rl.setClickable(true);
            rl.setOnTouchListener(new View.OnTouchListener() {
                private Rect rect;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        rl.setBackgroundColor(Color.argb(50, 0, 0, 0));
                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    }
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        rl.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    }
                    if(event.getAction() == MotionEvent.ACTION_MOVE){
                        rl.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    }
                    return false;
                }
            });


            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Congress.this, Detail.class);
                    intent.putExtra("rep", rep);
                    Intent toWatch = new Intent(Congress.this, PhoneToWatchService.class);
                    toWatch.putExtra("rep", rep.getName() + "\n" + zip + "\n" + votes);
                    startService(toWatch);
                    startActivity(intent);
                }
            });

            prev = rl;

            outside.addView(rl);

        }

        sv.addView(outside);

        setContentView(v);
    }

}
