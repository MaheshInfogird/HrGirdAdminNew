package com.hrgirdowner;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by adminsitrator on 20/01/2017.
 */
public class Leaves extends BaseActivityExp implements View.OnClickListener{

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    Toolbar toolbar;
    Button btn_Overview, btn_Holidays;
    
    public static boolean add_holiday = false;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaves);

        toolbar = (Toolbar)findViewById(R.id.toolbar_inner);
        TextView Header = (TextView)findViewById(R.id.header_text);
        ImageView img_logout = (ImageView)findViewById(R.id.img_logout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("");
            Header.setText("Leaves");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpDrawer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        
        btn_Overview = (Button)findViewById(R.id.btn_overview);
        btn_Holidays = (Button)findViewById(R.id.btn_holidays);

        btn_Overview.setOnClickListener(Leaves.this);
        btn_Holidays.setOnClickListener(Leaves.this);

        if (savedInstanceState == null) 
        {
            if (!add_holiday) 
            {
                OverviewFragment overview = new OverviewFragment();
                btn_Overview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_hover));
                btn_Holidays.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
                btn_Overview.setTextColor(getResources().getColor(R.color.RedTextColor));
                btn_Holidays.setTextColor(getResources().getColor(R.color.BlackTextColor));
                getSupportFragmentManager().beginTransaction().add(R.id.content_fragment, overview).commit();
                //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, overview).commit();
            }
            else
            {
                HolidaysFragment holiday = new HolidaysFragment();
                btn_Holidays.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_hover));
                btn_Overview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
                btn_Overview.setTextColor(getResources().getColor(R.color.BlackTextColor));
                btn_Holidays.setTextColor(getResources().getColor(R.color.RedTextColor));
                getSupportFragmentManager().beginTransaction().add(R.id.content_fragment, holiday).commit();
                //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, overview).commit();
            }
        }
    }

    @Override
    public void onClick(View v) 
    {
        switch (v.getId())
        {
            case R.id.btn_overview:
                btn_Overview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_hover));
                btn_Holidays.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
                btn_Overview.setTextColor(getResources().getColor(R.color.RedTextColor));
                btn_Holidays.setTextColor(getResources().getColor(R.color.BlackTextColor));
                
                OverviewFragment overview = new OverviewFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, overview).commit();
                
                break;

            case R.id.btn_holidays:
                btn_Holidays.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_hover));
                btn_Overview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
                btn_Overview.setTextColor(getResources().getColor(R.color.BlackTextColor));
                btn_Holidays.setTextColor(getResources().getColor(R.color.RedTextColor));
                
                HolidaysFragment holidays = new HolidaysFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, holidays).commit();
                
                break;
        }
    }

    @Override
    public void onBackPressed() {
        NavDrawerListAdapter.setSelectedPosition(0);
        Intent intent = new Intent(Leaves.this, DashBoard.class);
        startActivity(intent);
        finish();
    }
}
