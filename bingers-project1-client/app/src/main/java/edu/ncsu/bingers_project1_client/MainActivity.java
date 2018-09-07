package edu.ncsu.bingers_project1_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button stopButton = findViewById(R.id.btnStop);
    Button startButton = findViewById(R.id.btnStart);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user presses the START button */
    public void startTracking(View view) {
        stopButton.setVisibility(View.GONE);
        startButton.setVisibility(View.VISIBLE);
        android.content.Intent trackingIntent = new android.content.Intent(view.getContext(), LocationTracker.class);
        EditText hostField = findViewById(R.id.username);
        EditText usernameField = findViewById(R.id.host);
        trackingIntent.putExtra("HOST", hostField.getText().toString());
        trackingIntent.putExtra("USERNAME", usernameField.getText().toString());
        startActivityForResult(trackingIntent, 0);
    }
}
