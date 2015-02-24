package by.grodno.bus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.grodno.bus.R;

public class FavouritesFragment extends Fragment {
    public static Fragment newInstance(Bundle params){
        Fragment fragment = new FavouritesFragment();
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourities, null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
