package com.hrgirdowner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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

public class WorkAnnActivity extends BaseActivityExp {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    
    Toolbar toolbar;
    AutoCompleteTextView txt_workAnniversaryDate;
    TextView txt_wa_data;
    TextView txt_wa_count;
    ListView wa_list;
    LinearLayout layout_waDate;
    LinearLayout wa_Progress;
    //SwipeRefreshLayout mSwipeRefreshLayout;
    
    URL url, work_url;

    public static final String TAG_WA_FullName = "fullName";
    public static final String TAG_WA_FName = "firstName";
    public static final String TAG_WA_LName = "lastName";
    public static final String TAG_WA_Designation = "designation";
    public static final String TAG_WA_Department = "departmentName";
    public static final String TAG_WA_JoiningDate = "joiningDate";
    public static final String TAG_WA_Photo = "photo";

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    String response = "", response_wa = "", response_year = "";
    String myJson = "", myJson1 = "", myJson3 = "";
    String workAnniversaryDate;
    String WA_FName, WA_LName, WA_FullName, WA_Designation, WA_Department, WA_Photo, WA_JoiningDate;
    String Url;
    String url_http;
    String Packagename;
    String fromDate, toDate;

    String img_url = "http://infogird.gogird.com/files/infogird.gogird.com/images/employeephoto/";

    int WA_Count;
    int version_code;
    
    final ArrayList<HashMap<String, String>> wa_arrayList = new ArrayList<HashMap<String, String>>();

    ArrayAdapter<String> adapter;
    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;
    
    FrameLayout content_frame;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;

    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;
    boolean flag;
    String type="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_ann);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Work Anniversary");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        setUpDrawer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        
        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        //Log.i("Url", Url);

        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version_code = info.versionCode;
            Packagename = info.packageName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

       // mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_wa);
        wa_Progress = (LinearLayout) findViewById(R.id.wa_Progress);
        txt_workAnniversaryDate = (AutoCompleteTextView) findViewById(R.id.txt_workAnniversaryDate);
        txt_wa_data = (TextView) findViewById(R.id.txt_no_wa_data);
        txt_wa_count = (TextView) findViewById(R.id.txt_wa_count);
        layout_waDate = (LinearLayout) findViewById(R.id.layout_waDate);

        wa_list = (ListView) findViewById(R.id.wa_listView);

        content_frame = (FrameLayout) findViewById(R.id.content_frame);

       /* mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                year_array.clear();
                txt_workAnniversaryDate.setAdapter(null);
                wa_arrayList.clear();
                wa_list.setAdapter(null);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                workAnniversaryDate = format.format(c.getTime());
                txt_workAnniversaryDate.setText(workAnniversaryDate);
                getYearData();
                getWorkAnniversary();
            }
        });*/

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        flag = getIntent().getBooleanExtra("date_noti",false);
        Log.i("marriage","flag="+flag);

        if (flag)
        {
            type = "2";
            Calendar calendar = Calendar.getInstance();
            workAnniversaryDate = sdf1.format(calendar.getTime());
            Log.i("noti","msg="+workAnniversaryDate);
            txt_workAnniversaryDate.setVisibility(View.GONE);
        }else {

            String date_noti = getIntent().getStringExtra("notification_list");
            if (date_noti != null) {
                type = "2";
                flag = true;
                Log.i("date_list_noti", "not null");
                try {
                    Date d = sdf.parse(date_noti);
                    String date_final = sdf1.format(d);
                    workAnniversaryDate = date_final;
                    Log.i("noti", "msg=" + workAnniversaryDate);
                    txt_workAnniversaryDate.setVisibility(View.GONE);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {
                type = "1";
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                workAnniversaryDate = format.format(c.getTime());
                txt_workAnniversaryDate.setText(workAnniversaryDate);
            }
        }

       /* Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        workAnniversaryDate = format.format(c.getTime());
        txt_workAnniversaryDate.setText(workAnniversaryDate);*/
        
        txt_workAnniversaryDate.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                if (internetConnection.hasConnection(getApplicationContext()))
                {
                    workAnniversaryDate = (String) parent.getItemAtPosition(position);
                    wa_arrayList.clear();
                    wa_list.setAdapter(null);
                    refresh = false;
                    date_select = true;
                    getWorkAnniversary();
                }
                else {
                    Toast.makeText(WorkAnnActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        txt_workAnniversaryDate.setOnTouchListener(new View.OnTouchListener() 
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                txt_workAnniversaryDate.showDropDown();
                return true;
            }
        });
        
        
        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getYearData();
            getWorkAnniversary();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(WorkAnnActivity.this);
        }
    }


    public void getWorkAnniversary()
    {
        if (!flag)
        {
            workAnniversaryDate = txt_workAnniversaryDate.getText().toString();
        }

        class GetWorkAnniversary extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute()
            {
                if (!refresh) {
                    txt_wa_data.setVisibility(View.GONE);
                    wa_list.setVisibility(View.GONE);
                    wa_Progress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String wa_url = ""+url_http+""+Url+"/owner/hrmapi/workAnniversaryReport/?";

                    String query3 = String.format("date=%s&type=%s", URLEncoder.encode(workAnniversaryDate, "UTF-8")
                    ,URLEncoder.encode(type,"UTF-8"));
                    work_url = new URL(wa_url + query3);
                    Log.i("work_url", "" + work_url);

                    HttpURLConnection connection3 = (HttpURLConnection)work_url.openConnection();
                    connection3.setReadTimeout(10000);
                    connection3.setConnectTimeout(10000);
                    connection3.setRequestMethod("GET");
                    connection3.setUseCaches(false);
                    connection3.setAllowUserInteraction(false);
                    connection3.setDoInput(true);
                    connection3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection3.setDoOutput(true);
                    int responceCode = connection3.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK)
                    {
                        String line3 = "";
                        BufferedReader br3 = new BufferedReader(new InputStreamReader(connection3.getInputStream()));
                        while ((line3 = br3.readLine()) != null)
                        {
                            response_wa = "";
                            response_wa += line3;
                        }
                    }
                    else
                    {
                        response_wa = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_wa;
            }

            @Override
            protected void onPostExecute(String result)
            {
               // mSwipeRefreshLayout.setRefreshing(false);
                if (result != null)
                {
                    myJson3 = result;
                    Log.i("myJson3", myJson3);
                    if (!refresh) 
                    {
                        wa_list.setVisibility(View.VISIBLE);
                        wa_Progress.setVisibility(View.GONE);
                    }

                    try
                    {
                        JSONArray jsonArray = new JSONArray(myJson3);
                        //Log.i("jsonArray", "" + jsonArray);

                        WA_Count = jsonArray.length();

                        if (myJson3.equals("[]"))
                        {
                            txt_wa_data.setVisibility(View.VISIBLE);
                            wa_list.setVisibility(View.GONE);
                            txt_wa_count.setText("");
                        }
                        else 
                        {
                            txt_wa_data.setVisibility(View.GONE);
                            wa_list.setVisibility(View.VISIBLE);
                            if (WA_Count == 1) {
                                txt_wa_count.setText(String.valueOf(WA_Count) + " Anniversary");
                            }
                            else if (WA_Count > 1) {
                                txt_wa_count.setText(String.valueOf(WA_Count) + " Anniversary's");
                            }

                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                WA_FName = object.getString(TAG_WA_FName);
                                WA_LName = object.getString(TAG_WA_LName);
                                WA_FullName = WA_FName + " " + WA_LName;
                                WA_Designation = object.getString(TAG_WA_Designation);
                                WA_Department = object.getString(TAG_WA_Department);
                                WA_Photo = object.getString(TAG_WA_Photo);
                                WA_JoiningDate = object.getString(TAG_WA_JoiningDate);

                                HashMap<String, String> wa_hashMap = new HashMap<String, String>();
                                wa_hashMap.put(TAG_WA_FullName, WA_FullName);
                                wa_hashMap.put(TAG_WA_Designation, WA_Designation);
                                wa_hashMap.put(TAG_WA_Department, WA_Department);
                                wa_hashMap.put(TAG_WA_Photo, WA_Photo);
                                wa_hashMap.put(TAG_WA_JoiningDate, WA_JoiningDate);

                                wa_arrayList.add(wa_hashMap);
                                
                                adapter1 = new WAnniversaryList(WorkAnnActivity.this, wa_arrayList, R.layout.birthday_custom, new String[]{}, new int[]{});
                                wa_list.setAdapter(adapter1);
                            }
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(WorkAnnActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }

        GetWorkAnniversary getWorkAnniversary = new GetWorkAnniversary();
        getWorkAnniversary.execute();
    }


    public void getYearData()
    {
        class GetYearData extends AsyncTask<String, Void, String>
        {
            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/getfinancialyear";
                    
                    URL year_url = new URL(leave_url);
                    Log.i("year_url", "" + year_url);

                    HttpURLConnection connection = (HttpURLConnection)year_url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    int responceCode = connection.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK)
                    {
                        String line1;
                        BufferedReader br1 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line1 = br1.readLine()) != null)
                        {
                            response_year = "";
                            response_year += line1;
                        }
                    }
                    else
                    {
                        response_year = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_year;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson1 = result;
                    Log.i("myJson1", myJson1);

                    try
                    {
                        JSONArray jsonArray = new JSONArray(myJson1);
                        //Log.i("jsonArray", "" + jsonArray);

                        JSONObject object = jsonArray.getJSONObject(0);

                        fromDate = object.getString(TAG_fromDate);
                        toDate = object.getString(TAG_toDate);

                        DateFormat formater = new SimpleDateFormat("yyyy-MM");

                        Calendar beginCalendar = Calendar.getInstance();
                        Calendar finishCalendar = Calendar.getInstance();

                        try {
                            beginCalendar.setTime(formater.parse(fromDate));
                            finishCalendar.setTime(formater.parse(toDate));
                        }
                        catch (ParseException e) {
                            e.printStackTrace();
                        }

                        while (beginCalendar.before(finishCalendar)) 
                        {
                            String date = formater.format(beginCalendar.getTime()).toUpperCase();
                            beginCalendar.add(Calendar.MONTH, 1);
                            year_array.add(date);
                        }

                        year_array.add(toDate);

                        adapter = new ArrayAdapter<String>(WorkAnnActivity.this, R.layout.dropdown_custom, year_array);
                        txt_workAnniversaryDate.setAdapter(adapter);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(WorkAnnActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }
    
    
    
    public class  WAnniversaryList extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater=null;
        public WAnniversaryList(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, final View convertView, ViewGroup parent)
        {
            View vi = convertView;
            if(convertView == null)
            {
                vi = inflater.inflate(R.layout.workanniversary_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.wa_fullName);
            final TextView txt_designation = (TextView)vi.findViewById(R.id.wa_designation);
            final TextView txt_department = (TextView)vi.findViewById(R.id.wa_department);
            final TextView txt_joiningDate = (TextView)vi.findViewById(R.id.wa_joiningDate);
            final ImageView image =(ImageView)vi.findViewById(R.id.wa_image);

            String fullName = (String) data.get(TAG_WA_FullName);
            String designation = (String) data.get(TAG_WA_Designation);
            String department = (String) data.get(TAG_WA_Department);
            String joiningDate = (String) data.get(TAG_WA_JoiningDate);
            String photo = (String) data.get(TAG_WA_Photo);
            String image_url = ""+url_http+Url+"/files/"+Url+"/images/employeephoto/";
            String image_final_url = image_url + photo;

            txt_fullName.setText(fullName);
            txt_designation.setText(designation);
            txt_department.setText("Dept : - "+department);
            txt_joiningDate.setText("Joining Date : - "+joiningDate);

            Picasso.with(getApplicationContext()).load(image_final_url).into(image, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    RoundedBitmapDrawable round = RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), bitmap);
                    round.setCornerRadius(5);
                    round.setCircular(true);
                    image.setImageDrawable(round);
                }

                @Override
                public void onError() {
                }
            });

            return vi;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(WorkAnnActivity.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
