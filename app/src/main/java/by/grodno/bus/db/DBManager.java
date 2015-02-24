package by.grodno.bus.db;

import android.content.Context;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    private Context mContext;

    public DBManager(Context context){
        mContext  = context;
    }

    public void checkUpdateExists(UpdateListener listener){
        DBUpdater updater = new DBUpdater(mContext);
        updater.checkUpdateExists(listener);
    }

    public void updateDB(UpdateListener listener){
        DBUpdater updater = new DBUpdater(mContext);
        updater.updateDB(listener);
    }

}
