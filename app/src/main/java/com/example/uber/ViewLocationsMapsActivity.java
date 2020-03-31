package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Button btnStartRide;
    private String riderUsername,driverUsername;
    private LatLng driverLocation,riderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnStartRide=findViewById(R.id.btnStartRide);

        riderUsername=getIntent().getStringExtra("riderUsername");
        driverUsername= ParseUser.getCurrentUser().getUsername();

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

        driverLocation=new LatLng(getIntent().getDoubleExtra("driverLatitude",0),getIntent().getDoubleExtra("driverLongitude",0));

        riderLocation=new LatLng(getIntent().getDoubleExtra("riderLatitude",0),getIntent().getDoubleExtra("riderLongitude",0));



        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riderLocation,15));

        LatLngBounds.Builder builder=new LatLngBounds.Builder();

        Marker driverMarker=mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver"));
        Marker riderMarker=mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider"));

        ArrayList<Marker> myMarkers=new ArrayList<>();
        myMarkers.add(driverMarker);
        myMarkers.add(riderMarker);
        for (Marker marker:myMarkers)
        {
            builder.include(marker.getPosition());

        }

        LatLngBounds bounds=builder.build();


        int screenHeight=getResources().getDisplayMetrics().heightPixels;
        int screenWidth=getResources().getDisplayMetrics().widthPixels;
        Log.i("Screen","Screen Height : "+screenHeight+"Screen Width :  "+screenWidth);

        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,screenWidth-150,screenHeight-200,15);
        mMap.animateCamera(cameraUpdate);



        btnStartRide.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnStartRide)
        {

            ParseQuery<ParseObject> queryRideRequest=ParseQuery.getQuery("rideRequest");
            queryRideRequest.whereEqualTo("username",riderUsername);
            queryRideRequest.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null) {

                        if (objects.size() > 0) {

                            for(ParseObject parseObject:objects)
                            {

                                parseObject.put("driverUsername",driverUsername);
                                parseObject.put("driverLocLatitude",driverLocation.latitude);
                                parseObject.put("driverLocLongitude",driverLocation.longitude);
                                parseObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null)
                                        {

                                            Intent intentGoogleMaps=new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+driverLocation.latitude+","+driverLocation.longitude+"&"+"daddr="+riderLocation.latitude+","+riderLocation.longitude));
                                            startActivity(intentGoogleMaps);



                                        }
                                        else
                                        {
                                            Toast.makeText(ViewLocationsMapsActivity.this,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                        }
                    }
                }
            });
        }
    }
}
