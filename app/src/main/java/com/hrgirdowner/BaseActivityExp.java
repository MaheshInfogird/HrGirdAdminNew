package com.hrgirdowner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class BaseActivityExp extends ActionBarActivity {

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    SharedPreferences pref;
    UserSessionManager session;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final String MyPREFERENCES_notify = "MyPrefs_notify" ;
    SharedPreferences pref1;
    SharedPreferences.Editor editor, editor2;
    
    private static final String PREFER_NAME = "MyPref";
    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    public static final String MyPREFERENCES = "MyPrefs" ;
    int PRIVATE_MODE = 0;
    SharedPreferences shared_pref, pref_rm;
    SharedPreferences.Editor editor1;
    
    private DrawerLayout mDrawerLayout;
    private android.app.Fragment fragment = null;
    private ExpandableListView expListView;
    private HashMap<String, List<String>> listDataChild;
    private ExpandableListAdapter listAdapter;
    
    ImageView Headerimage;
    View view_Group;
    
    String Url, logo;
    String firstName, LastName, Email;
    static String reportingManager;
    int lastExpandedPosition = -1;
    
    Toolbar toolbar;
    private List<String> listDataHeader;
    
    static int[] icon_rm = { R.drawable.dashboard_icon,
                          //R.drawable.approved_leave,
                          //R.drawable.pending_approvals,
            R.drawable.need_to_review_icon,
                          R.drawable.attendance_icon,
            R.drawable.events_icon,
                          R.drawable.settings_icon
                         /* R.drawable.logout_icon*/};

    static int[] icon = { R.drawable.dashboard_icon,
            R.drawable.leave_menu_icon,
            R.drawable.events_icon,
            //R.drawable.approved_leave,
            //R.drawable.pending_approvals,
            //R.drawable.leave_menu_icon,
            R.drawable.attendance_listing_icon,
            R.drawable.settings_icon/*,
            R.drawable.logout_icon*/};
    
    static int[] sub_icon = { R.drawable.birthday_icon_new,
                              R.drawable.work_anni_icon,
                              R.drawable.marriage_anni_icon};

    static int[] sub_icon_att = { R.drawable.daily_attendance_icon,
            R.drawable.absent_attendance_icon,
            R.drawable.on_leave_attendance_icon,
            R.drawable.holiday_icon};
    
    static int[] sub_icon_review = { R.drawable.review_icon,
                                     R.drawable.reviewed_icon};

    static int[] sub_icon_settings = { R.drawable.reset_password_icon,
            R.drawable.logout_icon,R.drawable.logout_icon};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_exp);

        session = new UserSessionManager(getApplicationContext());

        toolbar = (Toolbar)findViewById(R.id.toolbar_inner);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle = mDrawerTitle = getTitle();

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        logo = (shared_pref.getString("logo", ""));
        
        pref = getApplicationContext().getSharedPreferences(PREFER_NAME, this.MODE_PRIVATE);
        firstName = pref.getString("Tag_firstname", "");
        LastName = pref.getString("Tag_lastname", "");
        Email = pref.getString("Tag_email", "");

        pref_rm = getApplicationContext().getSharedPreferences(MyPREFERENCES, this.MODE_PRIVATE);
        reportingManager = pref_rm.getString("reportingManager","");

        setUpDrawer();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        makeActionOverflowMenuShown();

    }
    
    // actionbar over flow icon
    public void makeActionOverflowMenuShown() 
    {
        try 
        {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } 
        catch (final Exception e) {
            Log.e("", e.getLocalizedMessage());
        }
    }

    
    public void setUpDrawer()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerListener);
        expListView = (ExpandableListView) findViewById(R.id.list_slidermenu);
        prepareListData();

        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.side_menu_header, null, false);
        Headerimage = (ImageView)listHeaderView.findViewById(R.id.header_logo);
        Picasso.with(this).load(logo).into(Headerimage);
        expListView.addHeaderView(listHeaderView);
        

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                Log.i("expand","last="+lastExpandedPosition);
                Log.i("expand","group="+groupPosition);
                if (lastExpandedPosition!=-1 && groupPosition!=lastExpandedPosition){
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
                Log.i("expand","last_after="+lastExpandedPosition);

            }
        });

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() 
        {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) 
            {
                v.setSelected(true);

                switch (groupPosition)
                {
                    case 0:
                        Intent intent1 = new Intent(getApplicationContext(), DashBoard.class);
                        startActivity(intent1);
                        finish();
                        break;

                 /*   case 1:
                        Intent intent2 = new Intent(getApplicationContext(), HolidayActivity.class);
                        startActivity(intent2);
                        finish();
                        break;*/

                    case 1:

                        if (!reportingManager.equals("1"))
                        {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivityExp.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setTitle("Not Authorised");
                            alertDialog.setMessage("Your not authorized for Review");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                        break;

                    case 2:
                        /*Intent intent4 = new Intent(getApplicationContext(), Approvals.class);
                        startActivity(intent4);
                        finish();*/
                       /* if (!reportingManager.equals("1"))
                        {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivityExp.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setTitle("Not Authorised");
                            alertDialog.setMessage("Your not authorized for Review");
                            alertDialog.setCancelable(true);
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                        break;*/
                        break;

                    case 3:
                       /* Intent intent5 = new Intent(getApplicationContext(), PendingApprovals.class);
                        startActivity(intent5);
                        finish();*/
                      /*  Intent intent6 = new Intent(getApplicationContext(), AttendanceListing.class);
                        startActivity(intent6);
                        finish();*/
                        break;

                    case 4:
                        /*Intent intent6 = new Intent(getApplicationContext(), AttendanceListing.class);
                        startActivity(intent6);
                        finish();*/
                      /*  Intent intent8 = new Intent(getApplicationContext(), Settings.class);
                        startActivity(intent8);
                        finish();*/
                        break;

//                    case 6:
                        /*Intent intent8 = new Intent(getApplicationContext(), Settings.class);
                        startActivity(intent8);
                        finish();*/
                      /*  AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivityExp.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        alertDialog.setTitle("Logout");
                        alertDialog.setMessage("Do you want to logout from Application?");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                session.logoutUser();
                                NavDrawerListAdapter.setSelectedPosition(0);
                                Intent intent = new Intent(BaseActivityExp.this, LogInActivity.class);
                                startActivity(intent);
                                finish();

                                pref1 = getApplicationContext().getSharedPreferences(MyPREFERENCES_notify, PRIVATE_MODE);
                                editor2 = pref.edit();
                                editor2.clear();
                                editor2.putBoolean("notification", false);
                                editor2.commit();
                            }
                        });

                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });
                        alertDialog.show();
                        break;*/

                    /*case 7:
                        *//*AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivityExp.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        alertDialog.setTitle("Logout");
                        alertDialog.setMessage("Do you want to logout from Application?");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                session.logoutUser();
                                NavDrawerListAdapter.setSelectedPosition(0);
                                Intent intent = new Intent(BaseActivityExp.this, LogInActivity.class);
                                startActivity(intent);
                                finish();

                                pref1 = getApplicationContext().getSharedPreferences(MyPREFERENCES_notify, PRIVATE_MODE);
                                editor2 = pref.edit();
                                editor2.clear();
                                editor2.putBoolean("notification", false);
                                editor2.commit();
                            }
//                        });

                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() 
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) 
                            {
                            }
                        });
                        alertDialog.show();*//*
                        
                        break;

                    case 8:
                        *//*Intent intent9 = new Intent(getApplicationContext(), Settings.class);
                        startActivity(intent9);
                        finish();*//*
                        break;*/

                    default:
                        break;

                }
                return false;
            }
        });
        
        
        expListView.setOnChildClickListener(new OnChildClickListener() 
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                v.setSelected(true);
                if (view_Group != null) {
                }
                view_Group = v;

                switch (groupPosition)
                {
                    case 0:

                        break;

                  /*  case 1:

                        break;*/
                    case 1:
                        if (reportingManager.equals("1")) {
                            switch (childPosition) {
                                case 0:
                                    Intent intent4 = new Intent(getApplicationContext(), ReviewActivity.class);
                                    startActivity(intent4);
                                    finish();
                                    break;

                                case 1:
                                    Intent intent5 = new Intent(getApplicationContext(), ReviewedActivity.class);
                                    startActivity(intent5);
                                    finish();
                                    break;

                                default:
                                    break;
                            }
                        }

                        break;

                    case 2:
                        Intent intent8 = new Intent(getApplicationContext(), AttendanceListing.class);
                        startActivity(intent8);
                        finish();
                        switch (childPosition)
                        {
                            case 0:
                                Intent intent2 = new Intent(getApplicationContext(), AttendanceListing.class);
                                startActivity(intent2);
                                finish();
                                break;

                            case 1:
                                Intent intent4 = new Intent(getApplicationContext(), AttendanceListingAbsent.class);
                                startActivity(intent4);
                                finish();
                                break;

                            case 2:
                                Intent intent5 = new Intent(getApplicationContext(), AttendanceListingOnLeave.class);
                                startActivity(intent5);
                                finish();
                                break;
                            case 3:
                                Intent intent6 = new Intent(getApplicationContext(), HolidayActivity.class);
                                startActivity(intent6);
                                finish();
                                break;

                            default:
                                break;
                        }
                        break;

                    case 3:
                    switch (childPosition)
                    {
                        case 0:
                            Intent intent2 = new Intent(getApplicationContext(), BirthdayActivity.class);
                            startActivity(intent2);
                            finish();
                            break;

                        case 1:
                            Intent intent4 = new Intent(getApplicationContext(), WorkAnnActivity.class);
                            startActivity(intent4);
                            finish();
                            break;

                        case 2:
                            Intent intent5 = new Intent(getApplicationContext(), MarriageAnnActivity.class);
                            startActivity(intent5);
                            finish();
                            break;

                        default:
                            break;
                    }
                    break;




                    case 4:
                        switch (childPosition) {
                            case 0:
                                Intent intent6 = new Intent(getApplicationContext(), Settings.class);
                                startActivity(intent6);
                                finish();
                                break;

                            case 1:
                                Intent intent7 = new Intent(getApplicationContext(), PostActivity.class);
                                startActivity(intent7);
                                finish();
                                break;

                            case 2:
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivityExp.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                alertDialog.setTitle("Logout");
                                alertDialog.setMessage("Do you want to logout from Application?");
                                alertDialog.setCancelable(false);
                                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        session.logoutUser();
                                        NavDrawerListAdapter.setSelectedPosition(0);
                                        Intent intent = new Intent(BaseActivityExp.this, LogInActivity.class);
                                        startActivity(intent);
                                        finish();

                                      /*  pref1 = getApplicationContext().getSharedPreferences(MyPREFERENCES_notify, PRIVATE_MODE);
                                        editor2 = pref.edit();
                                        editor2.clear();
                                        editor2.putBoolean("notification", false);
                                        editor2.commit();*/
                                    }
                                });

                                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                    }
                                });
                                alertDialog.show();
                                break;


                            default:
                                break;
                        }


               /*     case 6:

                        break;*/

                   /* case 7:
                        break;

                    case 8:
                        *//*Intent intent9 = new Intent(getApplicationContext(), Approvals.class);
                        startActivity(intent9);
                        finish();*/

                    default:
                        break;
                }

                expListView.setItemChecked(childPosition, true);
                expListView.setSelection(groupPosition);
                mDrawerLayout.closeDrawer(expListView);
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig); 
        // Pass any configuration //change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            if (mDrawerLayout.isDrawerOpen(expListView))
            {
                mDrawerLayout.closeDrawer(expListView);
            }
            else
            {
                mDrawerLayout.openDrawer(expListView);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    
    private DrawerListener mDrawerListener = new DrawerListener() {
        @Override
        public void onDrawerStateChanged(int status) {
        }

        @Override
        public void onDrawerSlide(View view, float slideArg) {
        }

        @Override
        public void onDrawerOpened(View view) {
            getSupportActionBar().setTitle(mDrawerTitle);
            // calling onPrepareOptionsMenu() to hide action bar icons
            supportInvalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(View view) {
            getSupportActionBar().setTitle(mTitle);
            // calling onPrepareOptionsMenu() to show action bar icons
            supportInvalidateOptionsMenu();
        }
    };


    private void prepareListData()
    {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        String[] array = getResources().getStringArray(R.array.nav_drawer_items_rm);
        listDataHeader = Arrays.asList(array);

        List<String> dashboard = new ArrayList<String>();
        //String[] dash = getResources().getStringArray(R.array.dashboard);
        //dashboard = Arrays.asList(dash);

//        List<String> leaves = new ArrayList<String>();
        //String[] sub_leaves = getResources().getStringArray(R.array.leaves);
        //leaves = Arrays.asList(sub_leaves);

        List<String> events = new ArrayList<String>();
        String[] sub_events = getResources().getStringArray(R.array.events);
        events = Arrays.asList(sub_events);

        List<String> settings = new ArrayList<String>();
        String[] sub_settings = getResources().getStringArray(R.array.settings);
        settings = Arrays.asList(sub_settings);
        
        List<String> approved = new ArrayList<String>();
        //String[] myproe = getResources().getStringArray(R.array.approved);
        //approved = Arrays.asList(myproe);

        List<String> pending = new ArrayList<String>();
        //String[] inco = getResources().getStringArray(R.array.pending);
        //pending = Arrays.asList(inco);
        
        List<String> attListing = new ArrayList<String>();
        String[] sub_att = getResources().getStringArray(R.array.attListing);
        attListing = Arrays.asList(sub_att);
        //String[] listing = getResources().getStringArray(R.array.attListing);
        //attListing = Arrays.asList(listing);
        
      /*  List<String> settings = new ArrayList<String>();*/

        List<String> logout = new ArrayList<String>();

        List<String> review = new ArrayList<String>();
        if (reportingManager.equals("1")) {
            String[] sub_review = getResources().getStringArray(R.array.need_review);
            review = Arrays.asList(sub_review);
        }

            listDataChild.put(listDataHeader.get(0), dashboard); // Header, Child data
        /*    listDataChild.put(listDataHeader.get(1), leaves);*/
            listDataChild.put(listDataHeader.get(1), review);
            listDataChild.put(listDataHeader.get(2), attListing);
            listDataChild.put(listDataHeader.get(3), events);
            //listDataChild.put(listDataHeader.get(3), approved);
            //listDataChild.put(listDataHeader.get(4), pending);

            listDataChild.put(listDataHeader.get(4), settings);
//            listDataChild.put(listDataHeader.get(6), logout);

        // assigning values to menu and submenu
    }
}