package uk.ac.aston.wadekabs.tourguideapplication.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NearFavouritePlaceMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("Notification about place:\t" + remoteMessage.getNotification().getTitle());
        // TODO: Create a new notification here
    }
}
