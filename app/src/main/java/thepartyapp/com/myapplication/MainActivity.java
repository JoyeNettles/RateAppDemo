package thepartyapp.com.myapplication;

import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final String applicationID = "Zwqc9EQYOTiVxhjejkS7kKqeqTuKshLeHS13cLVf";
    private final String clientID = "77U76sDyHqEg0KIkPqyxeABxKv8SW4qKHpJyh0kE";
    public static final String TAG = MainActivity.class.getSimpleName();

    private ParseGeoPoint currentLocation; // the current location of the user
    private String partyResponse; // the user rating of the party

    private GoogleApiClient mGoogleApiClient; // Used to get location
    private LocationRequest mLocationRequest; // Will handle current location changes
    private LocationListener mLocationListener; // handles changing locations

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; // request code to send to Google Play services for handling failure

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
//        Parse.enableLocalDatastore(this);
        Parse.initialize(this, applicationID, clientID);

        // Retrieve Location Information
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //  Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 seconds, in milliseconds

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                handleNewLocation(location); // Creates geoPoint of last location
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


        // Sets up buttons and collects user input
        Button yesButton = (Button) findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Communicates POSITIVE response to database
                ParseObject rowObject = new ParseObject("Activity"); // Used to communicate records to database
                rowObject.put("isHot", "yes");
                rowObject.put("location", currentLocation);
                rowObject.saveInBackground();
                Toast.makeText(MainActivity.this, "Awesome! Continue to have a great time!", Toast.LENGTH_LONG).show();
            }
        });
        Button kindaButton = (Button) findViewById(R.id.kindaButton);
        kindaButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Communicates medium response to database
                ParseObject rowObject = new ParseObject("Activity"); // Used to communicate records to database
                rowObject.put("isHot", "kinda");
                rowObject.put("location", currentLocation);
                rowObject.saveInBackground();
                Toast.makeText(MainActivity.this, "It will get better...", Toast.LENGTH_LONG).show();
            }
        });
        Button noButton = (Button) findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Communicates negative response to database
                ParseObject rowObject = new ParseObject("Activity"); // Used to communicate records to database
                rowObject.put("isHot", "no");
                rowObject.put("location", currentLocation);
                rowObject.saveInBackground();
                Toast.makeText(MainActivity.this, "Thanks for your honesty.", Toast.LENGTH_LONG).show();
            }
        });

    }


    /*
    Creates a new Parse GeoPoint given a location
     */
    public void handleNewLocation(Location location){
        currentLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    @Override
    /*
    Gets last location when client is connected
     */
    public void onConnected(Bundle bundle) {

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) mLocationListener);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                  }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    /*
    Reconnects client if app is canceled
     */
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    /*
    Disconnects client if our app is disconnected
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) mLocationListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
