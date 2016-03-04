package rkolady.knowhow;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Congress extends AppCompatActivity {

    private String[] names;
    private boolean started = false;
    private String zip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congress);
        int numReps = 4;
        Rep[] reps = new Rep[numReps];
        names = new String[numReps + 1];
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("name", "Rep. Barbara Lee (D)");
        map1.put("email", "lee@house.gov");
        map1.put("site", "lee.house.gov");
        map1.put("comm", "that one comm,the other comm");
        map1.put("bills", "that one bill,the other bill");
        map1.put("img", "barb");
        map1.put("tweet", "@RepBarbaraLee: Don’t forget - this Sunday, I’m hosting a screening & discussion of @findingsamlowe w/ @pamadison. See you there!");
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("name", "Sen. Barbara Boxer (D)");
        map2.put("email", "sen.boxer@opencongress.org");
        map2.put("site", "boxer.senate.gov");
        map2.put("comm", "House Committee on Appropriations,Subcommittee on Labor, Health and Human Services, Education and Related Agencies,Subcommittee on" +
                " Military Construction-Veterans Affairs,Subcommittee on State, Foreign Operations, and Related Programs,House Committee on The Budget,House Democratic Steering and Policy Committee");
        map2.put("bills", "more biils!,weee");
        map2.put("img", "barb2");
        map2.put("tweet", "@SenatorBoxer: Democrats have 3 words for the Senate #GOP: Do. Your. Job. bit.ly/1Lujvf0 pic.twitter.com/q4CMDUpfwj");
        HashMap<String, String> map3 = new HashMap<String, String>();
        map3.put("name", "Dianne Feinstein (D)");
        map3.put("email", "sen.feinstein@opencongress.org");
        map3.put("site", "feinstein.senate.gov");
        map3.put("comm", "plz,no,more");
        map3.put("bills", "can't,think");
        map3.put("img", "dianne");
        map3.put("tweet", "@SenFeinstein: Important discussion on combating sex trafficking with Alameda County DA @NancyOMalley—she’s doing a great job. pic.twitter.com/2pcEaAFAhL");
        reps[0] = new Rep(map1);
        names[0] = reps[0].getName();
        reps[1] = new Rep(map2);
        names[1] = reps[1].getName();
        reps[2] = new Rep(map3);
        names[2] = reps[2].getName();
        reps[3] = new Rep(map2);
        names[3] = reps[3].getName();
        updateView(reps);

        Intent rec = getIntent();
        if (rec.hasExtra("zip")) {
            zip = rec.getStringExtra("zip");
            names[4] = zip;
            Log.d("zip", zip);
        }

        Bundle extras = rec.getExtras();
        if (rec.hasExtra("rep")) {
            String input = extras.getString("rep");
            zip = input.split("\n")[1];
            input = input.split("\n")[0];
            for (Rep rep : reps) {
                if (input.equals(rep.getName())) {
                    Log.d("VOTE_DEBUG", "sending vote");
                    Intent toSend = new Intent(Congress.this, Detail.class);
                    toSend.putExtra("rep", rep);
                    Intent toWatch = new Intent(Congress.this, PhoneToWatchService.class);
                    toWatch.putExtra("rep", rep.getName() + "\n" + zip);
                    startService(toWatch);
                    startActivity(toSend);
                }
            }
        } else {
            Log.d("VOTE_DEBUG", "sending names");
            Intent intent = new Intent(Congress.this, PhoneToWatchService.class);
            intent.putExtra("rep", names);
            startService(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent check = getIntent();
        if (check.hasExtra("rep") == false) {
            Intent intent = new Intent(Congress.this, PhoneToWatchService.class);
            intent.putExtra("rep", names);
            startService(intent);
            started = true;
        }
    }


    public static class Rep implements Parcelable {
        private String name;
        private String email;
        private String site;
        private ArrayList<String> comm;
        private ArrayList<String> bills;
        private String imgPath;
        private String tweet;
        private HashMap<String, String> storage;

        public Rep(HashMap<String, String> values) {
            storage = values;
            name = values.get("name");
            email = values.get("email");
            site = values.get("site");
            String commString = values.get("comm");
            comm = new ArrayList<String>(Arrays.asList(commString.split(",")));
            String billsString = values.get("bills");
            bills = new ArrayList<String>(Arrays.asList(billsString.split(",")));
            imgPath = values.get("img");
            tweet = values.get("tweet");
        }

        public Rep(Parcel in) {
            HashMap<String, String> values = new HashMap<String, String>();

            in.readMap(values, null);
            storage = values;
            name = values.get("name");
            email = values.get("email");
            site = values.get("site");
            String commString = values.get("comm");
            comm = new ArrayList<String>(Arrays.asList(commString.split(",")));
            String billsString = values.get("bills");
            bills = new ArrayList<String>(Arrays.asList(billsString.split(",")));
            imgPath = values.get("img");
            tweet = values.get("tweet");
        }

        public String getName() {
            return this.name;
        }

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

        public String getTweet() {
            return this.tweet;
        }

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

        ScrollView sv = (ScrollView) v.findViewById(R.id.scrollView);

        RelativeLayout rl = new RelativeLayout(this);

        ImageView botBorder = null;

        int id = 9000;

        for (final Rep rep: reps) {
            if (rep == null) {
                continue;
            }

            ImageView border = new ImageView(this);
            //noinspection ResourceType
            border.setId(id);
            id++;
            border.setImageResource(R.drawable.line);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (botBorder != null) {
                params.addRule(RelativeLayout.BELOW, botBorder.getId());
                params.setMargins(0, 10, 0, 0);
            }

            border.setLayoutParams(params);
            rl.addView(border);

            TextView name = new TextView(this);
            //noinspection ResourceType
            name.setId(id);
            id++;
            name.setText(rep.getName());
//            name.setTextSize(android.R.style.TextAppearance_Large);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, border.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(5, 0, 0, 0);

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
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(5, 0, 0, 0);

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
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.setMargins(5, 0, 0, 0);

            site.setLayoutParams(params);
            rl.addView(site);
//
            ImageView pic = new ImageView(this);
            //noinspection ResourceType
            pic.setId(id);
            id++;
            String path = rep.getImgPath();
            Log.d("path", path);
            int imageId = getResources().getIdentifier("rkolady.knowhow:drawable/" + path, null, null);
            pic.setImageResource(imageId);

            pic.setClickable(true);
            pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Congress.this, Detail.class);
                    intent.putExtra("rep", rep);
                    Intent toWatch = new Intent(Congress.this, PhoneToWatchService.class);
                    toWatch.putExtra("rep", rep.getName() + "\n" + zip);
                    startService(toWatch);
                    startActivity(intent);
                }
            });

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, border.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.setMargins(0, 0, 5, 0);

            pic.setLayoutParams(params);
            rl.addView(pic);
//
            TextView tweet = new TextView(this);
            //noinspection ResourceType
            tweet.setId(id);
            id++;
            tweet.setText(rep.getTweet());
//            name.setTextSize(android.R.style.TextAppearance_Large);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, pic.getId());
            params.setMargins(5, 20, 0, 0);

            tweet.setLayoutParams(params);
            rl.addView(tweet);

            ImageView border2 = new ImageView(this);
            //noinspection ResourceType
            border2.setId(id);
            id++;
            border2.setImageResource(R.drawable.line);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, tweet.getId());
//            params.setMargins(0, 100, 0, 0);

            border2.setLayoutParams(params);
            rl.addView(border2);

            botBorder = border2;
        }

        sv.addView(rl);

        setContentView(v);
    }

}
