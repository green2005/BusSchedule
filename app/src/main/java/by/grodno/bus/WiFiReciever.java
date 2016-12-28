package by.grodno.bus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import by.grodno.bus.db.DBUpdater;
import by.grodno.bus.service.UpdateService;


public class WiFiReciever extends BroadcastReceiver {
    interface CheckStateListener {
        void onInetConnected(Context context);
    }


    private void checkConnection(final Context context, final CheckStateListener listener) {
        final Handler h = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context appContext = context.getApplicationContext();
                ConnectivityManager cm =
                        (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting())) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInetConnected(context);
                        }
                    });
                }
            }
        }).start();
    }

    private void checkForUpdates(Context context) {
        if (DBUpdater.needCheckUpdate(context)) {
            Intent serviceIntent = new Intent(context, UpdateService.class);
            context.startService(serviceIntent);
            DBUpdater.setCheckDate(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final CheckStateListener listener = new CheckStateListener() {
            @Override
            public void onInetConnected(Context context) {
                WiFiReciever.this.checkForUpdates(context);
            }
        };
        checkConnection(context, listener);
    }
}
