package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, Observer, ViewPager.OnPageChangeListener {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;

    private static GoogleApiClient mGoogleApiClient;
    private FilterPreferenceFragment mFilterPreferenceFragment;

    private ClusterManager<PlaceItem> mClusterManager;

    private List<Circle> mCircleList = new ArrayList<>();

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

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
        // userNameTextView.setText(User.getUser().getDisplayName());

        TextView userEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userEmailTextView);
        // userEmailTextView.setText(User.getUser().getEmail());


        NetworkImageView userImageView = (NetworkImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);

//        RoundedBitmapDrawable profilePicture = null;

        Uri uri = User.getUser() == null ? null : User.getUser().getPhotoUrl();

//        if (uri != null) {
//            try {
//                profilePicture = RoundedBitmapDrawableFactory.create(getResources(), new URL(uri.toString()).openStream());
//                profilePicture.setCircular(true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (profilePicture != null) {
//            userImageView.setImageBitmap(profilePicture.getBitmap());
//        }

        if (uri != null) {

            userImageView.setImageUrl(uri.toString(),

                    new ImageLoader(Volley.newRequestQueue(getApplicationContext()), new ImageLoader.ImageCache() {

                        private final LruCache<String, Bitmap>
                                cache = new LruCache<>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    }));
        }

        mFilterPreferenceFragment = (FilterPreferenceFragment) getFragmentManager().findFragmentById(R.id.filter);
        getFragmentManager().beginTransaction().hide(mFilterPreferenceFragment).commit();

        PlaceContent.addNearbyObserver(this);

        // TODO: Check whether this code is actually needed
        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("Token:\t" + token);

        Intent intent = new Intent(this, LocationAwarenessService.class);
        startService(intent);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.place_summary_pager);
        mPagerAdapter = new PlaceSummaryPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(this);
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
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

    // TODO: Check why this is not working
    private void animateToFirstPlace() {

        if (mMap != null && PlaceContent.nearby().size() > 0) {

            System.out.println("Animating to first place");

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
                // mRecyclerView.smoothScrollToPosition(PlaceContent.nearby().indexOf(selectedPlaceItem.getPlace()));
                mPager.setCurrentItem(PlaceContent.nearby().indexOf(selectedPlaceItem.getPlace()), true);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void update(Observable o, Object arg) {
        mPagerAdapter.notifyDataSetChanged();
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(PlaceContent.nearby().get(position).getLocation().latLng(), 15.0f));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static class PlaceSummaryFragment extends Fragment {

        public static final String SELECTED_PLACE_INDEX = "selected_place_index";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            Bundle args = getArguments();
            int i = args.getInt(SELECTED_PLACE_INDEX);
            Place place = PlaceContent.nearby().get(i);

            View view = inflater.inflate(R.layout.place_summary, container, false);

            ImageView photo = (ImageView) view.findViewById(R.id.photo);
            new PhotoTask(photo, MainActivity.mGoogleApiClient).execute(place.getPlaceId());

            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(place.getName());

            TextView address = (TextView) view.findViewById(R.id.address);
            address.setText(place.getAddress());

            return view;
        }
    }

    private class PlaceSummaryPagerAdapter extends FragmentStatePagerAdapter {

        PlaceSummaryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = new Bundle();
            args.putInt(PlaceSummaryFragment.SELECTED_PLACE_INDEX, position);

            PlaceSummaryFragment fragment = new PlaceSummaryFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return PlaceContent.nearby().size();
        }
    }
}
