package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class hydration_details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration_details);

        String drink_level = getIntent().getStringExtra("DRINK_STATUS");

        TextView verlust_p = (TextView) findViewById(R.id.verlust_p);
        verlust_p.append("gering");

        TextView trinken_p = (TextView) findViewById(R.id.trinken_p);
        trinken_p.append("1,5" + " L");

        TextView zeitTrinken_p = (TextView) findViewById(R.id.zeitTrinken_p);
        zeitTrinken_p.append("\n42" + " min");

        TextView TRINKEN = (TextView) findViewById(R.id.TRINKEN);
        TRINKEN.append(" " + "350" + " mL");

        this.setTitle(getResources().getString(R.string.drinklevel_headline) + ": " + drink_level + "%");
    }

}
