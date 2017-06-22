package com.hrgirdowner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
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
public class HolidaysFragment extends Fragment
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    
    public static final String TAG_title = "title";
    public static final String TAG_date = "Date";
    public static final String TAG_grant = "grant";
    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";
    public static final String TAG_fy_Id = "fy_Id";

    String holiday_title, holiday_date, grant;
    String fromDate, toDate;
    String response, response_year;
    String myJson, myJson1;
    String current_date;
    String Url;
    String url_http,year,fy_id,compYear1, compYear2;

    URL url, year_url;
    
    LinearLayout layout_addHoliday, layout_edit_delete;
    ListView holiday_list;
    AutoCompleteTextView ac_holidays_month;
    TextView txt_no_holidays, txt_holidayCount;
    LinearLayout layout_Progress;
    Snackbar snackbar;
    FrameLayout content_frame;
    CoordinatorLayout snackbarCoordinatorLayout;
    SwipeRefreshLayout mSwipeRefreshLayout;

    public static NetworkChange receiver;
    ConnectionDetector cd;
    
    CheckInternetConnection internetConnection;
    ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> array_list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;
    HashMap<String, String> map_getId = new HashMap<String, String>();
    
    ProgressDialog progressDialog;

    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.leave_holidays, container,false);

        internetConnection = new CheckInternetConnection(getActivity());
        cd = new ConnectionDetector(getActivity());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getActivity().getSharedPreferences(MyPREFERENCES_url, PRIVATE_MODE);
        Url = (shared_pref.getString("url", ""));
        //Log.i("Url", Url);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout_holiday);
        layout_Progress = (LinearLayout)rootView.findViewById(R.id.holiday_Progress);
        layout_addHoliday = (LinearLayout)rootView.findViewById(R.id.layout_addHoliday);
        holiday_list = (ListView)rootView.findViewById(R.id.holidays_list);
        ac_holidays_month = (AutoCompleteTextView)rootView.findViewById(R.id.ac_holiday_month);
        txt_no_holidays = (TextView)rootView.findViewById(R.id.txt_no_holidays);
        layout_edit_delete = (LinearLayout)rootView.findViewById(R.id.edit_delete_layout);
        txt_holidayCount = (TextView)rootView.findViewById(R.id.holiday_count);
        snackbarCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.snackbarCoordinatorLayout);


     /*   ac_holidays_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // ac_holidays_month.showDropDown();
                compYear1 = year.substring(0, Math.min(year.length(), 7));
                compYear2 = year.substring(11, Math.min(year.length(), 18));
                fy_id = map_getId.get(item_name);
            }
        });*/

        ac_holidays_month.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ac_holidays_month.showDropDown();
                return true;
            }
        });

        ac_holidays_month.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String year = parent.getItemAtPosition(position).toString();
                Editable message = ac_holidays_month.getText();
                String item_name = message.toString();
                compYear1 = year.substring(0, Math.min(year.length(), 7));
                compYear2 = year.substring(11, Math.min(year.length(), 18));
                fy_id = map_getId.get(item_name);

                current_date = (String) parent.getItemAtPosition(position);
                array_list.clear();
                holiday_list.setAdapter(null);
                refresh = false;
                date_select = true;
                getHolidaysData();
               // txtChange();
            }
        });


        receiver = new NetworkChange()
        {
            @Override
            protected void onNetworkChange()
            {
                if (receiver.isConnected)
                {
                    if (hit_once){
                        getYearData();
                        //getHolidaysData();
                    }
                    if (snackbar != null){
                        snackbar.dismiss();
                    }
                }
                else
                {
                    hit_once = true;
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
                    snackbar = Snackbar.make(snackbarCoordinatorLayout, "Please check your internet connection", Snackbar.LENGTH_INDEFINITE);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.RedTextColor));
                    snackbar.show();
                }
            }
        };

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                array_list.clear();
                holiday_list.setAdapter(null);
                year_array.clear();
                ac_holidays_month.setAdapter(null);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                current_date = format.format(c.getTime());
                //ac_holidays_month.setText(current_date);
                getYearData();
            }
        });
        
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        current_date = format.format(c.getTime());
        //ac_holidays_month.setText(current_date);

       /* ac_holidays_month.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (internetConnection.hasConnection(getActivity())) {
                    current_date = (String) parent.getItemAtPosition(position);
                    array_list.clear();
                    holiday_list.setAdapter(null);
                    refresh = false;
                    date_select = true;
                    getHolidaysData();
                }
                else {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });*/

        layout_addHoliday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddNewHoliday.class);
                startActivity(intent);
            }
        });

        holiday_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                layout_edit_delete.setVisibility(View.INVISIBLE);
            }
        });

        holiday_list.setLongClickable(true);

        holiday_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                return true;
            }
        });
        
        if (internetConnection.hasConnection(getActivity())) {
            hit_once = false;
            getYearData();
            //getHolidaysData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(getActivity());
        }
        
        return rootView;
    }

    public void getYearData()
    {
        class GetYearData extends AsyncTask<String, Void, String>
        {
            Date date1 = new Date();
            Date date2 = new Date();
            Date date3 = new Date();

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String fy_url = ""+url_http+""+Url+"/owner/hrmapi/getallfinancialyear";
                    year_url = new URL(fy_url);
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

                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject object = jsonArray.getJSONObject(i);

                            fromDate = object.getString(TAG_fromDate);
                            toDate = object.getString(TAG_toDate);

                            year = fromDate + " to " + toDate;
                            String fy_id1 = object.getString(TAG_fy_Id);

                            map_getId.put(year, fy_id1);

                            year_array.add(year);

                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                            String current_date = format.format(c.getTime());

                            try
                            {
                                date1 = format.parse(fromDate);
                                date2 = format.parse(current_date);
                                date3 = format.parse(toDate);
                            }
                            catch (ParseException e) {
                                Log.e("ParseException", e.toString());
                            }

                            if (date2.after(date1) && date2.before(date3) || fromDate.equals(current_date) || toDate.equals(current_date)) {
                                compYear1 = fromDate;
                                compYear2 = toDate;
                                ac_holidays_month.setText(year);
                                fy_id = fy_id1;
                            }

                            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.year_drop_down, year_array);
                            ac_holidays_month.setAdapter(adapter);
                        }

                        getHolidaysData();

                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }

   /* public void getYearData()
    {
        class GetYearData extends AsyncTask<String, Void, String>
        {
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

                        adapter = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_custom, year_array);
                        ac_holidays_month.setAdapter(adapter);
                    } 
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }*/


    public void getHolidaysData()
    {
        class GetLeaveTakersData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh) 
                {
                    if (date_select) {
                        txt_no_holidays.setVisibility(View.GONE);
                        holiday_list.setVisibility(View.GONE);
                        layout_Progress.setVisibility(View.VISIBLE);
                    }
                    else {
                        progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Getting data...", true);
                        progressDialog.show();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String leave_url = ""+url_http+""+Url+"/owner/hrmapi/holidaylist/?";
                    String query3 = String.format("fy_Id=%s", URLEncoder.encode(fy_id, "UTF-8"));
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
                            holiday_list.setVisibility(View.VISIBLE);
                            layout_Progress.setVisibility(View.GONE);
                        } 
                        else {
                            progressDialog.dismiss();
                        }
                    }

                    try 
                    {
                        JSONArray jsonArray = new JSONArray(myJson);
                        //Log.i("jsonArray", "" + jsonArray);

                        int count = jsonArray.length();

                        if (count == 1) {
                            String holiday_count = String.valueOf(count) + " Holiday in this Month";
                            txt_holidayCount.setText(holiday_count);
                        } 
                        else if (count >= 2) {
                            String holiday_count = String.valueOf(count) + " Holidays in this Month";
                            txt_holidayCount.setText(holiday_count);
                        }

                        if (myJson.equals("[]")) 
                        {
                            txt_holidayCount.setText("No Holiday in this Month");
                            txt_no_holidays.setVisibility(View.VISIBLE);
                            holiday_list.setVisibility(View.GONE);
                        } 
                        else 
                        {
                            txt_no_holidays.setVisibility(View.GONE);
                            holiday_list.setVisibility(View.VISIBLE);

                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                holiday_title = object.getString(TAG_title);
                                holiday_date = object.getString(TAG_date);
                                grant = object.getString(TAG_grant);

                                map = new HashMap<String, String>();
                                map.put(TAG_title, holiday_title);
                                map.put(TAG_date, holiday_date);
                                map.put(TAG_grant, grant);

                                array_list.add(map);

                                adapter1 = new holidayList(getActivity(), array_list, R.layout.leave_holidays_custom, new String[]{}, new int[]{});
                                holiday_list.setAdapter(adapter1);
                            }
                        }
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

        GetLeaveTakersData getLeaveTakersData = new GetLeaveTakersData();
        getLeaveTakersData.execute();
    }


    public class  holidayList extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        public holidayList(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
                vi = inflater.inflate(R.layout.leave_holidays_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_date = (TextView)vi.findViewById(R.id.txt_holiday_date);
            final TextView txt_title = (TextView)vi.findViewById(R.id.txt_holiday_title);
            final TextView txt_type = (TextView)vi.findViewById(R.id.txt_holiday_type);

            String date = (String) data.get(TAG_date);
            String title = (String) data.get(TAG_title);
            String type = (String) data.get(TAG_grant);

            txt_date.setText(date);
            txt_title.setText(title);
            txt_type.setText(type);

            return vi;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter1 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(receiver, filter1);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }
}
