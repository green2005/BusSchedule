package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.sun.mail.pop3.POP3SSLStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import by.grodno.bus.R;


public class DBUpdater {
    String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    Context mContext;
    Store store;


    public DBUpdater(Context context) {
        mContext = context;
    }

    private Folder getYandexInboxFolder() throws Exception {
        Properties pop3Props;
        Session session;

        String host = "pop.yandex.ru";
        String user = "green2005update";
        String password = "androidupdate1";
        pop3Props = new Properties();
        pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port", "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");
        URLName url = new URLName("pop3", host, 995, "",
                user, password);
        session = Session.getInstance(pop3Props, null);
        store = new POP3SSLStore(session, url);
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
                    String s1 = s.substring(0, 3);
                    if (!s1.equalsIgnoreCase("zip"))
                        continue;
                    s = s.replace("zip", "");
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

                    String lastUpdatedDay = lastUpdated.substring(0, 2);
                    String lastUpdatedMonth = lastUpdated.substring(3, 5);
                    String lastUpdatedYear = lastUpdated.substring(6, 10);

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

    private void checkScheduleUpdateExists(final UpdateListener listener, String lastUpdated, Handler handler) {
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
                    postSuccess(listener, handler, null);
                } else {
                    final String updated = msg.getSubject().replace("zip", "");
                    postSuccess(listener, handler, updated);
                }
            } finally {
                folder.close(false);
                if (store != null)
                    store.close();
            }
        } catch (Exception e) {
            postError(listener, handler, R.string.update_error);
        }
    }

    private void updateSchedule(final UpdateListener listener, String lastUpdated, Handler handler) {
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
                    postSuccess(listener, handler, null);
                    return;
                }
                String newFileName = DBManager.getDBfileName(mContext) + ".tmp";
                String dbFileName = DBManager.getDBfileName(mContext);
                OutputStream stream = new FileOutputStream(newFileName);
                Multipart multipart = (Multipart) msg.getContent();

                for (int j = 0; j < multipart.getCount(); j++) {
                    BodyPart bp = multipart.getBodyPart(j);
                    ZipInputStream is = new ZipInputStream(bp.getInputStream());
                    byte[] buffer = new byte[2048];
                    int length;

                    while ((length = is.read(buffer)) > 0) {
                        stream.write(buffer, 0, length);
                    }
                    is.closeEntry();
                    is.close();
                }
                stream.flush();
                stream.close();
                if (checkIsDbCorrect(newFileName)) {
                    File tmpfile = new File(newFileName);
                    File dbFile = new File(dbFileName);
                    if (tmpfile.renameTo(dbFile)) {
                        final String updatedDate = msg.getSubject().replace("zip", "");
                        postSuccess(listener, handler, updatedDate);
                    } else {
                        postError(listener, handler, R.string.update_error);
                    }
                } else {
                    try {
                        File tmpfile = new File(newFileName);
                        tmpfile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postError(listener, handler, R.string.update_error);
                }
            } finally {
                folder.close(false);
                if (store != null)
                    store.close();
            }
        } catch (Exception e) {
            postError(listener, handler, e.getMessage());
        }
    }

    private void postError(final UpdateListener listener, final Handler handler, final String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(error);
            }
        });
    }

    private void postError(final UpdateListener listener, final Handler handler, final int stringResId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(stringResId);
            }
        });
    }

    private void postSuccess(final UpdateListener listener, final Handler handler, final String updateDate) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(updateDate);
            }
        });
    }

    private boolean checkIsDbCorrect(String fileName) {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(fileName, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS
                            | SQLiteDatabase.CREATE_IF_NECESSARY);

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


    public void checkUpdateExists(final UpdateListener listener, final String lastUpdated) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkScheduleUpdateExists(listener, lastUpdated, handler);
            }
        }).start();
    }

    public void updateDB(final UpdateListener listener, final String lastUpdated) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateSchedule(listener, lastUpdated, handler);
            }
        }).start();
    }
}
