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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
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
public class LeaveApprovals extends AppCompatActivity
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    SharedPreferences shared_pref;
    SharedPreferences.Editor editor1;
    
    public static final String TAG_leaveDate = "leavedate";
    public static final String TAG_status = "status";
    public static final String TAG_leaveDetailsId = "leaveDetailsId";
    public static final String TAG_day = "day";
    public static final String TAG_leaveBalance = "leavebalance";
    public static final String TAG_departmentName = "departmentName";
    public static final String TAG_emponleave = "emponleave";

    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String password;
    
    ProgressDialog progressDialog;
    Toolbar toolbar;
    TextView txt_empName, txt_cId, txt_leaveBalance, txt_dept;
    Snackbar snackbar;
    CoordinatorLayout snackbarCoordinatorLayout;
    ListView leave_approval_list;
    
    String applyId, leaveType, empId, name, uId, cId;
    String leaveDate, Status, leaveDetailsId, Day;
    String response, post_response, myJson, myJson1;
    String actionType;
    String LeaveBalance, Dept, EmpOnLeave;
    String current_Date;
    String Url;
    String url_http;

    boolean hit_once = false;
    
    URL url, post_url;
    public static NetworkChange receiver;
    ConnectionDetector cd;
    CheckInternetConnection internetConnection;
    
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.approvals_leaves);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) 
        {
            getSupportActionBar().setTitle("");
            Header.setText("Leave Approvals");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        //Log.i("Url", Url);

        //snackbarCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.snackbarCoordinatorLayout);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        current_Date = format.format(c.getTime());
        
        receiver = new NetworkChange()
        {
            @Override
            protected void onNetworkChange()
            {
                if (receiver.isConnected)
                {
                    if (hit_once) {
                        getApprovalData();
                    }
                    if (snackbar != null){
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    Toast.makeText(LeaveApprovals.this, "No internet connection", Toast.LENGTH_LONG).show();
                   /* snackbar = Snackbar.make(snackbarCoordinatorLayout, "Please check your internet connection", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.RedTextColor));
                    snackbar.show();*/
                }
            }
        };
        
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        uId = pref.getString("uId", "uId");
        //Log.i("uId", uId);
        
        applyId = getIntent().getStringExtra("applyid");
        leaveType = getIntent().getStringExtra("leavetype");
        empId = getIntent().getStringExtra("uId");
        cId = getIntent().getStringExtra("cId");
        name = getIntent().getStringExtra("name");

        leave_approval_list = (ListView)findViewById(R.id.leave_approval_list);
        txt_empName = (TextView)findViewById(R.id.leave_aprv_name);
        txt_cId = (TextView)findViewById(R.id.leave_aprv_empid);
        txt_leaveBalance = (TextView)findViewById(R.id.leave_aprv_total_days);
        txt_dept = (TextView)findViewById(R.id.leave_aprv_dept);
        txt_empName.setText(name);
        txt_cId.setText("EMP ID : "+cId);

        if (internetConnection.hasConnection(getApplicationContext())) {
            hit_once = false;
            getApprovalData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(LeaveApprovals.this);
        }
    }

    public void getApprovalData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(LeaveApprovals.this, "Please wait", "Getting data...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/viewleavedetails/?";
                    //Log.i("leave_url", leave_url);
                    String query3 = String.format("applyId=%s&uId=%s&leaveType=%s",
                            URLEncoder.encode(applyId, "UTF-8"),
                            URLEncoder.encode(empId, "UTF-8"),
                            URLEncoder.encode(leaveType, "UTF-8"));
                    
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
                if (result != null) 
                {
                    myJson = result;
                    Log.i("myJson", myJson);
                    progressDialog.dismiss();
                    try
                    {
                        JSONArray jsonArray = new JSONArray(myJson);
                        //Log.i("jsonArray", "" + jsonArray);

                        for (int i = 0; i < jsonArray.length(); i++) 
                        {
                            JSONObject object = jsonArray.getJSONObject(i);

                            String leave_Date = object.getString(TAG_leaveDate);
                            String leave_DetailsId = object.getString(TAG_leaveDetailsId);
                            String Status_ = object.getString(TAG_status);
                            String Day_ = object.getString(TAG_day);
                            LeaveBalance = object.getString(TAG_leaveBalance);
                            Dept = object.getString(TAG_departmentName);
                            EmpOnLeave = object.getString(TAG_emponleave);
                            
                            map = new HashMap<String, String>();
                            map.put(TAG_leaveDate, leave_Date);
                            map.put(TAG_leaveDetailsId, leave_DetailsId);
                            map.put(TAG_status, Status_);
                            map.put(TAG_day, Day_);
                            map.put(TAG_leaveBalance, LeaveBalance);
                            map.put(TAG_departmentName, Dept);
                            map.put(TAG_emponleave, EmpOnLeave);
                            
                            array_list.add(map);

                            adapter = new approvalLeaveAdapter(LeaveApprovals.this, array_list, R.layout.approvals_leaves_custom, new String[]{}, new int[]{});
                            leave_approval_list.setAdapter(adapter);
                        }

                        txt_leaveBalance.setText(LeaveBalance);
                        txt_dept.setText("DEPT : "+Dept);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LeaveApprovals.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetLeaveTakersData getLeaveTakersData = new GetLeaveTakersData();
        getLeaveTakersData.execute();
    }

    public class approvalLeaveAdapter extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        public approvalLeaveAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
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
                vi = inflater.inflate(R.layout.approvals_leaves_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_leaveDate = (TextView)vi.findViewById(R.id.leave_aprv_date);
            final TextView txt_leaveDetailsId = (TextView)vi.findViewById(R.id.leave_aprv_details_id);
            final TextView txt_status = (TextView)vi.findViewById(R.id.leave_aprv_status);
            final TextView txt_day = (TextView)vi.findViewById(R.id.leave_aprv_day);
            final TextView txt_onLeave = (TextView)vi.findViewById(R.id.leave_aprv_onLeave);
            final TextView txt_approved = (TextView)vi.findViewById(R.id.approved_radio);
            final TextView txt_rejected = (TextView)vi.findViewById(R.id.rejected_radio);
            final TextView txt_leave_type = (TextView)vi.findViewById(R.id.leave_aprv_leavetype);
            final RadioButton rd_approve = (RadioButton)vi.findViewById(R.id.rd_approve);
            final RadioButton rd_reject = (RadioButton)vi.findViewById(R.id.rd_reject);
            final LinearLayout approve_layout = (LinearLayout)vi.findViewById(R.id.approve_layout);
            final LinearLayout reject_layout = (LinearLayout)vi.findViewById(R.id.reject_layout);

            final String leave_date = (String)data.get(TAG_leaveDate);
            final String leave_details_id = (String)data.get(TAG_leaveDetailsId);
            final String status = (String)data.get(TAG_status);
            final String day = (String)data.get(TAG_day);
            final String on_leave = (String)data.get(TAG_emponleave);

            txt_leaveDate.setText(leave_date);
            txt_leaveDetailsId.setText(leave_details_id);
            txt_status.setText(status);
            txt_day.setText(day);
            txt_onLeave.setText(on_leave);
            
            if (day.equals("1"))
            {
                txt_leave_type.setText("FullDay");
            }
            if (day.equals("2"))
            {
                txt_leave_type.setText("1st Half");
            }
            if (day.equals("3"))
            {
                txt_leave_type.setText("2nd Half");
            }
            
            if (status.equals("1"))
            {
                txt_approved.setText("Approve ");
                txt_rejected.setText("Reject");
                rd_approve.setEnabled(true);
                rd_reject.setEnabled(true);
                rd_approve.setChecked(false);
                rd_reject.setChecked(false);
                rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                approve_layout.setVisibility(View.VISIBLE);
                reject_layout.setVisibility(View.VISIBLE);
            }
            if (status.equals("2"))
            {
                txt_approved.setText("Approved");
                txt_rejected.setText("Cancel");
                rd_approve.setChecked(true);
                rd_approve.setEnabled(false);
                rd_reject.setEnabled(true);
                rd_reject.setChecked(false);
                rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.approve_rediobutton));
                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                approve_layout.setVisibility(View.VISIBLE);
                reject_layout.setVisibility(View.VISIBLE);
            }
            if (status.equals("3"))
            {
                txt_rejected.setText("Rejected");
                rd_reject.setChecked(true);
                rd_reject.setEnabled(false);
                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                approve_layout.setVisibility(View.GONE);
                reject_layout.setVisibility(View.VISIBLE);
            }

            if (status.equals("4"))
            {
                txt_rejected.setText("Cancelled");
                rd_reject.setChecked(true);
                rd_reject.setEnabled(false);
                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                approve_layout.setVisibility(View.GONE);
                reject_layout.setVisibility(View.VISIBLE);
            }

            if (status.equals("5"))
            {
                txt_rejected.setText("EG/LC");
                rd_reject.setChecked(false);
                rd_reject.setEnabled(false);
                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                approve_layout.setVisibility(View.GONE);
                reject_layout.setVisibility(View.VISIBLE);
                rd_reject.setVisibility(View.GONE);
            }

            rd_approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LeaveApprovals.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    alertDialog.setTitle("Approve");
                    alertDialog.setMessage("Do you want to Approve this Leave?");
                    alertDialog.setCancelable(true);
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leaveDetailsId = txt_leaveDetailsId.getText().toString();
                            actionType = "2";
                            leaveDate = txt_leaveDate.getText().toString();
                            Day = txt_day.getText().toString();
                            sendLeaveApproval();
                            txt_approved.setText("Approved");
                            txt_rejected.setText("Cancel");
                            rd_approve.setChecked(true);
                            rd_approve.setEnabled(false);
                            rd_reject.setEnabled(true);
                            rd_reject.setChecked(false);
                            rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.approve_rediobutton));
                            approve_layout.setVisibility(View.VISIBLE);
                            reject_layout.setVisibility(View.VISIBLE);
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alertDialog.show();
                }
            });

            rd_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LeaveApprovals.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    alertDialog.setTitle("Reject");
                    alertDialog.setMessage("Do you want to Reject this Leave?");
                    alertDialog.setCancelable(true);
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leaveDetailsId = txt_leaveDetailsId.getText().toString();
                            actionType = "3";
                            leaveDate = txt_leaveDate.getText().toString();
                            Day = txt_day.getText().toString();
                            String date = txt_leaveDate.getText().toString();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                            Date date1 = new Date();
                            Date date2 = new Date();

                            try
                            {
                                date1 = format.parse(current_Date);
                                date2 = format.parse(date);
                            }
                            catch (ParseException e) {
                                Log.e("ParseException", e.toString());
                            }

                            if (rd_approve.isChecked())
                            {
                                if (date2.before(date1))
                                {
                                    Toast.makeText(LeaveApprovals.this, "You can't cancel", Toast.LENGTH_SHORT).show();
                                    rd_reject.setChecked(false);
                                }
                                else {
                                    sendLeaveApproval();
                                    txt_rejected.setText("Rejected");
                                    rd_reject.setChecked(true);
                                    rd_reject.setEnabled(false);
                                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                                    approve_layout.setVisibility(View.GONE);
                                    reject_layout.setVisibility(View.VISIBLE);
                                }
                            }
                            else {
                                sendLeaveApproval();
                                txt_rejected.setText("Rejected");
                                rd_reject.setChecked(true);
                                rd_reject.setEnabled(false);
                                rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                                approve_layout.setVisibility(View.GONE);
                                reject_layout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alertDialog.show();
                }
            });
            
            return vi;
        }
    }

    public void sendLeaveApproval()
    {
        class SendLeaveApprovalData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(LeaveApprovals.this, "Please wait", "Processing request...", true);
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
                            URLEncoder.encode(actionType, "UTF-8"),
                            URLEncoder.encode(uId, "UTF-8"),
                            URLEncoder.encode(empId, "UTF-8"),
                            URLEncoder.encode(leaveType, "UTF-8"),
                            URLEncoder.encode(leaveDate, "UTF-8"),
                            URLEncoder.encode(Day, "UTF-8"));
                    
                    post_url = new URL(url1 + query);
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
                    } 
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LeaveApprovals.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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
        super.onBackPressed();
        Intent intent = new Intent(LeaveApprovals.this, PendingApprovals.class);
        startActivity(intent);
        finish();
    }
}
