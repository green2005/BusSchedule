package by.grodno.bus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import by.grodno.bus.R;
import by.grodno.indexableListView.StringMatcher;

public class StopsAdapter extends BaseAdapter implements SectionIndexer{
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private static final String SECTIONS = "АБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЬЪЭЮЯ";

    public StopsAdapter(Context context, Cursor cursor){
        mCursor  = cursor;
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(0);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = mInflater.inflate(R.layout.stops, null);
        }
        TextView tvTitle = (TextView)view.findViewById(R.id.tvTitle);
        mCursor.moveToPosition(position);
        tvTitle.setText(mCursor.getString(0));
        return view;

    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[SECTIONS.length()];
        for (int i = 0; i < SECTIONS.length(); i++)
            sections[i] = String.valueOf(SECTIONS.charAt(i));
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(((String)getItem(j)).charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(((String)getItem(j)).charAt(0)), String.valueOf(SECTIONS.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}
