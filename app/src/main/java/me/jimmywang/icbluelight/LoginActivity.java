package me.jimmywang.icbluelight;

/**
 * This is a login activity. In this screen the app will ask user for location permission the first time.
 * Since this is a v0.1 prototype, no real login implemented. Instead, user will ask for name and select a role
 *
 * Created by yanmingwang on 12/10/16.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private Button buttonStudent;
    private Button buttonStuff;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private EditText temp;
    private TextView tempII;
    private static final int tempP =1;


    /**
     * On start this will ask for location permission
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we have permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, tempP);
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, tempP);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        //get setting
        settings = getSharedPreferences(PREFS_NAME, 0);

        //set layout and other UI elements
        setContentView(R.layout.activity_login);
        temp = (EditText) findViewById(R.id.username);
        tempII = (TextView) findViewById(R.id.enterNamePrompt);

        //Load username if in preference
        if(settings.contains("username")) {
            String nameTmep = settings.getString("username", "");
            temp.setText(nameTmep);
            tempII.setText(getResources().getString(R.string.welcome));
            tempII.append("\nWelcome back " + nameTmep +"!");
        }
        //set UI elements listeners
        buttonStudent = (Button) findViewById(R.id.buttonStudent);
        buttonStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAsStudent();
                getName();
            }
        });

        buttonStuff = (Button) findViewById(R.id.buttonStuff);
        buttonStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAsStaff();
                getName();

            }
        });

    }

    /**
     * get name from edittext and push to preference
     */
    private void getName(){
        if(!temp.getText().toString().equals(settings.getString("username", ""))){
            editor = settings.edit();
            editor.putString("username", temp.getText().toString());
            editor.commit();
        }
    }


    /**
     * start as student
     */
    private void startAsStudent(){
        Intent intent = new Intent(this, StudentMainActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * start as staff
     */
    private void startAsStaff(){
        Intent intent = new Intent(this, StaffMainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Handle user permission respond. If user deny permission, close app.
     *
     * @param requestCode requestCode
     * @param permissions permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case tempP: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the thing!

                } else {
                    this.finishAffinity();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
