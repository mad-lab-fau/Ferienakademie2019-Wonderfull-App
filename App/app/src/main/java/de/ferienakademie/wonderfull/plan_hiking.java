package de.ferienakademie.wonderfull;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Bundle;

public class plan_hiking extends AppCompatActivity {



    private Integer gesamtStrecke = 15;
    private Integer hoch = 600;
    private Integer runter = 400;
    private TextView unitText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_hiking);

    }


    // getter and setter
    public void setGesamtStrecke(){
        EditText total = findViewById(R.id.streckenLaenge);
        gesamtStrecke = Integer.parseInt(total.getText().toString());
    }

    public int getGesamtStrecke(){
        return gesamtStrecke;
    }

    public void setHoch(){
        EditText nachOben = findViewById(R.id.nach_oben);
        hoch=Integer.parseInt(nachOben.getText().toString());
    }

    public int getHoch(){
        return hoch;
    }

    public void setRunter(){
        EditText nachUnten = findViewById(R.id.nach_unten);
        runter=Integer.parseInt(nachUnten.getText().toString());
    }

    public int getRunter(){
        return runter;
    }

    // helper methods
    int hikingTime;
    public void getTime(View v){
        setGesamtStrecke();
        setHoch();
        setRunter();
        double hikingTimeUp;
        double hikingTimeFlat;
        double hikingTimeDown;
        hikingTimeUp =(getHoch()/400)*60;
        hikingTimeDown = (getRunter()/800)*60;
        hikingTimeFlat = (getGesamtStrecke()/4)*60;
        if(hikingTimeUp >= hikingTimeDown){
            hikingTime = (int) (hikingTimeFlat + hikingTimeUp + 0.5* hikingTimeDown);
        } else{hikingTime = (int) (hikingTimeFlat + hikingTimeDown + 0.5*hikingTimeUp);};
        getUnits();
    }

    public void getUnits(){
        int hikingHours = hikingTime/60;
        int hikingMinutes = hikingTime%60;
        unitText = (TextView) findViewById(R.id.zeit_Einheit);
        unitText.setText(Integer.toString(hikingHours) + " h  " + Integer.toString(hikingMinutes)+ " min");
    }

}



