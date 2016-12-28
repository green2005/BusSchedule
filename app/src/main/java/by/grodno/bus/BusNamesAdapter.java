package by.grodno.bus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.db.CursorHelper;
import by.grodno.bus.db.DBContract;


public class BusNamesAdapter extends BaseAdapter {
    private static final int SELECTED_COLOR = Color.CYAN;
    private static final int UNSELECTED_COLOR = Color.LTGRAY;

    private LayoutInflater mInflater;
    private List<Integer> mPressedItems;
    private Cursor mCursor;

    public BusNamesAdapter(Cursor cr, Context context, TrackingParams trackingParams) {
        mCursor = cr;
        mInflater = LayoutInflater.from(context);
        mPressedItems = new ArrayList<>();
        cr.moveToFirst();
        if (trackingParams != null) {
            List<String> selItems = trackingParams.getBusNames();
            while (!cr.isAfterLast()) {
                String s = CursorHelper.getString(cr, DBContract.MapBusCoordsColumns.NUM);
                if (selItems.contains(s)) {
                    mPressedItems.add(cr.getPosition());
                }
                cr.moveToNext();
            }
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return mCursor;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        mCursor.moveToPosition(i);
        View cnView = view;
        Button btn;
        if (cnView == null) {
            cnView = mInflater.inflate(R.layout.item_bus_names_choice, null);
            btn = (Button) cnView.findViewById(R.id.btn);
            ViewHolder holder = new ViewHolder();
            cnView.setTag(holder);
            holder.btn = btn;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer i;
                    int j;
                    i = (Integer) view.getTag();
                    j = mPressedItems.indexOf(i);
                    if (j >= 0) {
                        mPressedItems.remove(j);
                        view.setBackgroundColor(UNSELECTED_COLOR);
                    } else {
                        mPressedItems.add(i);
                        view.setBackgroundColor(SELECTED_COLOR);
                    }
                }
            });

        } else {
            ViewHolder holder = (ViewHolder) cnView.getTag();
            btn = holder.btn;
        }
        String s = CursorHelper.getString(mCursor, DBContract.MapBusCoordsColumns.NUM);
        btn.setText(s);
        btn.setTag(i);
        if (mPressedItems.contains(i)) {
            btn.setBackgroundColor(SELECTED_COLOR);
        } else {
            btn.setBackgroundColor(UNSELECTED_COLOR);
        }
        return cnView;
    }

    private static class ViewHolder {
        private Button btn;
    }

    public void fillChosenNames(List<String> names, List<String> busTypes, String busType){
        for (int i:mPressedItems){
            mCursor.moveToPosition(i);
            names.add(CursorHelper.getString(mCursor, DBContract.MapBusCoordsColumns.NUM));
            busTypes.add(busType);
        }
    }

}
