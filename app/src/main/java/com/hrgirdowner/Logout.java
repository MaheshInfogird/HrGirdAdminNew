package com.hrgirdowner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by adminsitrator on 18/01/2017.
 */
public class Logout extends BaseActivityExp {

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    UserSessionManager session;

    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String password;

    Toolbar toolbar;
    EditText ed_Logout;
    Button btn_Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        
        session = new UserSessionManager(getApplicationContext());
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Logout.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.logoutUser();
                NavDrawerListAdapter.setSelectedPosition(0);
                Intent intent = new Intent(Logout.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavDrawerListAdapter.setSelectedPosition(0);
                Intent intent = new Intent(Logout.this, DashBoard.class);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onBackPressed()   
    {
        NavDrawerListAdapter.setSelectedPosition(0);
        Intent intent = new Intent(Logout.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
