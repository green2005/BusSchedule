package by.grodno.bus.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import by.grodno.bus.R;
import by.grodno.bus.fragments.StopRoutesFragment;

public class StopRoutesActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle bundle = getIntent().getExtras();
        Fragment fragment = StopRoutesFragment.getNewFragment(bundle);
        ft.replace(R.id.container, fragment);
        ft.commit();
    }
}