package uk.ac.aston.wadekabs.tourguideapplication.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class NearbyPlacesServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(NearbyPlacesServlet.class.getName());
    private static final String KEY = "AIzaSyC6EOOcdrhZYb1TgD8xpPlRfPwDHnSddGQ";
    private static GeoApiContext sContext = new GeoApiContext(new GaeRequestHandler()).setApiKey(KEY);

    static DatabaseReference REFERENCE;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);

        // Note: Ensure that the[PRIVATE_KEY_FILENAME].json has read
        // permissions set.
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getServletContext().getResourceAsStream("/WEB-INF/Tour Guide Application-87ea06bbf5ec.json"))
                .setDatabaseUrl("https://tourist-guide-application.firebaseio.com/")
                .build();

        try {
            FirebaseApp.getInstance();
        } catch (Exception error) {
            LOG.info("doesn't exist...");
        }

        try {
            FirebaseApp.initializeApp(options);
        } catch (Exception error) {
            LOG.info("already exists...");
        }

        REFERENCE = FirebaseDatabase.getInstance().getReference();

        REFERENCE.child("locations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot userLocationSnapshot, String s) {

                double lat = userLocationSnapshot.child("lat").getValue(Double.class);
                double lng = userLocationSnapshot.child("lng").getValue(Double.class);

                // First try with google maps services java library
                PlacesSearchResponse response;
                try {
                    response = PlacesApi.nearbySearchQuery(sContext, new LatLng(lat, lng)).await();
                    for (PlacesSearchResult result : response.results) {
                        REFERENCE.child("nearby").child(userLocationSnapshot.getKey()).child(result.placeId).setValue(true);
                    }
                    return;
                } catch (Exception ignored) {
                }

                // Next, try with URL.openStream() and org.json parsing
                URL url;
                try {

                    String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + KEY + "&location=" + lat + "," + lng + "&radius=50000";

                    url = new URL(urlString);

                    InputStream is = url.openStream();

                    byte[] b = new byte[is.available()];
                    is.read(b);

                    JSONObject result = new JSONObject(new String(b));

                    JSONArray placesArray = result.getJSONArray("results");

                    for (Object object : placesArray) {
                        JSONObject place = (JSONObject) object;
                        REFERENCE.child("nearby").child(userLocationSnapshot.getKey()).child(place.getString("place_id")).setValue(true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot userLocationSnapshot, String s) {

                double lat = userLocationSnapshot.child("lat").getValue(Double.class);
                double lng = userLocationSnapshot.child("lng").getValue(Double.class);

                URL url;
                try {

                    url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + KEY + "&location=" + lat + "," + lng + "&radius=50000");

                    InputStream is = url.openStream();

                    byte[] b = new byte[is.available()];
                    is.read(b);

                    JSONObject result = new JSONObject(new String(b));
                    JSONArray placesArray = result.getJSONArray("results");

                    for (Object object : placesArray) {
                        JSONObject place = (JSONObject) object;
                        REFERENCE.child("nearby").child(userLocationSnapshot.getKey()).child(place.getString("place_id")).setValue(true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference("nearby").child(dataSnapshot.getKey()).removeValue();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LOG.severe(databaseError.getMessage() + "\t" + databaseError.getDetails());
            }
        });
    }
}
