package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class EnergyDetails extends AppCompatActivity {

    private String energy_title = "Energie-Level: %s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_details);

        TextView herzfrequenz_p = (TextView) findViewById(R.id.herzfrequenz_p);
        herzfrequenz_p.append(" " + "80" + " Hz");

        TextView hoehenmeter_p = (TextView) findViewById(R.id.hoehenmeter_p);
        hoehenmeter_p.append(" " + "80" + " mBar");


        TextView strecke_p = (TextView) findViewById(R.id.strecke_p);
        strecke_p.append(" " + "2,5" + " km");

        TextView schritte_p = (TextView) findViewById(R.id.schritte_p);
        schritte_p.append(" " + "1042");

        TextView pause = (TextView) findViewById(R.id.pause);
        pause.append(" " + "15" + " min");

        /*
        TextView zeit_p = (TextView) findViewById(R.id.zeit_p);
        zeit_p.append(" " + "50" + " min"); */

        this.setTitle(String.format(energy_title, "45%"));

    }
}
