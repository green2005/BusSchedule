package by.grodno.bus.db;


import android.database.Cursor;
import android.os.Handler;

public class QueryHelper {
    private DBManager mManager;

    public interface QueryListener{
        public void onQueryCompleted(Cursor cursor);
    }

    public QueryHelper(DBManager manager) {
        mManager = manager;
        if (manager == null){
            throw new IllegalArgumentException("DBManager is null");
        }
    }

    public void rawQuery(final String sql, final QueryListener listener){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = mManager.rawQuery(sql);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onQueryCompleted(cursor);
                    }
                });
            }
        }).start();
    }
}
