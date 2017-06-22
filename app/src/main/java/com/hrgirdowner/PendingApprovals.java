package com.hrgirdowner;

import android.app.ProgressDialog;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingApprovals extends BaseActivityExp {

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
    public static final String TAG_applyId = "applyId";
    public static final String TAG_startfrom = "startfrom";
    public static final String TAG_endto = "endto";
    public static final String TAG_description = "description";
    public static final String TAG_leaveType = "leaveType";
    public static final String TAG_uId = "uId";
    public static final String TAG_cId = "cid";
    
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    public static NetworkChange receiver;

    URL url;
    
    String Url;
    String url_http;
    String response;
    String myJson, myJson1;
    String fullName, firstName, lastName, applyId, uId, leaveType, description, startFrom, endTo, cId;
    
    boolean hit_once = false;
    boolean refresh = false;
    
    ProgressDialog progressDialog;
    Toolbar toolbar;
    ListView approval_list;
    Snackbar snackbar;
    FrameLayout content_frame;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;

    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Pending Approvals");
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
                        getApprovalData();
                    }
                    if (snackbar != null) {
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    Toast.makeText(PendingApprovals.this, "No internet connection", Toast.LENGTH_LONG).show();
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_pnd);
        approval_list = (ListView)findViewById(R.id.pendingApproval_list);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                array_list.clear();
                approval_list.setAdapter(null);
                getApprovalData();
            }
        });
        
        if (internetConnection.hasConnection(getApplicationContext())) {
            hit_once = false;
            refresh = false;
            getApprovalData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(PendingApprovals.this);
        }
    }
    
    public void getApprovalData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh) {
                    progressDialog = ProgressDialog.show(PendingApprovals.this, "Please wait", "Getting data...", true);
                    progressDialog.show();
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/pendingleavelist";
                    url = new URL(leave_url);
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
                    if (!refresh) {
                        progressDialog.dismiss();
                    }

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
                            uId = object.getString(TAG_uId);
                            cId = object.getString(TAG_cId);
                            startFrom = object.getString(TAG_startfrom);
                            endTo = object.getString(TAG_endto);
                            description = object.getString(TAG_description);

                            map = new HashMap<String, String>();
                            map.put(TAG_fullName, fullName);
                            map.put(TAG_applyId, applyId);
                            map.put(TAG_leaveType, leaveType);
                            map.put(TAG_uId, uId);
                            map.put(TAG_cId, cId);
                            map.put(TAG_startfrom, startFrom);
                            map.put(TAG_endto, endTo);
                            map.put(TAG_description, description);

                            array_list.add(map);

                            adapter1 = new approvalAdapter(PendingApprovals.this, array_list, R.layout.pendig_approvals_custom, new String[]{}, new int[]{});
                            approval_list.setAdapter(adapter1);
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else 
                {
                    if (progressDialog.isShowing() && progressDialog != null){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(PendingApprovals.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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
                vi = inflater.inflate(R.layout.pendig_approvals_custom, null);
            }

            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.txt_pendingaprv_name);
            final TextView txt_date = (TextView)vi.findViewById(R.id.txt_pendingaprv_date);
            final TextView txt_description = (TextView)vi.findViewById(R.id.txt_pendingaprv_description);
            final TextView txt_applyId = (TextView)vi.findViewById(R.id.txt_pendingaprv_applyId);
            final TextView txt_leaveType = (TextView)vi.findViewById(R.id.txt_pendingaprv_leaveType);
            final TextView txt_uid = (TextView)vi.findViewById(R.id.txt_pendingaprv_uId);
            final TextView txt_cid = (TextView)vi.findViewById(R.id.txt_pendingaprv_cid);
            final LinearLayout layout_aprv = (LinearLayout)vi.findViewById(R.id.layout_pendingaprv);

            final String full_name = (String)data.get(TAG_fullName);
            final String from_date = (String)data.get(TAG_startfrom);
            final String to_date = (String)data.get(TAG_endto);
            final String date = from_date +" to " + to_date;
            final String descrption = (String)data.get(TAG_description);
            final String apply_id = (String)data.get(TAG_applyId);
            final String leave_type = (String)data.get(TAG_leaveType);
            final String uid = (String)data.get(TAG_uId);
            final String cid = (String)data.get(TAG_cId);

            txt_fullName.setText(full_name);
            txt_date.setText(date);
            txt_description.setText(descrption);
            txt_applyId.setText(apply_id);
            txt_leaveType.setText(leave_type);
            txt_uid.setText(uid);
            txt_cid.setText(cid);

            layout_aprv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String applyid = txt_applyId.getText().toString();
                    String leavetype = txt_leaveType.getText().toString();
                    String name = txt_fullName.getText().toString();
                    String uId = txt_uid.getText().toString();
                    String cId = txt_cid.getText().toString();

                    Intent intent = new Intent(PendingApprovals.this, LeaveApprovals.class);
                    intent.putExtra("applyid", applyid);
                    intent.putExtra("leavetype", leavetype);
                    intent.putExtra("uId", uId);
                    intent.putExtra("cId", cId);
                    intent.putExtra("name", name);
                    startActivity(intent);
                    //finish();
                }
            });

            return vi;
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
        Intent intent = new Intent(PendingApprovals.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
