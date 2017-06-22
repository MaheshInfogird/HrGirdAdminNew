package com.hrgirdowner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader;
    private HashMap<String, List<String>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData)
    {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navdrawer_child_item, null);
        }
        
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        ImageView imgListChild = (ImageView) convertView.findViewById(R.id.sub_menu_icon);

        if (BaseActivityExp.reportingManager.equals("1"))
        {
            if (groupPosition == 1) {
                imgListChild.setImageResource(BaseActivityExp.sub_icon_review[childPosition]);
            }
            else if (groupPosition == 2) {
                imgListChild.setImageResource(BaseActivityExp.sub_icon_att[childPosition]);
            }
            else if (groupPosition == 3) {
                imgListChild.setImageResource(BaseActivityExp.sub_icon[childPosition]);
            }
            else if (groupPosition == 4) {
                imgListChild.setImageResource(BaseActivityExp.sub_icon_settings[childPosition]);
            }
        }


        txtListChild.setText(childText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navdrawer_group_item, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        // lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
      
        ImageView imgListGroup = (ImageView) convertView.findViewById(R.id.ic_txt);
        ImageView imgdrop = (ImageView) convertView.findViewById(R.id.ic_drop_down);

        imgListGroup.setImageResource(BaseActivityExp.icon_rm[groupPosition]);
        if (groupPosition == 1)
        {
            imgdrop.setImageResource(R.drawable.plus_icon);
        }
        if (groupPosition == 1)
        {
            if (isExpanded) {
                imgdrop.setImageResource(R.drawable.minus_icon);
            }
        }
        
        if (groupPosition == 2)
        {
            imgdrop.setImageResource(R.drawable.plus_icon);
        }
        if (groupPosition == 2)
        {
            if (isExpanded) {
                imgdrop.setImageResource(R.drawable.minus_icon);
            }
        }
        if (groupPosition == 3)
        {
            imgdrop.setImageResource(R.drawable.plus_icon);
        }
        if (groupPosition == 3)
        {
            if (isExpanded) {
                imgdrop.setImageResource(R.drawable.minus_icon);
            }
        }

        if (groupPosition == 4)
        {
            imgdrop.setImageResource(R.drawable.plus_icon);
        }
        if (groupPosition == 4)
        {
            if (isExpanded) {
                imgdrop.setImageResource(R.drawable.minus_icon);
            }
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
