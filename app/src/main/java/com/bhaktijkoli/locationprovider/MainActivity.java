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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private Button btnFetch;
    private TextView tvLat;
    private TextView tvLong;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressDialog progressDialog;
    private static final int REQUEST_FINE_LOCATION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnFetch = (Button) findViewById(R.id.btnFetch);
        tvLat = (TextView) findViewById(R.id.tvLat);
        tvLong = (TextView) findViewById(R.id.tvLong);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationInfo();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_FINE_LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    }
                    progressDialog.dismiss();
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_CODE);
            }
            progressDialog.dismiss();
        }
    }
}
