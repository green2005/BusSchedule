package by.grodno.bus;

import android.app.Application;

import by.grodno.bus.db.DBManager;

public class BusApplication extends Application {
    private DBManager mDBManager;

    public void setDBManager(DBManager manager) {
        mDBManager = manager;
    }

    public DBManager getDBManager() {
        return mDBManager;
    }
}
