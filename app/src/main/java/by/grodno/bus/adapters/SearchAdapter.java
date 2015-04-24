package by.grodno.bus.adapters;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import by.grodno.bus.R;

public class SearchAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
   private TextView mTextView;

    public SearchAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.searchitem, null);
        mTextView = (TextView) view.findViewById(R.id.searchitem);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mTextView = (TextView) view.findViewById(R.id.searchitem);
        mTextView.setText(cursor.getString(0));
    }
}
