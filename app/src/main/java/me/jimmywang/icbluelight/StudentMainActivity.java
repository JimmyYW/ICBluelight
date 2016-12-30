package me.jimmywang.icbluelight;

/**
 * This is the main page a student user will see. This activity will dynamically manage (load and unload) fragments to
 * show context base on user behavior.
 *
 * Created by yanmingwang on 12/10/16.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.database.DatabaseReference;

import static me.jimmywang.icbluelight.LoginActivity.PREFS_NAME;

public class StudentMainActivity extends AppCompatActivity implements HelpUpdateFragment.onSafe, RequestHelpFragment.submit {
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get setting
        settings = getSharedPreferences(PREFS_NAME, 0);
        //set theme base on setting
        if(settings.getString("ThemeColor","Red").equals("Greentheme")){
            setTheme(R.style.Greentheme);
        }
        setContentView(R.layout.activity_studentmain);

        /**
         * The following block will check if user has created a help request before and never report as safe.
         * If so this will bypass create help steps and load the HelpUpdateFragment to resume last help session.
         * If not, continue with current activity setup
         */
        //check if there is a event id exist in preference
        if (settings.contains("evenId")){
            HelpUpdateFragment HelpUpdateFragment = new HelpUpdateFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HelpUpdateFragment)
                    .commit();

        }

        //setup UI elements and listeners
        Button requestHelp = (Button) findViewById(R.id.requestHelp);
        requestHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestHelpFragment StudentHelp = new RequestHelpFragment();

                StudentHelp.setArguments(getIntent().getExtras());

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, StudentHelp).commit();

            }
        });

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
    }


    /**
     * A callback interface from RequestHelpFragment. Will be called once user fill Info. This will start HelpUpdateFragment
     * and start location broadcast to database.
     *
     * @param MS Additional message user provided
     * @param DT Detailed location user provided
     */
    @Override
    public void onSubmit(String MS, String DT) {
        HelpUpdateFragment HelpUpdateFragment = new HelpUpdateFragment();
        Bundle args = new Bundle();
        args.putString("MS",MS);
        args.putString("DT",DT);
        HelpUpdateFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, HelpUpdateFragment)
                .commit();

    }

    /**
     * This is a callback interface from HelpUpdateFragment. This will be called user report as safe.
     * It will remove event id from preference and remove from database by onDestroyView calling event.delete
     * Also it will pop the fragment
     */

    @Override
    public void onSafe() {
        settings.edit().remove("evenId").apply();
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu menu
     * @return true
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, AppPreferenceActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
