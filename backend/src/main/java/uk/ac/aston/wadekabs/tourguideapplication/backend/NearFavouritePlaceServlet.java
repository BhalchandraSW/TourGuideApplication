package uk.ac.aston.wadekabs.tourguideapplication.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bhalchandra Wadekar on 14/03/2017.
 */

public class NearFavouritePlaceServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(NearFavouritePlaceServlet.class.getName());
    private static final String SERVER_KEY = "AAAAl41yw9c:APA91bHU4aNFb9l_kVnaPtq5D9eiMiCcbyXl70bnPdO7LBZYHxvwa43rCnJrdeZC_9Q7nsI-zIGjJg7eFovUkQvQg9YVYnjc0pHBuFnjSLZEvV9pvlf886NgQNjqt8orZSkK2FIehIzk";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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


        FirebaseDatabase.getInstance().getReference("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot userIdSnapshot, String s) {

                final String userId = userIdSnapshot.getKey();
                final String token = (String) userIdSnapshot.child("fcmToken").getValue();

                LOG.info("Filling out nearby and favourites for " + userId);

                final List<String> nearbyPlaces = new ArrayList<>();
                final List<String> favouritePlaces = new ArrayList<>();

                FirebaseDatabase.getInstance().getReference("nearby").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                            nearbyPlaces.add(placeSnapshot.getKey());
                        }
                        FirebaseDatabase.getInstance().getReference("favourites").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                                    favouritePlaces.add(placeSnapshot.getKey());
                                }

                                LOG.info("Nearby places:\n");
                                for (String nearby : nearbyPlaces) {
                                    LOG.info(nearby);
                                }

                                LOG.info("Favourite places:\n");
                                for (String nearby : favouritePlaces) {
                                    LOG.info(nearby);
                                }

                                for (String favouritePlace : favouritePlaces) {
                                    if (nearbyPlaces.contains(favouritePlace)) {
                                        try {
                                            LOG.info("Found common place " + favouritePlace);
                                            sendNotification(token, favouritePlace);
                                            break;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


//                final LatLng[] latLng = new LatLng[1];
//
//                FirebaseDatabase.getInstance().getReference("locations").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot locationSnapshot) {
//                        double lat = locationSnapshot.child("lat").getValue(Double.class);
//                        double lng = locationSnapshot.child("lng").getValue(Double.class);
//                        latLng[0] = new LatLng(lat, lng);
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });


//                StringEntity body = new StringEntity("");
//                post.setEntity(body);
//
//                { "notification": {
//                    "title": "Portugal vs. Denmark",
//                            "body": "5 to 1"
//                },
//                    "to" : "bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1..."
//                }
//
//                HttpClient client = HttpClientBuilder.create().build();
//                HttpResponse response = client.execute(post);
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

    public void sendNotification(String token, String place) throws IOException {

        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.addHeader("Authorization", "key=" + SERVER_KEY);
        post.addHeader("Content-Type", "application/json");

        JSONObject notification = new JSONObject();
        notification.put("title", place);
        notification.put("body", "");

        JSONObject body = new JSONObject();
        body.put("to", token);
        body.put("notification", notification);

        StringEntity bodyEntity = new StringEntity(body.toString());

        LOG.info(body.toString(4));

        post.setEntity(bodyEntity);

        LOG.info(post.getRequestLine().toString());

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(post);

        InputStream is = response.getEntity().getContent();
        byte[] b = new byte[is.available()];
        is.read(b);

        LOG.info("Sending notification response:\t" + new String(b));
    }
}
