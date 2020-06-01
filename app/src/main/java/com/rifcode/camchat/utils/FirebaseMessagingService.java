package com.rifcode.camchat.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.rifcode.camchat.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getNotification() != null) {

            /////// first you need to create in file index.js  //////////////////
            String notificationTitle = remoteMessage.getNotification().getTitle();
            String notificationMessage = remoteMessage.getNotification().getBody();
            String click_action = remoteMessage.getNotification().getClickAction();
            String from_user_id = remoteMessage.getData().get("userIDvisited");

//            startActivity(new Intent(this, CallingActivity.class));
//            Toast.makeText(this, click_action, Toast.LENGTH_SHORT).show();

                //Toast.makeText(this,from_user_id+"---"+click_action, Toast.LENGTH_SHORT).show();

                NotificationCompat.Builder mBuilder =
                         new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.video_chat_logo)
                                .setContentTitle(notificationTitle)
                                .setContentText(notificationMessage).
                                        setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.phone_ringing));
                mBuilder.setVibrate(new long[]{500, 1000, 500, 1000, 500});


                Intent resultIntent = new Intent(click_action);
                resultIntent.putExtra("userIDvisited", from_user_id);

                /////////// Because clicking the notification opens a new ("special") activity, there's
                ////////////// no need to create an artificial back stack.
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                //////////////////////////////////
                mBuilder.setContentIntent(resultPendingIntent);

                // Sets an ID for the notification
                int mNotificationId = (int) System.currentTimeMillis();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            // mNotifyMgr.cancel(mNotificationId);
        }
    }


}
