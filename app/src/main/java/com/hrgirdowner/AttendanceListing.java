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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by adminsitrator on 17/02/2017.
 */
public class AttendanceListing extends BaseActivityExp {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    
    private String[] navMenuTitles;  
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    
    public static final String TAG_fullName = "fullName";
    public static final String TAG_firstName = "firstName";
    public static final String TAG_lastName = "lastName";
    public static final String TAG_inTime = "intime";
    public static final String TAG_outTime = "outtime";
    public static final String TAG_status = "mast_title";
    public static final String TAG_cId = "cid";
    public static final String TAG_dept = "br_title";
    public static final String TAG_WorkHrs = "aer_worked_hrs";
    public static final String TAG_uId = "uId";


    public static NetworkChange receiver;
    ProgressDialog progressDialog;
    Toolbar toolbar;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    URL url;
    
    String response;
    String myJson;
    String Url;
    String current_date,date_month;
    String fullName, firstName, lastName;
    String Status_Title;
    String url_http;

    public int mYear, mMonth, mDay;
    int total = 0;
    int lastVisible = 0;
    int arraylenght;
    int startIndex = 0;

    boolean date_select = false;
    boolean hit_once = false;
    boolean userScrolled = false;
    boolean refresh = false;

    TextView ac_attDate, txt_no_data;
    ListView attList_list;
    LinearLayout layout_progress, layout_attList_date;
    Snackbar snackbar;
    FrameLayout content_frame;
    EditText ed_search;
    TextView load_more;
    ProgressBar bar;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayList<get_set_AttListing> array_list1 = new ArrayList<get_set_AttListing>();
    attListAdapter adapter;

    View loadMoreView;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_listing);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            Header.setText("Daily Attendance");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        content_frame = (FrameLayout) findViewById(R.id.content_frame_attList);

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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_att_list);
        layout_progress = (LinearLayout)findViewById(R.id.att_list_Progress);
        ac_attDate = (TextView)findViewById(R.id.att_list_txtDate);
        attList_list = (ListView)findViewById(R.id.att_listing);
        layout_attList_date = (LinearLayout)findViewById(R.id.att_list_date);
        txt_no_data = (TextView)findViewById(R.id.txt_no_attList_data);
        ed_search = (EditText)findViewById(R.id.att_list_edSearch);

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

                        if (ed_search.getText().toString().equals("")) {
                            if (attList_list.getFooterViewsCount() == 0) {
                                attList_list.addFooterView(loadMoreView);
                            }
                        }
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

        ed_search.addTextChangedListener(new TextWatcher()
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
        });

        layout_attList_date.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd = new DatePickerDialog(AttendanceListing.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        if (view.isShown()) 
                        {
                            if (dayOfMonth < 10 && (monthOfYear + 1) < 10)
                            {
                                current_date = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                ac_attDate.setText(current_date);
                            }
                            else if (dayOfMonth < 10) {
                                current_date = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                ac_attDate.setText(current_date);
                            } 
                            else if ((monthOfYear + 1) < 10) {
                                current_date = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                ac_attDate.setText(current_date);
                            } 
                            else {
                                current_date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
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
        });

        attList_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
                DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
                current_date = formater.format(c.getTime());
                ac_attDate.setText(current_date);
                DateFormat formater1 = new SimpleDateFormat("yyyy-MM");

                // date_month = formater1.format(c.getTime());
                /*String string = ac_attDate.getText().toString();
                DateFormat format = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
                Date date = null;
                try {
                    date = format.parse(string);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.println(date); // Sat Jan 02 00:00:00 GMT 2010

                date_month = date.toString();
                Log.i("date_month",date_month);*/
                
                if (attList_list.getFooterViewsCount() > 0) {
                    attList_list.removeFooterView(loadMoreView);
                }
                getAttendanceListing();
            }
        });
        
        Calendar c = Calendar.getInstance();
        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        current_date = formater.format(c.getTime());
        ac_attDate.setText(current_date);
        DateFormat formater1 = new SimpleDateFormat("yyyy-MM");
       // date_month = formater1.format(c.getTime());
       // date_month = formater1.format(ac_attDate.getText().toString());


        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getAttendanceListing();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(AttendanceListing.this);
        }
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
                        get_set.setInTime(object.getString(TAG_inTime));
                        get_set.setOutTime(object.getString(TAG_outTime));
                        get_set.setStatus(object.getString(TAG_status));
                        get_set.setCid(object.getString(TAG_cId));
                        get_set.setDept(object.getString(TAG_dept));
                        get_set.setWorkHour(object.getString(TAG_WorkHrs));
                        get_set.setuId(object.getString(TAG_uId));

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
                
                adapter = new attListAdapter(AttendanceListing.this, array_list1);
                attList_list.setAdapter(adapter);

                startIndex = adapter.getCount();
                attList_list.setSelection(total - lastVisible);
                adapter.notifyDataSetChanged();
            
            }
        }, 2000);
    }

    
    public void getAttendanceListing()
    {
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
                        progressDialog = ProgressDialog.show(AttendanceListing.this, "Please wait", "Getting data...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/datewiseattendanceReport/?";

                    String query3 = String.format("date=%s", URLEncoder.encode(current_date, "UTF-8"));
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
                mSwipeRefreshLayout.setRefreshing(false);
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
                            //Log.i("jsonArray", "" + jsonArray);
                            
                            int length = jsonArray.length();
                            
                            int end;
                            if (length > 13) {
                                end = startIndex + 13;
                                attList_list.addFooterView(loadMoreView);
                            }
                            else {
                                end = length;
                                if (attList_list.getFooterViewsCount() > 0) {
                                    attList_list.removeFooterView(loadMoreView);
                                }
                            }
                            
                            for (int i = startIndex; i < end; i++)
                            {
                                get_set_AttListing get_set = new get_set_AttListing();
                                
                                JSONObject object = jsonArray.getJSONObject(i);
                                
                                firstName = object.getString(TAG_firstName);
                                lastName = object.getString(TAG_lastName);
                                fullName = firstName + " " + lastName;

                                get_set.setFirstName(object.getString(TAG_firstName));
                                get_set.setLastName(object.getString(TAG_lastName));
                                get_set.setFullName(fullName);
                                get_set.setInTime(object.getString(TAG_inTime));
                                get_set.setOutTime(object.getString(TAG_outTime));
                                get_set.setStatus(object.getString(TAG_status));
                                get_set.setCid(object.getString(TAG_cId));
                                get_set.setDept(object.getString(TAG_dept));
                                get_set.setWorkHour(object.getString(TAG_WorkHrs));
                                get_set.setuId(object.getString(TAG_uId));
                                arraylenght = jsonArray.length();
                                
                                array_list1.add(get_set);

                                Log.i("Status",object.getString(TAG_status));

                                adapter = new attListAdapter(AttendanceListing.this, array_list1);
                                attList_list.setAdapter(adapter);
                            }

                            adapter.notifyDataSetChanged();
                            startIndex = adapter.getCount();
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
                    Toast.makeText(AttendanceListing.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.att_list_name);
            final TextView txt_intime   = (TextView)vi.findViewById(R.id.att_list_inTime);
            final TextView txt_outtime  = (TextView)vi.findViewById(R.id.att_list_outTime);
            final TextView txt_cid      = (TextView)vi.findViewById(R.id.att_list_cid);
            final TextView txt_dept     = (TextView)vi.findViewById(R.id.att_list_dept);
            final TextView txt_status   = (TextView)vi.findViewById(R.id.att_list_status);
            final TextView txt_work_hr  = (TextView)vi.findViewById(R.id.att_list_workHr);
            final TextView txt_stat     = (TextView)vi.findViewById(R.id.att_list_stat);
            final LinearLayout layout_status = (LinearLayout)vi.findViewById(R.id.att_list_st_layout);
            final LinearLayout layout_att_list = (LinearLayout)vi.findViewById(R.id.layout_att);

            int srno = position + 1;
         //   String formatted = String.format("%02d", srno);
            final String full_name = attDetails_list.get(position).getFullName();
            final String inTime    = attDetails_list.get(position).getInTime();
            final String outTime   = attDetails_list.get(position).getOutTime();
            final String dept      = attDetails_list.get(position).getDept();
            final String cid       = attDetails_list.get(position).getCid();
            final String status    = attDetails_list.get(position).getStatus();
            final String work_hr   = attDetails_list.get(position).getWorkHour();
            final String stat      = attDetails_list.get(position).getStatus();
            final String uID      = attDetails_list.get(position).getuId();

            txt_srno.setText(srno+"");
            txt_fullName.setText(full_name);
            txt_status.setText(status);
            txt_intime.setText(inTime);
            txt_outtime.setText(outTime);
            txt_dept.setText(dept);
            txt_cid.setText(cid);
            txt_work_hr.setText(work_hr);
            txt_stat.setText(stat);

            layout_att_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String string = ac_attDate.getText().toString();
                    DateFormat format = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = format.parse(string);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    System.out.println(date);// Sat Jan 02 00:00:00 GMT 2010

                    DateFormat formater1 = new SimpleDateFormat("yyyy-MM");

                     date_month = formater1.format(date);

                  //  date_month = date.toString();
                    Log.i("date_month",date_month);

                    Intent it = new Intent(AttendanceListing.this,AttendanceListingMonthly.class);
                    it.putExtra("UID",uID);
                    it.putExtra("Date",date_month);
                    startActivity(it);
                }
            });

            layout_status.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String current_stat = txt_status.getText().toString();
                    if (current_stat.equals("P")){
                        Toast.makeText(AttendanceListing.this, "Present", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("A")){
                        Toast.makeText(AttendanceListing.this, "Absent", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HA")){
                        Toast.makeText(AttendanceListing.this, "Half Absent", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("CL")){
                        Toast.makeText(AttendanceListing.this, "Casual Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("PL")){
                        Toast.makeText(AttendanceListing.this, "Privilege Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("L")){
                        Toast.makeText(AttendanceListing.this, "Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("SL")){
                        Toast.makeText(AttendanceListing.this, "Sick Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("ML")){
                        Toast.makeText(AttendanceListing.this, "Maternity Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HL")){
                        Toast.makeText(AttendanceListing.this, "Half Day Leave", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("HD")){
                        Toast.makeText(AttendanceListing.this, "Half Day", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("H")){
                        Toast.makeText(AttendanceListing.this, "Holiday", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("WO")){
                        Toast.makeText(AttendanceListing.this, "Weekly Off", Toast.LENGTH_SHORT).show();
                    }
                    if (current_stat.equals("EG")){
                        Toast.makeText(AttendanceListing.this, "Early Go", Toast.LENGTH_SHORT).show();
                    }

                    if (current_stat.equals("LC")){
                        Toast.makeText(AttendanceListing.this, "Late Come", Toast.LENGTH_SHORT).show();
                    }

                    if (current_stat.equals("E+L")){
                    Toast.makeText(AttendanceListing.this, "EG+LC", Toast.LENGTH_SHORT).show();
                    }

                    if (current_stat.equals("UP")){
                        Toast.makeText(AttendanceListing.this, "Unpaid Leave", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            Status_Title = txt_stat.getText().toString();

            if (Status_Title.equals("Present")){
                txt_status.setText("P");
                layout_status.setBackgroundResource((R.drawable.present));
            }
            if (Status_Title.equals("Unpaid Leave")){
                txt_status.setText("UP");
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
        Intent intent = new Intent(AttendanceListing.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
