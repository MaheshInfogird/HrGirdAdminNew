package com.hrgirdowner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

public class BirthdayActivity extends BaseActivityExp {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    Toolbar toolbar;
    
    public static final String TAG_BD_FullName = "fullName";
    public static final String TAG_BD_FName = "firstName";
    public static final String TAG_BD_LName = "lastName";
    public static final String TAG_BD_Designation = "designation";
    public static final String TAG_BD_Department = "departmentName";
    public static final String TAG_BD_Photo = "photo";
    public static final String TAG_BD_birthDate = "birthDate";

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    URL url, birthday_url;

    TextView txt_emp_count, txt_presentPercent, txt_attendanceDate;
    AutoCompleteTextView txt_birthdayDate;
    TextView txt_bd_data;
    TextView txt_bd_count;
    ListView bd_list;
    LinearLayout layout_attDate, layout_bdDate;
    LinearLayout bd_Progress;
    //SwipeRefreshLayout mSwipeRefreshLayout;

    String response = "", response_bd = "", response_year = "";
    String myJson = "", myJson1 = "";
    String birthdayDate;
    String BD_FName, BD_LName, BD_FullName, BD_Designation, BD_Department, BD_Photo, BD_DOB;
    String Url;
    String url_http;
    String fromDate, toDate;

    String img_url = "http://infogird.gogird.com/files/infogird.gogird.com/images/employeephoto/";

    int BD_Count;

    final ArrayList<HashMap<String, String>> bd_arrayList = new ArrayList<HashMap<String, String>>();

    ArrayAdapter<String> adapter;
    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;
    
    FrameLayout content_frame;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;

    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;

    String type="";
    String CurrentDate_auto="";
    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            Header.setText("Birthdays");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

       // mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_bd);
        bd_Progress = (LinearLayout) findViewById(R.id.bd_Progress);
        txt_emp_count = (TextView) findViewById(R.id.emp_count);
        txt_presentPercent = (TextView) findViewById(R.id.txt_presentPercent);
        txt_attendanceDate = (TextView) findViewById(R.id.txt_attendanceDate);
        txt_birthdayDate = (AutoCompleteTextView) findViewById(R.id.txt_birthdayDate);
        txt_bd_data = (TextView) findViewById(R.id.txt_no_bd_data);
        txt_bd_count = (TextView) findViewById(R.id.txt_bd_count);

        layout_attDate = (LinearLayout) findViewById(R.id.layout_attDate);
        layout_bdDate = (LinearLayout) findViewById(R.id.layout_bdDate);

        bd_list = (ListView) findViewById(R.id.bd_listView);

        content_frame = (FrameLayout) findViewById(R.id.content_frame);

       /* mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                year_array.clear();
                txt_birthdayDate.setAdapter(null);
                bd_arrayList.clear();
                bd_list.setAdapter(null);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                birthdayDate = format.format(c.getTime());
                txt_birthdayDate.setText(birthdayDate);
                
                getYearData();
                getBirthdayData();
            }
        });*/

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        flag = getIntent().getBooleanExtra("date_noti",false);
        Log.i("flag","=="+flag);
        if (flag)
        {
            type = "2";
            Calendar calendar = Calendar.getInstance();
            birthdayDate = sdf1.format(calendar.getTime());
            Log.i("noti","msg="+birthdayDate);
            txt_birthdayDate.setVisibility(View.GONE);
        }else {

            String date_noti = getIntent().getStringExtra("notification_list");
            if (date_noti != null)
            {
                type = "2";
                flag = true;
                Log.i("date_list_noti","not null");
                try
                {
                    Date d = sdf.parse(date_noti);
                    String date_final = sdf1.format(d);
                    birthdayDate = date_final;
                    Log.i("noti","msg="+birthdayDate);
                    txt_birthdayDate.setVisibility(View.GONE);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {

                type = "1";
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                birthdayDate = format.format(c.getTime());
                txt_birthdayDate.setText(birthdayDate);
            }
        }
        Log.i("birthday","date="+birthdayDate);


        txt_birthdayDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (internetConnection.hasConnection(getApplicationContext())) {
                    birthdayDate = (String) parent.getItemAtPosition(position);
                    bd_arrayList.clear();
                    bd_list.setAdapter(null);
                    refresh = false;
                    date_select = true;
                    getBirthdayData();
                }
                else {
                    Toast.makeText(BirthdayActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        txt_birthdayDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                txt_birthdayDate.showDropDown();
                return true;
            }
        });
        
        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getYearData();
            getBirthdayData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(BirthdayActivity.this);
        }
    }

    public void getBirthdayData()
    {
        if (!flag)
        {
            birthdayDate = txt_birthdayDate.getText().toString();
            Log.i("getBirthdayData","=="+birthdayDate);
        }

        class GetBirthdayData extends AsyncTask<String, Void, String>
        {
            @Override
            protected void onPreExecute() {
                if (!refresh)
                {
                    txt_bd_data.setVisibility(View.GONE);
                    bd_list.setVisibility(View.GONE);
                    bd_Progress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String bd_url = ""+url_http+""+Url+"/owner/hrmapi/birthDayReport/?";

                    String query1 = String.format("date=%s&type=%s", URLEncoder.encode(birthdayDate, "UTF-8"),
                            URLEncoder.encode(type,"UTF-8"));
                    birthday_url = new URL(bd_url + query1);
                    Log.i("birthday_url", "" + birthday_url);

                    HttpURLConnection connection1 = (HttpURLConnection)birthday_url.openConnection();
                    connection1.setReadTimeout(10000);
                    connection1.setConnectTimeout(10000);
                    connection1.setRequestMethod("GET");
                    connection1.setUseCaches(false);
                    connection1.setAllowUserInteraction(false);
                    connection1.setDoInput(true);
                    connection1.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection1.setDoOutput(true);
                    int responceCode = connection1.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK)
                    {
                        String line1 = "";
                        BufferedReader br1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                        while ((line1 = br1.readLine()) != null)
                        {
                            response_bd = "";
                            response_bd += line1;
                        }
                    }
                    else
                    {
                        response_bd = "";
                    }
                }
                catch (Exception e){
                    Log.e("Exception", e.toString());
                }

                return response_bd;
            }

            @Override
            protected void onPostExecute(String result)
            {
               // mSwipeRefreshLayout.setRefreshing(false);
                if (result != null)
                {
                    myJson1 = result;
                    Log.i("myJson1", myJson1);
                    
                    if (!refresh)
                    {
                        bd_list.setVisibility(View.VISIBLE);
                        bd_Progress.setVisibility(View.GONE);
                    }

                    try
                    {
                        JSONArray jsonArray = new JSONArray(myJson1);
                        //Log.i("jsonArray", "" + jsonArray);

                        BD_Count = jsonArray.length();

                        if (myJson1.equals("[]"))
                        {
                            txt_bd_data.setVisibility(View.VISIBLE);
                            bd_list.setVisibility(View.GONE);
                            txt_bd_count.setText("");
                        }
                        else
                        {
                            txt_bd_data.setVisibility(View.GONE);
                            bd_list.setVisibility(View.VISIBLE);
                            if (BD_Count == 1) {
                                txt_bd_count.setText(String.valueOf(BD_Count) + " Birthday");
                            }
                            else if (BD_Count > 1) {
                                txt_bd_count.setText(String.valueOf(BD_Count) + " Birthday's");
                            }

                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                BD_FName = object.getString(TAG_BD_FName);
                                BD_LName = object.getString(TAG_BD_LName);
                                BD_FullName = BD_FName + " " + BD_LName;
                                BD_Designation = object.getString(TAG_BD_Designation);
                                BD_Department = object.getString(TAG_BD_Department);
                                BD_Photo = object.getString(TAG_BD_Photo);
                                BD_DOB = object.getString(TAG_BD_birthDate);

                                HashMap<String, String> bd_hashMap = new HashMap<String, String>();
                                bd_hashMap.put(TAG_BD_FullName, BD_FullName);
                                bd_hashMap.put(TAG_BD_Designation, BD_Designation);
                                bd_hashMap.put(TAG_BD_Department, BD_Department);
                                bd_hashMap.put(TAG_BD_Photo, BD_Photo);
                                bd_hashMap.put(TAG_BD_birthDate, BD_DOB);


                                bd_arrayList.add(bd_hashMap);

                                adapter1 = new BirthdayList(BirthdayActivity.this, bd_arrayList, R.layout.birthday_custom, new String[]{}, new int[]{});
                                bd_list.setAdapter(adapter1);
                            }
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(BirthdayActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }

        GetBirthdayData getBirthdayData = new GetBirthdayData();
        getBirthdayData.execute();
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

                        adapter = new ArrayAdapter<String>(BirthdayActivity.this, R.layout.dropdown_custom, year_array);
                        txt_birthdayDate.setAdapter(adapter);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(BirthdayActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }


    public class  BirthdayList extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater = null;
        public BirthdayList(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
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
                vi = inflater.inflate(R.layout.birthday_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.bd_fullName);
            final TextView txt_designation = (TextView)vi.findViewById(R.id.bd_designation);
            final TextView txt_department = (TextView)vi.findViewById(R.id.bd_department);
            final TextView txt_dob = (TextView)vi.findViewById(R.id.bd_dob);
            final ImageView image =(ImageView)vi.findViewById(R.id.bd_img);

            String fullName = (String) data.get(TAG_BD_FullName);
            String designation = (String) data.get(TAG_BD_Designation);
            String department = (String) data.get(TAG_BD_Department);
            String photo = (String) data.get(TAG_BD_Photo);
            String dob = (String) data.get(TAG_BD_birthDate);
            //String image_url = img_url + photo;
            String image_url = ""+url_http+Url+"/files/"+Url+"/images/employeephoto/";
            String image_final_url = image_url + photo;

            txt_fullName.setText(fullName);
            txt_designation.setText(designation);
            txt_department.setText("Dept : - "+ department);
            txt_dob.setText("DOB : - "+ dob);
            
            Picasso.with(getApplicationContext()).load(image_final_url).into(image, new Callback() 
            {
                @Override
                public void onSuccess()
                {
                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    RoundedBitmapDrawable round = RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), bitmap);
                    round.setCornerRadius(5);
                    round.setCircular(true);
                    image.setImageDrawable(round);
                }

                @Override
                public void onError() {
                }
            });

            return vi;
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(BirthdayActivity.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
