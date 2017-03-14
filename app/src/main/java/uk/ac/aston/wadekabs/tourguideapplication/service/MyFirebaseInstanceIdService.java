package uk.ac.aston.wadekabs.tourguideapplication.service;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import uk.ac.aston.wadekabs.tourguideapplication.model.User;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    public static String TAG = MyFirebaseInstanceIdService.class.getName();

    @Override
    public void onTokenRefresh() {

        super.onTokenRefresh();

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if (User.getUser() != null) {
            FirebaseDatabase.getInstance().getReference("users").child(User.getUser().getUid()).child("fcmToken").setValue(refreshedToken);
        }
    }
}
