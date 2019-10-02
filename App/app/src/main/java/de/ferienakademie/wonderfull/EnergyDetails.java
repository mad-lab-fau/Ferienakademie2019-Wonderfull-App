package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.TextView;

public class EnergyDetails extends AppCompatActivity {

    private String energy_title = "Energie-Level: %s";
    private int too_fast = 0;
    public boolean recommendedPause = false;
    private double curr_altitude = 0;
    private double curr_strecke = 0;
    private double curr_schritte = 0;
    private double energy_level = 0;
    private double curr_time_paused = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_details);

        /*TextView herzfrequenz_p = (TextView) findViewById(R.id.herzfrequenz_p);
        herzfrequenz_p.append(" " + "80" + " Hz"); */

        TextView hoehenmeter_p = (TextView) findViewById(R.id.hoehenmeter_p);
        hoehenmeter_p.append(Double.toString(curr_altitude) + " m");

        TextView strecke_p = (TextView) findViewById(R.id.strecke_p);
        strecke_p.append(Double.toString(curr_strecke) + " km");

        TextView schritte_p = (TextView) findViewById(R.id.schritte_p);
        schritte_p.append(curr_schritte);

        TextView pause = (TextView) findViewById(R.id.pause);
        pause.append("15" + " min");

        TextView zeit_p = (TextView) findViewById(R.id.zeit_p);
        zeit_p.append(Double.toString(curr_time_paused) + " min");

        this.setTitle(String.format(energy_title, energy_level));

    }

    protected ArrayList<Integer> eval_velocity(WindowStats stat, ArrayList<Integer> last_ten_velos, float vHRef, float vARef, float vDRef) {
        float vH = stat.distanceH / stat.time; 
        float vA = stat.distanceA / stat.time; 
        float vD = stat.distanceD / stat.time;
        float percH = vH / vHRef * 100;
        float percA = vA / vARef * 100;
        float percD = vD / vDRef * 100;
        float meanPerc = percH * (1/3) + percA * 0.5 + percD * (1/6);
        if(meanPerc > 115) {
            last_ten_velos.append(1);
        } else {
            last_ten_velos.append(0);
        }

        if(last_ten_velos.size() > 9) {
            last_ten_velos.remove(0);
        } 
        
        return last_ten_velos;

    }

    protected void eval_curr_window(ArrayList<Float> baro_win, ArrayList<Float> baro_time, ArrayList<Float> gps_win, ArrayList<Float> gps_time) {
        ActivityTracker tracker = new ActivityTracker();
        WindowStats stat = tracker.process(baro_win, baro_time, gps_win, gps_time);
        //curr_schritte = tracker.; TODO:
        curr_strecke = tracker.getDistanceH();
        curr_altitude = tracker.getDistanceA();
        curr_time_paused = tracker.getTimeP();
        ArrayList<Integer> last_ten_velos = new ArrayList<Integer>();
        last_ten_velos = eval_velocity(stat, last_ten_velos, 4000, 400, 500);
        for(int i = 0; i < 10; i++) {
            if(last_ten_velos(i) == 1) {
                if (too_fast < 9) {
                    too_fast++;
                }
            } else if(too_fast > 0) {
                too_fast--;
            }
        }

        if(is_pause) {
            too_fast = 0;
            last_ten_velos = new ArrayList<Integer>();
        }

        if(too_fast == 9) {
            recommendedPause = true;
        }

        energy_level = (too_fast+1) * 100;
    }

}
