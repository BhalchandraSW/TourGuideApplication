package uk.ac.aston.wadekabs.tourguideapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;

import java.util.Observable;
import java.util.Observer;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * An activity representing a single PlaceItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlaceItemListActivity}.
 */
public class FavouritePlaceDetailActivity extends AppCompatActivity implements Observer {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0;
    private Place mSelectedPlace;

    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PlacePhotoPagerAdapter mPagerAdapter;

    private PhotoFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_place_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String placeId = getIntent().getStringExtra(PlaceItemDetailFragment.SELECTED_PLACE_ID);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(PlaceItemDetailFragment.SELECTED_PLACE_ID, placeId);

            PlaceItemDetailFragment fragment = new PlaceItemDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.placeitem_detail_container, fragment)
                    .commit();
        }

        mSelectedPlace = PlaceContent.getPlace(placeId);
        if (mSelectedPlace != null) {
            mSelectedPlace.addObserver(this);
        }

        mPager = (ViewPager) findViewById(R.id.place_photos_pager);
        mPagerAdapter = new PlacePhotoPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favourite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, PlaceItemListActivity.class));

                return true;

            case R.id.action_edit:

                System.out.println("edit was clicked");

                Intent intent = new Intent(this, EditableFavouritePlaceDetailActivity.class);
                intent.putExtra(PlaceItemDetailFragment.SELECTED_PLACE_ID, mSelectedPlace.getPlaceId());

                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickShare(View view) {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType("image/*");

        share.putExtra(Intent.EXTRA_TEXT, "Hey view/download this image");

        // Create the URI from the media
        int i = mPager.getCurrentItem();

        View viewTwo = mPager.getChildAt(mPager.getCurrentItem());
        NetworkImageView imageView = (NetworkImageView) viewTwo.findViewById(R.id.photo);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FavouritePlaceDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO: Show an explanation to the user *asynchronously*
                // -- don't block this thread waiting for the user's response!
                // After the user sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(FavouritePlaceDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
            return;
        }

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) imageView.getDrawable()).getBitmap(), "", null);
        Uri uri = Uri.parse(path);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share via ..."));
    }

    @Override
    public void update(Observable o, Object arg) {
        mPagerAdapter.notifyDataSetChanged();
    }

    private class PlacePhotoPagerAdapter extends FragmentStatePagerAdapter {

        PlacePhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = new Bundle();
            args.putInt(PhotoFragment.PHOTO_INDEX, position);
            args.putString(PhotoFragment.SELECTED_PLACE, mSelectedPlace.getPlaceId());

            PhotoFragment fragment = new PhotoFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return mSelectedPlace.getPictures().size();
        }
    }
}
