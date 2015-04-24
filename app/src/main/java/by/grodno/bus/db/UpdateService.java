package by.grodno.bus.db;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import by.grodno.bus.R;

public class UpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DBUpdater updater = new DBUpdater();
        updater.updateDB(new UpdateListener() {
            @Override
            public void onError(String error) {
                //just ignore errors cause service is started from
                //BroadCastReciever on network state changed
                stopSelf();
            }

            @Override
            public void onError(int stringResId) {
                stopSelf();
                //
            }

            @Override
            public void onSuccess(String updatedDate) {
                if (!TextUtils.isEmpty(updatedDate)) {
                    showNotification(getApplicationContext());
                }
                stopSelf();
            }
        }, true, this, true);
        return Service.START_NOT_STICKY;
    }

    private void showNotification(Context context) {
        Notification notification;
        String appName = getResources().getString(R.string.app_name);
        String scheduleUpdated = getResources().getString(R.string.scheduleUpdated);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(context)
                    .setContentTitle(appName)
                    .setContentText(scheduleUpdated).setSmallIcon(R.drawable.appico).build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(appName)
                    .setContentText(scheduleUpdated).setSmallIcon(R.drawable.appico).getNotification();
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }


}
