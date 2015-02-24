package by.grodno.bus.db;

import android.content.Context;
import android.text.TextUtils;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    private Context mContext;

    public DBManager(Context context){
        mContext  = context;
    }

    private String getUpdateDate(){
        return null;
    }

    private void setUpdateDate(String updateDate) throws Exception{
        if (TextUtils.isEmpty(updateDate)){
            throw new Exception("updateDate is null");
        }
    }

    public void checkUpdateExists(UpdateListener listener){
        DBUpdater updater = new DBUpdater(mContext);
        updater.checkUpdateExists(listener, getUpdateDate());
    }

    public void updateDB(UpdateListener listener){
        DBUpdater updater = new DBUpdater(mContext);
        updater.updateDB(listener, getUpdateDate());
    }

    public void close(){
        //todo

    }

}
