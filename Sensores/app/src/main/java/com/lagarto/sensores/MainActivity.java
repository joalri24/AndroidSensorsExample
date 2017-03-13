package com.lagarto.sensores;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // ----------------------------------------------------------
    // Constants
    // ----------------------------------------------------------

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int LOCATION_PERMISSION = 2;


    // ----------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------

    private ImageView mPhotoTumbnail;
    private TextView mCoordinatesTextView;
    private TextView mGravityTextView;

    private GoogleApiClient mGoogleApiClient;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;



    // ----------------------------------------------------------
    // Life Cycle Methods
    // ----------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhotoTumbnail = (ImageView) findViewById(R.id.iv_photo_tumbnail);
        mCoordinatesTextView = (TextView) findViewById(R.id.tv_coordinates_display);
        mGravityTextView = (TextView) findViewById(R.id.tv_gravity_display);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // List all sensor types available in the device.
        /*List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s:deviceSensors)
            Log.d("MainActivity", s.getType() + "");*/

        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Creates an instance of the Google API Client
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    protected void onStart()
    {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    // ----------------------------------------------------------
    // Methods
    // ----------------------------------------------------------

    /**
     * Executed when the "use camera" button is tapped.
     * @param v
     */
    public void onClickUseCamera(View v)
    {
        //Toast.makeText(this, "Utilizar la cámara del dispositivo", Toast.LENGTH_SHORT).show();

        Intent takePictureIntent = buildTakePictureIntent();

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Executed when the "get coordinates" button is tapped.
     * @param v
     */
    public void onClickUseGps(View v) {
        //Toast.makeText(this, "Utilizar el GPS del dispositivo", Toast.LENGTH_SHORT).show();
        getLocation();
    }

    /**
     * Gets the last known location using the Google API client.
     */
    private void getLocation()
    {
        // Check if the neccesary permissions are granted.
        // if not, request them.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            return;
        }

        // Get the location using the Google Api Client
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(loc != null)
        {
            Double latitude = loc.getLatitude();
            Double longitude = loc.getLongitude();
            String locationAsString = "Latitud: " + latitude + " Longitud: " + longitude;
            mCoordinatesTextView.setText(locationAsString);
        }
    }

    /**
     * Executed when the "Get gravity" button is tapped.
     */
    public void onClickGetGravity(View v)
    {
        Toast.makeText(this, "Calcular el efecto de la gravedad", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPhotoTumbnail.setImageBitmap(imageBitmap);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // ----------------------------------------------------------
    // Intent builders
    // ----------------------------------------------------------

    private Intent buildTakePictureIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent;
    }


    // ----------------------------------------------------------
    // GoogleApiClient implementation
    // ----------------------------------------------------------

    @Override
    public void onConnected(Bundle connectionHint)
    {
        // No idea what to put here...
    }


    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(this,"Connection suspended.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this,"Connection failed! :(", Toast.LENGTH_LONG).show();
    }

    // ----------------------------------------------------------
    // Sensor Event Listener implementation
    // ----------------------------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        String displayString = " Eje x: " + event.values[0] +
                " Eje y: " + event.values[1] +
                " Eje z: " + event.values[2];

        mGravityTextView.setText(displayString);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // No idea what to put here.
    }
}
