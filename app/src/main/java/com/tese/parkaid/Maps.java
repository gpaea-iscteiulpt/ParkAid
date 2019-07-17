package com.tese.parkaid;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
import java.io.*;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.*;
import hex.genmodel.MojoModel;
*/

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener{

    private static final String TAG = Maps.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private GeoApiContext mGeoApiContext = null;

    private ArrayList<Park> mParks = new ArrayList<>();
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private Marker mMarkerSelected = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private ArrayList<Marker> mMarkersArray = new ArrayList<>();

    private String mWhereFrom;
    private Place mDestinationPlace;
    private Location mLocation;
    private Park mClosestPark;
    private float mClosestDistance;
    private double mTimeToDestination;

    private Weather mCurrentWeather;
    private String mCurrentRoad;
    private HashMap<String, String> mRoadsCoordinates = new HashMap<String, String>();
    private HashMap<String, String> mRoadsTimes = new HashMap<String, String>();
    public HashMap<String, SnippetInformation> mPolylineInformation = new HashMap<String, SnippetInformation>();
    private DecisionFactor mDecisionFactor;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mParks = (ArrayList<Park>) getIntent().getExtras().getSerializable("Parks");

        //URL weatherUrl = WeatherApi.buildUrlWeather();
        //new JsonTask().execute(weatherUrl);

        mRoadsTimes = fillRoads();
        mRoadsCoordinates = fillRoads();
        //for(HashMap.Entry<String, String> pair: mRoadsCoordinates.entrySet()) {
        //    mCurrentRoad = pair.getKey();
         //   URL tomtomUrl = TomTomApi.buildUrlTomTom(pair.getValue());
         //   new JsonTaskTomTom().execute(tomtomUrl);
        //}

        Button btReset = (Button) findViewById(R.id.btReset);
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
                addMapMarkers();
            }
        });

        if(mGeoApiContext == null){
            mGeoApiContext =  new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Location newLocation = new Location("ISCTE");
        newLocation.setLatitude(38.749135);
        newLocation.setLongitude(-9.154833);
        mLocation = newLocation;

        mWhereFrom = (String) getIntent().getStringExtra("WhereFrom");

        startLocationService();
        addMapMarkers();

        switch (mWhereFrom){
            case "FromMap":
                setCameraView(mLocation);
                break;
            case "FromSearch":
                prepareNavigate();
                break;
        }
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);
    }

    private void prepareNavigate(){
        int mSearchRadius = (int) getIntent().getIntExtra("Radius", 1000);
        mDestinationPlace = (Place) getIntent().getParcelableExtra("DestinationPlace");
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(mDestinationPlace.getLatLng()).title(mDestinationPlace.getId()));
        newMarker.setTag("Destination");

        Location mDestination = new Location(LocationManager.GPS_PROVIDER);
        mDestination.setLatitude(mDestinationPlace.getLatLng().latitude);
        mDestination.setLongitude(mDestinationPlace.getLatLng().longitude);

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(newMarker.getPosition())
                .radius(mSearchRadius)
                .strokeColor(getColor(R.color.circleStrokeBlue))
                .fillColor(getColor(R.color.circleInsideBlue)));

        try {
            mDecisionFactor = checkBestPark(mDestination);
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (PredictException e) {
            e.printStackTrace();
        }*/
        mClosestDistance = (float) mDecisionFactor.getDistance();
        mMarkerSelected = mDecisionFactor.getMarker();
        mTimeToDestination = mDecisionFactor.getDuration();


        float lessDistance = checkParkInsideRadius(circle);
        if(lessDistance > 0) {
            for (Marker marker : mMarkersArray) {
                if (marker.getTag().equals(mClosestPark)) {
                    mMarkerSelected = marker;
                    calculateDirections(marker, true);
                    break;
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Closest parking lot is at " + Math.round(mClosestDistance) + " meters from the destination. Want to navigate to there?")
                    .setTitle("No parking lot found in the search radius.");
            builder.setPositiveButton("Navigate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (Marker marker : mMarkersArray) {
                        if (marker.getTag().equals(mClosestPark)) {
                            mMarkerSelected = marker;
                            calculateDirections(marker, true);
                            break;
                        }
                    }
                }
            });
            builder.setCancelable(false);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        setCameraView(mDestination);
    }

    private float checkParkInsideRadius(Circle circle){
        float[] distance = new float[2];
        float lessDistance = 0;
        mClosestDistance = 0;

        for(Park park : mParks) {
            Location.distanceBetween(park.getLatitude(), park.getLongitude(),
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);

            if (distance[0] < circle.getRadius() && (lessDistance > distance[0] || lessDistance == 0)) {
                lessDistance = distance[0];
            }

            if (distance[0] < mClosestDistance || mClosestDistance == 0) {
                mClosestDistance = distance[0];
                mClosestPark = park;
            }
        }

        return lessDistance;
    }


    private DecisionFactor checkBestPark(Location mDestination) throws IOException{//}, PredictException {

        ArrayList<DecisionFactor> decisionFactors = new ArrayList<DecisionFactor>();

        for(Marker marker : mMarkersArray) {
            Park tempPark = (Park) marker.getTag();
            Location parkingLotLocation = new Location("ParkingLotLocation");
            parkingLotLocation.setLatitude(marker.getPosition().latitude);
            parkingLotLocation.setLongitude(marker.getPosition().longitude);
            double distance = mLocation.distanceTo(parkingLotLocation);
            double pricePerHour = tempPark.getPricePerHour();
            double distanceDestinationToPL = mDestination.distanceTo(parkingLotLocation);
            double duration = getDurationToMarker(marker);
            /*int month = 0, year = 0, hour = 0, day = 0, dayofweek = 0, flag_weekend = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime datetime = LocalDateTime.now();
                month = datetime.getMonth().getValue();
                year = datetime.getYear();
                hour = datetime.getHour();
                day = datetime.getDayOfMonth();
                dayofweek = datetime.getDayOfWeek().getValue();
                flag_weekend = 0;
                if (dayofweek == 6 || dayofweek == 7){
                    flag_weekend = 1;
                }
                dayofweek = dayofweek - 1;
            }
            int isRain = 0;
            if(mCurrentWeather.getMain().equals("Drizzle") || mCurrentWeather.getMain().equals("Rain") || mCurrentWeather.getMain().equals("Thunderstorm")){
                isRain = 1;
            }
            int isFog = 0;
            if(mCurrentWeather.getMain().equals("Fog") || mCurrentWeather.getMain().equals("Mist") || mCurrentWeather.getMain().equals("Haze") || mCurrentWeather.getMain().equals("Smoke")){
                isFog = 1;
            }
            int flag_event = 0;
            int flag_vacationperiod = 0;
            int flag_holiday = 0;
            hour = hour + (int) (duration / 3600);
            /*RowData row = new RowData();
            row.put("dayofweek", dayofweek);
            row.put("flag_vacationperiod", flag_vacationperiod);
            row.put("flag_event", flag_event);
            row.put("flag_holiday", flag_holiday);
            row.put("flag_weekend", flag_weekend);
            row.put("flag_rain", isRain);
            row.put("flag_fog", isFog);
            row.put("weather_description", mCurrentWeather.getDescription());
            row.put("weather_main", mCurrentWeather.getMain());
            row.put("year", year);
            row.put("month", month);
            row.put("day", day);
            row.put("hour", hour);*/
            double predictedModel = 0;
            String predictedClass = "";
            if (tempPark.getName().equals("Park 1")){
                predictedModel = 2;
                /*
                EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load("GBM_model_park1.zip"));
                row.put("Praça Marquês de Pombal", mRoadsTimes.get("Praça Marquês de Pombal"));
                row.put("Rua Braamcamp", mRoadsTimes.get("Rua Braamcamp"));
                row.put("Avenida da Liberdade", mRoadsTimes.get("Avenida da Liberdade"));
                row.put("Avenida Duque de Loulé", mRoadsTimes.get("Avenida Duque de Loulé"));
                row.put("Rua Joaquim António de Aguiar", mRoadsTimes.get("Rua Joaquim António de Aguiar"));
                row.put("Avenida Fontes Pereira de Melo", mRoadsTimes.get("Avenida Fontes Pereira de Melo"));
                BinomialModelPrediction p = model.predictBinomial(row);
                predictedModel = p.classProbabilities[0];
                System.out.print("Class probabilities: " + p.classProbabilities.toString());
                */
                if(predictedModel == 1){
                    predictedClass = "0%-10%";
                } else if (predictedModel == 2){
                    predictedClass = "10%-30%";
                } else if (predictedModel == 3){
                    predictedClass = "30%-50%";
                } else if (predictedModel == 4){
                    predictedClass = "50%-75%";
                } else {
                    predictedClass = "75%-100%";
                }

            } else if (tempPark.getName().equals("Park 2")){
                predictedModel = 2;
                /*
                EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load("GBM_model_park2.zip"));
                row.put("Praça Marquês de Pombal", mRoadsTimes.get("Praça Marquês de Pombal"));
                row.put("Rua Braamcamp", mRoadsTimes.get("Rua Braamcamp"));
                row.put("Rua Castilho", mRoadsTimes.get("Rua Castilho"));
                row.put("Rua Alexandre Herculano", mRoadsTimes.get("Rua Alexandre Herculano"));
                BinomialModelPrediction p = model.predictBinomial(row);
                predictedModel = p.classProbabilities[0];
                System.out.print("Class probabilities: " + p.classProbabilities.toString());
                */
                if(predictedModel == 1){
                    predictedClass = "0%-10%";
                } else if (predictedModel == 2){
                    predictedClass = "10%-35%";
                } else if (predictedModel == 3){
                    predictedClass = "35%-60%";
                } else if (predictedModel == 4){
                    predictedClass = "60%-80%";
                } else {
                    predictedClass = "80%-100%";
                }

            } else {
                predictedModel = 2;
                /*
                EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load("GBM_model_park3.zip"));
                row.put("Praça Marquês de Pombal", mRoadsTimes.get("Praça Marquês de Pombal"));
                row.put("Rua Braamcamp", mRoadsTimes.get("Rua Braamcamp"));
                row.put("Rua Castilho", mRoadsTimes.get("Rua Castilho"));
                BinomialModelPrediction p = model.predictBinomial(row);
                predictedModel = p.classProbabilities[0];
                System.out.print("Class probabilities: " + p.classProbabilities.toString());
                */
                if(predictedModel == 1){
                    predictedClass = "0%-10%";
                } else if (predictedModel == 2){
                    predictedClass = "10%-20%";
                } else if (predictedModel == 3){
                    predictedClass = "20%-55%";
                } else if (predictedModel == 4){
                    predictedClass = "55%-75%";
                } else {
                    predictedClass = "75%-100%";
                }

            }
            decisionFactors.add(new DecisionFactor(duration, (int) predictedModel, pricePerHour, distance, distanceDestinationToPL, marker, predictedClass));
        }
        return returnBestMarker(decisionFactors);
    }

    public HashMap<String, String> fillRoads(){
        // Rua Castilho - 38.724143, -9.152748
        // Rua Braamcamp - 38.722842, -9.151529
        // Praça Marquês de Pombal - 38.725588, -9.150513
        // Rua Alexandre Herculano - 38.722377, -9.150168
        // Avenida da Liberdade - 38.720662, -9.145989
        // Avenida Duque de Loulé - 38.725590, -9.148507
        // Rua Joaquim António de Aguiar - 38.725135, -9.153723
        // Avenida Fontes Pereira de Melo - 38.726866, -9.149228

        HashMap<String, String> roads = new HashMap<String, String>();
        roads.put("Rua Castilho", "38.724143%2C-9.152748");
        roads.put("Rua Braamcamp", "38.722842%2C-9.151529");
        roads.put("Praça Marquês de Pombal", "38.725588%2C-9.150513");
        roads.put("Rua Alexandre Herculano", "38.722377%2C-9.150168");
        roads.put("Avenida da Liberdade", "38.720662%2C-9.145989");
        roads.put("Avenida Duque de Loulé", "38.725590%2C-9.148507");
        roads.put("Rua Joaquim António de Aguiar", "38.725135%2C-9.1537239");
        roads.put("Avenida Fontes Pereira de Melo", "38.726866%2C-9.149228");
        return roads;
    }

    public DecisionFactor returnBestMarker(ArrayList<DecisionFactor> decisionFactors){

        double maxDuration = 0;
        double maxDistanceDestinationToCs = 0;

        if (decisionFactors.size() >= 2) {
            for (int i = 0; i < decisionFactors.size() - 1; i++) {

                DecisionFactor df1 = decisionFactors.get(i);
                DecisionFactor df2 = decisionFactors.get(i + 1);

                if (df1.getDuration() > df2.getDuration()) {
                    maxDuration = df1.getDuration();
                } else {
                    maxDuration = df2.getDuration();
                }

                if (df1.getDistanceDestinationToPL() > df2.getDistanceDestinationToPL()) {
                    maxDistanceDestinationToCs = df1.getDistanceDestinationToPL();
                } else {
                    maxDistanceDestinationToCs = df2.getDistanceDestinationToPL();
                }
            }
        }else{
            maxDuration = decisionFactors.get(0).getDuration();
            maxDistanceDestinationToCs = decisionFactors.get(0).getDistanceDestinationToPL();
        }

        for(DecisionFactor df : decisionFactors) {
            int occupancyLevel = Integer.parseInt(df.getPredictedClass().substring(4,df.getPredictedClass().length()-1));
            df.setWeight(((110 - occupancyLevel) * 0.45) + (100 - ((df.getDuration() * 100) / maxDuration) * 0.25) + (100 - ((df.getDistanceDestinationToPL() * 100) / maxDistanceDestinationToCs) * 0.2) + (50 - (df.getPricePerHour() * 10)) * 0.1);
        }

        DecisionFactor temp = decisionFactors.get(0);
        if(decisionFactors.size()>=2) {
            for (int i = 1; i < decisionFactors.size(); i++) {
                DecisionFactor df1 = decisionFactors.get(i);
                if (temp.getWeight() > df1.getWeight()) {
                    temp = temp;
                } else {
                    temp = df1;
                }
            }
        }

        return temp;
    }

    public double getDurationToMarker(Marker marker){
        double duration = 999999;

        String request = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mLocation.getLatitude() + "," + mLocation.getLongitude() +
                "&destination=" + marker.getPosition().latitude + "," + marker.getPosition().longitude + "&key=" + getString(R.string.google_maps_key);
        StringBuffer response = new StringBuffer();
        HttpInformation httpInfo = new HttpInformation(request, response);
        new GetHttp().execute(httpInfo);
        int ret = httpInfo.value;
        double tempDuration = 0;
        if (ret == 0) {
            try {
                JSONArray routes = new JSONObject(response.toString()).getJSONArray("routes");
                tempDuration = getSmallestDuration(routes);
                if(duration > tempDuration){
                    duration = tempDuration;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return duration;
    }

    private double getSmallestDuration(JSONArray routes){
        double duration = 99999;
        for(int i = 0; i<routes.length(); i++){
            double tempDuration = 0;
            try {
                tempDuration = routes.getJSONObject(i).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getDouble("value");
                if (tempDuration<duration){
                    duration = tempDuration;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }

    private void setCameraView(Location loc) {

        double bottomBoundary = loc.getLatitude() - .1;
        double leftBoundary = loc.getLongitude() - .1;
        double topBoundary = loc.getLatitude() + .1;
        double rightBoundary = loc.getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }


    private void addMapMarkers(){
        for (Park park : mParks) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.parkingfree);
            Bitmap icon = Bitmap.createScaledBitmap(b, b.getWidth()/4,b.getHeight()/4, false);
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(park.getLatitude(), park.getLongitude())).title(park.getName()).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            marker.setTag(park);
            mMarkersArray.add(marker);
        }
        mMap.setOnInfoWindowClickListener(this);
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Maps.this.startForegroundService(serviceIntent);
            } else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.".equals(service.service.getClassName())){
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void removeTripMakers(){
        for(Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if(mMarkerSelected != null) {
            mMarkerSelected.setVisible(true);
            mMarkerSelected = null;
            removeTripMakers();
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {
        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.custom_map_popup, null);

        if(marker.getTag().equals("Destination")) {

        }else {

            Park mPark = (Park) marker.getTag();

            TextView name = (TextView) popupView.findViewById(R.id.name);
            name.setText(mPark.getName());
            TextView description = (TextView) popupView.findViewById(R.id.description);
            description.setText(mPark.getDescription());
            TextView address = (TextView) popupView.findViewById(R.id.address);
            address.setText(mPark.getAddress());
            TextView occupancy = (TextView) popupView.findViewById(R.id.occupancy);
            occupancy.setText(mPark.getOccupancyPercentage() + "%");
            TextView hours = (TextView) popupView.findViewById(R.id.hours);
            hours.setText(mPark.getWorkHours());
            TextView period = (TextView) popupView.findViewById(R.id.period);
            period.setText(mPark.getWorkPeriod());
            TextView price = (TextView) popupView.findViewById(R.id.price);
            price.setText(mPark.getPricePerHour() + "€ p/h");

            Button go = (Button) popupView.findViewById(R.id.go);
            String tempString = "Go to location";
            SpannableString spanString = new SpannableString(tempString);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            go.setText(spanString);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetSelectedMarker();
                    mMarkerSelected = marker;
                    calculateDirections(marker, false);
                    popupWindow.dismiss();
                }
            });

            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

            TextView close = (TextView) popupView.findViewById(R.id.close);
            close.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(20);
            }
        }

        return false;
    }

    public void calculateDirections(Marker marker, boolean isNavegate){

        String occupancyTemp = "";
        if(isNavegate) {
            occupancyTemp =  mDecisionFactor.getPredictedClass();
        }
        final String occupancyLevel = occupancyTemp;

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.getLatitude(),
                        mLocation.getLongitude()
                )
        );

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result, occupancyLevel);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Calculate", "onFailure: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result, final String occupancyLevel){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(mPolylinesData.size() > 0){
                    for(PolylineData polylineData: mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                }

                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    mPolylineInformation.put(polyline.getId(), new SnippetInformation(occupancyLevel));

                    polyline.setClickable(true);
                    polyline.setColor(ContextCompat.getColor(Maps.this, R.color.grey));
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    mMarkerSelected.setVisible(false);

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }
                }
            }
        });
    }

    private int getDistanceBetweenTwoPoints(Marker selected){
        Location destination = new Location("Point A");
        destination.setLatitude(selected.getPosition().latitude);
        destination.setLongitude(selected.getPosition().longitude);
        Location currentLocation = new Location("Point B");
        currentLocation.setLatitude(mLocation.getLatitude());
        currentLocation.setLongitude(mLocation.getLongitude());
        return Math.round(destination.distanceTo(currentLocation));
    }

    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            index++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.lightblue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);
                SnippetInformation info = mPolylineInformation.get(polyline.getId());
                String snippetString = "Occupation at ETA: " + info.ocuppancy;
                Marker marker = mMap.addMarker(new MarkerOptions().position(endLocation)
                                        .title("Duration: " + polylineData.getLeg().duration)
                                        .snippet(snippetString));

                marker.showInfoWindow();

                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker markerSelected) {
        if(markerSelected.getTitle().contains("Trip: #")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(markerSelected.getPosition().latitude);
                            String longitude = String.valueOf(markerSelected.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(Maps.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public class JsonTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL weatherUrl = urls[0];
            String weatherSearchResults = null;

            try{
                weatherSearchResults = WeatherApi.getResponseForAPI(weatherUrl);
            }catch (IOException ioe){
                ioe.printStackTrace();
            }

            return weatherSearchResults;

        }

        @Override
        protected void onPostExecute(String weatherSearchResults){
            if(weatherSearchResults != null && !weatherSearchResults.equals("")){
                mCurrentWeather = parseJSON(weatherSearchResults);
            }
        }

        private Weather parseJSON(String weatherSearchResults){

            if(weatherSearchResults != null){
                try {
                    JSONObject rootObject = new JSONObject(weatherSearchResults);
                    mCurrentWeather = new Weather();
                    JSONArray weather = rootObject.getJSONArray("weather");
                    JSONObject weatherObj = weather.getJSONObject(0);
                    mCurrentWeather.setDescription(weatherObj.getString("description"));
                    mCurrentWeather.setMain(weatherObj.getString("main"));
                    JSONObject main = rootObject.getJSONObject("main");
                    mCurrentWeather.setTemperature(main.getDouble("temp"));
                    mCurrentWeather.setTemperatureMax(main.getDouble("temp_max"));
                    mCurrentWeather.setTemperatureMin(main.getDouble("temp_min"));
                    mCurrentWeather.setHumidity(main.getInt("humidity"));
                    mCurrentWeather.setPressure(main.getLong("pressure"));
                    JSONObject wind = rootObject.getJSONObject("wind");
                    mCurrentWeather.setWindSpeed(wind.getDouble("speed"));
                    mCurrentWeather.setWindDeg(wind.getInt("deg"));
                    mCurrentWeather.setCloudsPercentage(rootObject.getJSONObject("clouds").getInt("all"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return mCurrentWeather;
        }


    }

    public class JsonTaskTomTom extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL tomtomUrl = urls[0];
            String tomtomSearchResults = null;
            try{
                tomtomSearchResults = TomTomApi.getResponseForAPI(tomtomUrl);
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            return tomtomSearchResults;
        }

        @Override
        protected void onPostExecute(String tomtomSearchResults){
            if(tomtomSearchResults != null && !tomtomSearchResults.equals("")){
                String currentTime = parseJSON(tomtomSearchResults);
                mRoadsTimes.put(mCurrentRoad, currentTime);
            }
        }

        private String parseJSON(String tomtomSearchResults){
            String currentTravelTime = "";
            if(tomtomSearchResults != null){
                try {
                    JSONObject rootObject = new JSONObject(tomtomSearchResults);
                    JSONObject tomtom = rootObject.getJSONObject("flowSegmentData");
                    currentTravelTime = tomtom.getString("currentTravelTime");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return currentTravelTime;
        }
    }

    private void showMessage(String str){
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    public class SnippetInformation{

        private String ocuppancy;

        public SnippetInformation(String ocuppancy){
            this.ocuppancy = ocuppancy;
        }

    }

}