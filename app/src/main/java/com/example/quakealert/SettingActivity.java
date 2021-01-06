package com.example.quakealert;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_main);
        Button filterBtn = findViewById(R.id.filterButton);

        filterBtn.setOnClickListener(v -> {
            Intent i = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(i);
        });
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_main);

        }
    }
}


//public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.preference_main);
//
//        Preference min_mag = findPreference(getString(R.string.settings_min_magnitude_key));
//        Preference no_of_quake = findPreference(getString(R.string.settings_no_of_earthquake_key));
//        Preference orderBy = findPreference(getString(R.string.settings_order_by_label));
//        bindPreferenceSummaryToValue(min_mag);
//        bindPreferenceSummaryToValue(no_of_quake);
//        bindPreferenceSummaryToValue(orderBy);
//    }
//
//    @Override
//    public boolean onPreferenceChange(Preference preference, Object newValue) {
//        String value = newValue.toString();
//        preference.setSummary(value);
//        return true;
//    }
//
//    private void bindPreferenceSummaryToValue(Preference preference) {
//        preference.setOnPreferenceChangeListener(this);
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
//        String preferenceString = preferences.getString(preference.getKey(), "");
//        onPreferenceChange(preference, preferenceString);
//    }
//}