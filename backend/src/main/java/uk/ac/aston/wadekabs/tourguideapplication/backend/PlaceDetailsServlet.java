package uk.ac.aston.wadekabs.tourguideapplication.backend;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.kgsearch.v1.Kgsearch;
import com.google.api.services.kgsearch.v1.model.SearchResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class PlaceDetailsServlet extends HttpServlet {

    private static Logger LOG = Logger.getLogger("uk.ac.aston.wadekabs.tourguideapplication.backend.PlaceDetailsServlet");
    private static final String API_KEY = "AIzaSyC6EOOcdrhZYb1TgD8xpPlRfPwDHnSddGQ";
    private static GeoApiContext sContext = new GeoApiContext(new GaeRequestHandler()).setApiKey(API_KEY);

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        // Note: Ensure that the[PRIVATE_KEY_FILENAME].json has read
        // permissions set.
        final FirebaseOptions options = new FirebaseOptions.Builder()
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

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("nearby").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot userIdDataSnapshot, String s) {

                for (DataSnapshot placeIdSnapshot : userIdDataSnapshot.getChildren()) {

                    String placeId = placeIdSnapshot.getKey();

                    try {

                        DatabaseReference newPlace = reference.child("details").child(placeId);

                        PlaceDetails placeDetails = PlacesApi.placeDetails(sContext, placeId).await();

                        newPlace.child("name").setValue(placeDetails.name);
                        newPlace.child("address").setValue(placeDetails.formattedAddress);
                        newPlace.child("priceLevel").setValue(placeDetails.priceLevel);

                        for (String type : placeDetails.types) {
                            newPlace.child("types").child(type).setValue(true);
                        }

                        newPlace.child("location").child("lat").setValue(placeDetails.geometry.location.lat);
                        newPlace.child("location").child("lng").setValue(placeDetails.geometry.location.lng);

                        for (Photo photo : placeDetails.photos) {
                            newPlace.child("pictures").child(photo.photoReference).setValue(true);
                        }

                        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                        com.google.api.client.json.JsonFactory factory = new GsonFactory();

                        List<String> types = new ArrayList<>();
                        types.add("Place");

                        Kgsearch kgsearch = new Kgsearch.Builder(transport, factory, null)
                                .setApplicationName("Bala")
                                .build();

                        Kgsearch.Entities.Search search = kgsearch.entities().search()
                                .setKey(API_KEY)
                                .setQuery(placeDetails.name)
                                .setLimit(1)
                                .setTypes(types);

                        SearchResponse response = search.execute();

                        List itemListElement = response.getItemListElement();
                        for (Object item : itemListElement) {
                            ArrayMap detailedDescription = (ArrayMap) ((ArrayMap) ((ArrayMap) item).get("result")).get("detailedDescription");
                            Object articleBody;
                            if (detailedDescription != null) {
                                if ((articleBody = detailedDescription.get("articleBody")) != null) {
                                    LOG.info("Found description for " + placeId + " with name " + placeDetails.name);
                                    newPlace.child("description").setValue(String.valueOf(articleBody));
                                }
                            }
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

        try {
            resp.sendRedirect(options.getDatabaseUrl() + "/details");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
