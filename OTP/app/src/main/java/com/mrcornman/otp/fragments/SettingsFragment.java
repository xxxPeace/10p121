package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrcornman.otp.R;

public class SettingsFragment extends Fragment {


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();

        return fragment;
    }

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        return rootView;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}