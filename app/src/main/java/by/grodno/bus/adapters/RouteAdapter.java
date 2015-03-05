package by.grodno.bus.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import by.grodno.bus.R;
import by.grodno.bus.activity.RouteStopsActivity;
import by.grodno.bus.db.DBManager;

public class RouteAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {
    private Cursor mGroupCursor;
    private DBManager mDBManager;
    private LayoutInflater mInflater;
    private Context mContext;

    public RouteAdapter(Context context, Cursor groupCursor, DBManager dbManager) {
        mInflater = LayoutInflater.from(context);
        mDBManager = dbManager;
        mGroupCursor = groupCursor;
        mContext = context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        mGroupCursor.moveToPosition(groupPosition);
        return mDBManager.getRouteChild(mGroupCursor.getString(0), childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View cnView = convertView;
        if (cnView == null) {
            cnView = mInflater.inflate(R.layout.routechild, null);
        }
        TextView childText = (TextView) cnView.findViewById(R.id.textChild);
        String s = (String) getChild(groupPosition, childPosition);
        childText.setText(s);
        return cnView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        mGroupCursor.moveToPosition(groupPosition);
        return mDBManager.getRouteDirCount(mGroupCursor.getString(0));
    }

    @Override
    public Object getGroup(int groupPosition) {
        mGroupCursor.moveToPosition(groupPosition);
        return mGroupCursor.getString(0);
    }

    @Override
    public int getGroupCount() {
        return mGroupCursor.getCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View cnView = convertView;
        if (cnView == null) {
            cnView = mInflater.inflate(R.layout.routetitle, null);
        }
        cnView.refreshDrawableState();

        TextView view = (TextView) cnView.findViewById(R.id.textTitle);
        mGroupCursor.moveToPosition(groupPosition);
        view.setText(mGroupCursor.getString(0));
        return cnView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Intent stops = new Intent(mContext, RouteStopsActivity.class);
        Bundle b = new Bundle();
        mGroupCursor.moveToPosition(groupPosition);
        String busId = mGroupCursor.getString(mGroupCursor.getColumnIndex(DBManager.BUS_ID));
        b.putString(DBManager.BUS_ID, busId);
        stops.putExtras(b);
        mContext.startActivity(stops);
        return false;
    }
}

