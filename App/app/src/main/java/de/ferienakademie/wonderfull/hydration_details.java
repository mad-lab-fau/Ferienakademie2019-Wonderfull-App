package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class hydration_details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration_details);

        TextView verlust_p = (TextView) findViewById(R.id.verlust_p);
        verlust_p.append("gering");

        TextView trinken_p = (TextView) findViewById(R.id.trinken_p);
        trinken_p.append("1,5" + " L");

        TextView zeitTrinken_p = (TextView) findViewById(R.id.zeitTrinken_p);
        zeitTrinken_p.append("42" + " min");

        TextView TRINKEN = (TextView) findViewById(R.id.TRINKEN);
        TRINKEN.append(" " + "350" + " mL");
    }


}
