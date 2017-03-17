package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class PlaceLocation implements Parcelable {

    private double lat;
    private double lng;

    public static final Creator<PlaceLocation> CREATOR = new Creator<PlaceLocation>() {
        @Override
        public PlaceLocation createFromParcel(Parcel in) {
            return new PlaceLocation(in);
        }

        @Override
        public PlaceLocation[] newArray(int size) {
            return new PlaceLocation[size];
        }
    };

    public PlaceLocation() {
        lat = lng = 0;
    }

    private PlaceLocation(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public LatLng latLng() {
        return new LatLng(lat, lng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }
}
