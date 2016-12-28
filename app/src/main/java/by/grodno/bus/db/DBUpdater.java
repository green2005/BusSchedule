package by.grodno.bus.db;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;


public class DBUpdater {
    ProgressDialog mProgressDialog;
    private String mUpdatedDate;
    private boolean mErrors = false;

    private static final String UPDATE_DATE = "update_date";
    private static final String CHECK_DATE = "check_date";

    private static final String SCHEDULE_UPDATE_URL = "https://www.dropbox.com/s/sfuczz8tfvf836k/schedule.txt?dl=1";

    public DBUpdater() {

    }


    private String getUpdateUrl(String lastUpdated, UpdateListener listener, Handler handler) throws Exception {
        URL u = new URL(SCHEDULE_UPDATE_URL);
        HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
        if (ucon.getResponseCode() != 200) {
            throw new Exception("server response: " + ucon.getResponseMessage());
        }
        InputStream is = ucon.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String date = reader.readLine();
        String url = reader.readLine();
        reader.close();
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        if (date.equals(lastUpdated)) {
            return "";
        }
        String ds = date.substring(0, 2);
        String ms = date.substring(3, 5);
        String ys = date.substring(6, 10);
        String lastUpdatedDay = "";
        String lastUpdatedMonth = "";
        String lastUpdatedYear = "";
        if (!TextUtils.isEmpty(lastUpdated)) {
            lastUpdatedDay = lastUpdated.substring(0, 2);
            lastUpdatedMonth = lastUpdated.substring(3, 5);
            lastUpdatedYear = lastUpdated.substring(6, 10);
        }

        int i = (ys.compareTo(lastUpdatedYear));
        if (i == 0) {
            i = (ms.compareTo(lastUpdatedMonth));
            if (i == 0)
                i = (ds.compareTo(lastUpdatedDay));
        }

        if (i <= 0) {
            return "";
        }
        mUpdatedDate = date;
        return url;
    }

    private void updateSchedule(final UpdateListener listener, String lastUpdated, Handler handler, final Context context) {
        mUpdatedDate = "";
        try {
            String updateUrl = getUpdateUrl(
                    lastUpdated,
                    listener,
                    handler);


            if (TextUtils.isEmpty(updateUrl)) {
                postSuccess(listener, handler, null, context);
                return;
            }
            String newFileName = DBManager.getDBfileName(context) + ".tmp";
            File file = new File(newFileName);
            if (!file.mkdirs()) {
                return;
            }

            String path = file.getParent();
            String fileName = file.getName();
            file = new File(path, fileName);
            file.delete();

            file.createNewFile();
            file.setWritable(true);

            OutputStream stream = new FileOutputStream(file, false);
            URL u = new URL(updateUrl);
            HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
            InputStream inputStream = ucon.getInputStream();

            ZipInputStream is = new ZipInputStream(inputStream);
            ZipEntry ze = is.getNextEntry();
            int length = 0;
            byte[] buffer = new byte[2048];
            while ((length = is.read(buffer)) > 0) {
                stream.write(buffer, 0, length);
            }
            is.closeEntry();

            stream.flush();
            stream.close();

            if (checkIsDbCorrect(newFileName)) {
                File tmpfile = new File(newFileName);
                String dbFileName = DBManager.getDBfileName(context);
                File dbFile = new File(dbFileName);
                if (tmpfile.renameTo(dbFile)) {
                    final String updatedDate = mUpdatedDate;//msg.getSubject().replace(MESSAGE_PREFIX, "");
                    postSuccess(listener, handler, updatedDate, context);
                } else {
                    postError(listener, handler, R.string.update_error);
                }
            } else {
                try {
                    File tmpfile = new File(newFileName);
                    if (!tmpfile.delete()) {
                        tmpfile.deleteOnExit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                postError(listener, handler, R.string.update_error);
            }

        } catch (Exception e) {
            if (e.getMessage().contains("Unable to resolve host")) {
                postError(listener, handler, R.string.check_inet_connection);
            } else {
                postError(listener, handler, e.getMessage());
            }
        }
    }

    private void hideProgress() {
        if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void postError(final UpdateListener listener, final Handler handler, final String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                listener.onError(error);
            }
        });
    }

    private void postError(final UpdateListener listener, final Handler handler, final int stringResId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                listener.onError(stringResId);
            }
        });
    }

    private void postSuccess(final UpdateListener listener, final Handler handler, final String updateDate, final Context context) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                if (!TextUtils.isEmpty(updateDate)) {
                    setUpdateDate(updateDate, context);
                }
                listener.onSuccess(updateDate);
            }
        });
    }

    public static String getPreferencesFileName(Context context) {
        return context.getPackageName();
    }

    private String getUpdateDate(Context context, boolean dbExists) {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        if (!dbExists) {
            return "";
        } else {
            return prefs.getString(UPDATE_DATE, null);
        }
    }

    private void setUpdateDate(String updateDate, Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(UPDATE_DATE, updateDate);
        editor.apply();
    }

    private boolean checkIsDbCorrect(String fileName) {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(fileName, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            String sql = " select name from buses group by name order by length(name),name";
            Cursor cr = db.rawQuery(sql, null);
            if (cr.getCount() == 0) {
                cr.close();
                db.close();
                return false;
            }
            cr.close();
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void updateDB(final UpdateListener listener, final boolean silent, final Context context, final boolean dbExists) {
        final Handler handler = new Handler();
        if (!silent) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setTitle(R.string.please_wait);
            mProgressDialog.setMessage(context.getString(R.string.db_is_updating));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateSchedule(listener, getUpdateDate(context, dbExists), handler, context);
            }
        }).start();
    }

    public static boolean needCheckUpdate(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        String autoUpdateKey = context.getResources().getString(R.string.autoupdate);
        String lastChecked = prefs.getString(CHECK_DATE, "");
        boolean autoUpdate = prefs.getBoolean(autoUpdateKey, true);
        if (!autoUpdate) {
            return false;
        }
        if (TextUtils.isEmpty(lastChecked)) {
            return true;
        } else {
            String currentDate = CalendarHelper.getDate();
            return !currentDate.equals(lastChecked);
        }
    }

    public static void setCheckDate(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences.Editor editor = appContext.getSharedPreferences(getPreferencesFileName(appContext),
                Context.MODE_PRIVATE).edit();
        editor.putString(CHECK_DATE, CalendarHelper.getDate());
        editor.apply();
    }
}
