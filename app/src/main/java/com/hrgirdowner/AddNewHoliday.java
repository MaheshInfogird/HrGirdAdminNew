package com.hrgirdowner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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

/**
 * Created by adminsitrator on 24/01/2017.
 */
public class AddNewHoliday extends AppCompatActivity
{
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences pref, shared_pref;
    SharedPreferences.Editor editor, editor1;
    Toolbar toolbar;

    public static final String TAG_fromDate = "fromyear";
    public static final String TAG_toDate = "toyear";
    public static final String TAG_fy_Id = "fy_Id";
    public static final String TAG_year = "year";

    String compYear1, compYear2, selectYear;
    String holidayTitle, holidayType, holidayDescription, holidayDate;
    String myJson, response, myJson1, response_year;
    String fromDate, toDate, fy_id, type;
    String year, selected_date;
    String[] holiday_type = {"Regular", "Optional"};
    String Url;
    String url_http;
    
    public int mYear, mMonth, mDay;

    ConnectionDetector cd;
    CheckInternetConnection internetConnection;
    URL url, year_url;
    
    ArrayList<String> year_array = new ArrayList<String>();
    HashMap<String, String> map_getId = new HashMap<String, String>();
    ArrayAdapter<String> adapter_type;
    
    EditText ed_holiday_title, ed_holiday_description;
    AutoCompleteTextView ac_holiday_type, ac_year;
    TextView txt_holiday_date;
    Button btn_addHoliday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_holidays);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inner);
        TextView Header = (TextView) findViewById(R.id.header_text);
        ImageView back = (ImageView)findViewById(R.id.tool_back);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) 
        {
            back.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("");
            Header.setText("Add New Holiday");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            //getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_arrow));
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

        internetConnection = new CheckInternetConnection(getApplicationContext());
        cd = new ConnectionDetector(getApplicationContext());
        url_http = cd.geturl();
        //Log.i("url_http", url_http);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        Log.i("Url", Url);
        
        ed_holiday_title = (EditText)findViewById(R.id.add_holiday_title);
        ed_holiday_description = (EditText)findViewById(R.id.add_holiday_description);
        ac_holiday_type = (AutoCompleteTextView)findViewById(R.id.add_holiday_type);
        ac_year = (AutoCompleteTextView)findViewById(R.id.ac_holiday_year);
        txt_holiday_date = (TextView)findViewById(R.id.add_holiday_date);
        btn_addHoliday = (Button)findViewById(R.id.btn_add_holiday);

        adapter_type = new ArrayAdapter<String>(getApplicationContext(), R.layout.dropdown_custom, holiday_type);
        ac_holiday_type.setAdapter(adapter_type);

        ac_year.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String year = parent.getItemAtPosition(position).toString();
                Editable message = ac_year.getText();
                String item_name = message.toString();
                compYear1 = year.substring(0, Math.min(year.length(), 7));
                compYear2 = year.substring(11, Math.min(year.length(), 18));
                fy_id = map_getId.get(item_name);
                txtChange();
            }
        });

        ac_holiday_type.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                holidayType = String.valueOf(position);
                txtChange();
            }
        });

        ac_holiday_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ac_holiday_type.showDropDown();
            }
        });

        /*ac_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ac_year.showDropDown();
            }
        });*/
        ac_year.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ac_year.showDropDown();
                return true;
            }
        });
        
        btn_addHoliday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                if (internetConnection.hasConnection(getApplicationContext()))
                {
                    if (validation())
                    {
                        addHoliday();
                    }
                }
                else {
                    Toast.makeText(AddNewHoliday.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        txt_holiday_date.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd = new DatePickerDialog(AddNewHoliday.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Date date1 = new Date();
                        Date date2 = new Date();
                        Date date3 = new Date();
                                
                        if (view.isShown())
                        {
                            if (!ac_year.getText().toString().equals("")) 
                            {
                                if ((monthOfYear + 1) < 10) {
                                    selectYear = year + "-" + "0" + (monthOfYear + 1);
                                } 
                                else {
                                    selectYear = year + "-" + (monthOfYear + 1);
                                }

                                try 
                                {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                                    date1 = sdf.parse(selectYear);
                                    date2 = sdf.parse(compYear1);
                                    date3 = sdf.parse(compYear2);
                                } 
                                catch (ParseException e) {
                                    Log.e("ParseException", e.toString());
                                }

                                if (date1.before(date2) || date1.after(date3))
                                {
                                    txt_holiday_date.setText("");
                                    txt_holiday_date.setError("Select date in between selected year");
                                    //Log.i("Invalid Date", "Invalid Date");
                                    Toast.makeText(AddNewHoliday.this, "Select date in between selected year", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                                        selected_date = year + "-" + "0" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                        txt_holiday_date.setText(selected_date);
                                    }
                                    else if ((monthOfYear + 1) < 10) {
                                        selected_date = year + "-" + "0" + (monthOfYear + 1) + "-" + dayOfMonth;
                                        txt_holiday_date.setText(selected_date);
                                    }
                                    else if (dayOfMonth < 10) {
                                        selected_date = year + "-" + (monthOfYear + 1) + "-" + "0" + dayOfMonth;
                                        txt_holiday_date.setText(selected_date);
                                    }
                                    else {
                                        selected_date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                        txt_holiday_date.setText(selected_date);
                                    }
                                }
                            }
                            else 
                            {
                                ac_year.setError("Please select year");
                                Toast.makeText(AddNewHoliday.this, "Please select year", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }, mYear, mMonth, mDay);
                dpd.show();
            }
        });

        txtChange();
        if (internetConnection.hasConnection(getApplicationContext())) {
            getYearData();
        }
        else {
            internetConnection.showNetDisabledAlertToUser(AddNewHoliday.this);
        }
    }
    
    public boolean validation()
    {
        if (ed_holiday_title.getText().toString().equals(""))
        {
            ed_holiday_title.setError("Please enter title");
            return false;
        }
        if (txt_holiday_date.getText().toString().equals(""))
        {
            txt_holiday_date.setError("Please select date");
            return false;
        }
        if (ed_holiday_description.getText().toString().equals(""))
        {
            ed_holiday_description.setError("Please enter description");
            return false;
        }
        if (ac_holiday_type.getText().toString().equals(""))
        {
            ac_holiday_type.setError("Please select type");
            return false;
        }
        if (ac_year.getText().toString().equals(""))
        {
            ac_year.setError("Please select year");
            return false;
        }
        return true;
    }

    public void addHoliday()
    {
        class AddHoliday extends AsyncTask<String, Void, String>
        {
            ProgressDialog progressDialog = null;
            @Override
            protected void onPreExecute() 
            {
                progressDialog = ProgressDialog.show(AddNewHoliday.this, "Please wait", "Adding Holiday...", true);
                progressDialog.show();
                
                holidayTitle = ed_holiday_title.getText().toString();
                holidayDate = txt_holiday_date.getText().toString();
                holidayDescription = ed_holiday_description.getText().toString();
                type = ac_holiday_type.getText().toString();
                
                /*Log.i("add_holiday_data", "holiday title : " + holidayTitle + "\n" +
                        "holiday date : " + holidayDate + "\n" +
                        "holiday description : " + holidayDescription + "\n" +
                        "holiday type : " + type);*/
            }

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    String add_holiday_url = ""+url_http+""+Url+"/owner/hrmapi/addyearlyholiday/?";
                    
                    String query = String.format("title=%s&date=%s&description=%s&type=%s&fyid=%s",
                            URLEncoder.encode(holidayTitle, "UTF-8"),
                            URLEncoder.encode(holidayDate, "UTF-8"),
                            URLEncoder.encode(holidayDescription, "UTF-8"),
                            URLEncoder.encode(holidayType, "UTF-8"),
                            URLEncoder.encode(fy_id, "UTF-8"));
                    
                    url = new URL(add_holiday_url + query);
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

                        JSONObject object = jsonArray.getJSONObject(0);

                        String responseCode = object.getString("responsecode");

                        if (responseCode.equals("1")) 
                        {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddNewHoliday.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setMessage("Holiday Added Successfully");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(AddNewHoliday.this, Leaves.class);
                                    Leaves.add_holiday = true;
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            alertDialog.show();
                        }
                        else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddNewHoliday.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setMessage("Holiday already exist");
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
                    Toast.makeText(AddNewHoliday.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        AddHoliday addHoliday = new AddHoliday();
        addHoliday.execute();
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
                                ac_year.setText(year);
                                fy_id = fy_id1;
                            }

                            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.year_drop_down, year_array);
                            ac_year.setAdapter(adapter);
                        }
                        
                    } 
                    catch (JSONException e) {
                        Log.e("JsonException", e.toString());
                    }
                }
                else {
                    Toast.makeText(AddNewHoliday.this, "Sorry...Bad internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }

        GetYearData getYearData = new GetYearData();
        getYearData.execute();
    }
    
    public void txtChange()
    {
        ac_year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ac_year.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ac_holiday_type.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ac_holiday_type.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        ed_holiday_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ed_holiday_title.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        ed_holiday_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ed_holiday_description.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        txt_holiday_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txt_holiday_date.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
