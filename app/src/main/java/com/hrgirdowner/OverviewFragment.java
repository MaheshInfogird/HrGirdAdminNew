package com.hrgirdowner;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

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
//import java.time.LocalDate;

/**
 * Created by adminsitrator on 21/01/2017.
 */
public class OverviewFragment extends Fragment 
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    
    public static final String TAG_fullName = "fullName";
    public static final String TAG_firstName = "firstName";
    public static final String TAG_lastName = "lastName";
    public static final String TAG_cId = "cid";
    public static final String TAG_leaveDays = "leavedays";
    public static final String TAG_uId = "uId";
    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    URL url, year_url, week_url;
    
    String response, response_year, response_week;
    String myJson, myJson1, myJson2;
    String fullName, firstName, lastName, cId, uId, leaveDays;
    String fromDate, toDate;
    String current_date, selectedDate;
    String Url;
    String url_http;
    String Days;

    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;
    
    int day1, day2, day3, day4, day5, day6, day7;
    public int mYear, mMonth, mDay;
    
    private LineChart line_Chart;
    LinearLayout layout_weeklyLeavesDate;
    TextView txt_weeklyLeavesDate, txt_emp_onleave_count, txt_noData;
    AutoCompleteTextView ac_leaveTakerDate;
    ListView leaveTaker_list;
    LinearLayout layout_Progress, chart_progress;
    Snackbar snackbar;
    FrameLayout content_frame;
    CoordinatorLayout snackbarCoordinatorLayout;
    SwipeRefreshLayout mSwipeRefreshLayout;
    
    ProgressDialog progressDialog = null;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;
    public static NetworkChange receiver;

    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<Entry> yVals1 = new ArrayList<Entry>();
    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
    LineData data;
    LineDataSet set1;
    
    ArrayList<String> weekDays = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;
   
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        View rootView = inflater.inflate(R.layout.leaves_overview, container, false);

        internetConnection = new CheckInternetConnection(getActivity());
        cd = new ConnectionDetector(getActivity());
        url_http = cd.geturl();
        
        shared_pref = getActivity().getSharedPreferences(MyPREFERENCES_url, PRIVATE_MODE);
        Url = (shared_pref.getString("url", ""));

        snackbarCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.snackbarCoordinatorLayout);

        receiver = new NetworkChange()
        {
            @Override
            protected void onNetworkChange()
            {
                if (receiver.isConnected)
                {
                    if (hit_once) 
                    {
                        //getWeeklyLeaveTakerData();
                        getLeaveTakersData();
                        getYearData();
                        leaveTaker_list.setVisibility(View.VISIBLE);
                    }
                    if (snackbar != null)
                    {
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    leaveTaker_list.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
                    snackbar = Snackbar.make(snackbarCoordinatorLayout, "Please check your internet connection", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.RedTextColor));
                    snackbar.show();
                }
            }
        };
        
        line_Chart = (LineChart) rootView.findViewById(R.id.line_chart);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout_ovr);
        chart_progress = (LinearLayout)rootView.findViewById(R.id.chart_Progress);
        layout_Progress = (LinearLayout)rootView.findViewById(R.id.linlaHeaderProgress);
        layout_weeklyLeavesDate = (LinearLayout)rootView.findViewById(R.id.layout_weeklyLeavesDate);
        ac_leaveTakerDate = (AutoCompleteTextView)rootView.findViewById(R.id.ed_leaveTakerDate);
        leaveTaker_list = (ListView)rootView.findViewById(R.id.leaveTaker_list);
        txt_weeklyLeavesDate = (TextView)rootView.findViewById(R.id.txt_weeklyLeavesDate);
        txt_emp_onleave_count = (TextView)rootView.findViewById(R.id.emp_onleave);
        txt_noData = (TextView)rootView.findViewById(R.id.txt_no_data);
        
        ac_leaveTakerDate.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
                ac_leaveTakerDate.showDropDown();
            }
        });
        
        Calendar c1 = Calendar.getInstance();
        DateFormat formater1 = new SimpleDateFormat("yyyy-MM-dd");
        selectedDate = formater1.format(c1.getTime());
        Log.i("selectedDate", selectedDate);
        
        Calendar beginCalendar = Calendar.getInstance();
        Calendar finishCalendar = Calendar.getInstance();
       
        c1.add(Calendar.DAY_OF_MONTH, 7);
        String output = formater1.format(c1.getTime());
        Log.i("output", output);

        Calendar c2 = Calendar.getInstance();
        DateFormat formater2 = new SimpleDateFormat("MMM d");
        String monthOfYear1 = formater2.format(c2.getTime());
        String monthOfYear2 = formater2.format(c1.getTime());
        
        String current_date1 = monthOfYear1+" - "+monthOfYear2;
        Log.i("current_date1", current_date1);
        txt_weeklyLeavesDate.setText(current_date1);
        
        try
        {
            beginCalendar.setTime(formater1.parse(selectedDate));
            finishCalendar.setTime(formater1.parse(output));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        while (beginCalendar.before(finishCalendar))
        {
            String date1 = formater1.format(beginCalendar.getTime()).toUpperCase();
            //System.out.println(date1);
            beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
            weekDays.add(date1);
        }

        //weekDays.add(""+output);
        Log.i("weekDays", ""+weekDays);

        Days = weekDays.toString().substring(1, weekDays.toString().length() - 1);
        Log.i("Days", ""+Days);
        //setLineChart();
        
        layout_weeklyLeavesDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                String currentDate = mDay + "-" + (mMonth + 1) + "-" + mYear;
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (view.isShown())
                        {
                            String startDate = "", endDate = "";
                            int endDay = 0;

                            line_Chart.clear();

                            //setLineChart();

                            SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                            Date date = new Date(year, monthOfYear, dayOfMonth - 1);
                            String dayOfWeek = simpledateformat.format(date);

                            xVals.clear();

                            xVals.add("");

                            for (int i = 0; i < 7; i++) 
                            {
                                date = new Date(year, monthOfYear, dayOfMonth - 1 + i);
                                dayOfWeek = simpledateformat.format(date);
                                xVals.add(dayOfWeek);
                            }

                            xVals.add("");

                            if (dayOfMonth < 10 && (monthOfYear + 1) < 10)
                            {
                                startDate = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                endDay = dayOfMonth + 6;
                                endDate = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + endDay;
                            } 
                            else if ((monthOfYear + 1) < 10) {
                                startDate = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                endDay = dayOfMonth + 6;
                                endDate = year + "-" + "0" + (monthOfYear + 1) + "-" + endDay;
                            } 
                            else if (dayOfMonth < 10) {
                                startDate = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                endDay = dayOfMonth + 6;
                                endDate = year + "-" + (monthOfYear + 1) + "-" + "0" + endDay;
                            }
                            else {
                                startDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                endDay = dayOfMonth + 6;
                                endDate = year + "-" + (monthOfYear + 1) + "-" + endDay;
                            }

                            DateFormat formater2 = new SimpleDateFormat("MMM d");
                            Date date5 = new Date(year, monthOfYear, dayOfMonth);
                            Date date6 = new Date(year, monthOfYear, endDay);
                            String monthOfYear1 = formater2.format(date5);
                            String monthOfYear2 = formater2.format(date6);

                            String current_date1 = monthOfYear1 + " - " + monthOfYear2;
                            txt_weeklyLeavesDate.setText(current_date1);

                            DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

                            Calendar beginCalendar = Calendar.getInstance();
                            Calendar finishCalendar = Calendar.getInstance();

                            try 
                            {
                                beginCalendar.setTime(formater.parse(startDate));
                                finishCalendar.setTime(formater.parse(endDate));
                            } 
                            catch (ParseException e) {
                                e.printStackTrace();
                            }

                            weekDays.clear();

                            while (beginCalendar.before(finishCalendar))
                            {
                                String date1 = formater.format(beginCalendar.getTime()).toUpperCase();
                                //System.out.println(date1);
                                beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
                                weekDays.add(date1);
                            }

                            String date2 = formater.format(beginCalendar.getTime()).toUpperCase();
                            beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            weekDays.add(date2);

                            Days = weekDays.toString().substring(1, weekDays.toString().length() - 1);

                            data.clearValues();
                            yVals1.clear();

                            refresh = false;
                            date_select = true;
                            //getWeeklyLeaveTakerData();
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });
        
        ac_leaveTakerDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (internetConnection.hasConnection(getActivity())) 
                {
                    current_date = (String) parent.getItemAtPosition(position);
                    array_list.clear();
                    leaveTaker_list.setAdapter(null);
                    getLeaveTakersData();
                } 
                else {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                array_list.clear();
                leaveTaker_list.setAdapter(null);
                year_array.clear();
                ac_leaveTakerDate.setAdapter(null);
                
                line_Chart.clear();
                
                //setLineChart();
                
                Calendar c1 = Calendar.getInstance();
                DateFormat formater1 = new SimpleDateFormat("yyyy-MM-dd");
                selectedDate = formater1.format(c1.getTime());

                Calendar beginCalendar = Calendar.getInstance();
                Calendar finishCalendar = Calendar.getInstance();

                c1.add(Calendar.DAY_OF_MONTH, 7);
                String output = formater1.format(c1.getTime());

                Calendar c2 = Calendar.getInstance();
                DateFormat formater2 = new SimpleDateFormat("MMM d");
                String monthOfYear1 = formater2.format(c2.getTime());
                String monthOfYear2 = formater2.format(c1.getTime());

                String current_date1 = monthOfYear1+" - "+monthOfYear2;
                txt_weeklyLeavesDate.setText(current_date1);

                try
                {
                    beginCalendar.setTime(formater1.parse(selectedDate));
                    finishCalendar.setTime(formater1.parse(output));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }

                weekDays.clear();
                
                while (beginCalendar.before(finishCalendar))
                {
                    String date1 = formater1.format(beginCalendar.getTime()).toUpperCase();
                    beginCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    weekDays.add(date1);
                }

                weekDays.add("" + output);

                Days = weekDays.toString().substring(1, weekDays.toString().length() - 1);
                
                SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                DateFormat formater3 = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();

                try {
                    date = formater3.parse(selectedDate);
                }
                catch (ParseException e){
                    e.printStackTrace();
                }

                //String dayOfWeek = simpledateformat.format(date);

                xVals.clear();
                xVals.add("");
                
                /*String year1 = selectedDate.substring(0,4);
                String monthOfYear3 = selectedDate.substring(6,7);
                String dayOfMonth1 = selectedDate.substring(9,10);

                int year = Integer.parseInt(year1);
                int monthOfYear = Integer.parseInt(monthOfYear3);
                int dayOfMonth = Integer.parseInt(dayOfMonth1);

                Date date1 = new Date(year, monthOfYear, dayOfMonth - 1);
                
                for (int i = 0; i < 7; i++) 
                {
                    date1 = new Date(year, monthOfYear, dayOfMonth - 1+ i);
                    String dayOfWeek = simpledateformat.format(date1);
                    xVals.add(dayOfWeek);
                }*/
                
                Date date2 = new Date();
                for (int j = 0; j < weekDays.size(); j++)
                {
                    String Days = weekDays.get(j);
                    Log.i("Days123", ""+Days);

                    try {
                        date2 = formater1.parse(Days);
                        //Log.i("date2", ""+date2);
                    }
                    catch (ParseException e){
                        e.printStackTrace();
                    }

                    String dayOfWeek = simpledateformat.format(date2);
                    Log.i("dayOfWeek123", ""+dayOfWeek);
                    xVals.add(dayOfWeek);
                }

                xVals.add("");

                data.clearValues();
                yVals1.clear();

                Calendar c = Calendar.getInstance();
                DateFormat formater = new SimpleDateFormat("yyyy-MM");
                current_date = formater.format(c.getTime());
                ac_leaveTakerDate.setText(current_date);
                
                //getWeeklyLeaveTakerData();
                getLeaveTakersData();
                getYearData();
            }
        });

        Calendar c = Calendar.getInstance();
        DateFormat formater = new SimpleDateFormat("yyyy-MM");
        current_date = formater.format(c.getTime());
        ac_leaveTakerDate.setText(current_date);

        if (internetConnection.hasConnection(getActivity()))
        {
            hit_once = false;
            //getWeeklyLeaveTakerData();
            getLeaveTakersData();
            getYearData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(getActivity());
        }
        
        return rootView;
    }

    public void setLineChart()
    {
        line_Chart.setDrawGridBackground(false);
        line_Chart.setTouchEnabled(false);
        line_Chart.setDragEnabled(false);
        line_Chart.setScaleEnabled(false);
        line_Chart.setPinchZoom(false);
        line_Chart.setExtraOffsets(0, 0, 0, 0);
        
        XAxis xAxis = line_Chart.getXAxis();
        YAxis yAxisRight = line_Chart.getAxisRight();
        YAxis yAxisLeft = line_Chart.getAxisLeft();
        
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        xAxis.setTextColor(Color.parseColor("#505050"));
        xAxis.setGridColor(Color.parseColor("#E3E3E3"));
        xAxis.setDrawGridLines(true);

        yAxisRight.setEnabled(false);
        yAxisLeft.setTextColor(Color.parseColor("#505050"));
        yAxisRight.setGridColor(Color.parseColor("#E3E3E3"));
        yAxisLeft.setGridColor(Color.parseColor("#E3E3E3"));
        yAxisLeft.setAxisMinValue(0f);
        yAxisRight.setAxisMinValue(0f);
        //yAxisLeft.setStartAtZero(false);
        
        line_Chart.setDescription("");
        line_Chart.getLegend().setEnabled(false);

        SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
        DateFormat formater1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        try {
            date = formater1.parse(selectedDate);
            Log.i("date", ""+date);
        }
        catch (ParseException e){
            e.printStackTrace();
        }

        //String dayOfWeek = simpledateformat.format(date);
        //Log.i("dayOfWeek", ""+dayOfWeek);
        
        xVals.clear();
        xVals.add("");

        /*String year1 = selectedDate.substring(0,4);
        String monthOfYear1 = selectedDate.substring(6,7);
        String dayOfMonth1 = selectedDate.substring(9,10);

        Log.i("year1", ""+year1);
        Log.i("monthOfYear1", ""+monthOfYear1);
        Log.i("dayOfMonth1", ""+dayOfMonth1);

        int year = Integer.parseInt(year1);
        int monthOfYear = Integer.parseInt(monthOfYear1);
        int dayOfMonth = Integer.parseInt(dayOfMonth1);

        Date date1 = new Date(year, monthOfYear, dayOfMonth - 1);

        //Date date1;
        
        for (int i = 0; i < 7; i++) 
        {
            date1 = new Date(year, monthOfYear, dayOfMonth - 1+ i);
            String dayOfWeek = simpledateformat.format(date1);
            Log.i("dayOfWeek", ""+dayOfWeek);
            //xVals.add(dayOfWeek);
        }*/

        Date date2 = new Date();
        for (int j = 0; j < weekDays.size(); j++)
        {
            String Days = weekDays.get(j);
            Log.i("Days123", ""+Days);

            try {
                date2 = formater1.parse(Days);
            }
            catch (ParseException e){
                e.printStackTrace();
            }

            String dayOfWeek = simpledateformat.format(date2);
            Log.i("dayOfWeek123", ""+dayOfWeek);
            xVals.add(dayOfWeek);
        }
        
        xVals.add("");
    }

    private void setData()
    {
        yVals1.add(new Entry(day1, 1));
        yVals1.add(new Entry(day2, 2));
        yVals1.add(new Entry(day3, 3));
        yVals1.add(new Entry(day4, 4));
        yVals1.add(new Entry(day5, 5));
        yVals1.add(new Entry(day6, 6));
        yVals1.add(new Entry(day7, 7));

        set1 = new LineDataSet(yVals1, "");
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setColor(Color.parseColor("#EB5F52"));
        set1.setCircleColor(Color.parseColor("#EB5F52"));
        set1.setFillColor(Color.parseColor("#EB5F52"));
        set1.setDrawCircleHole(false);
        set1.setValueFormatter(new MyValueFormatter());

        dataSets.clear();
        dataSets.add(set1);
        
        data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);

        line_Chart.setData(data);
        //line_Chart.animateX(2500);
        line_Chart.animateY(1000);
    }

    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
        }
    }


    public void getWeeklyLeaveTakerData()
    {
        class GetWeeklyLeaveTakerData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh) 
                {
                    if (date_select) {
                        chart_progress.setVisibility(View.VISIBLE);
                        line_Chart.setVisibility(View.GONE);
                    } 
                    else {
                        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Getting data...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/weeklyleaveTakers/?";

                    String query3 = String.format("date=%s", URLEncoder.encode(Days, "UTF-8"));
                    query3 = query3.replace("%2C+", ",");
                    week_url = new URL(leave_url + query3);
                    Log.i("week_url", "" + week_url);

                    HttpURLConnection connection = (HttpURLConnection)week_url.openConnection();
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
                            response_week = "";
                            response_week += line;
                        }
                    }
                    else
                    {
                        response_week = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_week;
            }

            @Override
            protected void onPostExecute(String result)
            {
                if (result != null)
                {
                    myJson2 = result;
                    Log.i("myJson2", myJson2);
                    if (!refresh)
                    {
                        if (date_select) {
                            date_select = false;
                            chart_progress.setVisibility(View.GONE);
                            line_Chart.setVisibility(View.VISIBLE);
                        } 
                        else {
                            progressDialog.dismiss();
                        }
                    }

                    try 
                    {
                        JSONArray jsonArray = new JSONArray(myJson2);
                        //Log.i("jsonArray", "" + jsonArray);

                        JSONObject object = jsonArray.getJSONObject(0);
                        day1 = object.getInt("leavedays");
                        JSONObject object1 = jsonArray.getJSONObject(1);
                        day2 = object1.getInt("leavedays");
                        JSONObject object2 = jsonArray.getJSONObject(2);
                        day3 = object2.getInt("leavedays");
                        JSONObject object3 = jsonArray.getJSONObject(3);
                        day4 = object3.getInt("leavedays");
                        JSONObject object4 = jsonArray.getJSONObject(4);
                        day5 = object4.getInt("leavedays");
                        JSONObject object5 = jsonArray.getJSONObject(5);
                        day6 = object5.getInt("leavedays");
                        JSONObject object6 = jsonArray.getJSONObject(6);
                        day7 = object6.getInt("leavedays");

                        int count = day1 + day2 + day3 + day4 + day5 + day6 + day7;
                        
                        if (count == 1) {
                            String str_count = String.valueOf(count + " Employee on leave in this week");
                            txt_emp_onleave_count.setText(str_count);
                        }
                        else if (count >= 2) {
                            String str_count = String.valueOf(count + " Employees on leave in this week");
                            txt_emp_onleave_count.setText(str_count);
                        } 
                        else if (count == 0) {
                            String str_count = "No Employee on leave in this week";
                            txt_emp_onleave_count.setText(str_count);
                        }
                        
                        setData();
                    } 
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    if (progressDialog.isShowing() && progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getActivity(), "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetWeeklyLeaveTakerData getWeeklyLeaveTakerData = new GetWeeklyLeaveTakerData();
        getWeeklyLeaveTakerData.execute();
    }
    
    public void getLeaveTakersData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                leaveTaker_list.setVisibility(View.GONE);
                layout_Progress.setVisibility(View.VISIBLE);
            }
            
            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/leaveTakers/?";

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
                    if (myJson.equals("[]")) 
                    {
                        leaveTaker_list.setVisibility(View.GONE);
                        layout_Progress.setVisibility(View.GONE);
                        txt_noData.setVisibility(View.VISIBLE);
                    } 
                    else 
                    {
                        leaveTaker_list.setVisibility(View.VISIBLE);
                        layout_Progress.setVisibility(View.GONE);
                        txt_noData.setVisibility(View.GONE);
                        
                        try 
                        {
                            JSONArray jsonArray = new JSONArray(myJson);
                            //Log.i("jsonArray", "" + jsonArray);

                            for (int i = 0; i < jsonArray.length(); i++) 
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                firstName = object.getString(TAG_firstName);
                                lastName = object.getString(TAG_lastName);
                                fullName = firstName +" "+ lastName;
                                cId = object.getString(TAG_cId);
                                leaveDays = object.getString(TAG_leaveDays);
                                uId = object.getString(TAG_uId);

                                map = new HashMap<String, String>();
                                map.put(TAG_fullName, fullName);
                                map.put(TAG_cId, cId);
                                map.put(TAG_leaveDays, leaveDays);
                                map.put(TAG_uId, uId);

                                array_list.add(map);

                                adapter1 = new topLeaveTakerAdapter(getActivity(), array_list, R.layout.leave_overview_custom, new String[]{}, new int[]{});
                                leaveTaker_list.setAdapter(adapter1);
                            }
                        }
                        catch (JSONException e) {
                            Log.e("JsonException", e.toString());
                        }
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
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

                    year_url = new URL(leave_url);
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
                    //progressDialog1.dismiss();

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
                        
                        adapter = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_custom, year_array);
                        ac_leaveTakerDate.setDropDownHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                        ac_leaveTakerDate.setAdapter(adapter);
                    } 
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    //progressDialog1.dismiss();
                    Toast.makeText(getActivity(), "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }
    
    public class  topLeaveTakerAdapter extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        public topLeaveTakerAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
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
                vi = inflater.inflate(R.layout.leave_overview_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            TextView txt_fullName = (TextView)vi.findViewById(R.id.txt_over_fullName);
            TextView txt_cid = (TextView)vi.findViewById(R.id.txt_cid);
            TextView txt_leaveDays = (TextView)vi.findViewById(R.id.txt_leaveDays);
            
            String full_name = (String)data.get(TAG_fullName);
            String cid = (String)data.get(TAG_cId);
            String leave_days = (String)data.get(TAG_leaveDays);
            
            txt_fullName.setText(full_name);
            txt_cid.setText(cid);
            txt_leaveDays.setText(leave_days);
            
            return vi;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter1 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(receiver, filter1);
    }
}
