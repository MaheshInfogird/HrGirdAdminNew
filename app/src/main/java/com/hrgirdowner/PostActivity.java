package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends BaseActivityExp
//implements Switch.OnCheckedChangeListener{
{

    private static final String TAG_NOTIFICATION_NAME = "notificationName";
    private static final String TAG_NOTIFICATION_TYPE_ID = "notificationTypeId";
    private static final String TAG_STATUS = "nstatus";

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ProgressDialog progressDialog;

    Button btn_post;
    Toolbar toolbar;
    ImageView tool_img;
    TextView tool_text,tool_tv_count;
    //Switch switch1,switch2,switch3;
    List<String> switch_status;
    HashMap<String,String> switch_Map;
    HashMap<String,String> map_noti;
    String response_noti,response_status;
    ListView lv_NotificationData;
    List<HashMap<String,String>> list_Notification;
    LinearLayout layout_listview;
    ListAdapter1 adapter1;
    CheckInternetConnection checkNet_on_click;
    NetworkChange netCheck;
    String uid,notification_ID,noti_status;
    SharedPreferences pref_login_url;
    private static final String PREFER_NAME_login_yrl = "MyPrefs";//only login url and apiname, http
    private static  final String PREFER_NAME="MyPref";
  //  String Login_url,api_name,http;
    String Url;
    String url_http;
    ConnectionDetector cd;


   // NotificationReceiver notificationReceiver;
    LinearLayout tool_notification_layout,tool_notification_layout1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        layout_listview = (LinearLayout)findViewById(R.id.list_layout);
        list_Notification = new ArrayList<>();
        switch_status = new ArrayList<>();
        switch_Map = new HashMap<>();

        lv_NotificationData = (ListView)findViewById(R.id.lv_postlist);

        toolbar  = (Toolbar) findViewById(R.id.toolbar_inner);
        tool_img  = (ImageView)findViewById(R.id.tool_back);
        tool_text = (TextView)findViewById(R.id.header_text);
        tool_notification_layout = (LinearLayout)findViewById(R.id.tool_notification_layout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            tool_img.setVisibility(View.GONE);
            getSupportActionBar().setTitle("");
            tool_text.setText("Notification");
            //  toolimg.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));
        }

        /*tool_notification_layout.setVisibility(View.GONE);
        DashBoard.tool_tv_count.setText("0");
        MyFirebaseMessagingService.count_new_noti = 0;*/

        /*tool_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(PostActivity.this,DashBoard.class);
                startActivity(it);
                finish();
            }
        });*/
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(PostActivity.this,DashBoard.class);
                startActivity(it);
                finish();
            }
        });*/

        checkNet_on_click =new CheckInternetConnection(this);
        //net coonect check broad cast
        //IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        netCheck = new NetworkChange() {
            @Override
            protected void onNetworkChange() {

            }
        };

        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));

        // brad cast
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        uid = pref.getString("uId", "");
        Log.i("uId",""+uid);

        /*btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.i("switch_btn","list="+switch_Map);

            }
        });*/

        if (checkNet_on_click.hasConnection(PostActivity.this))
        {
            getNotificationData();
        }
        else {
            checkNet_on_click.showNetDisabledAlertToUser(PostActivity.this);
        }

        setUpDrawer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    public void getNotificationData()
    {
        class getNotiData extends AsyncTask<String,Void,String>
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                progressDialog = ProgressDialog.show(PostActivity.this, "Please wait", "Getting data...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {

                //String noti_url = "http://hrsaas.safegird.com/owner/hrmessapi/notificationtype";
                try {

                    String noti_url = ""+url_http+""+Url+"/owner/hrmapi/notificationtype/?";
                    Log.i("noti_url", noti_url);
                    Log.i("uid", uid);
                   // String noti_url = ""+http+Login_url+api_name+"notificationtype/?";
                    String final_url = String.format("uId=%s",URLEncoder.encode(uid,"UTF-8"));

                    URL url = new URL(noti_url+final_url);
                    Log.i("noti_url","url "+url);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK)
                    {
                        response_noti = "";
                        String line;
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        while ((line = reader.readLine())!=null)
                        {
                            response_noti += line;
                        }
                    }
                    else {
                        response_noti = "";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("notification","response "+response_noti);
                return response_noti;
            }

            @Override
            protected void onPostExecute(String result)
            {
                super.onPostExecute(result);

                if (progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
                if (result != null)
                {
                    try
                    {

                        //[{"notificationName":"Birthday","notificationTypeId":1,"nstatus":"0"},
                        // {"notificationName":"Marriage","notificationTypeId":2,"nstatus":"0"},
                        // {"notificationName":"Work","notificationTypeId":3,"nstatus":"0"}]

                        JSONArray jsonArray = new JSONArray(result);

                        for (int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject object = jsonArray.getJSONObject(i);

                            String noti_name = object.getString(TAG_NOTIFICATION_NAME);
                            Log.i("notification","noti_name "+noti_name);
                            String noti_ID = object.getString(TAG_NOTIFICATION_TYPE_ID);
                            Log.i("notification","noti_ID "+noti_ID);
                            String nstatus = object.getString(TAG_STATUS);
                            Log.i("notification","nstatus "+nstatus);

                            map_noti = new HashMap<String, String>();

                            map_noti.put(TAG_NOTIFICATION_NAME,noti_name);
                            map_noti.put(TAG_NOTIFICATION_TYPE_ID,noti_ID);
                            map_noti.put(TAG_STATUS,nstatus);
                            //Log.i("notification","map_noti "+map_noti);

                            list_Notification.add(map_noti);

                        }

                        Log.i("notification","list_Notification "+list_Notification);
                        Log.i("notification","map_noti "+map_noti);

                        adapter1 = new ListAdapter1(PostActivity.this,list_Notification, R.layout.post_custom_layout, new String[]{}, new int[]{});
                        lv_NotificationData.setAdapter(adapter1);

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(PostActivity.this,"Sorry...Bad internet connection",Toast.LENGTH_SHORT).show();
                }
            }
        }
        getNotiData data = new getNotiData();
        data.execute();
    }

    public void setListViewHeight(ListView listView)
    {
        ListAdapter adapter = listView.getAdapter();
        Log.i("lv","count_adapter "+adapter.getCount());
        if (adapter == null)
        {
            return;
        }
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        Log.i("lv","desiredwidth "+desiredWidth);

        int totalHeight = 0;
        View view = null;

        for (int i=0;i<adapter.getCount();i++)
        {
            view = adapter.getView(i,view,listView);

            if (i == 0)
            {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
            Log.i("lv","totalHeight "+totalHeight);
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount()-1));
        listView.setLayoutParams(params);
    }

    public class ListAdapter1 extends SimpleAdapter
    {
        Context context;
        LayoutInflater inflater = null;

        public ListAdapter1(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
        {
            super(context,data,resource,from,to);
            this.context = context;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {

            boolean flag = false;
           View view = convertView;
            if (convertView == null)
            {
                flag = true;
               // LayoutInflater inflater = (LayoutInflater)PostActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.post_custom_layout,null);
            }

            TextView tv_name = (TextView)view.findViewById(R.id.tv_noti_name);
            Switch sw1 = (Switch)view.findViewById(R.id.switch1);

            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

            final String name = (String) data.get(TAG_NOTIFICATION_NAME);
            Log.i("adapter","name "+name);
            final String ID = (String) data.get(TAG_NOTIFICATION_TYPE_ID);
            Log.i("adapter","ID "+ID);
            String status = (String) data.get(TAG_STATUS);
            Log.i("adapter","status "+status);

            tv_name.setText(name);

            if (flag)
            {
                flag = false;
                if (status.equals("0")) {
                    sw1.setChecked(false);
                }
            }

            sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.i("switch", "=" + isChecked);
                    if (isChecked) {
                        String s = "1";
                        sendNotificationData(ID, s);
                        Toast.makeText(PostActivity.this, name + " notification enable", Toast.LENGTH_SHORT).show();
                        switch_Map.put(ID, "1");
                    } else {
                        String s = "0";
                        sendNotificationData(ID, s);
                        Toast.makeText(PostActivity.this, name + " notification disable", Toast.LENGTH_SHORT).show();
                        switch_Map.put(ID, "0");
                    }
                 }
            });

            return view;
        }
    }

    public void sendNotificationData(final String noti_ID,final String noti_status1)
    {
        Log.i("noti","ID="+noti_ID+" status="+noti_status1);

        class SendNotiData extends AsyncTask<String,Void,String>
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
               /* progressDialog = ProgressDialog.show(PostActivity.this, "Please wait", "Getting data...", true);
                progressDialog.show();*/
                progressDialog = ProgressDialog.show(PostActivity.this,"Please wait","Changing settings...",true);

            }

            @Override
            protected String doInBackground(String... params)
            {
                //http://hrsaas.safegird.com/owner/hrmessapi/notificationstatus/?uId=300&notificationTypeId=1&status=1
                try
                {
                    String send_noti_url = ""+url_http+""+Url+"/owner/hrmapi/notificationstatus/?";

                   // String send_noti_url = ""+http+Login_url+api_name+"notificationstatus/?";
                    String  query = String.format("uId=%s&notificationTypeId=%s&status=%s",
                            URLEncoder.encode(uid,"UTF-8"),
                            URLEncoder.encode(noti_ID,"UTF-8"),
                            URLEncoder.encode(noti_status1,"UTF-8"));
                    URL url_notiFinal = new URL(send_noti_url+query);

                    Log.i("url_notiFinal","=="+url_notiFinal);

                    HttpURLConnection connection = (HttpURLConnection) url_notiFinal.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK)
                    {
                        response_status = "";
                        String line;
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        while ((line = reader.readLine())!=null)
                        {
                            response_status += line;
                        }
                    }else {
                        response_status = "";
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                Log.i("response_status","=="+response_status);
                return response_status;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if (progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }

                if (result!=null)
                {
                    try
                    {
                        JSONArray jsonArray = new JSONArray(result);
                        JSONObject object = jsonArray.getJSONObject(0);
                        String responseMessage = object.getString("responseMessage");
                        Log.i("responseMessage",responseMessage);
                        String responsecode = object.getString("responsecode");
                        Log.i("responsecode",responsecode);
                       /* if (responsecode.equals("0"))
                        {
                            LayoutInflater inflater_alert = LayoutInflater.from(PostActivity.this);
                            View dialogLayout = inflater_alert.inflate(R.layout.unable_to_process_alert, null);
                            AlertDialog.Builder  builder = new AlertDialog.Builder(PostActivity.this);
                            builder.setView(dialogLayout);
                            TextView massage = (TextView)dialogLayout.findViewById(R.id.txt_masage);
                            massage.setText(responseMessage);
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int arg1)
                                {
                                    Intent intent = new Intent(PostActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).show();
                        }*/


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else {
                    Toast.makeText(PostActivity.this,"Sorry...Bad internet connection",Toast.LENGTH_SHORT).show();
                }

            }
        }

        SendNotiData sendNotiData = new SendNotiData();
        sendNotiData.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netCheck, filter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //unregisterReceiver(netCheck);
        Intent intent = new Intent(PostActivity.this,DashBoard.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(netCheck);
        //unregisterReceiver(notificationReceiver);
    }
}
