package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by adminsitrator on 23/01/2017.
 */
public class Settings extends BaseActivityExp
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    public static final String MyPREFERENCES = "MyPrefs" ;
    
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    ConnectionDetector cd;
    CheckInternetConnection internetConnection;
    ProgressDialog progressDialog = null;

    String url_http;
    String Url;
    String response, myJson;
    String uId, oldPass, newPass, confirmPass;

    Toolbar toolbar;
    EditText ed_oldPass, ed_newPass, ed_cnfrmPass;
    Button btn_resetPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Settings");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = shared_pref.getString("url", "");
        //Log.i("Url", Url);
        
        pref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        uId = pref.getString("uId", "");
        //Log.i("uId", uId);

        setUpDrawer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        
        ed_oldPass = (EditText)findViewById(R.id.old_password);
        ed_newPass = (EditText)findViewById(R.id.new_password);
        ed_cnfrmPass = (EditText)findViewById(R.id.confirm_password);
        btn_resetPass = (Button)findViewById(R.id.reset_password);
        
        btn_resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConnection.hasConnection(getApplicationContext()))
                {
                    if (checkPassword()) 
                    {
                        resetPassword();
                    }
                }
                else {
                    Toast.makeText(Settings.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    public boolean checkPassword()
    {
        oldPass = ed_oldPass.getText().toString();
        newPass = ed_newPass.getText().toString();
        confirmPass = ed_cnfrmPass.getText().toString();

        if (oldPass.equals(""))
        {
            ed_newPass.setError("Please enter old password");
            return false;
        }
        
        if (newPass.equals(""))
        {
            ed_newPass.setError("Please enter new password");
            return false;
        }
        
        if (confirmPass.equals(""))
        {
            ed_cnfrmPass.setError("Please enter confirm password");
            return false;
        }
        
        if (newPass.compareTo(confirmPass) != 0)
        {
            ed_cnfrmPass.setError("Password do not match");
            return false;
        }
        return true;
    }

    
    public void resetPassword()
    {
        class GetData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(Settings.this, "Please wait", "Resetting password...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/resetPassword/?";
                    
                    String query3 = String.format("uid=%s&oldpassword=%s&newpassword=%s&confirmpassword=%s",
                            URLEncoder.encode(uId, "UTF-8"),
                            URLEncoder.encode(oldPass, "UTF-8"),
                            URLEncoder.encode(newPass, "UTF-8"),
                            URLEncoder.encode(confirmPass, "UTF-8"));
                    
                    URL url = new URL(leave_url + query3);
                    Log.i("url", "" + url);

                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = br.readLine()) != null)
                        {
                            response = "";
                            response += line;
                        }
                    }
                    else
                    {
                        response = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson = result;
                    Log.i("myJson", myJson);

                    progressDialog.dismiss();


                    if (myJson.equals("[]"))
                    {
                        Toast.makeText(Settings.this, "Sorry... Bad internet connection", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        try
                        {
                            JSONArray jsonArray = new JSONArray(myJson);
                            //Log.i("jsonArray", "" + jsonArray);

                            JSONObject object = jsonArray.getJSONObject(0);

                            String responseCode = object.getString("responseCode");
                            String responseMessage = object.getString("responseMessage");
                            String message = responseMessage.substring(2, responseMessage.length()-2);

                            if (responseCode.equals("1"))
                            {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                alertDialog.setTitle("Successful");
                                alertDialog.setMessage(message);
                                alertDialog.setCancelable(true);
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ed_oldPass.setText("");
                                        ed_newPass.setText("");
                                        ed_cnfrmPass.setText("");
                                    }
                                });

                                alertDialog.show();
                            }
                            else
                            {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                alertDialog.setTitle("Invalid Password");
                                alertDialog.setMessage(message);
                                alertDialog.setCancelable(true);
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });

                                alertDialog.show();
                            }
                        }
                        catch (JSONException e) {
                            Log.e("JsonException", e.toString());
                        }
                    }
                }
                else {
                    if (progressDialog.isShowing() && progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(Settings.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetData getData = new GetData();
        getData.execute();
    }
    
    @Override
    public void onBackPressed() 
    {
        NavDrawerListAdapter.setSelectedPosition(0);
        Intent intent = new Intent(Settings.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
