package me.jimmywang.icbluelight;

/**
 * This is a start up activity with logo loading screen.
 * When the main activity is ready this will launch main activity (login).
 * Notice this activity inflate nothing, it only has a style.
 *
 * Created by yanmingwang on 12/10/16.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

