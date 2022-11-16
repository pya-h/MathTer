package com.thcplusplus.mathter;


import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.os.Bundle;



public class SettingActivityFragment extends PreferenceFragment {

    public SettingActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.game_settings);
    }
}
