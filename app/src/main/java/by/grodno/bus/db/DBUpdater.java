package by.grodno.bus.db;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;

import com.sun.mail.pop3.POP3SSLStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;


public class DBUpdater {
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static final String MESSAGE_PREFIX = "grodno";
    Store mStore;
    ProgressDialog mProgressDialog;

    private static final String UPDATE_DATE = "update_date";
    private static final String CHECK_DATE = "check_date";

    public DBUpdater() {

    }

    private Folder getYandexInboxFolder() throws Exception {
        String host = "pop.yandex.ru";
        String user = "green2005update";
        String password = "androidupdate1";

        Properties pop3Props = new Properties();

        pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port", "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");
        pop3Props.put("mail.smtp.starttls.enable", "true");

        // connect to my pop3 inbox

				 /*
                 pop3Props = System.getProperties();
				session = Session.getDefaultInstance(pop3Props);
				store = session.getStore("pop3");
				*/
        URLName url = new URLName("pop3", host, 995, "",
                user, password);

        Session session = Session.getInstance(pop3Props, null);
        Store store = new POP3SSLStore(session, url);

        store.connect(host, user, password);


        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        return inbox;

    }


    private Message getLastMessage(Folder folder, String lastUpdated, UpdateListener listener) {
        try {
            javax.mail.Message[] messages = folder.getMessages();//
            if (messages.length > 0) {
                for (javax.mail.Message message : messages) {
                    String s = message.getSubject();
                    if (!s.startsWith(MESSAGE_PREFIX)) {
                        continue;
                    }
                    s = s.replace(MESSAGE_PREFIX, "");
                    if (!TextUtils.isEmpty(s) && s.equals(lastUpdated)){
                        continue;
                    }
                    String ds = s.substring(0, 2);
                    if ((ds.compareTo("31") == 1)
                            || (ds.compareTo("00") == -1))
                        continue;
                    String ms = s.substring(3, 5);
                    if ((ms.compareTo("12") == 1)
                            || (ms.compareTo("00") == -1))
                        continue;
                    String ys = s.substring(6, 10);
                    if ((ys.compareTo("2100") == 1)
                            || (ys.compareTo("2000") == -1))
                        continue;

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
                        continue;
                    }
                    return message;
                }
            }
        } catch (Exception e) {
            listener.onError(e.getMessage());
        }
        return null;
    }

    private void checkScheduleUpdateExists(final UpdateListener listener, String lastUpdated, Handler handler, Context context) {
        try {
            Folder folder;
            try {
                folder = getYandexInboxFolder();
            } catch (final Exception e) {
                postError(listener, handler, e.getMessage());
                return;
            }
            if (folder == null) {
                postError(listener, handler, R.string.check_inet_connection);
                return;
            }
            try {
                Message msg = getLastMessage(folder, lastUpdated, listener);
                if (msg == null) {
                    postSuccess(listener, handler, null, context);
                } else {
                    final String updated = msg.getSubject().replace(MESSAGE_PREFIX, "");
                    postSuccess(listener, handler, updated, context);
                }
            } finally {
                folder.close(false);
                if (mStore != null)
                    mStore.close();
            }
        } catch (Exception e) {
            postError(listener, handler, R.string.update_error);
        }
    }

    private void updateSchedule(final UpdateListener listener, String lastUpdated, Handler handler,final Context context) {
        try {
            Folder folder;
            try {
                folder = getYandexInboxFolder();
            } catch (final Exception e) {
                postError(listener, handler, e.getMessage());
                return;
            }

            if (folder == null) {
                postError(listener, handler, R.string.check_inet_connection);
                return;
            }
            try {
                Message msg = getLastMessage(folder, lastUpdated, listener);
                if (msg == null) {
                    postSuccess(listener, handler, null, context);
                    return;
                }
                // new File("/mnt/external_sd/");
                String newFileName = DBManager.getDBfileName(context) + ".tmp"; //"/mnt/sdcard/stb.db"; //DBManager.getDBfileName(mContext) + ".tmp";
                File file = new File(newFileName);
                file.mkdirs();

                String path = file.getParent();
                String fileName = file.getName();
                file = new File(path, fileName);
                file.delete();

                file.createNewFile();
                file.setWritable(true);

                OutputStream stream = new FileOutputStream(file, false);
                Multipart multipart = (Multipart) msg.getContent();

                for (int j = 0; j < multipart.getCount(); j++) {
                    BodyPart bp = multipart.getBodyPart(j);
                    ZipInputStream is = new ZipInputStream(bp.getInputStream());
                    ZipEntry ze = is.getNextEntry();
                    byte[] buffer = new byte[2048];
                    int length = 0;
                    //multipart.getBodyPart(1).getSize()
                    android.os.Message msg2 = new android.os.Message();


                    while ((length = is.read(buffer)) > 0) {
                        stream.write(buffer, 0, length);
                    }
                    is.closeEntry();
                }
                stream.flush();
                stream.close();
                if (checkIsDbCorrect(newFileName)) {
                    File tmpfile = new File(newFileName);
                    String dbFileName = DBManager.getDBfileName(context);
                    File dbFile = new File(dbFileName);
                    if (tmpfile.renameTo(dbFile)) {
                        final String updatedDate = msg.getSubject().replace(MESSAGE_PREFIX, "");
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
            } finally {
                folder.close(false);
                if (mStore != null)
                    mStore.close();
            }
        } catch (Exception e) {
            postError(listener, handler, e.getMessage());
        }
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
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

    private static String getPreferencesFileName(Context context) {
        return context.getPackageName();
    }

    private String getUpdateDate(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        return prefs.getString(UPDATE_DATE, null);
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
            //  db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).moveToFirst()
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



    public void updateDB(final UpdateListener listener, final boolean silent, final Context context) {
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
                updateSchedule(listener, getUpdateDate(context), handler, context);
            }
        }).start();
    }

    public static boolean needCheckUpdate(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        String lastChecked = prefs.getString(CHECK_DATE, "");
        if (TextUtils.isEmpty(lastChecked)){
            return true;
        } else
        {
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
