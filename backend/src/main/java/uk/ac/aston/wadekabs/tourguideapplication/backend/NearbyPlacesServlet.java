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
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.io.IOException;
import java.util.Objects;
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

    private static DatabaseReference REFERENCE;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        // Note: Ensure that the[PRIVATE_KEY_FILENAME].json has read
        // permissions set.
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getServletContext().getResourceAsStream("/WEB-INF/Tour Guide Application-87ea06bbf5ec.json"))
                .setDatabaseUrl("https://tourist-guide-application.firebaseio.com/")
                .build();

        try {
            FirebaseApp.getInstance();
        } catch (Exception error) {
            resp.getWriter().println("doesn't exist...");
            LOG.info("doesn't exist...");
        }

        try {
            FirebaseApp.initializeApp(options);
        } catch (Exception error) {
            resp.getWriter().println("already exists...");
            LOG.info("already exists...");
            return;
        }

        REFERENCE = FirebaseDatabase.getInstance().getReference();

        ChildEventListener listener = REFERENCE.child("locations").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot userLocationSnapshot, String s) {
                addNearbyPlaces(userLocationSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot userLocationSnapshot, String s) {
                removeNearbyPlaces(userLocationSnapshot);
                addNearbyPlaces(userLocationSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot userLocationSnapshot) {
                removeNearbyPlaces(userLocationSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LOG.severe(databaseError.getMessage() + "\t" + databaseError.getDetails());
            }
        });

        resp.getWriter().println(listener + " successfully added.");
    }

    private void addNearbyPlaces(DataSnapshot userLocationSnapshot) {

        double lat = userLocationSnapshot.child("lat").getValue(Double.class);
        double lng = userLocationSnapshot.child("lng").getValue(Double.class);

        NearbySearchRequest request = PlacesApi.nearbySearchQuery(sContext, new LatLng(lat, lng));
        request.radius(50000);

        PlacesSearchResponse response = null;
        try {
            response = request.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (PlacesSearchResult result : Objects.requireNonNull(response).results) {
            REFERENCE.child("nearby").child(userLocationSnapshot.getKey()).child(result.placeId).setValue(true);
        }
    }

    private void removeNearbyPlaces(DataSnapshot userLocationSnapshot) {
        FirebaseDatabase.getInstance().getReference("nearby").child(userLocationSnapshot.getKey()).removeValue();
    }
}
