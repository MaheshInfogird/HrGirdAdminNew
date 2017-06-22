package com.hrgirdowner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

public class MarriageAnnActivity extends BaseActivityExp {

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    Toolbar toolbar;
    AutoCompleteTextView txt_marriageAnniversaryDate;
    TextView txt_ma_data;
    TextView txt_ma_count;
    ListView ma_list;
    LinearLayout layout_maDate;
    LinearLayout ma_Progress;
    //SwipeRefreshLayout mSwipeRefreshLayout;

    URL url, marriage_url;

    public static final String TAG_MA_FullName = "fullName";
    public static final String TAG_MA_FName = "firstName";
    public static final String TAG_MA_LName = "lastName";
    public static final String TAG_MA_Designation = "designation";
    public static final String TAG_MA_Department = "departmentName";
    public static final String TAG_MA_MarriageDate = "marriageDate";
    public static final String TAG_MA_Photo = "photo";

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";

    String response = "", response_ma = "", response_year = "";
    String myJson = "", myJson1 = "", myJson2 = "";
    String marriageAnniversaryDate;
    String MA_FName, MA_LName, MA_FullName, MA_Designation, MA_Department, MA_Photo, MA_MarriageDate;
    String Url;
    String url_http;
    String Packagename;
    String fromDate, toDate;

    String img_url = "http://infogird.gogird.com/files/infogird.gogird.com/images/employeephoto/";

    int MA_Count;
    int version_code;

    final ArrayList<HashMap<String, String>> ma_arrayList = new ArrayList<HashMap<String, String>>();

    ArrayAdapter<String> adapter;
    ArrayList<String> year_array = new ArrayList<String>();
    ListAdapter adapter1;

    FrameLayout content_frame;
    CheckInternetConnection internetConnection;
    ConnectionDetector cd;

    boolean hit_once = false;
    boolean date_select = false;
    boolean refresh = false;

    boolean flag;
    String type="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marriage_ann);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            Header.setText("Marriage Anniversary");
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

       // mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout_ma);
        ma_Progress = (LinearLayout) findViewById(R.id.ma_Progress);;
        txt_marriageAnniversaryDate = (AutoCompleteTextView) findViewById(R.id.txt_marriageAnniversaryDate);
        txt_ma_data = (TextView) findViewById(R.id.txt_no_ma_data);
        txt_ma_count = (TextView) findViewById(R.id.txt_ma_count);

        layout_maDate = (LinearLayout) findViewById(R.id.layout_maDate);
        ma_list = (ListView) findViewById(R.id.ma_listView);

        content_frame = (FrameLayout) findViewById(R.id.content_frame);

       /* mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                year_array.clear();
                txt_marriageAnniversaryDate.setAdapter(null);
                ma_arrayList.clear();
                ma_list.setAdapter(null);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                marriageAnniversaryDate = format.format(c.getTime());
                txt_marriageAnniversaryDate.setText(marriageAnniversaryDate);
                getYearData();
                getMarriageAnniversary();
            }
        });*/

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        flag = getIntent().getBooleanExtra("date_noti",false);
        Log.i("marriage","flag="+flag);
        if (flag)
        {
            type = "2";
            Calendar calendar = Calendar.getInstance();
            marriageAnniversaryDate = sdf1.format(calendar.getTime());
            Log.i("noti","msg="+marriageAnniversaryDate);
            txt_marriageAnniversaryDate.setVisibility(View.GONE);
        }else {

            String date_noti = getIntent().getStringExtra("notification_list");
            if (date_noti != null) {
                type = "2";
                flag = true;
                Log.i("date_list_noti", "not null");
                try {
                    Date d = sdf.parse(date_noti);
                    String date_final = sdf1.format(d);
                    marriageAnniversaryDate = date_final;
                    Log.i("noti", "msg=" + marriageAnniversaryDate);
                    txt_marriageAnniversaryDate.setVisibility(View.GONE);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {
                type = "1";
                Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                marriageAnniversaryDate = format.format(c.getTime());
                txt_marriageAnniversaryDate.setText(marriageAnniversaryDate);
            }
        }

        
        txt_marriageAnniversaryDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (internetConnection.hasConnection(getApplicationContext())) 
                {
                    marriageAnniversaryDate = (String) parent.getItemAtPosition(position);
                    ma_arrayList.clear();
                    ma_list.setAdapter(null);
                    refresh = false;
                    date_select = true;
                    getMarriageAnniversary();
                } 
                else {
                    Toast.makeText(MarriageAnnActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });


        txt_marriageAnniversaryDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                txt_marriageAnniversaryDate.showDropDown();
                return true;
            }
        });

        if (internetConnection.hasConnection(getApplicationContext()))
        {
            hit_once = false;
            getYearData();
            getMarriageAnniversary();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(MarriageAnnActivity.this);
        }
    }

    public void getMarriageAnniversary() {
        if (!flag)
        {
            marriageAnniversaryDate = txt_marriageAnniversaryDate.getText().toString();
            Log.i("getBirthdayData","=="+marriageAnniversaryDate);
        }
        class GetMarriageAnniversary extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                if (!refresh) 
                {
                    txt_ma_data.setVisibility(View.GONE);
                    ma_list.setVisibility(View.GONE);
                    ma_Progress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected String doInBackground(String... params) 
            {
                try 
                {
                    String ma_url = "" + url_http + "" + Url + "/owner/hrmapi/marriageAnniversaryReport/?";
                    String query2 = String.format("date=%s&type=%s", URLEncoder.encode(marriageAnniversaryDate, "UTF-8"),
                            URLEncoder.encode(type,"UTF-8"));
                    marriage_url = new URL(ma_url + query2);
                    Log.i("marriage_url", "" + marriage_url);

                    HttpURLConnection connection2 = (HttpURLConnection) marriage_url.openConnection();
                    connection2.setReadTimeout(10000);
                    connection2.setConnectTimeout(10000);
                    connection2.setRequestMethod("GET");
                    connection2.setUseCaches(false);
                    connection2.setAllowUserInteraction(false);
                    connection2.setDoInput(true);
                    connection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection2.setDoOutput(true);
                    int responceCode = connection2.getResponseCode();

                    if (responceCode == HttpURLConnection.HTTP_OK) 
                    {
                        String line2 = "";
                        BufferedReader br2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                        while ((line2 = br2.readLine()) != null) 
                        {
                            response_ma = "";
                            response_ma += line2;
                        }
                    } else {
                        response_ma = "";
                    }
                }
                catch (Exception e) {
                    Log.e("Exception", e.toString());
                }

                return response_ma;
            }

            @Override
            protected void onPostExecute(String result)
            {
               // mSwipeRefreshLayout.setRefreshing(false);
                if (result != null) 
                {
                    myJson2 = result;
                    Log.i("myJson2", myJson2);
                    if (!refresh) 
                    {
                        ma_list.setVisibility(View.VISIBLE);
                        ma_Progress.setVisibility(View.GONE);
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(myJson2);
                        //Log.i("jsonArray", "" + jsonArray);

                        MA_Count = jsonArray.length();

                        if (myJson2.equals("[]")) 
                        {
                            txt_ma_data.setVisibility(View.VISIBLE);
                            ma_list.setVisibility(View.GONE);
                            txt_ma_count.setText("");
                        } 
                        else {
                            txt_ma_data.setVisibility(View.GONE);
                            ma_list.setVisibility(View.VISIBLE);
                            if (MA_Count == 1) 
                            {
                                txt_ma_count.setText(String.valueOf(MA_Count) + " Anniversary");
                            } 
                            else if (MA_Count > 1) {
                                txt_ma_count.setText(String.valueOf(MA_Count) + " Anniversary's");
                            } 

                            for (int i = 0; i < jsonArray.length(); i++) 
                            {
                                JSONObject object = jsonArray.getJSONObject(i);

                                MA_FName = object.getString(TAG_MA_FName);
                                MA_LName = object.getString(TAG_MA_LName);
                                MA_FullName = MA_FName + " " + MA_LName;
                                MA_Designation = object.getString(TAG_MA_Designation);
                                MA_Department = object.getString(TAG_MA_Department);
                                MA_Photo = object.getString(TAG_MA_Photo);
                                MA_MarriageDate = object.getString(TAG_MA_MarriageDate);

                                HashMap<String, String> ma_hashMap = new HashMap<String, String>();
                                ma_hashMap.put(TAG_MA_FullName, MA_FullName);
                                ma_hashMap.put(TAG_MA_Designation, MA_Designation);
                                ma_hashMap.put(TAG_MA_Department, MA_Department);
                                ma_hashMap.put(TAG_MA_Photo, MA_Photo);
                                ma_hashMap.put(TAG_MA_MarriageDate, MA_MarriageDate);

                                ma_arrayList.add(ma_hashMap);

                                adapter1 = new MAnniversaryList(MarriageAnnActivity.this, ma_arrayList, R.layout.birthday_custom, new String[]{}, new int[]{});
                                ma_list.setAdapter(adapter1);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                } else {
                    Toast.makeText(MarriageAnnActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }

        GetMarriageAnniversary getMarriageAnniversary = new GetMarriageAnniversary();
        getMarriageAnniversary.execute();

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

                        while (beginCalendar.before(finishCalendar)) {
                            String date = formater.format(beginCalendar.getTime()).toUpperCase();
                            beginCalendar.add(Calendar.MONTH, 1);
                            year_array.add(date);
                        }

                        year_array.add(toDate);

                        adapter = new ArrayAdapter<String>(MarriageAnnActivity.this, R.layout.dropdown_custom, year_array);
                        txt_marriageAnniversaryDate.setAdapter(adapter);
                    }
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(MarriageAnnActivity.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }
    
    
    
    public class  MAnniversaryList extends SimpleAdapter
    {
        private Context mContext;
        public LayoutInflater inflater=null;
        public MAnniversaryList(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
                vi = inflater.inflate(R.layout.marriageanniversary_custom, null);
            }

            HashMap< String, Object > data = (HashMap<String, Object>) getItem(position);
            final TextView txt_fullName = (TextView)vi.findViewById(R.id.ma_fullName);
            final TextView txt_designation = (TextView)vi.findViewById(R.id.ma_designation);
            final TextView txt_department = (TextView)vi.findViewById(R.id.ma_department);
            final TextView txt_marriageDate= (TextView)vi.findViewById(R.id.ma_marriageDate);
            final ImageView image =(ImageView)vi.findViewById(R.id.ma_image);

            String fullName = (String) data.get(TAG_MA_FullName);
            String designation = (String) data.get(TAG_MA_Designation);
            String department = (String) data.get(TAG_MA_Department);
            String marriageDate = (String) data.get(TAG_MA_MarriageDate);
            String photo = (String) data.get(TAG_MA_Photo);
            //String image_url = img_url + photo;
            String image_url = ""+url_http+Url+"/files/"+Url+"/images/employeephoto/";
            String image_final_url = image_url + photo;

            txt_fullName.setText(fullName);
            txt_designation.setText(designation);
            txt_department.setText("Dept : - "+department);
            txt_marriageDate.setText("Marriage Date : - "+marriageDate);

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
    public void onBackPressed() {
        Intent intent = new Intent(MarriageAnnActivity.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
