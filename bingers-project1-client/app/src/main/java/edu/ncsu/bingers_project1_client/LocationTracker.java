package edu.ncsu.bingers_project1_client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class LocationTracker extends AppCompatActivity implements LocationListener {

    /* Constant Fine Location Permission */
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    Button stopButton;
    Button startButton;

    double latitude;
    double longitude;
    String username;
    TextView logText;
    String host;
    String serverURL;

    Handler handler = new Handler();
    int delay; //milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        stopButton = findViewById(R.id.btnStop);
        startButton = findViewById(R.id.btnStart);

        stopButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);

        logText = findViewById(R.id.logging);

        username = getIntent().getStringExtra("USERNAME");
        host = getIntent().getStringExtra("HOST");
        serverURL = "http://" + host + "/locationupdate";

        // logText.append(username + " " + host + "\n");
        logText.append("Tracking started...\n");

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);
                android.content.Intent myIntent = new android.content.Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });

        final RequestQueue queue = newRequestQueue(this); // this = context

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

        final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        while (location == null) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Long timestampLong = System.currentTimeMillis();
        String timestamp = timestampLong.toString();

        HashMap<String, String>  params = new HashMap<>();
        params.put("username", username);
        params.put("timestamp", timestamp);
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("newSession", "true");

        JsonObjectRequest postRequest = new JsonObjectRequest(serverURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
//                            double roundedDistance = new BigDecimal(response.getDouble("Distance Traveled")).setScale(3, RoundingMode.HALF_UP).doubleValue();
                            logText.append("Distance traveled: " + response.getDouble("totalDist") + " m\n");
                            delay = response.getInt("delay");

                            handler.postDelayed(new Runnable(){
                                public void run(){
                                    if ( ContextCompat.checkSelfPermission( LocationTracker.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                                        ActivityCompat.requestPermissions( LocationTracker.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, MY_PERMISSION_ACCESS_FINE_LOCATION);
                                    }
                                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();

                                    Long timestampLong = System.currentTimeMillis();
                                    String timestamp = timestampLong.toString();

                                    HashMap<String, String>  params = new HashMap<>();
                                    params.put("username", username);
                                    params.put("timestamp", timestamp);
                                    params.put("latitude", String.valueOf(latitude));
                                    params.put("longitude", String.valueOf(longitude));
                                    params.put("newSession", "false");

                                    JsonObjectRequest postRequest = new JsonObjectRequest(serverURL, new JSONObject(params),
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
//                                    double roundedDistance = new BigDecimal(response.getDouble("Distance Traveled")).setScale(3, RoundingMode.HALF_UP).doubleValue();
                                                        logText.append("Distance traveled: " + response.getDouble("totalDist") + " m\n");
                                                        delay = response.getInt("delay");
                                                    } catch (Exception e) {
                                                        logText.append("JSONException: " + e.getMessage() + "\n");
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            logText.append("Error: " + error.getMessage() + "\n");
                                        }
                                    });

                                    queue.add(postRequest);

                                    handler.postDelayed(this, delay);
                                }
                            }, delay);
                        } catch (Exception e) {
                            logText.append("JSONException: " + e.getMessage() + "\n");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logText.append("Error: " + error.getMessage() + "\n");
            }
        });

        queue.add(postRequest);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}