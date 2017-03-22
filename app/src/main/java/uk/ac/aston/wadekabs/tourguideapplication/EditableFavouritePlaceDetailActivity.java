package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
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
public class EditableFavouritePlaceDetailActivity extends AppCompatActivity implements Observer {

    private Place mSelectedPlace;

    private ViewPager mPager;

    private Date date;
    private String notes;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PlacePhotoPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable_favourite_place_detail);

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
        getMenuInflater().inflate(R.menu.editable_favourite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:

                if (date != null)
                    mSelectedPlace.setWantToVisitDate(date);

                EditText notesText = (EditText) findViewById(R.id.notes);
                notes = notesText.getText().toString();
                mSelectedPlace.setNotes(notes);

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

    public void onClickWantToVisit(final View view) {

        final EditText text = (EditText) view;

        //To show current date in the datepicker
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(EditableFavouritePlaceDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                date = calendar.getTime();

                CharSequence relativeDate = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis()); //, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_RELATIVE);

                System.out.println("Date:\t" + relativeDate);

                text.setText(relativeDate);
            }

        }, mYear, mMonth, mDay);

        mDatePicker.setTitle("Select date");

        mDatePicker.show();
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
