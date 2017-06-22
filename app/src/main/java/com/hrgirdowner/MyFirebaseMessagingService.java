package com.hrgirdowner;
//legacy server key :  AIzaSyCB2JQb8yqaHDKciPJ_zBeAdA4bveNmla4
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by infogird on 09/05/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService
{


    private static final String TAG = "MyFirebaseMsgService";

    public static final String MyPREFERENCES = "MyPrefs" ;//MyPref
    public static final String MyPREFERENCES_MSG_NOTI = "MyPref";
    private static final String PREFER_NAME_NOTI = "repManager";

    int PRIVATE_MODE = 0;
    SharedPreferences pref,pref_noti_msg,pref_noti_repManager;
    SharedPreferences.Editor editor;
    String msg="";
    static int count_new_noti = 0;
    boolean flag = true;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        boolean notification = pref.getBoolean("notification1", false);
        Log.i("noti","boolean "+notification);

        if (notification)
         {

             try
             {
                 Log.i("json_type", "==" + remoteMessage.getMessageType());
                 JSONObject json = new JSONObject(String.valueOf(remoteMessage.getData()));

                 Log.i("normal_noti ", "notification==" + json);
                 String title = json.getString("title");
                 Log.i("json_title: ", "" + title);
                 String body = json.getString("message");
                 Log.i("json_body: ", "" + body);

                 String title1 = title.replaceAll("&", " ");
                 Log.i("title1: ", "" + title1);
                 String body1 = body.replaceAll("&", " ");
                 Log.i("body: ", "" + body1);

                 sendNotification(title1, body1);

             }
             catch (Exception e)
             {
                 Log.e("Exception: ", "" + e.getMessage());
             }

          /* String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.i("body: ", ""+ body);
            Log.i("title: ", ""+ title);*/

             //sendNotification(title, body);
             //}
         /* else
         {
            Log.i("disable", ""+notification);
        }*/
         }else {
             Log.i("noti","login first");
         }
    }

    private void sendNotification(String messageTitle, String messageBody)
    {
        //count_new_noti = 0;
        count_new_noti++;
        Log.i("count_new_noti","=="+count_new_noti);

       // Dashbord.tool_tv_count.setText(String.valueOf(count_new_noti));

        Intent intent1 = null;
        Intent intent2 = null;
        Intent intent3 = null;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (messageTitle.equals("Birthday"))
        {
            intent1 = new Intent(this, BirthdayActivity.class);
            intent1.putExtra("date_noti",true);
            //intent1 = new Intent(this,Notification.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder1 = new NotificationCompat.Builder(this)

                    .setSmallIcon(R.mipmap.ic_launcher)
                    //.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(messageTitle).bigText(messageBody))
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {1000, 1000, 1000, 1000})
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent1);

            notificationManager.notify(0, notificationBuilder1.build());

        }
        //Anniversary
        if (messageTitle.equals("Marriage Anniversary"))
        {
            intent2 = new Intent(this, MarriageAnnActivity.class);
            intent2.putExtra("date_noti",true);

            //intent2 = new Intent(this,Notification.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder2 = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {1000, 1000, 1000, 1000})
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent2);

            notificationManager.notify(1, notificationBuilder2.build());

        }
        if (messageTitle.equals("Work Anniversary"))
        {
           /* String date = sdf.format(calendar.getTime());
            Log.i("noti","msg="+date);
            editor.putString("date",date);*/

            intent3 = new Intent(this, WorkAnnActivity.class);
            intent3.putExtra("date_noti",true);

            //intent3 = new Intent(this,Notification.class);
            intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent3 = PendingIntent.getActivity(this, 0, intent3, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder3 = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {1000, 1000, 1000, 1000})
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent3);

            notificationManager.notify(2, notificationBuilder3.build());

        }
        if (messageTitle.equals("Leave"))
        {
            Intent intent = new Intent(this,Notification.class);
            intent.putExtra("title_flag",true);
            intent.putExtra("noti_body",messageBody);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent3 = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder4 = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {1000, 1000, 1000, 1000})
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent3);

            notificationManager.notify(3, notificationBuilder4.build());

        }
        if (messageTitle.equals("Leave Status"))
        {
            Intent intent = new Intent(this,Notification.class);
            intent.putExtra("title_flag",true);
            intent.putExtra("noti_body",messageBody);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent5 = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder5 = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {1000, 1000, 1000, 1000})
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent5);

            notificationManager.notify(4, notificationBuilder5.build());

        }

       /* if (flag)
        {
            flag = false;
            Dashbord.tool_notification_layout1.setVisibility(View.VISIBLE);
            //Dashbord.updateNotification();
        }*/
        DashBoard.tool_tv_count.setText(String.valueOf(count_new_noti));

    }
}
