package rkolady.knowhow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View view) {
        EditText zip = (EditText) findViewById(R.id.editText);
        Log.d("main", zip.getText().toString());
        Intent toSend = new Intent(MainActivity.this, Congress.class);
        String zipString = zip.getText().toString();
        if (zipString.length() == 0) {
            zipString = "94704";
        }
        toSend.putExtra("zip", zipString);
        startActivity(toSend);
    }
}
