package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton playClick;
        playClick = findViewById(R.id.ImageButton_start);
        playClick.setOnClickListener(this);

        playClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
            }
        });

        ImageButton planClick;
        planClick = findViewById(R.id.ImageButton_plan);
        planClick.setOnClickListener(this);

        planClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, plan_hiking.class));
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case (R.id.menu_emergency):
                Log.d("MainMenu", "menu_emergency");
                Intent mainIntent = new Intent(this, EmergencyActivity.class);
                startActivity(mainIntent);
                return true;
            default:
                return false;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.ImageButton_start:
                //code what should happen
                break;
        }
    }

}
