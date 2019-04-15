package com.krayrr.Activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.krayrr.Global;
import com.krayrr.Helper.API;
import com.krayrr.Helper.AppController;
import com.krayrr.Helper.DbHelper;
import com.krayrr.Helper.SQLiteHandler;
import com.krayrr.Helper.SessionManager;
import com.krayrr.R;
import com.krayrr.SensorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    TextView Distance,calcualteAMount,mapStop;
    FusedLocationProviderClient mFusedLocationClient;
    double lat1;
    double lon1;
    List<Float> list = new ArrayList<>();
    DbHelper sqLiteHandler;
    DecimalFormat df = new DecimalFormat("#.##");
    SQLiteHandler db;
    SessionManager sessionManager;
    private final static int INTERVAL = 1000 * 60 * 5 ; //2 minutes
    Handler mHandler = new Handler();
    SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy");
    ProgressDialog progressDialog;
    Boolean isStarted = false;
    String strlocation;
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            live_ride_record();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Distance=findViewById(R.id.tlDist);
        mapStop=findViewById(R.id.mapStop);
        calcualteAMount=findViewById(R.id.calcualteAMount);
        progressDialog = new ProgressDialog(MapsActivity.this);

        startService(new Intent(getApplicationContext(),SensorService.class));
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire();
        sqLiteHandler= new DbHelper(this);
        db= new SQLiteHandler(this);
        sessionManager = new SessionManager(this);
        getSupportActionBar().setTitle(Global.campaign_name);
        getSupportActionBar().setSubtitle(Global.Date(Global.camp_start_date) + " TO " + Global.Date(Global.camp_end_date));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


        Button button = findViewById(R.id.getDistance);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("CAR Total Distance :-" +sum(list)* -1);
                System.out.println("CAR Total Distance KM :-" +Math.abs((sum(list) / 1000)));

                //     Toast.makeText(getApplicationContext(),"CAR Total Distance KM :-"+ getResults(),Toast.LENGTH_LONG).show();

                live_ride_record();

            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                mHandlerTask.run();
            }
        }, 5000);

        mHandlerTask.run();

        mapStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                car_ride_stop();
            }
        });

    }



    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.mnLogout) {



            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MapsActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(MapsActivity.this);
            }
            builder.setCancelable(false);
            builder.setTitle("Logout..!")
                    .setMessage("Do you want to logout...")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sessionManager.setLogin(false);
                            db.deleteUsers();
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


            return true;
        }else if (id == R.id.mnContact) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void live_ride_record() {


        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, API.live_ride_record, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject object= new JSONObject(response);
                    if (object.getBoolean("status")){
                        //   Global.successDilogue(MapsActivity.this,object.getString("success_msg"));
                        Log.d("Start Ride",response);

                        Toast.makeText(MapsActivity.this, "Data Successfully uploaded", Toast.LENGTH_SHORT).show();
                        sqLiteHandler.deleteride();
                    }else{
                        Global.successDilogue(MapsActivity.this,object.getString("error_msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){            @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String>  params = new HashMap<String, String>();

            params.put("user_id"        , Global.uid);
            params.put("session_id"     , Global.Sessionid);
            params.put("ride_id"        , Global.ride_id);
            params.put("campaign_id"    , Global.campaign_id);
            params.put("live_coordinate", strlocation);
            params.put("ride_kilometer" , String.valueOf(df.format(sum(list)/1000)).replace("-",""));

            return params;
        }};

        AppController.getInstance().addToRequestQueue(stringRequest);

    }

    @Override
    public void onPause() {
        super.onPause();

        onRestart();
        //stop location updates when Activity is no longer active
/*
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                //  Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            // Log.e(TAG, "Can't find style. Error: ", e);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        live_ride_record();
        car_ride_stop();
    }

    private void DrawPath(double latitude, double longitude) {


        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //  mMap.getUiSettings().setZoomControlsEnabled(false);


        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }

        if (myLocation != null) {
            LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            lat1 = latitude;
            lon1 = longitude;




            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location myLocation) {

                    final double latitude = myLocation.getLatitude();
        float bearing = myLocation.getBearing();


                    final double longitude = myLocation.getLongitude();

                    LatLng latLng = new LatLng(latitude, longitude);
                    //Draw polyline


                    UpdateMarker (latLng ,bearing);

                    drawPolygon(latitude, longitude);

                    String Location = String.valueOf(latitude) + " , " + String.valueOf(longitude);
                    sqLiteHandler.addNewLocation(Location);

                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });
        }

    }

    private void UpdateMarker(LatLng latLng, float myLocation) {
        mCurrLocationMarker.setPosition(latLng);
       //    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        mCurrLocationMarker.setRotation(myLocation);
       // MarkerAnimation.move(mGoogleMap,mCurrLocationMarker, myLocation);
       // animateMarker(myLocation);
       // MarkerAnimation.animateMarkerToGB(mCurrLocationMarker, latLng, new LatLngInterpolator.Spherical());


    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                strlocation = String.valueOf(location.getLatitude()).concat(" ").concat(String.valueOf(location.getLongitude()));

                DrawPath( location.getLatitude(), location.getLongitude());

                //Place current location marker

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                mCurrLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("My Location")
                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_green_car_marker)));
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void writeToFile(String content) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/test.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
    }
    private void drawPolygon(double latitude, double longitude) {
        List<LatLng> polygon = new ArrayList<>();
        //old lat and long
        polygon.add(new LatLng(lat1, lon1));
        //new lat and long
        polygon.add(new LatLng(latitude, longitude));
        mGoogleMap.addPolygon(new PolygonOptions()
                .addAll(polygon)
                .strokeColor(Color.parseColor("#03A9F4"))
                .strokeWidth(7)
                .fillColor(Color.parseColor("#B3E5FC"))
        );

        lat1 = latitude;
        lon1 = longitude;

        System.out.println("Distance :-" + getTripDistance(polygon));
        list.add( getTripDistance(polygon));
        Distance.setText(String.valueOf(df.format(sum(list)/1000)).replace("-",""));

        double Km = Double.parseDouble(String.valueOf(df.format(sum(list)/1000)).replace("-",""));
        double totalEarning = Km * 1.16;
        calcualteAMount.setText(String.valueOf(df.format(totalEarning)));


        //   int KM =( /1000);


        System.out.println("Total Distance :-" +sum(list));

    }

    float getTripDistance(List<LatLng> vertices) {
        float totalDistance = 0;

        for (int i = 0; i < vertices.size()-1; i++) {
            Location tLoc1 = new Location("");
            Location tLoc2 = new Location("");

            tLoc1.setLatitude(vertices.get(i).latitude);
            tLoc1.setLongitude(vertices.get(i).longitude);

            tLoc2.setLatitude(vertices.get(i + 1).latitude);
            tLoc2.setLongitude(vertices.get(i + 1).longitude);

            totalDistance += tLoc1.distanceTo(tLoc2);
        }

        return Math.abs(totalDistance);
    }

    public float sum(List<Float> list) {
        float sum = 0;
        for (Float i: list) {
            sum += i;
        }
        return Math.abs(sum);
    }

    private JSONArray getResults() {

        //String myPath =getDatabasePath("my_db_test.db");// Set path to your database
/*
        String myTable = "user";//Set name of your table

//or you can use `context.getDatabasePath("my_db_test.db")`

        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase(String.valueOf(getDatabasePath("krayar")), null, SQLiteDatabase.OPEN_READONLY);

        String searchQuery = "SELECT  * FROM " + myTable;*/
        Cursor cursor = sqLiteHandler.getLocation();

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            //  Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("Location", resultSet.toString());
        return resultSet;

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    static public void rotateMarker(final Marker marker, final float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1000;

        final LinearInterpolator interpolator = new LinearInterpolator();
      //  L.d("Bearing: "+toRotation);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;
                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                }
            }
        });
    }

    public void stop(View view) {
        final Dialog dialog = new Dialog(MapsActivity.this);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.success_dilogue);
        Button button = dialog.findViewById(R.id.btnsucces);
        TextView textView = dialog.findViewById(R.id.successtext);
        textView.setText("You have successfully completed your ride.");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }


    private void car_ride_stop() {


        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, API.car_ride_stop, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject object= new JSONObject(response);
                    if (object.getBoolean("status")){
                        //   Global.successDilogue(MapsActivity.this,object.getString("success_msg"));
                        Log.d("Start Ride",response);

                        Toast.makeText(MapsActivity.this, "Data Successfully uploaded", Toast.LENGTH_SHORT).show();
                        sqLiteHandler.deleteride();
                    }else{
                        Global.successDilogue(MapsActivity.this,object.getString("error_msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){            @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            Map<String, String>  params = new HashMap<String, String>();

            params.put("user_id"        , Global.uid);
            params.put("session_id"     , Global.Sessionid);
            params.put("ride_id"        , Global.ride_id);
            params.put("campaign_id"    , Global.campaign_id);
            params.put("live_coordinate", strlocation);
            params.put("status"         , "stop");
            params.put("ride_kilometer" , String.valueOf(df.format(sum(list)/1000)).replace("-",""));

            return params;
        }};

        AppController.getInstance().addToRequestQueue(stringRequest);

    }



}