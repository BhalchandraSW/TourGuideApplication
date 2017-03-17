package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItem;
import uk.ac.aston.wadekabs.tourguideapplication.model.User;
import uk.ac.aston.wadekabs.tourguideapplication.service.LocationAwarenessService;


// TODO: When internet is not available
// TODO: Do not check location here instead use LocationAwarenessService
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, Observer {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private static final String MAP_BOTTOM_PADDING = "map_bottom_padding";

    private GoogleMap mMap;
    private int mMapBottonPadding;

    private GoogleApiClient mGoogleApiClient;
    private FilterPreferenceFragment mFilterPreferenceFragment;

    private LocationRequest mLocationRequest;

    private ClusterManager<PlaceItem> mClusterManager;

    private List<Circle> mCircleList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private PlaceItemRecyclerViewAdapter mPlaceItemRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("onCreate: called\tsavedInstanceState: " + savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // TODO: Show logged in user's profile picture in navigation drawer

        TextView userNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userNameTextView);
        userNameTextView.setText(User.getUser().getDisplayName());

        TextView userEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userEmailTextView);
        userEmailTextView.setText(User.getUser().getEmail());

        NetworkImageView userImageView = (NetworkImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
        Uri uri = User.getUser().getPhotoUrl();
        if (uri != null)
            userImageView.setImageUrl(uri.toString(), new ImageLoader(Volley.newRequestQueue(getApplicationContext()), new ImageLoader.ImageCache() {

                private final LruCache<String, Bitmap>
                        cache = new LruCache<String, Bitmap>(20);

                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            }));
        mFilterPreferenceFragment = (FilterPreferenceFragment) getFragmentManager().findFragmentById(R.id.filter);
        getFragmentManager().beginTransaction().hide(mFilterPreferenceFragment).commit();

        mPlaceItemRecyclerViewAdapter = new PlaceItemRecyclerViewAdapter(PlaceContent.nearby(), mGoogleApiClient, "nearby");
        mRecyclerView = (RecyclerView) findViewById(R.id.placeitem_card_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mPlaceItemRecyclerViewAdapter);

        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);

        PlaceContent.addNearbyObserver(this);

        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("Token:\t" + token);

//        LocationAwarenessService service = new LocationAwarenessService();
        Intent intent = new Intent(this, LocationAwarenessService.class);
        startService(intent);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        System.out.println("onRestoreInstaceState: called");
        mMapBottonPadding = savedInstanceState.getInt(MAP_BOTTOM_PADDING);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MAP_BOTTOM_PADDING, mMapBottonPadding);
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    private void animateToFirstPlace() {
        if (mMap != null && PlaceContent.nearby().size() > 0) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(PlaceContent.nearby().get(0).getLocation().latLng(), 15.0f));
        }
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

        if (id == R.id.action_filter) {

            // Toggle filter preference fragment's visibility
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            if (mFilterPreferenceFragment.isHidden())
                fragmentTransaction.show(mFilterPreferenceFragment);
            else
                fragmentTransaction.hide(mFilterPreferenceFragment);

            fragmentTransaction.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_home:
                break;

            case R.id.nav_favourites:

                Intent intent = new Intent(getApplicationContext(), PlaceItemListActivity.class);
                startActivity(intent);

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (mMapBottonPadding > 0) {
            mMap.setPadding(0, 0, 0, mMapBottonPadding);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                mMapBottonPadding = recyclerView.getMeasuredHeight();
                mMap.setPadding(0, 0, 0, mMapBottonPadding);

                if (PlaceContent.nearby().size() > 0) {
                    int i = recyclerView.computeHorizontalScrollOffset() / (recyclerView.computeHorizontalScrollRange() / PlaceContent.nearby().size());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(PlaceContent.nearby().get(i).getLocation().latLng(), 15.0f));
                }
            }
        });

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);

        mClusterManager = new ClusterManager<>(getApplicationContext(), mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                handleOffCameraPlaces();
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<PlaceItem>() {
            @Override
            public boolean onClusterItemClick(PlaceItem selectedPlaceItem) {
                // TODO: Make sure this is synced with correct place item list.
                mRecyclerView.smoothScrollToPosition(PlaceContent.nearby().indexOf(selectedPlaceItem.getPlace()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceItem.getPosition(), 15.0f));
                return true;
            }
        });

        onAccessFineLocationPermissionGranted();

        animateToFirstPlace();

        updateUI();
    }

    private void handleOffCameraPlaces() {

        VisibleRegion region = mMap.getProjection().getVisibleRegion();

        for (Circle circle : mCircleList) {

            // if place is visible do not show circle
            if (region.latLngBounds.contains(circle.getCenter())) {
                circle.setVisible(false);
            } else {

                // else show circle with appropriate radius if place is just off the map

                Location circleLocation = new Location("");
                circleLocation.setLatitude(circle.getCenter().latitude);
                circleLocation.setLongitude(circle.getCenter().longitude);

                Location nearLeftLocation = new Location("");
                nearLeftLocation.setLatitude(region.nearLeft.latitude);
                nearLeftLocation.setLongitude(region.nearLeft.longitude);

                Location nearRightLocation = new Location("");
                nearRightLocation.setLatitude(region.nearRight.latitude);
                nearRightLocation.setLongitude(region.nearRight.longitude);

                Location farLeftLocation = new Location("");
                nearLeftLocation.setLatitude(region.farLeft.latitude);
                nearLeftLocation.setLongitude(region.farLeft.longitude);

                Location farRightLocation = new Location("");
                farRightLocation.setLatitude(region.farRight.latitude);
                farRightLocation.setLongitude(region.farRight.longitude);

                double horizontalDistance = nearLeftLocation.distanceTo(nearRightLocation);
                double verticalDistance = nearLeftLocation.distanceTo(farLeftLocation);

                double maximumRadius = nearLeftLocation.distanceTo(nearRightLocation);
                double minimumRadius = maximumRadius / 2;

                LatLng currentTarget = mMap.getCameraPosition().target;

                Location currentTargetLocation = new Location("");
                currentTargetLocation.setLatitude(currentTarget.latitude);
                currentTargetLocation.setLongitude(currentTarget.longitude);

                double radius = circleLocation.distanceTo(currentTargetLocation);

                if (minimumRadius <= radius && radius <= maximumRadius) {
                    circle.setVisible(true);
                    circle.setRadius(radius * .6);
                } else {
                    circle.setVisible(false);
                }
            }
        }
    }

    private void onAccessFineLocationPermissionGranted() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // TODO: Show an explanation to the user *asynchronously*
                    // -- don't block this thread waiting for the user's response!
                    // After the user sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
                }
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onAccessFineLocationPermissionGranted();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void update(Observable o, Object arg) {
        mPlaceItemRecyclerViewAdapter.notifyDataSetChanged();
        updateUI();
    }

    public void updateUI() {

        mMap.clear();

        mClusterManager.clearItems();
        for (Place place : PlaceContent.nearby())
            mClusterManager.addItem(new PlaceItem(place));

        mCircleList.clear();
        for (Place place : PlaceContent.nearby())
            mCircleList.add(mMap.addCircle(new CircleOptions().center(place.getLocation().latLng()).visible(false)));
    }
}
