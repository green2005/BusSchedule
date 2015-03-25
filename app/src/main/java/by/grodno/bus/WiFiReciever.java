package by.grodno.bus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import by.grodno.bus.db.DBUpdater;
import by.grodno.bus.db.UpdateService;

public class WiFiReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null) {
            if (info.isConnected()) {
                if (DBUpdater.needCheckUpdate(context)) {
                    Intent serviceIntent = new Intent(context, UpdateService.class);
                    context.startService(serviceIntent);
                    DBUpdater.setCheckDate(context);
                }
            }
        }
    }
}
