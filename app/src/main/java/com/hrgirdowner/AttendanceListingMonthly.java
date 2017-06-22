package com.hrgirdowner;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AttendanceListingMonthly  extends AppCompatActivity {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    public static final String TAG_actualdate = "actualdate";
    public static final String TAG_intime = "intime";
    public static final String TAG_outtime = "outtime";
    public static final String TAG_cid = "cid";
    public static final String TAG_firstName = "firstName";
    public static final String TAG_lastName = "lastName";
    public static final String TAG_mast_title = "mast_title";
    public static final String TAG_br_title = "br_title";
    public static final String TAG_aer_worked_hrs = "aer_worked_hrs";

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    public static NetworkChange receiver;
    ProgressDialog progressDialog;
    Toolbar toolbar;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    URL url;

    String response;
    String myJson;
    String Url;
    String current_date;
    String fullName, firstName, lastName,Emp_name;
    String Status_Title;
    String url_http;
    String uID,Datemo;

    public int mYear, mMonth, mDay;
    int total = 0;
    int lastVisible = 0;
    int arraylenght;
    int startIndex = 0;

    boolean date_select = false;
    boolean hit_once = false;
    boolean userScrolled = false;
    boolean refresh = false;

    TextView txt_no_data;
    AutoCompleteTextView ac_attDate;
    ListView attList_list;
    LinearLayout layout_progress, layout_attList_date;
    Snackbar snackbar;
    FrameLayout content_frame;
    TextView txt_empname;
    TextView load_more;
    ProgressBar bar;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayList<get_set_AttListing> array_list1 = new ArrayList<get_set_AttListing>();
    attListAdapter adapter;

    View loadMoreView;
    JSONArray jsonArray;
    String response_year="",myJson1 = "";
    String fromDate, toDate;
    ArrayList<String> year_array = new ArrayList<String>();
    ArrayAdapter<String> adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_listing_monthly);
        /*toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);



        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            Header.setText("Monthly Attendance");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
*/

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView back = (ImageView)findViewById(R.id.tool_back);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            back.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("");
            Header.setText("Monthly Attendance");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            //getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            uID = extras.getString("UID");
            Datemo = extras.getString("Date");

            // and get whatever type user account id is
        }
       // ac_attDate.setText(Datemo);
       // content_frame = (FrameLayout) findViewById(R.id.content_frame_attList_mo);

        receiver = new NetworkChange()
        {
            @Override
            protected void onNetworkChange()
            {
                if (receiver.isConnected)
                {
                    if (hit_once) {
                        getAttendanceListing();
                    }
                    if (snackbar != null) {
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    snackbar = Snackbar.make(content_frame, "Please check your internet connection", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.RedTextColor));
                    snackbar.show();
                }
            }
        };

//        setUpDrawer();
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//        mDrawerToggle.syncState();

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        //Log.i("Url", Url);

        //mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_att_list_mo);
        layout_progress = (LinearLayout)findViewById(R.id.att_list_Progress_mo);
        ac_attDate = (AutoCompleteTextView) findViewById(R.id.att_list_txtDate_mo);
        attList_list = (ListView)findViewById(R.id.att_listing_mo);
        layout_attList_date = (LinearLayout)findViewById(R.id.att_list_date_mo);
        txt_no_data = (TextView)findViewById(R.id.txt_no_attList_data_mo);
        txt_empname = (TextView) findViewById(R.id.att_emp_name);

        loadMoreView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loadmore, null, false);
        load_more = (TextView)loadMoreView.findViewById(R.id.txt_loadmore);
        bar = (ProgressBar)loadMoreView.findViewById(R.id.bar);
        //attList_list.addFooterView(loadMoreView);
        attList_list.setTextFilterEnabled(true);

        attList_list.setOnScrollListener(new AbsListView.OnScrollListener()
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
                        if (attList_list.getFooterViewsCount() == 0) {
                            attList_list.addFooterView(loadMoreView);
                        }

                        bar.setVisibility(View.GONE);
                        total = 0;
                        load_more.setText("No more data");
                    }
                }
            }
        });

      /*  ed_search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = ed_search.getText().toString().toLowerCase(Locale.getDefault());

                if (adapter != null) {
                    adapter.filter(text);
                    total = 0;
                    lastVisible = 0;
                    attList_list.setSelection(total);
                    attList_list.removeFooterView(loadMoreView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*/

        /*layout_attList_date.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
//                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd = new DatePickerDialog(AttendanceListingMonthly.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        if (view.isShown())
                        {
                            if (*//*dayOfMonth < 10 &&*//* (monthOfYear + 1) < 10)
                            {
                                current_date = year + "-" + "0" + (monthOfYear + 1)*//* + "-" + "0" + dayOfMonth*//*;
                                ac_attDate.setText(current_date);
                            }
                       *//*     else if (dayOfMonth < 10) {
                                current_date = year + "-" + (monthOfYear + 1)*//**//* + "-" + "0" + dayOfMonth*//**//*;
                                ac_attDate.setText(current_date);
                            }*//*
                           *//* else if ((monthOfYear + 1) < 10) {
                                current_date = year + "-" + "0" + (monthOfYear + 1)*//**//* + "-" + dayOfMonth*//**//*;
                                ac_attDate.setText(current_date);
                            }*//*
                            else {
                                current_date = year + "-" + (monthOfYear + 1)*//* + "-" + dayOfMonth*//*;
                                ac_attDate.setText(current_date);
                            }

                            startIndex = 0;
                            arraylenght = 0;
                            total = 0;
                            lastVisible = 0;
                            array_list1.clear();
                            attList_list.setAdapter(null);
                            refresh = false;
                            date_select = true;

                            if (attList_list.getFooterViewsCount() > 0) {
                                attList_list.removeFooterView(loadMoreView);
                            }
                            getAttendanceListing();
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });*/

        attList_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


       /* mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                startIndex = 0;
                arraylenght = 0;
                total = 0;
                lastVisible = 0;
                array_list1.clear();
                attList_list.setAdapter(null);
                Calendar c = Calendar.getInstance();
                DateFormat formater = new SimpleDateFormat("yyyy-MM");
                current_date = formater.format(c.getTime());
                ac_attDate.setText(current_date);

                if (attList_list.getFooterViewsCount() > 0) {
                    attList_list.removeFooterView(loadMoreView);
                }
                getAttendanceListing();
            }
        });*/

       ac_attDate.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               ac_attDate.showDropDown();
               return true;
           }
       });
        ac_attDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                current_date = (String)parent.getItemAtPosition(position);

                startIndex = 0;
                arraylenght = 0;
                array_list1.clear();
                attList_list.setAdapter(null);

                getAttendanceListing();

            }
        });
        Calendar c = Calendar.getInstance();
        DateFormat formater = new SimpleDateFormat("yyyy-MM");
        current_date = formater.format(c.getTime());
      //  ac_attDate.setText(current_date);
        ac_attDate.setText(Datemo);

        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getYearData();
            getAttendanceListing();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(AttendanceListingMonthly.this);
        }
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

                        Calendar c = Calendar.getInstance();
                        //DateFormat formater = new SimpleDateFormat("yyyy-MM");
                        String current_date1 = formater.format(c.getTime());

                        Calendar beginCalendar = Calendar.getInstance();
                        Calendar finishCalendar = Calendar.getInstance();

                        try {
                            beginCalendar.setTime(formater.parse(fromDate));
                            //finishCalendar.setTime(formater.parse(toDate));
                            finishCalendar.setTime(formater.parse(current_date1));

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

                        year_array.add(current_date1);

                        adapter1 = new ArrayAdapter<String>(AttendanceListingMonthly.this, R.layout.dropdown_custom, year_array);
                        ac_attDate.setAdapter(adapter1);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(AttendanceListingMonthly.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
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
                        get_set_AttListing get_set = new get_set_AttListing();
                        JSONObject object = jsonArray.getJSONObject(i);

                        firstName = object.getString(TAG_firstName);
                        lastName = object.getString(TAG_lastName);
                        fullName = firstName + " " + lastName;

                        get_set.setFirstName(object.getString(TAG_firstName));
                        get_set.setLastName(object.getString(TAG_lastName));
                        get_set.setFullName(fullName);

                        get_set.setDate(object.getString(TAG_actualdate));
                        get_set.setInTime(object.getString(TAG_intime));
                        get_set.setOutTime(object.getString(TAG_outtime));
                        get_set.setStatus(object.getString(TAG_mast_title));
                        get_set.setCid(object.getString(TAG_cid));
                        get_set.setDept(object.getString(TAG_br_title));
                        get_set.setWorkHour(object.getString(TAG_aer_worked_hrs));

                        array_list1.add(get_set);

                        arraylenght = jsonArray.length();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (arraylenght > startIndex)
                {
                    if (attList_list.getFooterViewsCount() == 0) {
                        attList_list.addFooterView(loadMoreView);
                    }

                    bar.setVisibility(View.GONE);
                    load_more.setText("No more data");
                }

                adapter = new attListAdapter(AttendanceListingMonthly.this, array_list1);
                attList_list.setAdapter(adapter);

                startIndex = adapter.getCount();
                attList_list.setSelection(total - lastVisible);
                adapter.notifyDataSetChanged();

            }
        }, 2000);
    }


    public void getAttendanceListing()
    {
        current_date = ac_attDate.getText().toString();

        class GetAttListingData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh)
                {
                    if (date_select)
                    {
                        layout_progress.setVisibility(View.VISIBLE);
                        attList_list.setVisibility(View.GONE);
                        txt_no_data.setVisibility(View.GONE);
                    }
                    else {
                        progressDialog = ProgressDialog.show(AttendanceListingMonthly.this, "Please wait", "Getting data...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/empmonthwiseattendanceReport/?";

                    String query3 = String.format("date=%s&uId=%s", URLEncoder.encode(current_date, "UTF-8"),URLEncoder.encode(uID, "UTF-8"));
                    url = new URL(leave_url + query3);
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
                //mSwipeRefreshLayout.setRefreshing(false);
                if (result != null)
                {
                    myJson = result;
                    Log.i("myJson", myJson);
                    if (!refresh)
                    {
                        if (date_select) {
                            date_select = false;
                            layout_progress.setVisibility(View.GONE);
                        }
                        else {
                            progressDialog.dismiss();
                        }
                    }

                    if (myJson.equals("[]"))
                    {
                        txt_no_data.setVisibility(View.VISIBLE);
                        layout_progress.setVisibility(View.GONE);
                        attList_list.setVisibility(View.GONE);
                    }
                    else
                    {
                        txt_no_data.setVisibility(View.GONE);
                        layout_progress.setVisibility(View.GONE);
                        attList_list.setVisibility(View.VISIBLE);
                        try
                        {
                            jsonArray = new JSONArray(myJson);
                            Log.i("jsonArray", "" + jsonArray);

                            int length = jsonArray.length();
                            Log.i("jsonArray", "length=" + jsonArray.length());

                            int end;
                            if (length > 13) {
                                end = startIndex + 13;
                                attList_list.addFooterView(loadMoreView);
                            }
                            else {
                                end = length;
                                Log.i("end","less=="+end);
                                if (attList_list.getFooterViewsCount() > 0) {
                                    attList_list.removeFooterView(loadMoreView);
                                }
                            }

                            Log.i("statrtIndex","before_for="+startIndex);
                            for (int i = startIndex; i < end; i++)
                            {
                                Log.i("startindex","="+startIndex+" i="+i+" end="+end);
                                get_set_AttListing get_set = new get_set_AttListing();

                                JSONObject object = jsonArray.getJSONObject(i);

                                firstName = object.getString(TAG_firstName);
                                lastName = object.getString(TAG_lastName);
                                fullName = firstName + " " + lastName;
                                Emp_name = fullName;

                                get_set.setFirstName(object.getString(TAG_firstName));
                                get_set.setLastName(object.getString(TAG_lastName));
                                get_set.setFullName(fullName);
                                get_set.setDate(object.getString(TAG_actualdate));
                                get_set.setInTime(object.getString(TAG_intime));
                                get_set.setOutTime(object.getString(TAG_outtime));
                                get_set.setStatus(object.getString(TAG_mast_title));
                                get_set.setCid(object.getString(TAG_cid));
                                get_set.setDept(object.getString(TAG_br_title));
                                get_set.setWorkHour(object.getString(TAG_aer_worked_hrs));
                                arraylenght = jsonArray.length();

                                array_list1.add(get_set);

                                adapter = new attListAdapter(AttendanceListingMonthly.this, array_list1);
                                attList_list.setAdapter(adapter);
                            }

                            adapter.notifyDataSetChanged();
                            startIndex = adapter.getCount();

                            txt_empname.setText(Emp_name);
                        }
                        catch (JSONException e) {
                            Log.e("JsonException", e.toString());
                        }
                    }
                }
                else {
                    if (progressDialog.isShowing() && progressDialog != null){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AttendanceListingMonthly.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetAttListingData getAttListingData = new GetAttListingData();
        getAttListingData.execute();
    }


    public class attListAdapter extends BaseAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        ArrayList<get_set_AttListing> citylist;
        private List<get_set_AttListing> attDetails_list = null;
        private ArrayList<get_set_AttListing> arraylist;

        public attListAdapter(Context context, List<get_set_AttListing> attDetails_list)
        {
            this.mContext = context;
            this.attDetails_list = attDetails_list;
            this.inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.citylist = new ArrayList<get_set_AttListing>();
            this.citylist.addAll(attDetails_list);
        }

        @Override
        public int getCount() {
            return attDetails_list.size();
        }

        @Override
        public get_set_AttListing getItem(int position) {
            return attDetails_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, final View convertView, ViewGroup parent)
        {
            View vi = convertView;
            if(convertView == null)
            {
                vi = inflater.inflate(R.layout.attendance_listing_custom, null);
            }

            //HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_srno = (TextView)vi.findViewById(R.id.att_list_sr);
            final TextView txt_date = (TextView)vi.findViewById(R.id.att_list_name);
            final TextView txt_intime   = (TextView)vi.findViewById(R.id.att_list_inTime);
            final TextView txt_outtime  = (TextView)vi.findViewById(R.id.att_list_outTime);
            final TextView txt_cid      = (TextView)vi.findViewById(R.id.att_list_cid);
            final TextView txt_dept     = (TextView)vi.findViewById(R.id.att_list_dept);
            final TextView txt_status   = (TextView)vi.findViewById(R.id.att_list_status);
            final TextView txt_work_hr  = (TextView)vi.findViewById(R.id.att_list_workHr);
            final TextView txt_stat     = (TextView)vi.findViewById(R.id.att_list_stat);
            final LinearLayout layout_status = (LinearLayout)vi.findViewById(R.id.att_list_st_layout);


            txt_date.setTextColor(getResources().getColor(R.color.GreyTextColor));
            int srno = position + 01;
            String formatted = String.format("%02d", srno);
            final String date = attDetails_list.get(position).getDate();
            Log.i("adapter","date="+date);
            final String inTime    = attDetails_list.get(position).getInTime();
            Log.i("adapter","inTime="+inTime);
            final String outTime   = attDetails_list.get(position).getOutTime();
            Log.i("adapter","outTime="+outTime);
            final String dept      = attDetails_list.get(position).getDept();
            Log.i("adapter","dept="+dept);
            final String cid       = attDetails_list.get(position).getCid();
            Log.i("adapter","cid="+cid);
            final String status    = attDetails_list.get(position).getStatus();
            Log.i("adapter","status="+status);
            final String work_hr   = attDetails_list.get(position).getWorkHour();
            Log.i("adapter","work_hr="+work_hr);
            final String stat      = attDetails_list.get(position).getStatus();
            Log.i("adapter","stat="+stat);

            txt_srno.setText(formatted);
            txt_date.setText(date);
            txt_status.setText(status);
            txt_intime.setText(inTime);
            txt_outtime.setText(outTime);
            txt_dept.setText(dept);
            txt_cid.setText(cid);
            txt_work_hr.setText(work_hr);
            txt_stat.setText(stat);

            layout_status.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String current_stat = txt_status.getText().toString();
                    if (current_stat.equals("P")){
                        Toast.makeText(AttendanceListingMonthly.this, "Present", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("A")){
                        Toast.makeText(AttendanceListingMonthly.this, "Absent", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HA")){
                        Toast.makeText(AttendanceListingMonthly.this, "Half Absent", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("CL")){
                        Toast.makeText(AttendanceListingMonthly.this, "Casual Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("PL")){
                        Toast.makeText(AttendanceListingMonthly.this, "Privilege Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("L")){
                        Toast.makeText(AttendanceListingMonthly.this, "Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("SL")){
                        Toast.makeText(AttendanceListingMonthly.this, "Sick Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("ML")){
                        Toast.makeText(AttendanceListingMonthly.this, "Maternity Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HL")){
                        Toast.makeText(AttendanceListingMonthly.this, "Half Day Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HD")){
                        Toast.makeText(AttendanceListingMonthly.this, "Half Day", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("H")){
                        Toast.makeText(AttendanceListingMonthly.this, "Holiday", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("WO")){
                        Toast.makeText(AttendanceListingMonthly.this, "Weekly Off", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("EG")){
                        Toast.makeText(AttendanceListingMonthly.this, "Early Go", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("LC")){
                        Toast.makeText(AttendanceListingMonthly.this, "Late Come", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("E+L")){
                        Toast.makeText(AttendanceListingMonthly.this, "EG+LC", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("UP"))
                    {
                        Toast.makeText(AttendanceListingMonthly.this, "Unpaid Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("UHD"))
                    {
                        Toast.makeText(AttendanceListingMonthly.this, "Unpaid Half Day Leave", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Status_Title = txt_stat.getText().toString();

            if (Status_Title.equals("Present")){
                txt_status.setText("P");
                layout_status.setBackgroundResource((R.drawable.present));
            }
            if (Status_Title.equals("null")){
                txt_status.setText("P");
                layout_status.setBackgroundResource((R.drawable.present));
            }
            if (Status_Title.equals("Absent")){
                txt_status.setText("A");
                layout_status.setBackgroundResource((R.drawable.absent));
            }
            if (Status_Title.equals("Half Absent")){
                txt_status.setText("HA");
                layout_status.setBackgroundResource((R.drawable.absent));
            }
            if (Status_Title.equals("Casual Leave")){
                txt_status.setText("CL");
                layout_status.setBackgroundResource((R.drawable.leave));
            }
            if (Status_Title.equals("Privilege Leave")){
                txt_status.setText("PL");
                layout_status.setBackgroundResource((R.drawable.leave));
            }
            if (Status_Title.equals("Leave")){
                txt_status.setText("L");
                layout_status.setBackgroundResource((R.drawable.leave));
            }
            if (Status_Title.equals("Sick Leave")){
                txt_status.setText("SL");
                layout_status.setBackgroundResource((R.drawable.leave));
            }
            if (Status_Title.equals("Maternity Leave")){
                txt_status.setText("ML");
                layout_status.setBackgroundResource((R.drawable.leave));
            }
            if (Status_Title.equals("Half Day Leave")){
                txt_status.setText("HL");
                layout_status.setBackgroundResource((R.drawable.half_leave));
            }
            if (Status_Title.equals("Half Day")){
                txt_status.setText("HD");
                layout_status.setBackgroundResource((R.drawable.hd));
            }
            if (Status_Title.equals("Holiday")){
                txt_status.setText("H");
                layout_status.setBackgroundResource((R.drawable.offday));
            }
            if (Status_Title.equals("Weekly Off")){
                txt_status.setText("WO");
                layout_status.setBackgroundResource((R.drawable.offday));
            }
            if (Status_Title.equals("Early Go")){
                txt_status.setText("EG");
                layout_status.setBackgroundResource((R.drawable.eg));
            }
            if (Status_Title.equals("Late Come")){
                txt_status.setText("LC");
                layout_status.setBackgroundResource((R.drawable.lc));
            }

            if (Status_Title.equals("EG+LC")){
                txt_status.setText("E+L");
                layout_status.setBackgroundResource((R.drawable.eg));
            }

            if (Status_Title.equals("Unpaid Half Day Leave"))
            {
                txt_status.setText("UHD");
                layout_status.setBackgroundResource((R.drawable.hd));
            }
            if (Status_Title.equals("Unpaid Leave"))
            {
                txt_status.setText("UP");
                layout_status.setBackgroundResource((R.drawable.hd));
            }
            return vi;
        }

        public void filter(String charText)
        {
            charText = charText.toLowerCase(Locale.getDefault());
            attDetails_list.clear();

            if (charText.length() == 0) {
                attDetails_list.addAll(citylist);
            }
            else {
                int i = 0;
                for (get_set_AttListing wp : citylist)
                {
                    if (wp.getFullName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        attDetails_list.add(wp);
                    }
                }
            }

            notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter1 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        NavDrawerListAdapter.setSelectedPosition(0);
        Intent intent = new Intent(AttendanceListingMonthly.this, AttendanceListing.class);
        startActivity(intent);
        finish();
    }
}
