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
import android.widget.ArrayAdapter;
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

/**
 * Created by adminsitrator on 21/01/2017.
 */
public class Approvals extends BaseActivityExp 
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

    public static final String TAG_fullName = "fullName";
    public static final String TAG_firstName = "firstName";
    public static final String TAG_lastName = "lastName";
    public static final String TAG_applyId = "applyId";
    public static final String TAG_description = "description";
    public static final String TAG_leaveType = "leaveType";
    public static final String TAG_status = "status";
    public static final String TAG_leaveDetailsId = "leaveDetailsId";
    public static final String TAG_day = "day";
    public static final String TAG_uId = "uId";

    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    URL url;
    public static NetworkChange receiver;

    String Url;
    String url_http;
    String response;
    String myJson, myJson1;
    String selected_date, current_date;
    String fullName, firstName, lastName, applyId, uId, leaveType, description,
            status, empId, leaveDetailsId, Day;

    public int mYear, mMonth, mDay;
    
    boolean date_select = false;
    boolean hit_once = false;
    boolean refresh = false;
    
    ProgressDialog progressDialog;
    Toolbar toolbar;
    
    TextView ac_leaveApproveDate, txt_no_data;
    ListView approval_list;
    LinearLayout layout_progress, layout_aprv_date;
    Snackbar snackbar;
    FrameLayout content_frame;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    ListAdapter adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvals);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Approved Leaves");
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
                        getApprovalData();
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_appr);
        layout_progress = (LinearLayout)findViewById(R.id.approval_Progress);
        ac_leaveApproveDate = (TextView)findViewById(R.id.txt_leaveApproveDate);
        approval_list = (ListView)findViewById(R.id.approval_list);
        layout_aprv_date = (LinearLayout)findViewById(R.id.layout_aprv_date);
        txt_no_data = (TextView)findViewById(R.id.txt_no_aprv_data);

        layout_aprv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd = new DatePickerDialog(Approvals.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (view.isShown())
                        {
                            if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                                selected_date = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                ac_leaveApproveDate.setText(selected_date);
                            }
                            else if (dayOfMonth < 10) {
                                selected_date = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                ac_leaveApproveDate.setText(selected_date);
                            }
                            else if ((monthOfYear + 1) < 10) {
                                selected_date = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                ac_leaveApproveDate.setText(selected_date);
                            }
                            else {
                                selected_date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                ac_leaveApproveDate.setText(selected_date);
                            }
                            array_list.clear();
                            approval_list.setAdapter(null);
                            refresh = false;
                            date_select = true;
                            getApprovalData();
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.show();
            }
        });
        

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                array_list.clear();
                approval_list.setAdapter(null);
                Calendar c = Calendar.getInstance();
                DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
                selected_date = formater.format(c.getTime());
                current_date = formater.format(c.getTime());
                ac_leaveApproveDate.setText(selected_date);
                
                getApprovalData();
            }
        });

        Calendar c = Calendar.getInstance();
        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        selected_date = formater.format(c.getTime());
        current_date = formater.format(c.getTime());
        ac_leaveApproveDate.setText(selected_date);
        
        if (internetConnection.hasConnection(getApplicationContext())) {
            hit_once = false;
            //getYearData();
            getApprovalData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(Approvals.this);
        }
    }
    
    public void getApprovalData()
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
                        progressDialog = ProgressDialog.show(Approvals.this, "", "Please wait...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/approvedleavelist/?";
                    String query3 = String.format("date=%s", URLEncoder.encode(selected_date, "UTF-8"));
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

                                firstName = object.getString(TAG_firstName);
                                lastName = object.getString(TAG_lastName);
                                fullName = firstName + " " + lastName;
                                applyId = object.getString(TAG_applyId);
                                leaveType = object.getString(TAG_leaveType);
                                empId = object.getString(TAG_uId);
                                String leaveDetailsId = object.getString(TAG_leaveDetailsId);
                                String Day = object.getString(TAG_day);
                                description = object.getString(TAG_description);
                                status = object.getString(TAG_status);

                                map = new HashMap<String, String>();
                                map.put(TAG_fullName, fullName);
                                map.put(TAG_applyId, applyId);
                                map.put(TAG_leaveType, leaveType);
                                map.put(TAG_uId, empId);
                                map.put(TAG_leaveDetailsId, leaveDetailsId);
                                map.put(TAG_day, Day);
                                map.put(TAG_description, description);
                                map.put(TAG_status, status);

                                array_list.add(map);

                                adapter1 = new approvalAdapter(Approvals.this, array_list, R.layout.approvals_custom, new String[]{}, new int[]{});
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
                    Toast.makeText(Approvals.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetLeaveTakersData getLeaveTakersData = new GetLeaveTakersData();
        getLeaveTakersData.execute();
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
                vi = inflater.inflate(R.layout.approvals_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.txt_aprv_name);
            final TextView txt_description = (TextView)vi.findViewById(R.id.txt_aprv_description);
            final TextView txt_applyId = (TextView)vi.findViewById(R.id.txt_aprv_applyId);
            final TextView txt_leaveType = (TextView)vi.findViewById(R.id.txt_aprv_leaveType);
            final TextView txt_status = (TextView)vi.findViewById(R.id.txt_aprv_status);
            final LinearLayout layout_aprv = (LinearLayout)vi.findViewById(R.id.layout_aprv);
            final ImageView action = (ImageView)vi.findViewById(R.id.img_aprv);
            final TextView txt_uid = (TextView)vi.findViewById(R.id.txt_aprv_uId);
            final TextView txt_leaveDetailsId = (TextView)vi.findViewById(R.id.txt_aprv_leaveDetailId);
            final TextView txt_day = (TextView)vi.findViewById(R.id.txt_aprv_day);

            final String full_name = (String)data.get(TAG_fullName);
            final String descrption = (String)data.get(TAG_description);
            final String apply_id = (String)data.get(TAG_applyId);
            final String leave_type = (String)data.get(TAG_leaveType);
            final String status = (String)data.get(TAG_status);
            final String uid = (String)data.get(TAG_uId);
            final String leave_details_id = (String)data.get(TAG_leaveDetailsId);
            final String day = (String)data.get(TAG_day);
            
            txt_fullName.setText(full_name);
            txt_status.setText(status);
            txt_description.setText(descrption);
            txt_applyId.setText(apply_id);
            txt_leaveType.setText(leave_type);
            txt_uid.setText(uid);
            txt_leaveDetailsId.setText(leave_details_id);
            txt_day.setText(day);
            
            if (status.equals("2")){
                action.setImageDrawable(getResources().getDrawable(R.drawable.checked_approve_radio_btn));
            }
            if (status.equals("3")){
                action.setImageDrawable(getResources().getDrawable(R.drawable.checked_rejected_radio_btn));
            }
            if (status.equals("4")){
                action.setImageDrawable(getResources().getDrawable(R.drawable.checked_rejected_radio_btn));
            }

            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String applyid = txt_applyId.getText().toString();
                    String leavetype = txt_leaveType.getText().toString();
                    String emp_id = txt_uid.getText().toString();
                    Day = txt_day.getText().toString();
                    String status = txt_status.getText().toString();
                    leaveDetailsId = txt_leaveDetailsId.getText().toString();
                    
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date1 = new Date();
                    Date date2 = new Date();

                    try
                    {
                        date1 = format.parse(selected_date);
                        date2 = format.parse(current_date);
                    }
                    catch (ParseException e) {
                        Log.e("ParseException", e.toString());
                    }

                    if (status.equals("2")) {

                        if (date1.before(date2)) 
                        {
                            Toast.makeText(Approvals.this, "You can't reject", Toast.LENGTH_SHORT).show();
                        } 
                        else {

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Approvals.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setMessage("Do you want to Reject Leave?");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendLeaveApproval();
                                }
                            });
                            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            alertDialog.show();
                        }
                    }
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
                progressDialog = ProgressDialog.show(Approvals.this, "Please wait", "Rejecting leave...", true);
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
                            Intent intent = new Intent(Approvals.this, Approvals.class);
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
                    Toast.makeText(Approvals.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(Approvals.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
