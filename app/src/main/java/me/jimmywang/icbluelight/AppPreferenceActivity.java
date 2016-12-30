package me.jimmywang.icbluelight;
/**
 * This is a preference activity
 * Created by yanmingwang on 12/10/16.
 */

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import static me.jimmywang.icbluelight.LoginActivity.PREFS_NAME;

public class AppPreferenceActivity extends android.preference.PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            PreferenceManager manager = getPreferenceManager();
            //Use my own prefs name
            manager.setSharedPreferencesName(PREFS_NAME);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
