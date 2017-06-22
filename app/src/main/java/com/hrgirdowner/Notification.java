package com.hrgirdowner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification extends BaseActivityExp {

    private static final String TAG_NOTI_TITLE = "notificationTitle";
    private static final String TAG_NOTI_NAME = "notificationName";
    private static final String  TAG_NOTI_DATE = "notificationDateTime";
    private static final String  TAG_NOTI_APPID = "applicationId";
    private static final String  TAG_NOTI_REVIEWID = "reviewId";
    private static final String  TAG_LEAVE_STATUS = "status";


    ListView lv_notification;

    List<HashMap<String,String>> list_noti;
    HashMap<String,String> map_noti;
    CheckInternetConnection checkNet_on_click;
    NetworkChange netCheck;
    SharedPreferences pref,pref11;
    SharedPreferences.Editor editor;
    SharedPreferences pref_login_url;
  //  String Login_url,api_name,http;
    private static final String PREFER_NAME_login_yrl = "MyPrefs";//only login url and apiname, http
    private static  final String PREFER_NAME="MyPref";
    String uid,response_noti;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    Toolbar toolbar;
    ImageView toolimg;
    TextView tooltext,tool_tv_count;
    ProgressDialog progressDialog;
    int count_birth=0,count_marriage=0,count_work=0;
    LinearLayout tool_notification_layout;
    private static  final String PREFER_NAME11="MyPref_notification";
    boolean userScrolled = false;
    int total = 0;
    int lastVisible = 0;
    int startIndex = 0;
    int end;
    JSONArray jsonArray;
    NotificationAdapter adapter;
    View loadMoreView;
    TextView load_more;
    ProgressBar bar;
    int arraylenght;
    boolean title_msg;
    String noti_body = "";
    String Url;
    String url_http;
    ConnectionDetector cd;

    String notificationName;
    String notificationTitle;
    String applicationId1;
    String reviewId1;// = ob
    String leave_status="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);


        list_noti = new ArrayList<>();

        lv_notification = (ListView)findViewById(R.id.lv_notification);

        toolbar  = (Toolbar) findViewById(R.id.toolbar_inner);
        toolimg  = (ImageView)findViewById(R.id.tool_back);
        tooltext = (TextView)findViewById(R.id.header_text);
        tool_notification_layout = (LinearLayout)findViewById(R.id.tool_notification_layout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            tooltext.setText("Notification");
          //  toolimg.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));
        }

        tool_notification_layout.setVisibility(View.GONE);
        //DashBoard.tool_tv_count.setText("0");
        MyFirebaseMessagingService.count_new_noti = 0;

       /* toolimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Notification.this,DashBoard.class);
                startActivity(it);
                finish();
            }
        });
       /* pref11 = getApplicationContext().getSharedPreferences(PREFER_NAME11,getApplicationContext().MODE_PRIVATE);
        editor = pref11.edit();
        editor.putBoolean("noti_count",false);
        editor.commit();*/

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
        //Log.i("Url", Url);

        //registerReceiver(netCheck, filter);
        // brad cast
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        uid = pref.getString("uId", "");
        Log.i("uId",""+uid);



       // setUpDrawer();
      /*  mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState(); */

        if (checkNet_on_click.hasConnection(Notification.this))
        {
            getNotificationData();
        }
        else {
            checkNet_on_click.showNetDisabledAlertToUser(Notification.this);

        }

        loadMoreView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loadmore, null, false);
        load_more = (TextView)loadMoreView.findViewById(R.id.txt_loadmore);
        bar = (ProgressBar)loadMoreView.findViewById(R.id.bar);

        title_msg = getIntent().getBooleanExtra("title_flag",false);
        Log.i("title_msg","=="+title_msg);

        noti_body = getIntent().getStringExtra("noti_body");

        lv_notification.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {
                    userScrolled = false;
                    if (arraylenght > startIndex) {
                        updateListView();
                        total = firstVisibleItem + visibleItemCount;
                        lastVisible = visibleItemCount - 1;

                        bar.setVisibility(View.VISIBLE);
                        load_more.setText("Loading more data...");

                        /*if (ed_search.getText().toString().equals("")) {
                            if (attList_list.getFooterViewsCount() == 0) {
                                attList_list.addFooterView(loadMoreView);
                            }
                        }*/
                    } else {
                        if (lv_notification.getFooterViewsCount() == 0) {
                            lv_notification.addFooterView(loadMoreView);
                        }

                        bar.setVisibility(View.GONE);
                        total = 0;
                        load_more.setText("No more data");
                    }
                }
            }
        });

    }

    private void updateListView()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                int end = startIndex + 13;
                for (int i = startIndex; i < end; i++)
                {
                    try
                    {
                        //get_set_AttListing get_set = new get_set_AttListing();
                        JSONObject object = jsonArray.getJSONObject(i);

                        notificationTitle = object.getString(TAG_NOTI_TITLE);
                        Log.i("noti", "notificationTitle " + notificationTitle);

                        notificationName = object.getString(TAG_NOTI_NAME);
                        Log.i("noti", "notificationName " + notificationName);

                        String notificationDateTime = object.getString(TAG_NOTI_DATE);
                        Log.i("noti", "notificationDateTime " + notificationDateTime);

                        applicationId1 = object.getString(TAG_NOTI_APPID);
                        Log.i("noti", "applicationId=" + applicationId1);

                        reviewId1 = object.getString(TAG_NOTI_REVIEWID);
                        Log.i("noti", "reviewId=" + reviewId1);

                        leave_status = object.getString(TAG_LEAVE_STATUS);
                        Log.i("noti", "leave_status=" + leave_status);

                        map_noti = new HashMap<>();

                        map_noti.put(TAG_NOTI_TITLE, notificationTitle);
                        map_noti.put(TAG_NOTI_NAME, notificationName);
                        map_noti.put(TAG_NOTI_DATE, notificationDateTime);
                        map_noti.put(TAG_NOTI_APPID, applicationId1);
                        map_noti.put(TAG_NOTI_REVIEWID, reviewId1);
                        map_noti.put(TAG_LEAVE_STATUS,leave_status);

                        list_noti.add(map_noti);

                        arraylenght = jsonArray.length();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (arraylenght > startIndex)
                {
                    if (lv_notification.getFooterViewsCount() == 0) {
                        lv_notification.addFooterView(loadMoreView);
                    }

                    bar.setVisibility(View.GONE);
                    load_more.setText("No more data");
                }

                adapter = new NotificationAdapter(Notification.this,list_noti, R.layout.notification_custom,new String[]{},new int[]{});
                lv_notification.setAdapter(adapter);

                startIndex = adapter.getCount();
                lv_notification.setSelection(total - lastVisible);
                adapter.notifyDataSetChanged();

            }
        }, 2000);
    }

     public void getNotificationData()
    {
        class NotificationData extends AsyncTask<String,Void,String>
        {
            boolean flag_birth = true;
            boolean flag_marriage = true;
            boolean flag_work = true;
            boolean flag_leave = true;


            boolean find_body = true;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(Notification.this, "Please wait", "Getting data...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {

                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/notificationlist/?";
                  //  String leave_url = ""+http+Login_url+api_name+"notificationlist/?";
                    String query3 = String.format("uId=%s", URLEncoder.encode(uid, "UTF-8"));
                    URL url = new URL(leave_url+query3);
                    Log.i("url", "notification==" + url);

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
                    if (responseCode==HttpURLConnection.HTTP_OK)
                    {
                        response_noti = "";
                        String line;
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        while ((line=reader.readLine())!=null)
                        {
                            response_noti += line;
                        }

                    }else {
                        response_noti = "";
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.i("response","=="+response_noti);
                return response_noti;
            }

            @Override
            protected void onPostExecute(String result)
            {
                count_birth = 0;
                count_marriage = 0;
                count_work = 0;

                super.onPostExecute(result);

                if (progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
                if (result !=null)
                {
                    try
                    {
                        jsonArray = new JSONArray(result);
                        int length = jsonArray.length();
                        Log.i("response","jsonArray "+length);

                        int end;
                        if (length > 13) {
                            end = startIndex + 13;
                            lv_notification.addFooterView(loadMoreView);
                        }
                        else {
                            end = length;
                            Log.i("end","less=="+end);
                            if (lv_notification.getFooterViewsCount() > 0) {
                                lv_notification.removeFooterView(loadMoreView);
                            }
                        }

                            for (int i = startIndex; i < end; i++)
                            {

                                if (find_body)
                                {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    notificationTitle = object.getString(TAG_NOTI_TITLE);
                                    Log.i("noti", "notificationTitle " + notificationTitle);

                                    notificationName = object.getString(TAG_NOTI_NAME);
                                    Log.i("noti", "notificationName " + notificationName);

                                    String notificationDateTime = object.getString(TAG_NOTI_DATE);
                                    Log.i("noti", "notificationDateTime " + notificationDateTime);

                                    applicationId1 = object.getString(TAG_NOTI_APPID);
                                    Log.i("noti", "applicationId=" + applicationId1);

                                    reviewId1 = object.getString(TAG_NOTI_REVIEWID);
                                    Log.i("noti", "reviewId=" + reviewId1);

                                    leave_status = object.getString(TAG_LEAVE_STATUS);
                                    Log.i("noti", "leave_status=" + leave_status);

                                    map_noti = new HashMap<>();

                                    map_noti.put(TAG_NOTI_TITLE, notificationTitle);
                                    map_noti.put(TAG_NOTI_NAME, notificationName);
                                    map_noti.put(TAG_NOTI_DATE, notificationDateTime);
                                    map_noti.put(TAG_NOTI_APPID, applicationId1);
                                    map_noti.put(TAG_NOTI_REVIEWID, reviewId1);
                                    map_noti.put(TAG_LEAVE_STATUS,leave_status);

                                    arraylenght = jsonArray.length();

                                    list_noti.add(map_noti);


                                    if (notificationName.equals(noti_body))
                                    {
                                        Log.i("noti3333","list_noti"+list_noti);
                                        find_body = false;

                                    }
                            }
                        }
                        Log.i("count","birth="+count_birth+" marriage="+count_marriage+" work="+count_work);
                        Log.i("noti","list_noti"+list_noti);

                        Log.i("notificationName",notificationName);
                        //Log.i("noti_body",noti_body);

                        if(notificationTitle.equals("Leave"))
                        {
                            if (leave_status.equals("1"))
                            {
                                if (notificationName.equals(noti_body))
                                {
                                    Log.i("notificationName11",notificationName);
                                    Log.i("noti_body11",noti_body);
                                    if (title_msg)
                                    {
                                        Log.i("notificationName22",notificationName);
                                        Log.i("noti_body22",noti_body);
                                        Intent intent = new Intent(Notification.this, ReviewApprove.class);
                                        intent.putExtra("noti_list_leave", true);
                                        intent.putExtra("appid", applicationId1);
                                        intent.putExtra("leaveId", reviewId1);
                                        startActivity(intent);
                                        finish();

                                    }
                                }
                            }else {
                                if (notificationName.equals(noti_body)) {
                                    Log.i("notificationName11", notificationName);
                                    Log.i("noti_body11", noti_body);
                                    if (title_msg) {
                                        Log.i("notificationName22", notificationName);
                                        Log.i("noti_body22", noti_body);
                                        Intent intent = new Intent(Notification.this, ReviewedApprove.class);
                                        intent.putExtra("noti_list_leave", true);
                                        intent.putExtra("appid", applicationId1);
                                        intent.putExtra("leaveId", reviewId1);
                                        startActivity(intent);
                                        finish();

                                    }
                                }
                            }
                        }

                       /* if (notificationTitle.equals("Leave Status"))
                        {
                            if (notificationName.equals(noti_body))
                            {
                                Log.i("notificationName11", "==status==" + notificationName);
                                Log.i("noti_body11", "status==" + noti_body);
                                if (title_msg)
                                {
                                    Log.i("notificationName22", "status==" + notificationName);
                                    Log.i("noti_body22", "status==" + noti_body);
                                    Intent intent = new Intent(Notification.this, Leave_history_Action.class);
                                    intent.putExtra("noti_list_leave", true);
                                    intent.putExtra("appid", applicationId1);
                                    //intent.putExtra("leaveId", reviewId);
                                    startActivity(intent);
                                    finish();

                                }
                            }
                        }*/


                        adapter = new NotificationAdapter(Notification.this,list_noti, R.layout.notification_custom,new String[]{},new int[]{});
                        lv_notification.setAdapter(adapter);

                        adapter.notifyDataSetChanged();
                        startIndex = adapter.getCount();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else {
                    if (progressDialog.isShowing() && progressDialog != null)
                    {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(Notification.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }
        NotificationData data = new NotificationData();
        data.execute();
    }

    public class NotificationAdapter extends SimpleAdapter
    {
        //boolean flag_birth = true;
        boolean flag_marriage = true;
        boolean flag_work = true;
        Context context1;
        public LayoutInflater inflater = null;

        public NotificationAdapter(Context context, List<?extends Map<String,?>> data,int resource,String[] from,int[] to)
        {
            super(context,data,resource,from,to);
            this.context1 = context;
            inflater = (LayoutInflater)context1.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vi = convertView;
            if (vi == null)
            {
                vi = inflater.inflate(R.layout.notification_custom,null);
            }

            ImageView noti_img = (ImageView)vi.findViewById(R.id.noti_img);
            TextView noti_tv_title = (TextView)vi.findViewById(R.id.noti_tv_title);
            final TextView noti_tv_msg = (TextView)vi.findViewById(R.id.noti_tv_msg);
            LinearLayout layout_notification = (LinearLayout)vi.findViewById(R.id.layout_notification);

            final HashMap<String,Object> data = (HashMap<String,Object>) getItem(position);
            String title = (String)data.get(TAG_NOTI_TITLE);
            Log.i("adapter","title "+title);
            String name = (String)data.get(TAG_NOTI_NAME);
            Log.i("adapter","name "+name);
            String date_noti = (String)data.get(TAG_NOTI_DATE);
            Log.i("adapter","date_noti="+date_noti);
            final String applicationId = (String)data.get(TAG_NOTI_APPID);
            Log.i("adapter","applicationId="+applicationId);
            final String reviewId = (String)data.get(TAG_NOTI_REVIEWID);
            Log.i("adapter","reviewId="+reviewId);
            String leave_ststus1 = (String)data.get(TAG_LEAVE_STATUS);
            Log.i("adapter","leave_ststus1="+leave_ststus1);

            //noti_tv_title.setText(name);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String date_final="";
            try {
                Date d = sdf1.parse(date_noti);
                date_final = sdf.format(d);
                Log.i("adpter","date_final="+date_final);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            noti_tv_title.setText(name);
            noti_tv_msg.setText(date_final);

            if (title.equals("Leave"))
            {
                 noti_img.setImageResource(R.drawable.leave_big_icon);

                if (leave_ststus1.equals("1"))
                {
                    layout_notification.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context1,ReviewApprove.class);
                            intent.putExtra("noti_list_leave", true);
                            intent.putExtra("appid",applicationId);
                            intent.putExtra("leaveId",reviewId);
                            context1.startActivity(intent);
                            finish();
                        }
                    });
                }else {
                    layout_notification.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context1,ReviewedApprove.class);
                            intent.putExtra("noti_list_leave", true);
                            intent.putExtra("appid",applicationId);
                            intent.putExtra("leaveId",reviewId);
                            context1.startActivity(intent);
                            finish();
                        }
                    });
                }


            }
            /*if (title.equals("Leave Status"))
            {
                noti_img.setImageResource(R.drawable.leave_big_icon);
                layout_notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context1,Leaves.class);
                        //intent.putExtra("date_noti",true);
                        context1.startActivity(intent);
                        finish();
                    }
                });
            }*/
            if (title.equals("Birthday"))
            {
                Log.i("birthady","in");
                Log.i("birthady","in"+name);
                Log.i("birthady","in"+date_final);

               // noti_tv_title.setText(name);
                //noti_tv_msg.setText(date_final);
                noti_img.setImageResource(R.drawable.birthday_icon);

                layout_notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String date = noti_tv_msg.getText().toString();
                        Intent intent = new Intent(context1,BirthdayActivity.class);
                        intent.putExtra("notification_list",date);
                        context1.startActivity(intent);
                        finish();
                    }
                });

            }

            //Marriage
            if (title.equals("Marriage Anniversary"))
            {
                Log.i("Marriage","in");
                Log.i("Marriage","in");
                Log.i("Marriage","in"+name);
                Log.i("Marriage","in"+date_final);

                //noti_tv_title.setText(name);
                //noti_tv_msg.setText(date_final);
                noti_img.setImageResource(R.drawable.anniversary_icon);

                layout_notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String date = noti_tv_msg.getText().toString();
                        Intent intent = new Intent(context1,MarriageAnnActivity.class);
                        intent.putExtra("notification_list",date);
                        context1.startActivity(intent);
                        finish();
                    }
                });
            }

            if (title.equals("Work Anniversary"))
            {
                Log.i("Work","in");
                Log.i("Work","in");
                Log.i("Work","in");
                Log.i("Work","in"+name);
                Log.i("Work","in"+date_final);

                //noti_tv_title.setText(name);
                //noti_tv_msg.setText(date_final);
                noti_img.setImageResource(R.drawable.anniversary_icon);

                layout_notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String date = noti_tv_msg.getText().toString();
                        Intent intent = new Intent(context1,WorkAnnActivity.class);
                        intent.putExtra("notification_list",date);
                        context1.startActivity(intent);
                        finish();
                    }
                });
            }

            return vi;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //registerReceiver(netCheck, filter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //unregisterReceiver(netCheck);
        Intent intent = new Intent(this,DashBoard.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregisterReceiver(netCheck);
    }
}
