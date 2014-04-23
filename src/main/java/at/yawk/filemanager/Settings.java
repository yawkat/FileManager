package at.yawk.filemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author Jonas Konrad (yawkat)
 */
public class Settings extends PreferenceActivity implements Constants {
    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.xml.prefs);

        ListPreference sorter = (ListPreference) findPreference("sorter.btn");
        sorter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preferences.edit().putInt(PREFERENCE_SORTER, Integer.parseInt(newValue.toString())).commit();
                return true;
            }
        });
        int sorterId = preferences.getInt(PREFERENCE_SORTER, SORTER_NAME_INTELLIGENT);
        sorter.setValue(String.valueOf(sorterId));
    }
}
