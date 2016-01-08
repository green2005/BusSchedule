package by.grodno.bus.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.activity.BusStopActivity;
import by.grodno.bus.activity.StopRoutesActivity;
import by.grodno.bus.adapters.FavouritiesAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesDBHelper;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.db.QueryHelper;

public class FavouritesFragment extends Fragment {
    private DBManager mDbManager;
    private View mDelSelectedView;
    private Button mDelBtn;
    private List<Integer> mItems;
    private List<FavouritiesItem> mFavouritiesItems;
    private FavouritiesAdapter mAdapter;

    public static Fragment newInstance(Bundle params) {
        Fragment fragment = new FavouritesFragment();
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourities, null);
        mDbManager = ((BusApplication) getActivity().getApplication()).getDBManager();
        initListView(view);

        return view;
    }

    private void initListView(View parentView) {
        final ListView listView = (ListView) parentView.findViewById(R.id.favItems);
        final TextView tvEmptyMsg = (TextView) parentView.findViewById(R.id.favIsEmpty);
        mDelSelectedView = parentView.findViewById(R.id.bla);
        mDelBtn = (Button) mDelSelectedView.findViewById(R.id.delBtn);
        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelectedFromFavourities();
            }
        });
        mDbManager.getFavourities(new FavouritiesDBHelper.FavoritiesListener() {
            @Override
            public void onGetFavourities(final List<FavouritiesItem> favouritiesItems) {
                if (favouritiesItems.size() > 0) {
                    mFavouritiesItems = favouritiesItems;
                    tvEmptyMsg.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            FavouritiesItem item = favouritiesItems.get(position);
                            startSchedule(item);
                        }
                    });

                    mAdapter = new FavouritiesAdapter(getActivity(), favouritiesItems);
                    mAdapter.setSelectionChangedListener(new FavouritiesAdapter.SelectionChangedListener() {
                        @Override
                        public void onChanged(List<Integer> selectedItems) {
                            itemsSelectionChanged(selectedItems);
                        }
                    });
                    listView.setAdapter(mAdapter);
                } else {
                    tvEmptyMsg.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            }

            private void itemsSelectionChanged(List<Integer> items) {
                if (items.size() > 0) {
                    mDelSelectedView.setVisibility(View.VISIBLE);
                } else {
                    mDelSelectedView.setVisibility(View.GONE);
                }
                mItems = items;
            }


            private void startSchedule(final FavouritiesItem item) {
                final Activity activity = getActivity();
                if (TextUtils.isEmpty(item.getBusName())) {
                    String sql = " select id from stops where name = \"" + item.getStopName() + "\"";
                    new QueryHelper(mDbManager).rawQuery(sql, new QueryHelper.QueryListener() {
                        @Override
                        public void onQueryCompleted(Cursor cursor) {
                            Intent intent = new Intent(activity, StopRoutesActivity.class);
                            intent.putExtra(DBManager.STOP_NAME, item.getStopName());
                            cursor.moveToFirst();
                            if (cursor.getCount() > 0) {
                                intent.putExtra(DBManager.STOP_ID, cursor.getString(0));
                                startActivity(intent);
                            }
                        }
                    });

                } else {
                    String sql = "select buses.id as [" + DBManager.BUS_ID + "],\n" +
                            "stops.[id] as [" + DBManager.STOP_ID + "]  \n" +
                            "from buses \n" +
                            "join stops on stops.[name] = \"" + item.getStopName() + "\"\n" +
                            "join [rlbusstops] on [rlbusstops].[idbus]= [buses].[id] and [rlbusstops].[idstop]=stops.[id]\n" +
                            "where buses.name = \"" + item.getBusName() + "\" \n" +
                            "and buses.tr= \"" + item.getTr() + "\"\n" +
                            "and buses.direction = \"" + item.getDirectionName() + "\"" +
                            "\n";
                    new QueryHelper(mDbManager).rawQuery(sql, new QueryHelper.QueryListener() {
                        @Override
                        public void onQueryCompleted(Cursor cursor) {
                            Intent intent = new Intent(activity, BusStopActivity.class);
                            cursor.moveToFirst();
                            if (cursor.getCount() > 0) {
                                String busId = cursor.getString(0);
                                String stopId = cursor.getString(1);
                                intent.putExtra(DBManager.BUS_ID, busId);
                                intent.putExtra(DBManager.STOP_ID, stopId);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onGetCursor(Cursor cursor) {

            }
        });
    }

    private void removeSelectedFromFavourities() {
        List<FavouritiesItem> selection = new ArrayList<>();
        for (Integer pos : mItems) {
            selection.add(mFavouritiesItems.get(pos));
            mDbManager.removeFavourities(mFavouritiesItems.get(pos));
        }

        for (FavouritiesItem item : selection) {
            mFavouritiesItems.remove(item);
        }
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }
}
