package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItem;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItemContent;
import uk.ac.aston.wadekabs.tourguideapplication.model.User;

/**
 * An activity representing a single PlaceItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlaceItemListActivity}.
 */
public class PlaceItemDetailActivity extends AppCompatActivity {

    private PlaceItem mSelectedPlaceItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeitem_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
            arguments.putInt(PlaceItemDetailFragment.SELECTED_PLACE_ITEM,
                    getIntent().getIntExtra(PlaceItemDetailFragment.SELECTED_PLACE_ITEM, 0));
            PlaceItemDetailFragment fragment = new PlaceItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.placeitem_detail_container, fragment)
                    .commit();
        }

        mSelectedPlaceItem = PlaceItemContent.getInstance().getPlaceItemList().get(getIntent().getIntExtra(PlaceItemDetailFragment.SELECTED_PLACE_ITEM, 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PlaceItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickShare(View view) {

        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_favourite, null);
            share.setType("image/jpeg");
        }

        // Set the MIME type
//        share.setType(type);
//
//        // Create the URI from the media
//        File media = new File(mediaPath);
//        Uri uri = Uri.fromFile(media);
//
//        // Add the URI to the Intent.
//        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }

    public void onClickFavourite(View view) {

        mSelectedPlaceItem.setFavourite(!mSelectedPlaceItem.isFavourite());

        if (mSelectedPlaceItem.isFavourite())
            FirebaseDatabase.getInstance().getReference("favourites").child(User.getUser().getUid()).child(mSelectedPlaceItem.getId()).setValue(mSelectedPlaceItem.isFavourite());
        else
            FirebaseDatabase.getInstance().getReference("favourites").child(User.getUser().getUid()).child(mSelectedPlaceItem.getId()).removeValue();

        ((ImageView) view).setColorFilter(mSelectedPlaceItem.isFavourite() ? Color.RED : Color.GRAY);
    }

    public void onClickVisited(View view) {
        mSelectedPlaceItem.setVisited(!mSelectedPlaceItem.isVisited());

        ((ImageView) view).setColorFilter(mSelectedPlaceItem.isVisited() ? Color.GREEN : Color.GRAY);
    }
}
