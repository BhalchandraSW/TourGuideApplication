package uk.ac.aston.wadekabs.tourguideapplication.backend;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.client.methods.HttpPost;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Bhalchandra Wadekar on 14/03/2017.
 */

public class NearFavouritePlaceServlet extends HttpServlet {

    private static final String SERVER_KEY = "AAAAl41yw9c:APA91bHU4aNFb9l_kVnaPtq5D9eiMiCcbyXl70bnPdO7LBZYHxvwa43rCnJrdeZC_9Q7nsI-zIGjJg7eFovUkQvQg9YVYnjc0pHBuFnjSLZEvV9pvlf886NgQNjqt8orZSkK2FIehIzk";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.doGet(req, resp);

        FirebaseDatabase.getInstance().getReference("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot userIdSnapshot, String s) {

                String token = (String) userIdSnapshot.child("fcmToken").getValue();

//                String userId = userIdSnapshot.getKey();
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

                HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
                post.addHeader("Authorization", "key=" + SERVER_KEY);
                post.addHeader("Content-Type", "application/json");

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
}
