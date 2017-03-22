package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Bhalchandra Wadekar on 08/03/2017.
 */

public class User {

    private static User user;

    private FirebaseUser sUser;
    private DatabaseReference reference;

    private User() {
    }

    public static User getInstance() {
        if (user == null) {
            user = new User();
        }
        return user;
    }

    public FirebaseUser getUser() {
        return sUser;
    }

    public void setUser(FirebaseUser user) {

        sUser = user;

        reference = FirebaseDatabase.getInstance().getReference("users").child(sUser.getUid());
    }

    public DatabaseReference getFirebaseReference() {
        return reference;
    }
}
