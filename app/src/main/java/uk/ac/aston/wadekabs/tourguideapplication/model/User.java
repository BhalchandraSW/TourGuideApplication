package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class User {

    private static FirebaseUser sUser;

    public static FirebaseUser getUser() {
        return sUser;
    }

    public static void setUser(FirebaseUser user) {
        sUser = user;
    }
}
