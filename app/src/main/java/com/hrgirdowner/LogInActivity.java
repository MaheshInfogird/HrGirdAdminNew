package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by admin on 25-11-2016.
 */
public class LogInActivity extends AppCompatActivity
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    SharedPreferences shared_pref;
    SharedPreferences.Editor editor1;

    public static final String MyPREFERENCES_notify = "MyPrefs_notify" ;
    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, pref1;
    SharedPreferences.Editor editor, editor2;

    ConnectionDetector cd;
    UserSessionManager session;
    CheckInternetConnection internetConnection;

    String Url, logo;
    String url_http;
    String myJSON = null;
    String UserName, Password;
    String GetIntent;
    String response, myJson;
    String response_version, myJson1;
    
    int version_code;
    boolean notification;
    
    ProgressDialog progressDialog;
    EditText ed_userName, ed_password;
    TextView txt_forgotPass;
    Button btn_signIn;
    LinearLayout signIn_layout, progress_layout, poweredby_layout;
    ImageView logo_login;

    String mobile_id, simSerialNo = "", Android_version, android_id, token;
    int pack_version_code;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        
        session = new UserSessionManager(getApplicationContext());
        ed_userName = (EditText)findViewById(R.id.ed_userName);
        ed_password = (EditText)findViewById(R.id.ed_password);
        btn_signIn = (Button) findViewById(R.id.btn_signIn);
        signIn_layout = (LinearLayout)findViewById(R.id.signIn_layout);
        progress_layout = (LinearLayout)findViewById(R.id.progress_layout);
        logo_login = (ImageView)findViewById(R.id.logo_login);
        txt_forgotPass = (TextView)findViewById(R.id.forgot_pass);
        poweredby_layout = (LinearLayout)findViewById(R.id.layout_poweredby_login);
        
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version_code = info.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        txt_forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        ed_userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poweredby_layout.setVisibility(View.GONE);
            }
        });

        btn_signIn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                poweredby_layout.setVisibility(View.VISIBLE);
            }
        });
        
        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        logo = (shared_pref.getString("logo", ""));

        token = MyFirebaseInstanceIDService.getTokenFrom();
        Log.i("token","=="+token);

        if (internetConnection.hasConnection(getApplicationContext()))
        {
            Picasso.with(LogInActivity.this).load(logo).into(logo_login);
            getCheckVersion();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(LogInActivity.this);
        }
        DeviceInfo();
        logo_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor1 = shared_pref.edit();
                editor1.clear();
                editor1.commit();
                session.logout_url();
                Intent intent = new Intent(LogInActivity.this, UrlActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        btn_signIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                UserName = ed_userName.getText().toString();
                Password = ed_password.getText().toString();
                if (internetConnection.hasConnection(getApplicationContext())) 
                {
                    if (UserName.equals("") && Password.equals("")) 
                    {
                        ed_userName.setError("Please enter email/mobile no");
                        ed_password.setError("Please enter password");
                        txtChange();
                    }
                    else if (UserName.equals("")) {
                        ed_userName.setError("Please enter email/mobile no");
                        txtChange();
                    } 
                    else if (Password.equals("")) {
                        ed_password.setError("Please enter password");
                        txtChange();
                    } 
                    else {
                        signIn();
                    }
                }
                else {
                    internetConnection.showNetDisabledAlertToUser(LogInActivity.this);
                }
            }
        });
    }

    public void DeviceInfo()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//Get IMEI Number of Phone //////////////// for this example i only need the IMEI
        mobile_id = telephonyManager.getDeviceId();
        //Log.i("mobile_id", mobile_id);

        simSerialNo = telephonyManager.getSimSerialNumber();
        //Log.i("simSerialNo", simSerialNo);
//Log.i("simSerialNo", "simSerialNo" + simSerialNo_att);
//mPhoneNumber_att = telephonyManager.getLine1Number();
//Log.i("mPhoneNumber", "mPhoneNumber" + mPhoneNumber);
      /*  android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);*/
        android_id = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        Log.i("android_id", android_id);
//Log.i("Androidid", "" + android_id_att);//4bdbdcf21b49033d
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        Android_version = String.valueOf(version);
        String versionRelease = Build.VERSION.RELEASE;
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
//string version_code = info.versionName;
            pack_version_code = info.versionCode;
            Log.i("version_code", "" + pack_version_code);//1
        }
        catch (PackageManager.NameNotFoundException e)
        {
        }


        String Androidversion_att = manufacturer + model + version + versionRelease;
    }

    public void txtChange()
    {
        ed_userName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ed_userName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ed_password.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ed_password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void signIn()
    {
        class GetDataJSON extends AsyncTask<String, Void, String>
        {
            private URL url;
            private String response = "";

            @Override
            protected void onPreExecute()
            {
                progressDialog = new ProgressDialog(LogInActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Signing In...");
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {

              //  http://hrsaas.safegird.com/owner/hrmapi/signIn/?email=1111122222&password=Shriram12345&imeiNumber=355427060795552&simNo=89911100000258258749&version=5&androidVersion=21&androidId=37a5adb7f03c8150&token=eGrAYTYrB3E%3AAPA91bGk8jdAXYLmCFw7M5Ex2pu7vsCUE8o4gk_MUXRm7Mw9jsdskzu3F9S3EVhJ7bDc1youVTGBGz80P_ON0L-7RXRDUAc7Ku8o9fCa5RhFJcm2xdgRtaNbbu461JagIxyfO1miJS_6&signinby=2


                try
                {
                    String Transurl = ""+url_http+""+Url+"/owner/hrmapi/signIn/?";
                    Log.i("Transurl", "" + Transurl);


                    String query = String.format("email=%s&password=%s&imeiNumber=%s&simNo=%s&version=%s&androidVersion=%s&androidId=%s&token=%s&signinby=%s",
                            URLEncoder.encode(UserName, "UTF-8"), 
                            URLEncoder.encode(Password, "UTF-8"),
                            URLEncoder.encode(mobile_id, "UTF-8"),
                            URLEncoder.encode(simSerialNo, "UTF-8"),
                            URLEncoder.encode(String.valueOf(pack_version_code), "UTF-8"),
                            URLEncoder.encode(Android_version, "UTF-8"),
                            URLEncoder.encode(android_id, "UTF-8"),
                            URLEncoder.encode(token, "UTF-8"),
                            URLEncoder.encode("2", "UTF-8"));
                    
                    url = new URL(Transurl + query);
                    Log.i("url", "" + url);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null)
                        {
                            response += line;
                        }
                    }
                    else {
                        response = "";
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result)
            {
                myJSON = result;
                if (response.equals("[]"))
                {
                    Toast.makeText(LogInActivity.this, "Sorry... Bad internet connection", Toast.LENGTH_LONG).show();
                }
                else
                {
                    try
                    {
                        JSONArray json = new JSONArray(result);
                        Log.i("json", "" + json);

                        JSONObject object = json.getJSONObject(0);

                        String responsecode = object.getString("responseCode");

                        if (responsecode.equals("1"))
                        {
                            progressDialog.dismiss();

                            session.createUserLoginSession(UserName, Password);

                            pref1 = getApplicationContext().getSharedPreferences(MyPREFERENCES_notify, PRIVATE_MODE);
                            editor2 = pref1.edit();
                            editor2.putBoolean("notification", true);
                            editor2.commit();

                            String uId = object.getString("uId");
                            String firstName = object.getString("firstName");
                            String lastName = object.getString("lastName");
                            String Name = firstName + lastName;
                            String email = object.getString("email");
                            String reportingManager = object.getString("reportingManager");
                            String subuserid = object.getString("subuserid");
                            
                            pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
                            editor = pref.edit();
                            editor.putString("password", Password);
                            editor.putString("uId", uId);
                            editor.putString("reportingManager", reportingManager);
                            editor.commit();
                            
                            Intent intent = new Intent(LogInActivity.this, DashBoard.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            progressDialog.dismiss();

                            String msg = object.getString("responseMessage");
                            String message = msg.substring(2, msg.length()-2);

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LogInActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setTitle(message);
                            alertDialog.setCancelable(false);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            });

                            alertDialog.show();
                        }
                    }
                    catch (JSONException e){
                        progressDialog.dismiss();
                        Log.i("Exception", e.toString());
                    }
                }
            }
        }
        GetDataJSON getDataJSON = new GetDataJSON();
        getDataJSON.execute();
    }
    
    
    public void getCheckVersion()
    {
        class GetCheckVersion extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/getversion/?";
                    String query3 = String.format("apptype=%s", URLEncoder.encode("3", "UTF-8"));
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
                            response_version = "";
                            response_version += line;
                        }
                    }
                    else
                    {
                        response_version = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_version;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson1 = result;
                    Log.i("myJson", myJson1);

                    if (myJson1.equals("[]"))
                    {
                        Toast.makeText(LogInActivity.this, "Sorry... Bad internet connection", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        try
                        {
                            JSONArray jsonArray = new JSONArray(myJson1);
                           // Log.i("jsonArray", "" + jsonArray);

                            JSONObject object = jsonArray.getJSONObject(0);

                            int get_version = object.getInt("Version");

                            if (version_code != get_version)
                            {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LogInActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                alertDialog.setTitle("New Update");
                                alertDialog.setMessage("Please update your app");
                                alertDialog.setCancelable(false);
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.hrgirdowner&hl=en"));
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                
                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                                        startMain.addCategory(Intent.CATEGORY_HOME);
                                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(startMain);
                                        finish();
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
                    Toast.makeText(LogInActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetCheckVersion getCheckVersion = new GetCheckVersion();
        getCheckVersion.execute();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);
    }
}
