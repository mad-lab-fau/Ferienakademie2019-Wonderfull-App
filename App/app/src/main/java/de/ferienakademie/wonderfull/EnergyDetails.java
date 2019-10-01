package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class EnergyDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_details);

        TextView herzfrequenz = (TextView) findViewById(R.id.herzfrequenz);
        herzfrequenz.append(" " + "80" + "hz");

        TextView hoehenmeter = (TextView) findViewById(R.id.hoehenmeter);
        hoehenmeter.append(" " + "80" + "mBar");

        TextView strecke = (TextView) findViewById(R.id.strecke);
        strecke.append(" " + "2,5" + "km");

        TextView zeit = (TextView) findViewById(R.id.zeit);
        zeit.append(" " + "50" + "min");

        TextView schritte = (TextView) findViewById(R.id.schritte);
        schritte.append(" " + "1042");

        TextView pause = (TextView) findViewById(R.id.pause);
        pause.append(" " + "15" + "min");

    }
}
