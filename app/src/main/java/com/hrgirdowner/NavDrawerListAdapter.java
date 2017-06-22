package com.hrgirdowner;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by adminsitrator on 17/01/2017.
 */
public class NavDrawerListAdapter extends BaseAdapter {
    
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private static int selectedPosition = 0;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }


    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
        TextView title = (TextView)convertView.findViewById(R.id.title);
        final RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.outer_layout);
        
        icon.setImageResource(navDrawerItems.get(position).getIcon());
        title.setText(navDrawerItems.get(position).getTitle());

        if (position == selectedPosition) {
            title.setTextColor(context.getResources().getColor(R.color.WhiteTextColor));
            layout.setBackgroundColor(context.getResources().getColor(R.color.RedBgColor));
        }
        else {
            layout.setBackgroundColor(context.getResources().getColor(R.color.GreyBgColor));
            title.setTextColor(context.getResources().getColor(R.color.BlackTextColor));
        }
        
        return convertView;
    }

    public static void setSelectedPosition(int position) {
        selectedPosition = position;
    }
}
