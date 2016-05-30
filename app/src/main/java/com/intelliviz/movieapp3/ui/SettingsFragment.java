package com.intelliviz.movieapp3.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.intelliviz.movieapp3.R;

/**
 * Settings activity. Manage user setttings.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener{

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_by_key)));

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
                FragmentManager manager = appCompatActivity.getSupportFragmentManager();
                manager.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference)preference;
            String summary = (String)newValue;
            CharSequence[] entries = listPreference.getEntries();
            int index = listPreference.findIndexOfValue(summary);

            if(index != -1) {
                preference.setSummary(entries[index]);
                return true;
            }
        }

        return false;
    }
}
