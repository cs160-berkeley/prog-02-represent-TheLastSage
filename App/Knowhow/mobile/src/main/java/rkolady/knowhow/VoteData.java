package rkolady.knowhow;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Created by RKolady on 3/10/2016.
 */
public class VoteData {

    JSONArray data;
    String votes;

    public VoteData(Context context, String county) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.election);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();

            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }

            String resultStr = builder.toString();
            JSONTokener tokener = new JSONTokener(resultStr);
            data = new JSONArray(tokener);

            Log.d("voteTest", "yes");

            for (int i = 0; i < data.length(); i++) {
                JSONObject countyData = (JSONObject) data.get(i);
                if (countyData.get("county-name").equals(county)) {
                    Log.d("voteTest", "yes");
                    votes = countyData.get("obama-percentage") + "-" + countyData.get("romney-percentage") + "-" + countyData.get("county-name") + " County, " + countyData.get("state-postal");
                    Log.d("voteTest", votes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVotes() {
        return this.votes;
    }

    public String getRandom() {
        Random rand = new Random();
        int index = rand.nextInt(data.length());
        try {
            JSONObject countyData = (JSONObject) data.get(index);
            Log.d("voteTest", "yes");
            votes = countyData.get("county-name") + "\n" + countyData.get("state-postal");
            Log.d("voteTest", votes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.votes;
    }
}
