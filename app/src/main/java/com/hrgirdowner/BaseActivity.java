package com.hrgirdowner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by adminsitrator on 17/01/2017.
 */
public class BaseActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    Toolbar toolbar;
    private CharSequence mTitle;
    SharedPreferences pref;
    UserSessionManager session;
    ImageView Headerimage;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    public static final String MyPREFERENCES_url = "MyPrefs_url" ;
    int PRIVATE_MODE = 0;
    SharedPreferences shared_pref;
    SharedPreferences.Editor editor1;
    
    String Url, logo;
    String logo_final;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);
        
        toolbar = (Toolbar)findViewById(R.id.toolbar_inner);
        
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        shared_pref = getSharedPreferences(MyPREFERENCES_url, MODE_PRIVATE);
        Url = (shared_pref.getString("url", ""));
        logo = (shared_pref.getString("logo", ""));
        Log.i("Url", Url);
        Log.i("logo", logo);

        String logo_url = "https://"+Url+"/files/"+Url+"/images/logo/";

        logo_final = logo_url + logo;
        Log.i("logo_final", logo_final);
        
    }

    public void set(String[] navMenuTitles, TypedArray navMenuIcons)
    {
        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList   = (ListView) findViewById(R.id.drawer_listview);

        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.side_menu_header, null, false);
        Headerimage = (ImageView)listHeaderView.findViewById(R.id.header_logo);
        Picasso.with(BaseActivity.this).load(logo_final).into(Headerimage);
        mDrawerList.addHeaderView(listHeaderView);

        navDrawerItems = new ArrayList<NavDrawerItem>();
        // adding nav drawer items
        if (navMenuIcons == null)
        {
            for (int i = 0; i < navMenuTitles.length; i++)
            {
                navDrawerItems.add(new NavDrawerItem(navMenuTitles[i]));
            }
        }
        else
        {
            for (int i = 0; i < navMenuTitles.length; i++)
            {
                navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
            }
        }

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view)
            {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView)
            {
                getActionBar().setTitle(mDrawerTitle);

                // calling onPrepareOptionsMenu() to hide action bar icons
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }
    private class SlideMenuClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            NavDrawerListAdapter.setSelectedPosition(position - 1);
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(mDrawerList)){
                mDrawerLayout.closeDrawer(mDrawerList);
            }
            else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void displayView(int position) 
    {
        switch (position)
        {
            case 0:
               /* Intent intent = new Intent(this,BaseActivity.class);
                startActivity(intent);
                finish();*/
                break;

            case 1:
                Intent intent1 = new Intent(this, DashBoard.class);
                startActivity(intent1);
                finish();
                break;

            case 2:
                Intent intent3 = new Intent(this, HolidayActivity.class);
                startActivity(intent3);
                finish();
                /*Intent intent2 = new Intent(this, RegistrationActivity.class);
                startActivity(intent2);
                finish();*/
                break;

            case 3:
                Intent intent5 = new Intent(this, PendingApprovals.class);
                startActivity(intent5);
                finish();
                break;

            case 4:
                Intent intent6 = new Intent(this, AttendanceListing.class);
                startActivity(intent6);
                finish();
                break;
            case 5:
                Intent intent4 = new Intent(this, Approvals.class);
                startActivity(intent4);
                finish();
                break;

            case 6:
                Intent intent8 = new Intent(this, Settings.class);
                startActivity(intent8);
                finish();
                break;

            case 7:
                Intent intent9 = new Intent(this, Logout.class);
                startActivity(intent9);
                finish();
                /*Intent intent7 = new Intent(this, AttendanceActivity.class);
                startActivity(intent7);
                finish();*/
                break;
           /* case 8:
                
                break;

            case 9:
               
                break;*/
            
            default:
                break;
                
        }
        
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
