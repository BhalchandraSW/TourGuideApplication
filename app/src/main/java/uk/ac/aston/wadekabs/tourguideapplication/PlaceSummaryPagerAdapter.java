package uk.ac.aston.wadekabs.tourguideapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * Created by Bhalchandra Wadekar on 21/03/2017.
 */

class PlaceSummaryPagerAdapter extends FragmentStatePagerAdapter {

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
