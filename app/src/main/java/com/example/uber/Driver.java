package com.example.uber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class Driver extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> listRideRequests;

    private  ArrayList<LatLng> listRidersLatLong;

    private  ArrayList<String> listRidersUserName;


    private boolean isGPSEnabled=false;

    private boolean isNetworkEnabled=false;


    //The minimum distance to change Updates in metres
    private  static  final  long MIN_DISTANCE_CHANGE_FOR_UPDATES=10;  //10 metres

    //The minimum time between updates in millisec
    private  static  final long MIN_TIME_BTW_UPDATES=200*10*1;


    private LocationListener locationListener;
    private LocationManager locationManager;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        btnGetRequests=findViewById(R.id.btnGetRequests);
        listView=findViewById(R.id.listView);

        listRideRequests=new ArrayList();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,listRideRequests);
        listView.setAdapter(arrayAdapter);


        btnGetRequests.setOnClickListener(this);



        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);


        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                Log.i("listRideRequests","onLocationChanged Before updating by  :"+listRideRequests.toString());
                Toast.makeText(Driver.this,"Updating List - Location Changed",Toast.LENGTH_SHORT).show();
                updateRequestsListView(location);
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



        listView.setOnItemClickListener(this);
        listRidersLatLong=new ArrayList<>();
        listRidersUserName=new ArrayList<>();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        Log.i("location","inside request");

        if(requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(Driver.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {

                Log.i("location","inside if in request");


                Location currentDriverLocation=getLocation();
                if(currentDriverLocation==null)
                {
                    Toast.makeText(Driver.this,"current driver location is null",Toast.LENGTH_SHORT).show();
                    showSettingsAlert();
                }
//                else
//                {
//                    Log.i("listRideRequests ","onRequestPermissionResult    Before updating :"+listRideRequests.toString());
//                    updateRequestsListView(currentDriverLocation);
//                }
            }
        }


    }


    private void showSettingsAlert() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu ?");


        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1);

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

        Log.i("location","Returning from location settings with request code : "+requestCode+"  resultcode : "+ resultCode);

        if(resultCode==RESULT_CANCELED && requestCode==1)
        {
            finish();
            startActivity(getIntent());
        }

    }

    private Location getLocation() {

        Location location=null;
        try {


            //get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

//        get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                //no network provider enabled
                Toast.makeText(this, "No location provider enabled ",Toast.LENGTH_SHORT).show();

            } else {

                //first check if network provider is enabled
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                    if (locationManager != null) {
                        Log.i("provider network", "Getting last location");
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

//                    if(location!=null)
//                    {
//                        Log.i("provider network","Getting location latitude and longitude");
//
//                    location.getLatitude();
//                    location.getLongitude();
//
//                    }

                    }

                }
                //if GPS is enabled
                else if (isGPSEnabled) {
                    Log.i("provider", "GPS is enabled");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            Log.i("location", "location is null");
                        }

                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return  location;
    }

    private void updateRequestsListView(Location driverLocation) {

        if(driverLocation!=null)
        {
//            listRideRequests.clear();



            final ParseGeoPoint driverCurrentLocation=new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());
            ParseQuery<ParseObject> requestRideQuery=ParseQuery.getQuery("rideRequest");

            requestRideQuery.whereNear("riderLocation",driverCurrentLocation);
            requestRideQuery.whereDoesNotExist("driver");

            requestRideQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null) {


                        if (objects.size() > 0) {
                            
                            if(listRideRequests.size()>0)
                                listRideRequests.clear();
                            if(listRidersLatLong.size()>0)
                                listRidersLatLong.clear();
                            if(listRideRequests.size()>0)
                                listRideRequests.clear();
                            
                            for (ParseObject nearRequest : objects) {

                                ParseGeoPoint riderLocation=nearRequest.getParseGeoPoint("riderLocation");
                                LatLng rLatLng=new LatLng(riderLocation.getLatitude(),riderLocation.getLongitude());
                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(nearRequest.getParseGeoPoint("riderLocation"));

                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;

                                listRideRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));
                                listRidersLatLong.add(rLatLng);
                                listRidersUserName.add(nearRequest.get("username").toString());

                            }
//                            Log.i("listRideRequests","After updating :"+listRideRequests.toString());

                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                    else
                    {
                        Toast.makeText(Driver.this,"Sorry . There are no ride requests yet . ",Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
        else
        {
            Log.i("location","Driver Location is null");
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnGetRequests)
        {



            if(Build.VERSION.SDK_INT<23)
            {
                Location currentDriverLocation=getLocation();
//                Log.i("listRideRequests","btnGetNearbyRequest   Before updating :"+listRideRequests.toString());
                updateRequestsListView(currentDriverLocation);

            }
            else if(Build.VERSION.SDK_INT>=23)
            {

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);

                }
                else
                {

                    Location currentDriverLocation=getLocation();
                    if(currentDriverLocation==null)
                    {
                        Log.i("location"," current driver location is null");
                        showSettingsAlert();
                    }
                    else {
//                        Log.i("listRideRequests"," btnGetNearbyRequest Before updating :"+listRideRequests.toString());
                        updateRequestsListView(currentDriverLocation);
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
                        Toast.makeText(Driver.this,"Successfully Logged Out",Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(Driver.this,SignUp.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(Driver.this,"Error : "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Location driverLocation=getLocation();
        if(driverLocation==null)
        {
            showSettingsAlert();
        }else {


            Intent intent = new Intent(this, ViewLocationsMapsActivity.class);
            intent.putExtra("riderLatitude", listRidersLatLong.get(position).latitude);
            intent.putExtra("riderLongitude", listRidersLatLong.get(position).longitude);
            intent.putExtra("driverLatitude", driverLocation.getLatitude());
            intent.putExtra("driverLongitude", driverLocation.getLongitude());
            intent.putExtra("riderUsername", listRidersUserName.get(position));
            startActivity(intent);


        }



    }
}
