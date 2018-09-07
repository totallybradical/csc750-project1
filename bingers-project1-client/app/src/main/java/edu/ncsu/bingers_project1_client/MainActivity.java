package edu.ncsu.bingers_project1_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button stopButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stopButton = findViewById(R.id.btnStop);
        startButton = findViewById(R.id.btnStart);
        stopButton.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.VISIBLE);
    }

    /** Called when the user presses the START button */
    public void startTracking(View view) {
        android.content.Intent trackingIntent = new android.content.Intent(view.getContext(), LocationTracker.class);
        EditText hostText = findViewById(R.id.host);
        EditText usernameText = findViewById(R.id.username);
        trackingIntent.putExtra("HOST", hostText.getText().toString());
        trackingIntent.putExtra("USERNAME", usernameText.getText().toString());
        startActivityForResult(trackingIntent, 0);
    }
}
