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
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class PlaceDetailsServlet extends HttpServlet {

    private static Logger Log = Logger.getLogger("uk.ac.aston.wadekabs.tourguideapplication.backend.PlaceDetailsServlet");
    private static GeoApiContext sContext = new GeoApiContext(new GaeRequestHandler()).setApiKey("AIzaSyCpbB9sM5IyFaG9OsW8MfuEahPnWxHTTEA");

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
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
            Log.info("doesn't exist...");
        }

        try {
            FirebaseApp.initializeApp(options);
        } catch (Exception error) {
            Log.info("already exists...");
        }

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        
        reference.child("nearbyPlaces").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot userIdDataSnapshot, String s) {

                for (DataSnapshot placeIdSnapshot : userIdDataSnapshot.getChildren()) {

                    String placeId = placeIdSnapshot.getKey();

                    try {

                        PlaceDetails placeDetails = PlacesApi.placeDetails(sContext, placeId).await();

                        reference.child("placeDetails").child(placeId).child("name").setValue(placeDetails.name);
                        reference.child("placeDetails").child(placeId).child("address").setValue(placeDetails.formattedAddress);
                        reference.child("placeDetails").child(placeId).child("priceLevel").setValue(placeDetails.priceLevel);

                        for (String type : placeDetails.types) {
                            reference.child("placeDetails").child(placeId).child("types").child(type).setValue(true);
                        }

                        reference.child("placeDetails").child(placeId).child("location").child("lat").setValue(placeDetails.geometry.location.lat);
                        reference.child("placeDetails").child(placeId).child("location").child("lng").setValue(placeDetails.geometry.location.lng);

                        for (Photo photo : placeDetails.photos) {
                            reference.child("placeDetails").child(placeId).child("photos").child(photo.photoReference).setValue(true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
}
