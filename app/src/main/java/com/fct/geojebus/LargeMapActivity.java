package com.fct.geojebus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fct.geojebus.database.BusDB;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class LargeMapActivity extends AppCompatActivity implements LocationListener {
    private GoogleMap mGoogleMap;
    private BusDB mDbHelper;
    private String mMode;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_map);

        setTitle(null);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDbHelper = BusDB.getInstance(this);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mGoogleMap = fm.getMap();
        }

        Intent intent = getIntent();
        mMode = intent.getStringExtra("mode");
        if (mMode.equals("near")) {
            mGoogleMap.setMyLocationEnabled(true);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(34.89072333, 128.62422), 17);
            mGoogleMap.moveCamera(cameraUpdate);
            mGoogleMap.animateCamera(cameraUpdate);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            //체크 퍼미션
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(
                                new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                0);
                    }
                };

                new AlertDialog.Builder(this)
                        .setTitle("GPS 설정")
                        .setMessage(
                                "GPS를 사용하면 더욱 정확한 위치정보를 얻을 수 있습니다. GPS 설정을 하시겠습니까?")
                        .setPositiveButton("설정", ok).setNegativeButton("닫기", null)
                        .show();
            }

            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
            mGoogleMap.setInfoWindowAdapter(new NearByInfoWindowAdapter());
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(LargeMapActivity.this, StopDetailActivity.class);
                    intent.putExtra("_id", Integer.parseInt(marker.getSnippet().split(",")[0]));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        } else if (intent.getStringExtra("mode").equals("stop")) {
            Cursor cursor = mDbHelper.selectBusStop(intent.getIntExtra("id", 0));
            cursor.moveToFirst();
            final LatLng StopLatLng = new LatLng(cursor.getDouble(cursor
                    .getColumnIndex("st_lat")),
                    cursor.getDouble(cursor
                            .getColumnIndex("st_lng")));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(StopLatLng, 17);
            MarkerOptions marker = new MarkerOptions().position(StopLatLng);

            mGoogleMap.moveCamera(cameraUpdate);
            mGoogleMap.animateCamera(cameraUpdate);
            mGoogleMap.addMarker(marker);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

            cursor.close();
        } else if (intent.getStringExtra("mode").equals("route")) {
            Cursor c = mDbHelper.selectBusRoute(intent.getIntExtra("id", 0));
            c.moveToFirst();
            String[] ViaStopArray = c.getString(c.getColumnIndex("rt_stop_list"))
                    .split(",");
            LatLng beforeLatlng = null;
            LatLng currentLatlng = null;
            double latsum = 0;
            double lngsum = 0;

            for (int i = 0; i < ViaStopArray.length; i++) {
                //지역코드 넣자
                Cursor cursor = mDbHelper.StopBisQuery(ViaStopArray[i], c.getString(c.getColumnIndex("rt_country")));
                cursor.moveToFirst();
                double latitude = Double.parseDouble(cursor.getString(cursor
                        .getColumnIndex("st_lat")));
                double longitude = Double.parseDouble(cursor.getString(cursor
                        .getColumnIndex("st_lng")));
                latsum = latsum + latitude;
                lngsum = lngsum + longitude;

                currentLatlng = new LatLng(latitude, longitude);

                MarkerOptions marker = new MarkerOptions()
                        .position(currentLatlng)
                        .title(i
                                + 1
                                + ". "
                                + cursor.getString(cursor
                                .getColumnIndex("st_name")))
                        .snippet(cursor.getString(cursor.getColumnIndex("st_ars")));

                if (beforeLatlng != null) {
                    mGoogleMap.addPolyline(new PolylineOptions()
                            .add(beforeLatlng, currentLatlng).geodesic(true)
                            .width(5).color(Color.parseColor("#009b77")));
                }

                beforeLatlng = currentLatlng;

                if (i == 0) {
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mGoogleMap.addMarker(marker).showInfoWindow();
                } else if (i == ViaStopArray.length - 1) {
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mGoogleMap.addMarker(marker);
                } else {
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mGoogleMap.addMarker(marker);
                }
                cursor.close();
            }

            CameraPosition INIT = new CameraPosition.Builder()
                    .target(new LatLng(latsum / ViaStopArray.length, lngsum
                            / ViaStopArray.length)).zoom(13F).bearing(0F) // orientation
                    .tilt(0F).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(INIT));
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

            c.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.animateCamera(cameraUpdate);

        if (mMode.equals("near")) {
            drawMarker(location);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        if (mMode.equals("near")) {
            registerLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMode.equals("near")) {
            locationManager.removeUpdates(this);
        }
    }

    public void drawMarker(Location location) {
        mGoogleMap.clear();

        Cursor mCursor = mDbHelper.selectLocation(location.getLatitude(),
                location.getLongitude());

        if (mCursor.moveToFirst()) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                LatLng latlng = new LatLng(
                        mCursor.getDouble(mCursor.getColumnIndex("st_lat")),
                        mCursor.getDouble(mCursor.getColumnIndex("st_lng")));
                MarkerOptions marker = new MarkerOptions()
                        .position(latlng)
                        .title(mCursor.getString(mCursor
                                .getColumnIndex("st_name")))
                        .snippet(mCursor.getString(mCursor.getColumnIndex("_id")) + "," + mCursor.getString(mCursor.getColumnIndex("st_ars")));

                mGoogleMap.addMarker(marker);
                mCursor.moveToNext();
            }
        }

        mCursor.close();
    }

    public class NearByInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        NearByInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.large_map_infowindow, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = (TextView) myContentsView.findViewById(R.id.title);
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = (TextView) myContentsView.findViewById(R.id.snippet);
            final String snippet = marker.getSnippet();
            tvSnippet.setText(snippet.split(",")[1]);
            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }
}
