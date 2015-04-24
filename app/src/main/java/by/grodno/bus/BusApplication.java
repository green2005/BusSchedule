package by.grodno.bus;

import android.app.Application;

import by.grodno.bus.db.DBManager;

public class BusApplication extends Application {
    private DBManager mDBManager;

    @Override
    public void onCreate() {
        super.onCreate();
        getDBManager();
    }

    public DBManager getDBManager() {
        if (mDBManager == null) {
            mDBManager = new DBManager(this);
        }
        return mDBManager;
    }
}
