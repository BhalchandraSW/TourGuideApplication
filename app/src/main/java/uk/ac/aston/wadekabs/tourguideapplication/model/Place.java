package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bhalchandra Wadekar on 13/03/2017.
 */

public class Place implements Parcelable {

    private String mPlaceId;
    private PlaceLocation mLocation;
    private String mName;
    private String mAddress;
    private long mPriceLevel = -1; // -1 = unknown price level
    private Map<String, Boolean> mTypes;
    private Bitmap mPhoto;

    private boolean mFavourite;
    private boolean mVisited;

    private Date mWantToVisitDate;

    public Place() {
    }

    public Place(String placeId) {
        this.mPlaceId = placeId;
    }

    protected Place(Parcel in) {
        mPlaceId = in.readString();
        mLocation = in.readParcelable(PlaceLocation.class.getClassLoader());
        mName = in.readString();
        mAddress = in.readString();
        mPriceLevel = in.readLong();
        mPhoto = in.readParcelable(Bitmap.class.getClassLoader());
        mFavourite = in.readByte() != 0;
        mVisited = in.readByte() != 0;
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getPlaceId() {
        return mPlaceId;
    }

    void setPlaceId(String placeId) {
        this.mPlaceId = placeId;
    }

    public PlaceLocation getLocation() {
        return mLocation;
    }

    public void setLocation(PlaceLocation location) {
        this.mLocation = location;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public boolean isFavourite() {
        return mFavourite;
    }

    public void setFavourite(boolean favourite) {
        this.mFavourite = favourite;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        this.mVisited = visited;
    }

    public long getPriceLevel() {
        return mPriceLevel;
    }

    public void setPriceLevel(long priceLevel) {
        this.mPriceLevel = priceLevel;
    }

    public Map<String, Boolean> getTypes() {
        return mTypes;
    }

    public void setTypes(Map<String, Boolean> types) {
        this.mTypes = types;
    }

    public Date getWantToVisitDate() {
        return mWantToVisitDate;
    }

    public void setWantToVisitDate(Date wantToVisitDate) {
        this.mWantToVisitDate = wantToVisitDate;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        this.mPhoto = photo;
    }

    boolean satisfiesFilter() {

        if (mPriceLevel == -1 || mPriceLevel == PlaceFilter.getInstance().getPriceLevel()) {
            Set<String> allowedTypes = PlaceFilter.getInstance().getTypes().keySet();
            if (allowedTypes.size() > 0) {
                for (String type : allowedTypes) {
                    if (mTypes.containsKey(type))
                        return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Place && mPlaceId.equals(((Place) obj).mPlaceId));
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPlaceId);
        dest.writeParcelable(mLocation, flags);
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeLong(mPriceLevel);
        dest.writeParcelable(mPhoto, flags);
        dest.writeByte((byte) (mFavourite ? 1 : 0));
        dest.writeByte((byte) (mVisited ? 1 : 0));
    }
}
