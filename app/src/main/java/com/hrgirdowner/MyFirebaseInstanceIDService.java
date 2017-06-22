package com.hrgirdowner;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by infogird on 09/05/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{


    private static final String TAG = "MyFirebaseIIDService";
    static String getToken;

    @Override
    public void onTokenRefresh()
    {
        getToken = "";
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("token_Main", "MyFirebaseIIDService " + refreshedToken);
        getToken = refreshedToken;
        Log.d("token_Main", "MyFirebaseIIDService//getToken " + getToken);
        //sendRegistrationToServer(getToken);
    }

    public static String  getTokenFrom()
    {
        getToken = "";
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("token_Main", "sendRegistrationToServer " + refreshedToken);
        getToken = refreshedToken;
        Log.d("token_Main", "sendRegistrationToServer//getToken " + getToken);
        return getToken;
        //You can implement this method to store the token on your server

    }

}
