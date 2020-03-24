package com.example.uber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Passenger extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {




    //flag for GPS status
    boolean isGPSEnabled=false;

    //flag for network status
    boolean isNetworkEnabled=false;


    boolean canGetLocation=false;


    //The minimum distance to change Updates in metres
    private  static  final  long MIN_DISTANCE_CHANGE_FOR_UPDATES=10;  //10 metres

    //The minimum time between updates in millisec
    private  static  final long MIN_TIME_BTW_UPDATES=200*10*1;


    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button btnRequestRide;



    private  boolean isUserRiding=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnRequestRide=findViewById(R.id.btnRequestRide);

        btnRequestRide.setOnClickListener(this);

        ParseQuery<ParseObject> queryRideRequest=ParseQuery.getQuery("rideRequest");
        queryRideRequest.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        queryRideRequest.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null && objects.size()>0)
                {
                    isUserRiding=true;
                    btnRequestRide.setText("Cancel Ride");

                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;




        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateCameraPassengerLocation(location);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            Location currentPassengerLocation = getLocation();
            updateCameraPassengerLocation(currentPassengerLocation);


        } else if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(Passenger.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Passenger.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);


            } else {


                Location currentPassengerLocation = getLocation();
                if(currentPassengerLocation==null) {
                    Log.i("location", "current passenger location is null");
                    showSettingsAlert();


                }
                else {
                    updateCameraPassengerLocation(currentPassengerLocation);

                }

            }
        }


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i("location","inside request");
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(Passenger.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Log.i("location","inside if in request");

                Location currentPassengerLocation = getLocation();
                if(currentPassengerLocation==null)
                {
                    Toast.makeText(this,"current passenger location is null",Toast.LENGTH_SHORT).show();
                    showSettingsAlert();
                }
                else {
                    updateCameraPassengerLocation(currentPassengerLocation);

                }

            }
        }

    }

    private void updateCameraPassengerLocation(Location pLocation) {


        Log.i("location5",pLocation.getProvider());
        LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));

        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

    }








    public Location getLocation()
    {

        Location location=null;
        try
        {


            //get GPS status
            isGPSEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //get network status
            isNetworkEnabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if(!isNetworkEnabled    &&  !isGPSEnabled)
            {
                //no network provider enabled
                Toast.makeText(this,"NO LOCATION PROVIDER ENABLED",Toast.LENGTH_SHORT).show();


            }
            else
            {
                this.canGetLocation=true;

                //first get location from Network Provider
                if(isNetworkEnabled)
                {
                    Log.i("Provider network","Network is enabled");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BTW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListener);


                    if(locationManager!=null)
                    {
                        Log.i("provider network","Getting Last location");
                        location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if(location!=null)
                        {
                            Log.i("provider network","Getting location Latitude longitude");

                            location.getLatitude();
                            location.getLongitude();
                        }

                    }

                }
                else
                {
                    //if GPS is enabled
                    Log.i("provider","GPS is enabled");
                    if(isGPSEnabled)
                    {

                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BTW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListener);


                            if(locationManager!=null)
                            {
                                location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if(location==null)
                                {
                                    Log.i("location","location is null");

                                }

                            }

                    }

                }


            }




        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }













    public  void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu ?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,1);
            }
        });



        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("location","Returning from location settings with request code  : "+ requestCode+"  resultcode : "+ resultCode);
        if(resultCode==RESULT_CANCELED && requestCode==1)
        {
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.btnRequestRide)
        {
            //if user is riding then cancel the ride
            if(isUserRiding)
            {
                ParseQuery<ParseObject> queryCancelRide=ParseQuery.getQuery("rideRequest");
                queryCancelRide.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                queryCancelRide.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> rideRequestList, ParseException e) {
                        if(rideRequestList.size()>0 && e==null)
                        {
                            isUserRiding=false;
                            btnRequestRide.setText("Request Ride");

                            for (ParseObject rideRequest:rideRequestList)
                            {
                                rideRequest.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null)
                                        {
                                            Toast.makeText(Passenger.this,"Ride is cancelled successfully",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
            //user is not riding so book a ride
            else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location passengerCurrentLocation = getLocation();

                    if (passengerCurrentLocation != null) {
                        final ParseObject requestRide = new ParseObject("rideRequest");
                        requestRide.put("username", ParseUser.getCurrentUser().getUsername());
                        ParseGeoPoint riderLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                        requestRide.put("riderLocation", riderLocation);
                        requestRide.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Toast.makeText(Passenger.this, "A ride request is sent", Toast.LENGTH_SHORT).show();

                                btnRequestRide.setText("Cancel Ride");
                                isUserRiding = true;
                            }
                        });
                    } else {
                        showSettingsAlert();
//                        Toast.makeText(this, "Unknown Error ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.logoutItem)
        {
            ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null)
                    {
                        Toast.makeText(Passenger.this,"Successfully Logged Out",Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(Passenger.this,SignUp.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(Passenger.this,"Error : "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}


