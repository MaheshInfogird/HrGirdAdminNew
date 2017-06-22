package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adminsitrator on 21/01/2017.
 */
public class ReviewApprove extends AppCompatActivity
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    SharedPreferences shared_pref;
    SharedPreferences.Editor editor1;

//[{"id":198,"date":"24 Aug 2017","approvedBy":0,"status":3,"leavestatus":1,"day":2,
// "createdDate":"Mon 03 Apr 2017","ldrcreatedDate":"Mon 03 Apr 2017","leaveDetailsId":198,
// "uId":32,"reason":"leave on 24 ","applyId":138,"reportuId":1,"bdr_id":2,"onleave":"0",
// "leavecount":"2","loginuseractionstatus":1},{"id":199,"date":"25 Aug 2017","approvedBy":0,
// "status":2,"leavestatus":1,"day":3,"createdDate":"Mon 03 Apr 2017","ldrcreatedDate":
// "Mon 03 Apr 2017","leaveDetailsId":199,"uId":32,"reason":"leave on 24 ","applyId":138,
// "reportuId":1,"bdr_id":2,"onleave":"0","leavecount":"2","loginuseractionstatus":1}],
// "status":1,"leavecount":"2"}

    public static final String TAG_LeaveDetails = "dtls";
    public static final String TAG_Id = "id";
    public static final String TAG_Date = "date";
    public static final String TAG_Status = "status";
    public static final String TAG_LeaveStatus = "leavestatus";
    public static final String TAG_Day = "day";
    public static final String TAG_LeaveDetailsId = "leaveDetailsId";
    public static final String TAG_EmpId = "uId";
    public static final String TAG_Reason = "reason";
    public static final String TAG_ApplyId = "applyId";
    public static final String TAG_ReportUid = "reportuId";
    public static final String TAG_Bdr_Id = "bdr_id";
    public static final String TAG_LeaveCount = "leavecount";
    public static final String TAG_EmpOnLeave = "onleave";

    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ProgressDialog progressDialog;
    Toolbar toolbar;
    TextView txt_empName, txt_cId, txt_leaveBalance, txt_dept, txt_days;
    TextView txt_startFrom, txt_allowed, txt_taken, txt_balance;
    Snackbar snackbar;
    ListView leave_approval_list;

    String applyId, leaveType, leaveId, name, uId, cId;
    String leaveDate, leaveDetailsId, leaveStatus;
    String response, post_response, myJson, myJson1;
    String response_leave, myJson2;
    String actionType;
    String current_Date;
    String Url;
    String url_http;
    String Reason;
    String Emp_Id;
    String Reject_Reason;

    boolean hit_once = false;

    URL url, post_url;
    public static NetworkChange receiver;
    ConnectionDetector cd;
    CheckInternetConnection internetConnection;

    ListAdapter adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;

    Button btn_submit;
    public static ArrayList<String> leave_status_list;
    public static ArrayList<String> leave_id_list;
    public static ArrayList<String> leave_date_list;
    public static ArrayList<String> select_reject;
    boolean not_check = false;
    boolean reject_selected = false;
    boolean popup_shows = false;
    PopupWindow pw;
    EditText ed_reason;

    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_approve);

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
                        //getLeaveBalanceData();
                    }
                    if (snackbar != null){
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    Toast.makeText(ReviewApprove.this, "No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        };

        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, PRIVATE_MODE);
        uId = pref.getString("uId", "uId");
        //Log.i("uId", uId);

        boolean flag = getIntent().getBooleanExtra("noti_list_leave",false);
        if (flag)
        {
            applyId = getIntent().getStringExtra("appid");
            leaveId = getIntent().getStringExtra("leaveId");
        }
        else {
            applyId = getIntent().getStringExtra("appid");
            leaveType = getIntent().getStringExtra("leavetype");
            leaveId = getIntent().getStringExtra("leaveId");
            //cId = getIntent().getStringExtra("cId");
            name = getIntent().getStringExtra("name");
        }


        btn_submit = (Button)findViewById(R.id.rev_aprv_submit);
        leave_approval_list = (ListView)findViewById(R.id.leave_approval_list);
        txt_empName = (TextView)findViewById(R.id.leave_aprv_name);
        txt_cId = (TextView)findViewById(R.id.leave_aprv_empid);
        txt_dept = (TextView)findViewById(R.id.rev_aprv_reason);
        txt_days = (TextView)findViewById(R.id.rev_aprv_days);
        txt_startFrom = (TextView)findViewById(R.id.rev_aprv_start_from);
        txt_allowed = (TextView)findViewById(R.id.rev_aprv_allowed);
        txt_taken = (TextView)findViewById(R.id.rev_aprv_taken);
        txt_balance = (TextView)findViewById(R.id.rev_aprv_balance);
        txt_empName.setText(name);
        txt_cId.setText("EMP ID : "+cId);

        leave_status_list = new ArrayList<>();
        leave_id_list = new ArrayList<>();
        leave_date_list = new ArrayList<>();
        select_reject = new ArrayList<>();
        leave_status_list.clear();
        leave_id_list.clear();
        leave_date_list.clear();
        select_reject.clear();

        btn_submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("leave_status_list", leave_status_list.toString());
                Log.i("leave_id_list", leave_id_list.toString());
                Log.i("leave_date_list", leave_date_list.toString());
                Log.i("ArrayList_select_reject", select_reject.toString());
                int index = -1;
                for (int i = 0; i < leave_status_list.size(); i++)
                {
                    if (!not_check)
                    {
                        message = leave_status_list.get(i);
                        if (message.equals("not"))
                        {
                            not_check = true;
                            Log.i("Please select all", "Please select all");
                        }
                        else {
                            not_check = false;
                            Log.i("message", message);
                        }
                    }

                    index = leave_status_list.indexOf("Cancelled");
                    Log.i("index", ""+index);

                    if (index >= 0) {
                        leave_status_list.remove(index);
                        leave_id_list.remove(index);
                        leave_date_list.remove(index);
                    }
                    Log.i("leave_status_list123", leave_status_list.toString());
                    Log.i("leave_id_list123", leave_id_list.toString());
                    Log.i("leave_date_list123", leave_date_list.toString());
                }

                if (not_check)
                {
                    not_check = false;
                    Toast.makeText(getApplicationContext(), "Please take action on all leaves", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String leave_date = leave_date_list.toString();
                    String leave_details_id = leave_id_list.toString();
                    String leave_status = leave_status_list.toString();

                    leaveDate = leave_date.substring(1, leave_date.length()-1);
                    leaveDetailsId = leave_details_id.substring(1, leave_details_id.length()-1);
                    leaveStatus = leave_status.substring(1, leave_status.length()-1);

                    Log.i("post_data : ","select_reject -"+ select_reject.toString());
                    Log.i("post_data : ", "leaveDate -"+leaveDate+"\n"
                            +"leaveDetailsId -"+leaveDetailsId+"\n"
                            +"leaveStatus -"+leaveStatus);

                    reject_selected = false;

                    for (int i = 0; i < select_reject.size(); i++)
                    {
                        if (!reject_selected)
                        {
                            String rejected = select_reject.get(i);
                            Log.i("rejected", rejected);

                            if (rejected.equals("Reject"))
                            {
                                reject_selected = true;
                                Log.i("reject_selected", ""+reject_selected);
                            }
                            else {
                                reject_selected = false;
                                Log.i("reject_selected", ""+reject_selected);
                            }
                        }
                    }

                    if (reject_selected)
                    {
                        popup_shows = true;
                        popup_window(v);
                    }
                    else {
                        Reject_Reason = "";
                        popup_shows = false;
                        sendLeaveApproval();
                        Log.i("succes", "approve");
                    }
                }
            }
        });

        if (internetConnection.hasConnection(getApplicationContext())) {
            hit_once = false;
            getApprovalData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(ReviewApprove.this);
        }
    }


    public void popup_window(View v)
    {
        try {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            LayoutInflater inflater = (LayoutInflater) ReviewApprove.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.reject_popup, (ViewGroup) findViewById(R.id.reject_popup_layout));

            pw = new PopupWindow(layout, width, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            pw.setWidth(width-40);
            pw.showAtLocation(v, Gravity.CENTER, 0, 0);

            dimBehind(pw);

            ed_reason = (EditText)layout.findViewById(R.id.popup_ed_reason);
            Button OkButton = (Button) layout.findViewById(R.id.popup_btn_ok);
            Button cancelButton = (Button) layout.findViewById(R.id.popup_btn_cancel);

            OkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reject_Reason = ed_reason.getText().toString();
                    Log.i("Reject_Reason", Reject_Reason);
                    if (Reject_Reason.equals(""))
                    {
                        Toast.makeText(ReviewApprove.this, "Please enter reason", Toast.LENGTH_LONG).show();
                    }
                    else {
                        sendLeaveApproval();
                    }
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void dimBehind(PopupWindow popupWindow)
    {
        View container;
        if (popupWindow.getBackground() == null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            }
            else {
                container = popupWindow.getContentView();
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            }
            else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }

        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.4f;
        wm.updateViewLayout(container, p);
    }



    public void getApprovalData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(ReviewApprove.this, "Please wait", "Getting data...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/getDataofreview/?";

                    String query3 = String.format("application_id=%s&reviewId=%s&uId=%s",
                            URLEncoder.encode(applyId, "UTF-8"),
                            URLEncoder.encode(leaveId, "UTF-8"),
                            URLEncoder.encode(uId, "UTF-8"));

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
                        JSONObject jsonObject = new JSONObject(myJson);
                        JSONArray array = jsonObject.getJSONArray(TAG_LeaveDetails);

                        String leave_day_count = jsonObject.getString(TAG_LeaveCount);
                        Log.i("leave_day_count", leave_day_count);

                        for (int i = 0; i < array.length(); i++)
                        {
                            JSONObject object = array.getJSONObject(i);

                            String leave_ID = object.getString(TAG_Id);
                            String leave_Date = object.getString(TAG_Date);
                            String emp_id = object.getString(TAG_EmpId);
                            Emp_Id = object.getString(TAG_EmpId);
                            Reason = object.getString(TAG_Reason);
                            String leave_DetailsId = object.getString(TAG_LeaveDetailsId);
                            String Status = object.getString(TAG_Status);
                            String leave_Status = object.getString(TAG_LeaveStatus);
                            String Day = object.getString(TAG_Day);
                            String Apply_id = object.getString(TAG_ApplyId);
                            String report_uid = object.getString(TAG_ReportUid);
                            String bdr_id = object.getString(TAG_Bdr_Id);
                            String onLeave = object.getString(TAG_EmpOnLeave);

                            map = new HashMap<String, String>();
                            map.put(TAG_Id, leave_ID);
                            map.put(TAG_Date, leave_Date);
                            map.put(TAG_EmpId, emp_id);
                            map.put(TAG_Reason, Reason);
                            map.put(TAG_LeaveDetailsId, leave_DetailsId);
                            map.put(TAG_Status, Status);
                            map.put(TAG_LeaveStatus, leave_Status);
                            map.put(TAG_Day, Day);
                            map.put(TAG_ApplyId, Apply_id);
                            map.put(TAG_ReportUid, report_uid);
                            map.put(TAG_Bdr_Id, bdr_id);
                            map.put(TAG_EmpOnLeave, onLeave);


                            array_list.add(map);

                            leave_status_list.add("not");
                            leave_date_list.add(leave_Date);
                            leave_id_list.add(leave_DetailsId);
                            select_reject.add("yes");
                        }

                        adapter = new approvalLeaveAdapter(ReviewApprove.this, array_list, R.layout.review_approve_custom, new String[]{}, new int[]{});
                        leave_approval_list.setAdapter(adapter);
                        setListViewHeightBasedOnChildren(leave_approval_list);

                        //txt_leaveBalance.setText("0");
                        txt_dept.setText("Leave Reason : "+Reason);
                        txt_days.setText("Day : "+leave_day_count);

                        getLeaveBalanceData();
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(ReviewApprove.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetLeaveTakersData getLeaveTakersData = new GetLeaveTakersData();
        getLeaveTakersData.execute();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getDividerHeight()) * (listAdapter.getCount()));

        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public void getLeaveBalanceData()
    {
        class GetLeaveBalanceData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    //http://hrsaas.safegird.com/owner/hrmapi/getEmpBalanceLeave/?empid=32&application_id=138
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/getEmpBalanceLeave/?";

                    String query3 = String.format("empid=%s&application_id=%s",
                            URLEncoder.encode(Emp_Id, "UTF-8"),
                            URLEncoder.encode(applyId, "UTF-8"));

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
                    int responceCode = connection.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = br.readLine()) != null)
                        {
                            response_leave = "";
                            response_leave += line;
                        }
                    }
                    else
                    {
                        response_leave = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_leave;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson2 = result;
                    Log.i("myJson", myJson2);
//{"newarray":[{"startFromForDisp":"Mar 2017","endToForDisp":"Aug 2017","empAllowedLeave":"4.00",
// "emptakenLeave":"2.5","empBalanceleve":1.5}]}
                    try
                    {
                        JSONObject object = new JSONObject(myJson2);

                        JSONArray jsonArray = object.getJSONArray("newarray");

                        JSONObject obj = jsonArray.getJSONObject(0);

                        String start_from = obj.getString("startFromForDisp");
                        String endTo = obj.getString("endToForDisp");
                        String allowed = obj.getString("empAllowedLeave");
                        String taken = obj.getString("emptakenLeave");
                        String balance = obj.getString("empBalanceleve");
                        String empid = obj.getString("empId");
                        String flag = obj.getString("flag");
                        Log.i("flag","=="+flag);



                        if (flag.equals("2"))
                        {
                            String[] start_date = start_from.split(" ");
                            String s_date = start_date[0];
                            String s_year = start_date[1];
                            Log.i("s_date", s_date);
                            Log.i("s_year", s_year);

                            String[] end_date = endTo.split(" ");
                            String e_date = end_date[0];
                            String e_year = end_date[1];
                            Log.i("e_date", e_date);
                            Log.i("e_year", e_year);

                            txt_cId.setText("EMP ID : "+empid);

                            if (s_year.equals(e_year))
                            {
                                txt_startFrom.setText(s_date+"-"+endTo);
                            }
                            else {
                                txt_startFrom.setText(start_from+"-"+endTo);
                            }

                            if (allowed.equals("null"))
                            {
                                txt_allowed.setText("Allowed : 0");
                            }
                            else {
                                txt_allowed.setText("Allowed : " + allowed);
                            }

                            if (taken.equals("null")){
                                txt_taken.setText("Taken : 0");
                            }
                            else {
                                txt_taken.setText("Taken : " + taken);
                            }

                            if (balance.equals("null")){
                                txt_balance.setText("Balance : 0");
                            }
                            else {
                                txt_balance.setText("Balance : "+balance);
                            }
                        }
                        if (flag.equals("1"))
                        {
                            txt_startFrom.setVisibility(View.GONE);
                            txt_allowed.setVisibility(View.GONE);
                            txt_balance.setVisibility(View.GONE);

                            txt_cId.setText("EMP ID : "+empid);
                            if (taken.equals("null"))
                            {
                                txt_taken.setText("Taken : 0");
                            }
                            else
                            {
                                txt_taken.setText("Taken : " + taken);
                            }
                        }
                        if (flag.equals("3"))
                        {

                            txt_startFrom.setVisibility(View.GONE);
                            txt_cId.setText("EMP ID : "+empid);

                            if (allowed.equals("null"))
                            {
                                txt_allowed.setText("Allowed : 0");
                            }
                            else
                            {
                                txt_allowed.setText("Allowed : " + allowed);
                            }
                            if (taken.equals("null"))
                            {
                                txt_taken.setText("Taken : 0");
                            }
                            else
                            {
                                txt_taken.setText("Taken : " + taken);
                            }
                            if (balance.equals("null")){
                                txt_balance.setText("Balance : 0");
                            }
                            else
                            {
                                txt_balance.setText("Balance : "+balance);
                            }
                        }

                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(ReviewApprove.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetLeaveBalanceData getLeaveBalanceData = new GetLeaveBalanceData();
        getLeaveBalanceData.execute();
    }



    public class approvalLeaveAdapter extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        boolean click = false;
        //RadioGroup rd_group;

        public approvalLeaveAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
        {
            super(context, data, resource, from, to);
            mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent)
        {
            View vi = convertView;
            if(convertView == null)
            {
                vi = inflater.inflate(R.layout.review_approve_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_leaveDate = (TextView)vi.findViewById(R.id.rev_aprv_date);
            final TextView txt_leaveDetailsId = (TextView)vi.findViewById(R.id.rev_aprv_details_id);
            final TextView txt_status = (TextView)vi.findViewById(R.id.rev_aprv_status);
            final TextView txt_day = (TextView)vi.findViewById(R.id.rev_aprv_day);
            final TextView txt_onLeave = (TextView)vi.findViewById(R.id.rev_aprv_onLeave);
            final TextView txt_leave_type = (TextView)vi.findViewById(R.id.rev_aprv_leavetype);
            final TextView txt_leave_status = (TextView)vi.findViewById(R.id.rev_aprv_leavestatus);
            final TextView txt_leavestatus = (TextView)vi.findViewById(R.id.rev_aprv_leave_status);
            final RadioButton rd_approve = (RadioButton)vi.findViewById(R.id.rev_rd_approve);
            final RadioButton rd_reject = (RadioButton)vi.findViewById(R.id.rev_rd_reject);
            final RadioGroup rd_group = (RadioGroup)vi.findViewById(R.id.rev_apr_rdGrp);

            final String leave_date = (String)data.get(TAG_Date);
            final String leave_details_id = (String)data.get(TAG_LeaveDetailsId);
            final String status = (String)data.get(TAG_Status);
            final String day = (String)data.get(TAG_Day);
            final String leave_status = (String)data.get(TAG_LeaveStatus);
            final String on_leave = (String)data.get(TAG_EmpOnLeave);

            txt_leaveDate.setText(leave_date);
            txt_leaveDetailsId.setText(leave_details_id);
            txt_status.setText(status);
            txt_day.setText(day);
            txt_onLeave.setText(on_leave);
            txt_leavestatus.setText(leave_status);

            String day_session = txt_day.getText().toString();
            String leaveStatus = txt_leavestatus.getText().toString();
            String Status = txt_status.getText().toString();

            if (day_session.equals("1"))
            {
                txt_leave_type.setText("FD");
            }
            else if (day_session.equals("2"))
            {
                txt_leave_type.setText("1st H");
            }
            else if (day_session.equals("3"))
            {
                txt_leave_type.setText("2nd H");
            }

            if (leaveStatus.equals("1"))
            {
                txt_leave_status.setText("Pending");
            }
            else if (leaveStatus.equals("2"))
            {
                txt_leave_status.setText("Approved");
            }
            else if (leaveStatus.equals("3"))
            {
                txt_leave_status.setText("Rejected");
            }
            else if (leaveStatus.equals("4"))
            {
                txt_leave_status.setText("Cancelled");
            }


            if (!click)
            {
                if (Status.equals("1"))
                {
                    rd_group.setOnCheckedChangeListener(null);
                    Log.i("status", "1");
                    rd_reject.setEnabled(true);
                    rd_approve.setVisibility(View.VISIBLE);
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                }
                else if (Status.equals("2"))
                {
                    Log.i("status", "2");
                    rd_group.setOnCheckedChangeListener(null);
                    rd_approve.setChecked(true);
                    rd_reject.setEnabled(true);
                    leave_status_list.set(position, "approve");

                    rd_approve.setVisibility(View.VISIBLE);
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.approve_rediobutton));
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                }
                else if (Status.equals("3"))
                {
                    Log.i("status", "3");
                    rd_group.setOnCheckedChangeListener(null);
                    rd_approve.setVisibility(View.GONE);

                    rd_reject.setChecked(true);
                    leave_status_list.set(position, "reject");
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                }
                else if (leaveStatus.equals("4"))
                {
                    Log.i("status", "4");
                    rd_group.setOnCheckedChangeListener(null);
                    rd_approve.setVisibility(View.GONE);

                    rd_reject.setChecked(true);
                    leave_status_list.set(position, "Cancelled");
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                }
            }

            /*rd_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // TODO Auto-generated method stub
                    View radioButton = group.findViewById(checkedId);
                    int radioId = group.indexOfChild(radioButton);
                    click = true;

                    Log.i("radioId", ""+radioId);

                    if(radioId == 0)
                    {
                        leave_status_list.set(position, "Approved");
                        Log.i("selectedAnswers_approve", ""+leave_status_list);
                        Log.i("position_approve", ""+position);
                        rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.approve_rediobutton));
                        rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                    }

                    else if(radioId == 1)
                    {
                        leave_status_list.set(position, "Rejected");
                        Log.i("selectedAnswers_reject", ""+leave_status_list);
                        Log.i("position_reject", ""+position);
                        rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                        rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                    }
                }
            });*/

            rd_approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click = true;
                    //if (isChecked) {
                        //leave_status_list.notify();
                    leave_status_list.set(position, "approve");
                    select_reject.set(position, "Approve");
                    Log.i("position_approve", ""+position);
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.approve_rediobutton));
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_reject_new));
                    //}
                }
            });

            rd_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click = true;
                    //if (isChecked) {
                        //leave_status_list.notify();
                    leave_status_list.set(position, "reject");
                    select_reject.set(position, "Reject");
                    Log.i("position_reject", ""+position);
                    rd_reject.setButtonDrawable(getResources().getDrawable(R.drawable.reject_rediobutton));
                    rd_approve.setButtonDrawable(getResources().getDrawable(R.drawable.unchecked_approve_new));
                    //}
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
                progressDialog = ProgressDialog.show(ReviewApprove.this, "Please wait", "Processing request...", true);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String url1 = ""+url_http+""+Url+"/owner/hrmapi/responseonleavereview/?";

                    String query = String.format("loginuId=%s&application_id=%s&reason=%s&leaveid=%s&leavestatus=%s&leaveDate=%s",
                            URLEncoder.encode(uId, "UTF-8"),
                            URLEncoder.encode(applyId, "UTF-8"),
                            URLEncoder.encode(Reject_Reason, "UTF-8"),
                            URLEncoder.encode(leaveDetailsId, "UTF-8"),
                            URLEncoder.encode(leaveStatus, "UTF-8"),
                            URLEncoder.encode(leaveDate, "UTF-8"));

                    Log.i("query", "" + query);

                    String replace_query = query.replace("%2C+",",");
                    Log.i("replace_query", "" + replace_query);

                    post_url = new URL(url1 + replace_query);
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

                    if (popup_shows) {
                        pw.dismiss();
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(myJson1);
                        Log.i("jsonArray", "" + jsonObject);

                        String response = jsonObject.getString("result");
                        Log.i("response", "" + response);

                        if (response.equals("1"))
                        {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReviewApprove.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setMessage("Reviewed Successfully");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ReviewApprove.this, ReviewActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            alertDialog.show();

                        }
                        else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReviewApprove.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setMessage("Something went wrong");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            alertDialog.show();
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(ReviewApprove.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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
        //allEds_redio.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leave_status_list.clear();
        Intent intent = new Intent(ReviewApprove.this, ReviewActivity.class);
        startActivity(intent);
        finish();
    }
}
