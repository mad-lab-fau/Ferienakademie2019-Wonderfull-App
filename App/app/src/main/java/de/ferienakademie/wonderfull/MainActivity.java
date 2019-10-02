package de.ferienakademie.wonderfull;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playClick;
        playClick = findViewById(R.id.start);
        playClick.setOnClickListener(this);

        playClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SensorActivity.class));
            }
        });

        Button planClick;
        planClick = findViewById(R.id.plan);
        planClick.setOnClickListener(this);

        planClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, plan_hiking.class));
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("EmergencyActivity", "Asking for permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_emergency):
                Intent mainIntent = new Intent(this, EmergencyActivity.class);
                startActivity(mainIntent);
                return true;
            case (R.id.menu_profile):
                Intent profilIntent = new Intent(this, profile.class);
                startActivity(profilIntent);
                return true;
            case (R.id.menu_hike):
                Intent hikeIntent = new Intent(this, HikeMainscreen.class);
                startActivity(hikeIntent);
                return true;
            case R.id.menu_sensor:
                startActivity(new Intent(this, SensorActivity.class));
                return true;
            case R.id.menu_plots:
                startActivity(new Intent(this, Graphen.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.start:
                //code what should happen
                break;
        }
    }

}
