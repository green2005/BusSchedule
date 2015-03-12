package by.grodno.bus.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import by.grodno.bus.R;
import by.grodno.bus.activity.RouteStopsActivity;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.fragments.RoutesFragment;

public class RouteAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {
    private Cursor mGroupCursor;
    private DBManager mDBManager;
    private LayoutInflater mInflater;
    private Context mContext;
    private QueryHelper mQueryHelper;
    private Handler mHandler;
    private RoutesFragment.TransportKind mTransportKind;

    public RouteAdapter(Context context, Cursor groupCursor, DBManager dbManager, RoutesFragment.TransportKind transportKind) {
        mInflater = LayoutInflater.from(context);
        mDBManager = dbManager;
        mGroupCursor = groupCursor;
        mContext = context;
        mTransportKind = transportKind;
        mQueryHelper = new QueryHelper(mDBManager);
        mHandler = new Handler();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        mGroupCursor.moveToPosition(groupPosition);
        return mGroupCursor;
      //  return mDBManager.getRouteChild(mGroupCursor.getString(0), childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.routechild, null);
        }
        final View cnView = view;
        mGroupCursor.moveToPosition(groupPosition);
        String name = mGroupCursor.getString(0);
        final TextView childText = (TextView) cnView.findViewById(R.id.textChild);
        String sql = DBManager.getRouteChildSQL(name, mTransportKind);

        mQueryHelper.rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(final Cursor cursor) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cursor.moveToPosition(childPosition);
                        childText.setText(cursor.getString(cursor.getColumnIndex(DBManager.BUS_DIRECTION)));
                        cnView.setTag(cursor.getString(cursor.getColumnIndex(DBManager.BUS_ID)));
                        cursor.close();
                    }
                });
            }
        });
        return cnView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        mGroupCursor.moveToPosition(groupPosition);
        return mDBManager.getRouteDirCount(mGroupCursor.getString(0), mTransportKind);
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
        String busId = (String)v.getTag();
        Bundle b = new Bundle();
        mGroupCursor.moveToPosition(groupPosition);
        //String busId = mGroupCursor.getString(mGroupCursor.getColumnIndex(DBManager.BUS_ID));
        b.putString(DBManager.BUS_ID, busId);
        stops.putExtras(b);
        mContext.startActivity(stops);
        return false;
    }
}

