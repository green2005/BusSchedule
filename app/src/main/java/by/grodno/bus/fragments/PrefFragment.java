package by.grodno.bus.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import by.grodno.bus.BusApplication;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.UpdateListener;

public class PrefFragment extends PreferenceFragment {

    public static PrefFragment getNewFragment() {
        return new PrefFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {
        String prefKey = preference.getKey();
        String rateKey = getResources().getString(R.string.rateKey);
        String updateKey = getResources().getString(R.string.updateKey);
        if (prefKey.equals(updateKey)) {
            updateSchedule();
        } else if (prefKey.equals(rateKey)) {
            rateMe();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void rateMe() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            ErrorHelper.showErrorDialog(R.string.errormarket, activity, null);
        }
    }

    private void updateSchedule() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        DBManager dbManager = ((BusApplication) activity.getApplication()).getDBManager();
        UpdateListener listener = new UpdateListener() {
            @Override
            public void onError(String error) {
                ErrorHelper.showErrorDialog(error, activity, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            @Override
            public void onError(int errorResId) {
                ErrorHelper.showErrorDialog(errorResId, activity, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            @Override
            public void onSuccess(String updatedDate) {
                if (!TextUtils.isEmpty(updatedDate)) {
                    Toast.makeText(activity, R.string.updatesucceded, Toast.LENGTH_LONG).show();
                    restartApp();
                } else {
                    Toast.makeText(activity, R.string.updatenotneeded, Toast.LENGTH_LONG).show();
                }
            }
        };
        dbManager.updateDB(listener, false, activity);
    }


    private void restartApp() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent i = activity.getApplication().getPackageManager()
                    .getLaunchIntentForPackage(activity.getApplication().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            activity.finish();
        }
    }
}
