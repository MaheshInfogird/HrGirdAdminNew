package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewedActivity extends BaseActivityExp
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

//{"mr_id":61,"created_on":"2017-04-11 10:31:24","emp_name":"Kshitija Giri",
// "emp_contact":"7648590467", "designation":"Sr Tester","reviewname":"Leave","application_id":195,
// "reviewId":1,"status":2,"departmentName":null,"remark":"N\/A"}

    public static final String TAG_CreatedOn = "created_on";
    public static final String TAG_EmpName = "emp_name";
    public static final String TAG_Designation = "designation";
    public static final String TAG_ReviewName = "reviewname";
    public static final String TAG_AppId = "application_id";
    public static final String TAG_ReviewId = "reviewId";
    public static final String TAG_Status = "status";

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    URL url;
    public static NetworkChange receiver;

    String fromDate, toDate;
    String uid;
    String Url;
    String url_http;
    String response,response_year;
    String myJson, myJson1, myJson3;
    String selected_date, current_date;
    String fullName, firstName, lastName, applyId, uId, leaveType, description,
            status, empId, leaveDetailsId, Day;

    public int mYear, mMonth, mDay;

    ArrayList<String> year_array = new ArrayList<String>();

    boolean date_select = false;
    boolean hit_once = false;
    boolean refresh = false;

    ProgressDialog progressDialog;
    Toolbar toolbar;
    AutoCompleteTextView ac_leaveApproveDate;
    TextView  txt_no_data;
    ListView approval_list;
    LinearLayout layout_progress, layout_aprv_date;
    Snackbar snackbar;
    FrameLayout content_frame;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter3;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    ListAdapter adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewed);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Reviewed");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        //Log.i("Url", Url);

        content_frame = (FrameLayout) findViewById(R.id.content_frame_approval);

        receiver = new NetworkChange()
        {
            @Override
            protected void onNetworkChange()
            {
                if (receiver.isConnected)
                {
                    if (hit_once) {
                        //getYearData();
                        getReviewedData();
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

        pref = getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        uId = pref.getString("uId", "");
        //Log.i("uId", uId);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_revwedlayout_appr);
        layout_progress = (LinearLayout)findViewById(R.id.revwed_Progress);
        ac_leaveApproveDate = (AutoCompleteTextView)findViewById(R.id.txt_revwedDate);
        approval_list = (ListView)findViewById(R.id.revwed_list);
        layout_aprv_date = (LinearLayout)findViewById(R.id.layout_revwed_date);
        txt_no_data = (TextView)findViewById(R.id.txt_no_revwed_data);

        ac_leaveApproveDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ac_leaveApproveDate.showDropDown();
            }
        });

        ac_leaveApproveDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (internetConnection.hasConnection(ReviewedActivity.this))
                {
                    selected_date = (String) parent.getItemAtPosition(position);
                    array_list.clear();
                    approval_list.setAdapter(null);
                    getReviewedData();
                }
                else {
                    Toast.makeText(ReviewedActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                array_list.clear();
                approval_list.setAdapter(null);
                year_array.clear();
                ac_leaveApproveDate.setAdapter(null);
                Calendar c = Calendar.getInstance();
                DateFormat formater = new SimpleDateFormat("yyyy-MM");
                selected_date = formater.format(c.getTime());
                //current_date = formater.format(c.getTime());
                ac_leaveApproveDate.setText(selected_date);

                getReviewedData();
                getYearData();
            }
        });

        Calendar c = Calendar.getInstance();
        DateFormat formater = new SimpleDateFormat("yyyy-MM");
        selected_date = formater.format(c.getTime());
        ac_leaveApproveDate.setText(selected_date);

        if (internetConnection.hasConnection(getApplicationContext())) {
            hit_once = false;
            //getYearData();
            getReviewedData();
            getYearData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(ReviewedActivity.this);
        }
    }

    public void getReviewedData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh)
                {
                    if (date_select) {
                        layout_progress.setVisibility(View.VISIBLE);
                        approval_list.setVisibility(View.GONE);
                        txt_no_data.setVisibility(View.GONE);
                    }
                    else {
                        progressDialog = ProgressDialog.show(ReviewedActivity.this, "", "Please wait...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/donereview/?";
                    String query3 = String.format("uId=%s&date=%s",
                            URLEncoder.encode(uId, "UTF-8"),
                            URLEncoder.encode(selected_date, "UTF-8"));
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
                    int responceCode = connection.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK)
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
                        approval_list.setVisibility(View.GONE);
                    }
                    else
                    {
                        txt_no_data.setVisibility(View.GONE);
                        layout_progress.setVisibility(View.GONE);
                        approval_list.setVisibility(View.VISIBLE);

                        try
                        {
                            JSONArray jsonArray = new JSONArray(myJson);
                            //Log.i("jsonArray", "" + jsonArray);

                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String created_on = object.getString(TAG_CreatedOn);
                                String emp_name = object.getString(TAG_EmpName);
                                String designation = object.getString(TAG_Designation);
                                String review_name = object.getString(TAG_ReviewName);
                                String app_id = object.getString(TAG_AppId);
                                String review_id = object.getString(TAG_ReviewId);
                                String status = object.getString(TAG_Status);
                                Log.i("get_data : ", "created_on :"+ created_on+"emp_name :"+ emp_name+"designation :"+designation +""+ review_name
                                        +"app_id :"+app_id +"review_id :"+review_id +"status :"+status );

                                map = new HashMap<String, String>();
                                map.put(TAG_CreatedOn,created_on);
                                map.put(TAG_EmpName,emp_name);
                                map.put(TAG_Designation,designation);
                                map.put(TAG_ReviewName,review_name);
                                map.put(TAG_AppId,app_id);
                                map.put(TAG_ReviewId,review_id);
                                map.put(TAG_Status,status);

                                array_list.add(map);

                                adapter1 = new approvalAdapter(ReviewedActivity.this, array_list, R.layout.reviewed_custom, new String[]{}, new int[]{});
                                approval_list.setAdapter(adapter1);
                            }
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
                    Toast.makeText(ReviewedActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetLeaveTakersData getLeaveTakersData = new GetLeaveTakersData();
        getLeaveTakersData.execute();
    }

    public void getYearData()
    {
        class GetYearData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                //progressDialog1 = ProgressDialog.show(getActivity(), "", "Please wait...", true);
                //progressDialog1.show();
            }

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
                    myJson3 = result;
                    Log.i("myJson1", myJson3);
                    //progressDialog1.dismiss();

                    try
                    {
                        JSONArray jsonArray = new JSONArray(myJson3);
                        //Log.i("jsonArray", "" + jsonArray);

                        JSONObject object = jsonArray.getJSONObject(0);

                        fromDate = object.getString(TAG_fromDate);
                        toDate = object.getString(TAG_toDate);

                        DateFormat formater = new SimpleDateFormat("yyyy-MM");

                        Calendar beginCalendar = Calendar.getInstance();
                        Calendar finishCalendar = Calendar.getInstance();

                        try
                        {
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

                        adapter3 = new ArrayAdapter<String>(ReviewedActivity.this, R.layout.dropdown_custom, year_array);
                        ac_leaveApproveDate.setDropDownHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                        ac_leaveApproveDate.setAdapter(adapter3);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    //progressDialog1.dismiss();
                    Toast.makeText(ReviewedActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }

    public class approvalAdapter extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        public approvalAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
        {
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
                vi = inflater.inflate(R.layout.reviewed_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);

            final TextView txt_fullName = (TextView)vi.findViewById(R.id.txt_revwed_name);
            final TextView txt_applyId = (TextView)vi.findViewById(R.id.txt_revwed_appId);
            final TextView txt_ReviewName = (TextView)vi.findViewById(R.id.txt_review_name);
            final TextView txt_status = (TextView)vi.findViewById(R.id.txt_revwed_cid);
            final TextView txt_CreatedOn = (TextView)vi.findViewById(R.id.txt_revwed_date);
            final TextView txt_Designation = (TextView)vi.findViewById(R.id.txt_revwed_designation);
            final TextView txt_ReviewId = (TextView)vi.findViewById(R.id.txt_revwed_leaveid);
            final LinearLayout layout_revwed = (LinearLayout)vi.findViewById(R.id.layout_revwed_apr);

            String full_name = (String)data.get(TAG_EmpName);
            String applyid = (String)data.get(TAG_AppId);
            String ReviewName = (String)data.get(TAG_ReviewName);
            String status = (String)data.get(TAG_Status);
            String CreatedOn = (String)data.get(TAG_CreatedOn);
            String Designation = (String)data.get(TAG_Designation);
            String ReviewId = (String)data.get(TAG_ReviewId);

            txt_fullName.setText(full_name);
            txt_applyId.setText(applyid);
            txt_ReviewName.setText(ReviewName);
            txt_status.setText(status);
            txt_CreatedOn.setText(CreatedOn);
            txt_Designation.setText(Designation);
            txt_ReviewId.setText(ReviewId);

            layout_revwed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String appid = txt_applyId.getText().toString();
                    String leavetype = txt_ReviewName.getText().toString();
                    String name = txt_fullName.getText().toString();
                    String leave_id = txt_ReviewId.getText().toString();
                    //String cId = txt_cid.getText().toString();

                    Intent intent = new Intent(ReviewedActivity.this, ReviewedApprove.class);
                    intent.putExtra("appid", appid);
                    intent.putExtra("leavetype", leavetype);
                    intent.putExtra("leaveId", leave_id);
                    //intent.putExtra("cId", cId);
                    intent.putExtra("name", name);
                    startActivity(intent);
                    //finish();
                }
            });
            return vi;
        }
    }

    public void sendLeaveApproval()
    {
        class SendLeaveApprovalData extends AsyncTask<String, Void, String>
        {
            String post_response;

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(ReviewedActivity.this, "Please wait", "Rejecting leave...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String url1 = ""+url_http+""+Url+"/owner/hrmapi/reactonleave/?";

                    String query = String.format("leaveDetailsId=%s&actiontype=%s&uid=%s&empid=%s&leaveType=%s&leavedate=%s&day=%s",
                            URLEncoder.encode(leaveDetailsId, "UTF-8"),
                            URLEncoder.encode("3", "UTF-8"),
                            URLEncoder.encode(uId, "UTF-8"),
                            URLEncoder.encode(empId, "UTF-8"),
                            URLEncoder.encode(leaveType, "UTF-8"),
                            URLEncoder.encode(selected_date, "UTF-8"),
                            URLEncoder.encode(Day, "UTF-8"));

                    URL post_url = new URL(url1 + query);
                    Log.i("post_url", "" + post_url);

                    HttpURLConnection connection = (HttpURLConnection)post_url.openConnection();
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
                            post_response = "";
                            post_response += line1;
                        }
                    }
                    else
                    {
                        post_response = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return post_response;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson1 = result;
                    Log.i("myJson1", myJson1);
                    progressDialog.dismiss();

                    try {
                        JSONArray jsonArray = new JSONArray(myJson1);
                        //Log.i("jsonArray", "" + jsonArray);

                        JSONObject object = jsonArray.getJSONObject(0);

                        String responceCode = object.getString("responsecode");

                        if (responceCode.equals("1"))
                        {
                            Intent intent = new Intent(ReviewedActivity.this, Approvals.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(ReviewedActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        SendLeaveApprovalData sendLeaveApprovalData = new SendLeaveApprovalData();
        sendLeaveApprovalData.execute();
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
        Intent intent = new Intent(ReviewedActivity.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
