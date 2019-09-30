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

   // private ImageButton calc;
    private Integer gesamtStrecke = 15;
    private Integer hoch = 600;
    //private Integer runter = 400;
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

    // helper methods
    int hikingTime;
    public void getTime(View v){
        setGesamtStrecke();
        setHoch();
        //int hikingTime;
        double hikingTimeUp;
        double hikingTimeFlat;
        hikingTimeUp =(getHoch()/400)*60;
        hikingTimeFlat = (getGesamtStrecke()/4)*60;
        if(hikingTimeUp >= hikingTimeFlat){
            hikingTime = (int) (hikingTimeUp + 0.5* hikingTimeFlat);
        } else{hikingTime = (int) (0.5*hikingTimeUp + hikingTimeFlat);};
        getUnits();
    }

    public void getUnits(){
        int hikingHours = hikingTime/60;
        int hikingMinutes = hikingTime%60;
        unitText = (TextView) findViewById(R.id.zeit_Einheit);
        unitText.setText(Integer.toString(hikingHours) + " h  " + Integer.toString(hikingMinutes)+ " min");
    }

}



