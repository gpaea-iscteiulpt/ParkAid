package com.tese.parkaid;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.tese.parkaid.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.tese.parkaid.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class Home extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener  {

    private boolean mLocationPermissionGranted = false;
    private static final String TAG = Home.class.getSimpleName();
    private Location mLastLocation;
    private LocationManager mLocationManager;
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;

    //	CST3Yb4sdzAfXdd5KQUdiUgAGm7Ce6ut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getLocationPermission();

        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.input_search);
        mPlaceAutocompleteFragment.setFilter(new AutocompleteFilter.Builder().setCountry("ID").build());
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                final LatLng destination = place.getLatLng();


                Toast.makeText(Home.this, "Teste", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Home.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, Maps.class);
        intent.putExtra("LastLocation", mLastLocation);
        intent.putExtra("WhereFrom", "FromMap");
        startActivity(intent);
    }

    public void goForSearch(View view){
        Intent intent = new Intent(this, Maps.class);
        intent.putExtra("LastLocation", mLastLocation);
        intent.putExtra("WhereFrom", "FromSearch");
        EditText radius = (EditText) findViewById(R.id.searchRadius);
        Constants.setSearchRadius(Integer.parseInt(radius.getText().toString()));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                    mLastLocation = getLastKnownLocation();
                } else {
                    getLocationPermission();
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMapsEnabled()) {
            if (mLocationPermissionGranted) {
                mLastLocation = getLastKnownLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public boolean isMapsEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This application requires GPS to run, do you want to enable it?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mLastLocation = getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
