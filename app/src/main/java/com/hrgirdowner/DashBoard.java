package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by adminsitrator on 18/01/2017.
 */
public class DashBoard extends BaseActivityExp {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    boolean backPressTwice = false;
    
    private Handler handler = new Handler();
    
    Toolbar toolbar;
    TextView txt_emp_count, txt_presentPercent, txt_attendanceDate, txt_pieDate, txt_piePer;
    LinearLayout layout_attDate, layout_pieDate;
    LinearLayout graph_progress, layout_navigation;
    TextView txt_no_data;
    TextView txt_present, txt_onLeave, txt_absent;
    static TextView tool_tv_count;
    
    public int mYear, mMonth, mDay;

    BarChart chart;
    ArrayList<BarEntry> barEntries;
    ArrayList<String> barEntryLabels;
    BarDataSet barDataSet;
    BarData barData;
    
    PieChart pieChart;
    PieDataSet pieDataSet;
    
    URL url;
    
    String response = "";
    String myJson = "", myJson1 = "", myJson4 = "";
    String presentPrecent;
    String currentDate, attendanceDate;
    String Url;
    String url_http;
    String noti_count;
    String Packagename, response_version;
    
    String img_url = "http://infogird.gogird.com/files/infogird.gogird.com/images/employeephoto/";
    
    //double presentEmp=0f, lateEmp = 0f, onLeaveEmp = 0f, absentEmp = 0f;
    int presentEmp=0, lateEmp = 0, onLeaveEmp = 0, absentEmp = 0;
    int version_code;
    
    public static NetworkChange receiver;
    Snackbar snackbar;
    FrameLayout content_frame;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    
    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;

    SwipeRefreshLayout mSwipeRefreshLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        LinearLayout tool_notification_layout = (LinearLayout)findViewById(R.id.tool_notification_layout);
        tool_notification_layout.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            Header.setText("Dashboard");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpDrawer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    /*    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        set(navMenuTitles, navMenuIcons);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();*/

        tool_notification_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(DashBoard.this,Notification.class);
                startActivity(it);
            }
        });

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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        graph_progress = (LinearLayout) findViewById(R.id.graph_Progress);

        txt_no_data = (TextView)findViewById(R.id.txt_no_data);
        layout_navigation = (LinearLayout)findViewById(R.id.navigation_color); 
        pieChart = (PieChart) findViewById(R.id.chart);
        chart = (BarChart) findViewById(R.id.chart1);
        txt_emp_count = (TextView) findViewById(R.id.emp_count);
        txt_presentPercent = (TextView) findViewById(R.id.txt_presentPercent);
        txt_attendanceDate = (TextView) findViewById(R.id.txt_attendanceDate);
        layout_attDate = (LinearLayout) findViewById(R.id.layout_attDate);
        txt_pieDate = (TextView) findViewById(R.id.txt_inTimeDate);
        layout_pieDate = (LinearLayout) findViewById(R.id.layout_pieDate);
        txt_piePer = (TextView) findViewById(R.id.pie_ovrallPer); 
        content_frame = (FrameLayout) findViewById(R.id.content_frame);
        tool_tv_count = (TextView)findViewById(R.id.tool_tv_count);

        txt_present = (TextView)findViewById(R.id.txt_dash_present);
        txt_onLeave = (TextView)findViewById(R.id.txt_dash_onleave);
        txt_absent = (TextView)findViewById(R.id.txt_dash_absent);

        pieChart.setVisibility(View.GONE);
        receiver = new NetworkChange() {
            @Override
            protected void onNetworkChange() {
                if (receiver.isConnected) {
                    if (hit_once) {
                        getDailyAttendance();
                    }

                    if (snackbar != null) {
                        snackbar.dismiss();
                    }
                } 
                else {
                    hit_once = true;
                    snackbar = Snackbar.make(content_frame, "Please check your internet connection", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.RedTextColor));
                    snackbar.show();
                }
            }
        };

        noti_count = String.valueOf(MyFirebaseMessagingService.count_new_noti);
        Log.i("count","=="+noti_count);
        tool_tv_count.setText(noti_count);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() 
        {
            @Override
            public void onRefresh() 
            {
                refresh = true;
                
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                attendanceDate = format.format(c.getTime());
                txt_attendanceDate.setText(attendanceDate);
                
                getDailyAttendance();
            }
        });
        
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        attendanceDate = format.format(c.getTime());
        txt_attendanceDate.setText(attendanceDate);
        txt_pieDate.setText(attendanceDate);

        layout_attDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                currentDate = mDay + "-" + (mMonth + 1) + "-" + mYear;
                DatePickerDialog dpd = new DatePickerDialog(DashBoard.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (view.isShown()) {
                            if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                                attendanceDate = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                txt_attendanceDate.setText(attendanceDate);
                            } 
                            else if (dayOfMonth < 10) {
                                attendanceDate = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                txt_attendanceDate.setText(attendanceDate);
                            } 
                            else if ((monthOfYear + 1) < 10) {
                                attendanceDate = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                txt_attendanceDate.setText(attendanceDate);
                            } 
                            else {
                                attendanceDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                txt_attendanceDate.setText(attendanceDate);
                            }
                            refresh = false;
                            date_select = true;
                            getDailyAttendance();
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });

        layout_pieDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                currentDate = mDay + "-" + (mMonth + 1) + "-" + mYear;
                DatePickerDialog dpd = new DatePickerDialog(DashBoard.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (view.isShown())
                        {
                            if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                                attendanceDate = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                txt_pieDate.setText(attendanceDate);
                            }
                            else if (dayOfMonth < 10) {
                                attendanceDate = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                txt_pieDate.setText(attendanceDate);
                            }
                            else if ((monthOfYear + 1) < 10) {
                                attendanceDate = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                txt_pieDate.setText(attendanceDate);
                            }
                            else {
                                attendanceDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                txt_pieDate.setText(attendanceDate);
                            }
                            refresh = false;
                            date_select = true;
                            getDailyAttendance();
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });
        
        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getDailyAttendance();
            getCheckVersion();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(DashBoard.this);
        }
        
        //inTimePieChart();
    }

    public void AddValuesToBarEntry()
    {
        barEntries = new ArrayList<>();
        barEntryLabels = new ArrayList<String>();

        barEntries.add(new BarEntry(presentEmp, 0));
        //barEntries.add(new BarEntry(lateEmp, 1));
        barEntries.add(new BarEntry(onLeaveEmp, 1));
        barEntries.add(new BarEntry(absentEmp, 2));

        barEntryLabels.add("Present");
        //barEntryLabels.add("Late Comers");
        barEntryLabels.add("On Leave");
        barEntryLabels.add("Absent");
        
        XAxis xAxis = chart.getXAxis();
        YAxis yAxisRight = chart.getAxisRight();
        YAxis yAxisLeft = chart.getAxisLeft();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#505050"));
        yAxisLeft.setTextColor(Color.parseColor("#505050"));
        yAxisRight.setEnabled(false);
        yAxisLeft.setAxisMinValue(0f);
        //xAxis.setAxisMinValue(0f);
        //yAxisRight.setAxisMinValue(10f);
        //yAxisLeft.setAxisMaxValue(50f);
        chart.setDescription("");
        chart.getLegend().setEnabled(false);
        
        barDataSet = new BarDataSet(barEntries,"Projects");
        barDataSet.setBarSpacePercent(50f);
        barDataSet.setColors(new int[]{Color.parseColor("#8EC35B"), Color.parseColor("#F36732"),
                Color.parseColor("#FEA525"), Color.parseColor("#E83133"), Color.parseColor("#8EC35B")});
        barDataSet.setValueFormatter(new MyValueFormatter());
        //barDataSet.setDrawValues(false);
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        barData = new BarData(barEntryLabels, barDataSet);
        
        chart.setDoubleTapToZoomEnabled(false);
        chart.setTouchEnabled(false);
        chart.setPinchZoom(false);
        chart.setData(barData);
        chart.animateY(1000);
    }

    public void inTimePieChart()
    {
        pieChart.setVisibility(View.VISIBLE);
        layout_navigation.setVisibility(View.VISIBLE);
        txt_no_data.setVisibility(View.GONE); 
        
        pieChart.setExtraOffsets(0, 0, 0, 0);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(10f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.setUsePercentValues(true);
        //pieChart.setCenterText(generateCenterSpannableText());
        
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        Log.i("entries", "entries");
              if (onLeaveEmp == 0 && absentEmp == 0 && presentEmp != 0)
        {
            Log.i("onLeaveEmp_absentEmp", "onLeaveEmp_absentEmp = 0");
            entries.add(new Entry(presentEmp, 0));
            //entries.add(new Entry(onLeaveEmp, 1));
            //entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            //labels.add("");
            //labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#8EC35B")});
        }
        else if (onLeaveEmp != 0 && absentEmp == 0 && presentEmp == 0)
        {
            Log.i("presentEmp_absentEmp", "presentEmp_absentEmp = 0");
            //entries.add(new Entry(presentEmp, 0));
            entries.add(new Entry(onLeaveEmp, 1));
            //entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            //labels.add("");
            //labels.add("");

            pieDataSet = new PieDataSet(entries, "");
           // pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#FEA525")});
        }
        else if (onLeaveEmp == 0 && absentEmp != 0 && presentEmp == 0)
        {
            Log.i("onLeaveEmp_presentEmp", "onLeaveEmp_presentEmp = 0");
            //entries.add(new Entry(presentEmp, 0));
            //entries.add(new Entry(onLeaveEmp, 1));
            entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            //labels.add("");
            //labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#E83133")});
        }
        else if (onLeaveEmp == 0 && presentEmp != 0 && absentEmp != 0)
        {
            Log.i("onLeaveEmp", "onLeaveEmp = 0");
            entries.add(new Entry(presentEmp, 0));
            //entries.add(new Entry(onLeaveEmp, 1));
            entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            //labels.add("");
            labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#8EC35B"),
                    Color.parseColor("#E83133")});
        }
        else if (presentEmp == 0 && onLeaveEmp != 0 && absentEmp != 0)
        {
            Log.i("presentEmp", "presentEmp = 0");
            //entries.add(new Entry(presentEmp, 0));
            entries.add(new Entry(onLeaveEmp, 1));
            entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            //labels.add("");
            labels.add("");
            labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#FEA525"),
                    Color.parseColor("#E83133")});
        }
        else if (absentEmp == 0 && presentEmp != 0 && onLeaveEmp != 0)
        {
            Log.i("absentEmp", "absentEmp = 0");
            entries.add(new Entry(presentEmp, 0));
            entries.add(new Entry(onLeaveEmp, 1));
            //entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            labels.add("");
            //labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#8EC35B"), Color.parseColor("#FEA525")});

        }
        else if (onLeaveEmp == 0 && absentEmp == 0 && presentEmp == 0)
        {
            Log.i("presentEmp_absentEmp", "presentEmp_absentEmp = 0");
            pieChart.setVisibility(View.GONE);
            layout_navigation.setVisibility(View.GONE);
            txt_no_data.setVisibility(View.VISIBLE);
        }
        else
        {
            Log.i("else", "else");
            entries.add(new Entry(presentEmp, 0));
            entries.add(new Entry(onLeaveEmp, 1));
            entries.add(new Entry(absentEmp, 2));

            //pieDataSet = new PieDataSet(entries, "");

            labels.add("");
            labels.add("");
            labels.add("");

            pieDataSet = new PieDataSet(entries, "");
            //pieDataSet.setValueFormatter(new MyValueFormatterPie());

            PieData data = new PieData(labels, pieDataSet);
            pieChart.setData(data);
            pieDataSet.setColors(new int[]{Color.parseColor("#8EC35B"), Color.parseColor("#FEA525"),
                    Color.parseColor("#E83133")});
        }

        /*pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setValueFormatter(new MyValueFormatterPie());

        PieData data = new PieData(labels, pieDataSet);
        pieChart.setData(data);
        pieDataSet.setColors(new int[]{Color.parseColor("#8EC35B"), Color.parseColor("#FEA525"), 
                Color.parseColor("#E83133")});*/
        pieDataSet.setValueTextSize(12f);
        pieChart.highlightValues(null);
        pieChart.invalidate();
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        Legend l = pieChart.getLegend();
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        pieChart.setDescription("");
    }
    
    public class MyValueFormatter implements ValueFormatter
    {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
        }
    }

    public class MyValueFormatterPie implements ValueFormatter 
    {
        private DecimalFormat mFormat, vFormat;

        public MyValueFormatterPie() {
            mFormat = new DecimalFormat("###,###,###"); // use no decimals
            vFormat = new DecimalFormat("##.##"); // use no decimals
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value)+ " %";
           /* float abc = (value/33)*100;
            String vl = String.format("%.2f", abc);
            String vl1 = String.valueOf(abc);
            String vll = mFormat.format(value);
            String show_value = vl+"%"+"\n "+vll;
            //return mFormat.format(value);
            return show_value;*/
        }
    }
    
    public void getDailyAttendance()
    {
        class GetDailyAttendance extends AsyncTask<String, Void, String>
        {
            ProgressDialog dialog = null;
            
            @Override
            protected void onPreExecute() {
                if (!refresh) 
                {
                    if (date_select) {
                        chart.setVisibility(View.GONE);
                        graph_progress.setVisibility(View.VISIBLE);
                    } 
                    else {
                        dialog = ProgressDialog.show(DashBoard.this, "Please wait", "Getting data...", true);
                        dialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String attendnceurl = ""+url_http+""+Url+"/owner/hrmapi/dailyAttendanceReport/?";

                    String query = String.format("date=%s", URLEncoder.encode(attendanceDate, "UTF-8"));
                    url = new URL(attendnceurl + query);
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
                    Log.i("myJson", "" + myJson);
                    if (!refresh) 
                    {
                        if (date_select) {
                            date_select = false;
                            chart.setVisibility(View.VISIBLE);
                            graph_progress.setVisibility(View.GONE);
                        }
                        else {
                            dialog.dismiss();
                        }
                    }

                    try 
                    {
                        JSONArray jsonArray = new JSONArray(myJson);
                        //Log.i("jsonArray", "" + jsonArray);

                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        presentEmp = jsonObject.getInt("present");
                        //String present = jsonObject.getString("present");
                        //Log.i("present","present="+present);
                        //presentEmp = Float.parseFloat(present);
                        //presentEmp = Integer.parseInt(present);
                        Log.i("present","presentEmp="+presentEmp);

                        lateEmp = jsonObject.getInt("late");
                        //String late = jsonObject.getString("late");
                        //Log.i("lateEmp","late="+late);
                        // lateEmp = Float.parseFloat(late);
                        //lateEmp = Integer.parseInt(late);
                        Log.i("lateEmp","lateEmp="+lateEmp);

                        onLeaveEmp = jsonObject.getInt("leave");
                        //String leave = jsonObject.getString("leave");
                        //Log.i("onLeaveEmp","leave="+leave);
                        //onLeaveEmp = Float.parseFloat(leave);
                        //onLeaveEmp = Integer.parseInt(leave);
                        Log.i("onLeaveEmp","onLeaveEmp="+onLeaveEmp);

                        absentEmp = jsonObject.getInt("absent");
                        //String absent = jsonObject.getString("absent");
                        //Log.i("absentEmp","absent="+absent);
                        //absentEmp = Float.parseFloat(absent);
                        //absentEmp = Integer.parseInt(absent);
                        Log.i("absentEmp","absentEmp="+absentEmp);

                        presentPrecent = jsonObject.getString("percentofpresent");
                        Log.i("presentPrecent","presentPrecent="+presentPrecent);

                        presentEmp = presentEmp + lateEmp;

                        String percent = "Overall " + presentPrecent + "% Employee Present today";
                        txt_presentPercent.setText(percent);
                        txt_piePer.setText(percent);

                        txt_present.setText("Present "+presentEmp);
                        txt_onLeave.setText("On Leave "+onLeaveEmp);
                        txt_absent.setText("Absent "+absentEmp);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }

                  //  AddValuesToBarEntry();
                    inTimePieChart();
                }

                else {
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    Toast.makeText(DashBoard.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }
        
        GetDailyAttendance getDailyAttendance = new GetDailyAttendance();
        getDailyAttendance.execute();
    }
    
    public void getCheckVersion()
    {
        class GetCheckVersion extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/getversion/?";
                    String query3 = String.format("apptype=%s", URLEncoder.encode("3", "UTF-8"));
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
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = br.readLine()) != null)
                        {
                            response_version = "";
                            response_version += line;
                        }
                    }
                    else
                    {
                        response_version = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_version;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson4 = result;
                    Log.i("myJson4", myJson4);
                    
                    if (myJson4.equals("[]"))
                    {
                        Toast.makeText(DashBoard.this, "Sorry... Bad internet connection", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        try
                        {
                            JSONArray jsonArray = new JSONArray(myJson4);
                            //Log.i("jsonArray", "" + jsonArray);

                            JSONObject object = jsonArray.getJSONObject(0);
                            
                            int get_version = object.getInt("Version");

                            if (version_code != get_version)
                            {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashBoard.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                alertDialog.setTitle("New Update");
                                alertDialog.setMessage("Please update your app");
                                alertDialog.setCancelable(false);
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.hrgirdowner&hl=en"));
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                
                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                                        startMain.addCategory(Intent.CATEGORY_HOME);
                                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(startMain);
                                        finish();
                                    }
                                });
                                alertDialog.show();
                            }
                        }
                        catch (JSONException e) {
                            Log.e("JsonException", e.toString());
                        }
                    }
                }
                else {
                    Toast.makeText(DashBoard.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetCheckVersion getCheckVersion = new GetCheckVersion();
        getCheckVersion.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter1 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter1);
    }

    @Override
    protected void onPause() 
    {
        super.onPause();
        handler.removeCallbacks(mExitRunnable);
        unregisterReceiver(receiver);
    }

    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            backPressTwice = false;
        }
    };
    
    @Override
    public void onBackPressed() 
    {
        if (backPressTwice) 
        {
            super.onBackPressed();
            finish();
            return;
        }

        this.backPressTwice = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG).show();
        handler.postDelayed(mExitRunnable, 2000);
    }
}
