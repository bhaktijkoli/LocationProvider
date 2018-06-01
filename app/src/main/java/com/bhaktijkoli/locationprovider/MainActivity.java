package com.bhaktijkoli.locationprovider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Struct;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnFetch;
    private TextView tvLat;
    private TextView tvLong;
    private TextView tvState;
    private TextView tvCity;
    private TextView tvLocation;
    private TextView tvStreet;
    private TextView tvPremise;
    private TextView tvPin;
    private TextView tvAddress;
    private OkHttpClient okHttpClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressDialog progressDialog;

    private static final String TAG = "TAG";
    private static final String GEOLOCATION_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final String GEOLOCATION_KEY = "AIzaSyA6igkJuPk3HcPiqMeZRf4ISOLUOwonBg0";
    private static final int REQUEST_FINE_LOCATION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnFetch = (Button) findViewById(R.id.btnFetch);
        tvLat = (TextView) findViewById(R.id.tvLat);
        tvLong = (TextView) findViewById(R.id.tvLong);
        tvState = (TextView) findViewById(R.id.tvState);
        tvCity = (TextView) findViewById(R.id.tvCity);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvStreet = (TextView) findViewById(R.id.tvStreet);
        tvPremise = (TextView) findViewById(R.id.tvPremise);
        tvPin = (TextView) findViewById(R.id.tvPin);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationInfo();
            }
        });
        okHttpClient = new OkHttpClient();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationInfo();
        } else {
            finish();
        }
    }

    private void getLocationInfo() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        tvLat.setText(String.valueOf(location.getLatitude()));
                        tvLong.setText(String.valueOf(location.getLongitude()));
                        String url = GEOLOCATION_URL + location.getLatitude() + "," + location.getLongitude() + "&key=" + GEOLOCATION_KEY;
                        Log.i(TAG, "Requesting " + url);
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "An error occurred while trying to fetch the location info.", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
//                                    Log.i(TAG, response.body().string());
                                    try {
                                        JSONObject rootJsonObject = new JSONObject(response.body().string());
                                        JSONArray rootJsonArray = rootJsonObject.getJSONArray("results");
                                        JSONObject parentJsonObject = rootJsonArray.getJSONObject(0);
                                        final JSONArray jsonArray = parentJsonObject.getJSONArray("address_components");
                                        final String address = parentJsonObject.getString("formatted_address");
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tvState.setText(getLongName(jsonArray, "administrative_area_level_1"));
                                                tvCity.setText(getLongName(jsonArray, "administrative_area_level_2"));
                                                tvLocation.setText(getLongName(jsonArray, "sublocality_level_2"));
                                                tvStreet.setText(getLongName(jsonArray, "sublocality_level_3"));
                                                tvPremise.setText(getLongName(jsonArray, "premise"));
                                                tvPin.setText(getLongName(jsonArray, "postal_code"));
                                                tvAddress.setText(address);
                                            }
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_CODE);
            }
            progressDialog.dismiss();
        }
    }

    private String getLongName(JSONArray jsonArray, String type) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray jsonArray1 = jsonObject.getJSONArray("types");
                for(int n=0; n < jsonArray1.length(); n++) {
                    if(jsonArray1.getString(n).equals(type)) return jsonObject.getString("long_name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
