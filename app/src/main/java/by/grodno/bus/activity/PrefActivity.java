package by.grodno.bus.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import by.grodno.bus.BusApplication;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.UpdateListener;
import by.grodno.bus.fragments.PrefFragment;

public class PrefActivity extends PreferenceActivity {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;


    public boolean isNewV11Prefs() {
        if (mHasHeaders != null && mLoadHeaders != null) {
            try {
                return (Boolean) mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            try {
                mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
                mHasHeaders = getClass().getMethod("hasHeaders");
            } catch (NoSuchMethodException e) {
            }
            super.onCreate(savedInstanceState);
            if (!isNewV11Prefs()) {
                addPreferencesFromResource(R.xml.preferences);
            }
        } else
        {
            super.onCreate(savedInstanceState);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, PrefFragment.getNewFragment());
            ft.commit();
        }
        //  super.onCreate(savedInstanceState);


        /*FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, PrefFragment.getNewFragment());
        ft.commit();*/

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //return super.onPreferenceTreeClick(preferenceScreen, preference);

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
        Activity activity = this;
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
        final Activity activity = this;
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
        Activity activity = this;
        if (activity != null) {
            Intent i = activity.getApplication().getPackageManager()
                    .getLaunchIntentForPackage(activity.getApplication().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            activity.finish();
        }
    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion <= Build.VERSION_CODES.GINGERBREAD_MR1) {

            try {
                mLoadHeaders.invoke(this, new Object[]{R.xml.headers, aTarget});
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
    }

    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            //int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("preferences"),
            //          "xml", anAct.getPackageName());
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

}