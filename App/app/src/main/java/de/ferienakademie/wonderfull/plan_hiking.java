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
import android.widget.Toast;

public class plan_hiking extends AppCompatActivity {



    private double gesamtStrecke = 15;
    private double hoch = 600;
    private double runter = 400;
    private TextView unitText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_hiking);
        this.setTitle(getResources().getString(R.string.wanderung_planen));
    }



    // getter and setter
    public void setGesamtStrecke(EditText total){
        String totalString = total.getText().toString();
            gesamtStrecke = Double.parseDouble(totalString);
    }

    public double getGesamtStrecke(){
        return gesamtStrecke;
    }

    public void setHoch(EditText nachOben){
        String nachObenString = nachOben.getText().toString();
            hoch=Double.parseDouble(nachObenString);
    }

    public double getHoch(){
        return hoch;
    }

    public void setRunter(EditText nachUnten){
        String nachUntenString = nachUnten.getText().toString();
            runter=Double.parseDouble(nachUntenString);
    }

    public double getRunter(){
        return runter;
    }

    // helper methods
    int hikingTime;
    public void getTime(View v){
        EditText total = findViewById(R.id.streckenLaenge);
        EditText nachOben = findViewById(R.id.nach_oben);
        EditText nachUnten = findViewById(R.id.nach_unten);
        if(checkInput(total, nachOben, nachUnten)){
            Toast.makeText(getApplicationContext(), getString(R.string.plan_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        setGesamtStrecke(total);
        setHoch(nachOben);
        setRunter(nachUnten);
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
        unitText = (TextView) findViewById(R.id.zeit_Einheit);
        //if(check == false){
         //   unitText.setText("bitte Zahlen eingeben!");
        //}
        int hikingHours = hikingTime/60;
        int hikingMinutes = hikingTime%60;
        unitText.setText(Integer.toString(hikingHours) + " h  " + Integer.toString(hikingMinutes)+ " min");
    }

    public boolean checkInput(EditText a, EditText b, EditText c){
        String s = a.getText().toString();
        String v = b.getText().toString();
        String w = c.getText().toString();
        if(s.isEmpty() || v.isEmpty() || w.isEmpty()) {
            return true;  // positive oder neg ganze Zahl
        }
        return false;
    }




}



