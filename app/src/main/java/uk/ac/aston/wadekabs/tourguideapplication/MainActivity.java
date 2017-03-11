package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItem;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItemContent;
import uk.ac.aston.wadekabs.tourguideapplication.model.SupportedPlaceTypes;
import uk.ac.aston.wadekabs.tourguideapplication.model.User;


// TODO: When internet is not available

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FilterPreferenceFragment mFilterPreferenceFragment;

    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private ClusterManager<PlaceItem> mClusterManager;

    private List<PlaceItem> mPlaceItemList = new ArrayList<>();

    private List<Circle> mCircleList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private PlaceItemRecyclerViewAdapter mPlaceItemRecyclerViewAdapter;

    private ViewPager mPhotosViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView userNameTextView = (TextView) navigationView.findViewById(R.id.userNameTextView);
        if (userNameTextView != null)
            userNameTextView.setText(User.getUser().getDisplayName());

        TextView userEmailTextView = (TextView) navigationView.findViewById(R.id.userEmailTextView);
        if (userEmailTextView != null)
            userEmailTextView.setText(User.getUser().getEmail());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFilterPreferenceFragment = (FilterPreferenceFragment) getFragmentManager().findFragmentById(R.id.filter);
        getFragmentManager().beginTransaction().hide(mFilterPreferenceFragment).commit();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .enableAutoManage(this, this)
                    .build();
        }

        createLocationRequest();

        mPlaceItemRecyclerViewAdapter = new PlaceItemRecyclerViewAdapter(PlaceItemContent.getInstance().getPlaceItemList());
        mRecyclerView = (RecyclerView) findViewById(R.id.placeitem_card_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mPlaceItemRecyclerViewAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        mPlaceItemList = PlaceItemContent.getInstance().getPlaceItemList();

        final DatabaseReference placeDetailsReference = FirebaseDatabase.getInstance().getReference("placeDetails");

        FirebaseDatabase.getInstance().getReference("nearbyPlaces").child(User.getUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                placeDetailsReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        PlaceItem placeItem = dataSnapshot.getValue(PlaceItem.class);
                        SupportedPlaceTypes types = placeItem.getTypes();

                        if (types.isRestaurant() || types.isFood()) {

                            placeItem.setId(dataSnapshot.getKey());
                            PlaceItemContent.getInstance().getPlaceItemList().add(placeItem);
                            mPlaceItemRecyclerViewAdapter.notifyDataSetChanged();

                            mClusterManager.addItem(placeItem);
                            mCircleList.add(mMap.addCircle(new CircleOptions().center(placeItem.getPosition()).visible(false)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("MainActivity", databaseError.getMessage() + databaseError.getDetails());
                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {

            @Override
            public void onChildViewAttachedToWindow(View view) {
                mMap.setPadding(0, 0, 0, mRecyclerView.getMeasuredHeight());
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int i = recyclerView.computeHorizontalScrollOffset() / (recyclerView.getMeasuredWidth() / mPlaceItemList.size()) / 4;

                if (0 <= i && i < mPlaceItemList.size())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPlaceItemList.get(i).getPosition(), 15.0f));
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

                for (Circle circle : mCircleList) {

                    VisibleRegion region = mMap.getProjection().getVisibleRegion();

                    if (region.latLngBounds.contains(circle.getCenter())) {
                        circle.setVisible(false);
                    } else {

                        Location nearLeftLocation = new Location("");
                        nearLeftLocation.setLatitude(region.nearLeft.latitude);
                        nearLeftLocation.setLongitude(region.nearLeft.longitude);

                        Location nearRightLocation = new Location("");
                        nearRightLocation.setLatitude(region.nearRight.latitude);
                        nearRightLocation.setLongitude(region.nearRight.longitude);

                        double maximumRadius = nearLeftLocation.distanceTo(nearRightLocation);
                        double minimumRadius = maximumRadius / 2;

                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(circle.getCenter().latitude);
                        markerLocation.setLongitude(circle.getCenter().longitude);

                        LatLng currentTarget = mMap.getCameraPosition().target;

                        Location currentTargetLocation = new Location("");
                        currentTargetLocation.setLatitude(currentTarget.latitude);
                        currentTargetLocation.setLongitude(currentTarget.longitude);

                        double radius = markerLocation.distanceTo(currentTargetLocation);

                        if (minimumRadius <= radius && radius <= maximumRadius) {
                            circle.setVisible(true);
                            circle.setRadius(radius);
                        } else {
                            circle.setVisible(false);
                        }
                    }
                }
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<PlaceItem>() {
            @Override
            public boolean onClusterItemClick(PlaceItem selectedPlaceItem) {
                // TODO: Make sure this is synced with correct place item list.
                mRecyclerView.smoothScrollToPosition(mPlaceItemList.indexOf(selectedPlaceItem));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceItem.getPosition(), 15.0f));
                return true;
            }
        });

        onAccessFineLocationPermissionGranted();
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

        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();

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
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        FirebaseDatabase.getInstance().getReference("locations").child(User.getUser().getUid()).child("lat").setValue(location.getLatitude());
        FirebaseDatabase.getInstance().getReference("locations").child(User.getUser().getUid()).child("lng").setValue(location.getLongitude());
    }

    public class PlaceItemRecyclerViewAdapter extends RecyclerView.Adapter<PlaceItemRecyclerViewAdapter.PlaceItemViewHolder> {

        private List<PlaceItem> mPlaceItemList;

        public PlaceItemRecyclerViewAdapter(List<PlaceItem> placeItemList) {
            mPlaceItemList = placeItemList;
        }

        @Override
        public PlaceItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.placeitem_card, parent, false);
            return new PlaceItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PlaceItemViewHolder holder, int position) {
            holder.mItem = PlaceItemContent.getInstance().getPlaceItemList().get(position);
            holder.nameTextView.setText(holder.mItem.getTitle());
            holder.addressTextView.setText(holder.mItem.getAddress());

            new PhotoTask(holder).execute(holder.mItem.getId());

//            holder.mPhotoImageView.setImageUrl(holder.mItem.getPhoto(), new ImageLoader(Volley.newRequestQueue(getApplicationContext()), new ImageLoader.ImageCache() {
//                @Override
//                public Bitmap getBitmap(String url) {
//
//                    // Get a PlacePhotoMetadataResult containing metadata for the first 10 photos.
//                    PlacePhotoMetadataResult result = Places.GeoDataApi
//                            .getPlacePhotos(mGoogleApiClient, holder.mItem.getId()).await();
//                    // Get a PhotoMetadataBuffer instance containing a list of photos (PhotoMetadata).
//                    if (result != null && result.getStatus().isSuccess()) {
//                        PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
//                        // Get the first photo in the list.
//                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
//                        // Get a full-size bitmap for the photo.
//                        Bitmap image = photo.getPhoto(mGoogleApiClient).await()
//                                .getBitmap();
//                        // Get the attribution text.
//                        CharSequence attribution = photo.getAttributions();
//
//                        System.out.println("returning image");
//
//                        return image;
//                    }
//
//                    System.out.println("returning null");
//
//                    return null;
//                }
//
//                @Override
//                public void putBitmap(String url, Bitmap bitmap) {
//
//                }
//            }));

            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PlaceItemDetailActivity.class);
                    intent.putExtra(PlaceItemDetailFragment.SELECTED_PLACE_ITEM, holder.getAdapterPosition());

                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPlaceItemList.size();
        }

        public class PlaceItemViewHolder extends RecyclerView.ViewHolder {

            public PlaceItem mItem;

            public final CardView mItemView;
            public ImageView imageView;

            // public final NetworkImageView mPhotoImageView;

            public final TextView nameTextView;
            public final TextView addressTextView;
            public final TextView descriptionTextView;

            public PlaceItemViewHolder(CardView itemView) {

                super(itemView);

                mItemView = itemView;

                // mPhotoImageView = (NetworkImageView) itemView.findViewById(R.id.imageView);

                imageView = (ImageView) findViewById(R.id.imageView);

                nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
                addressTextView = (TextView) itemView.findViewById(R.id.addressTextView);
                descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);

            }
        }
    }

    class PhotoTask extends AsyncTask<Object, Void, Bitmap> {

        PlaceItemRecyclerViewAdapter.PlaceItemViewHolder mHolder = null;

        public PhotoTask(PlaceItemRecyclerViewAdapter.PlaceItemViewHolder holder) {
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            if (params.length != 1) {
                return null;
            }

            final String placeId = (String) params[0];
            Bitmap image = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadata = result.getPhotoMetadata();
                if (photoMetadata.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadata.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    image = photo.getPhoto(mGoogleApiClient).await()
                            .getBitmap();
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadata.release();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mHolder.imageView.setImageBitmap(bitmap);
        }
    }
}
