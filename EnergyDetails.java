package de.ferienakademie.wonderfull;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import java.util.stream.Stream;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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
        schritte_p.append(Double.toString(curr_schritte));

        TextView pause = (TextView) findViewById(R.id.pause);
        pause.append("15" + " min");

        TextView zeit_p = (TextView) findViewById(R.id.zeit_p);
        zeit_p.append(Double.toString(curr_time_paused) + " min");

        this.setTitle(String.format(energy_title, energy_level));

    }

    protected ArrayList<Integer> eval_velocity(ActivityTracker stat, ArrayList<Integer> last_ten_velos, float vHRef, float vARef, float vDRef) {
        double vH = stat.getDistanceH() / stat.getTimeH();
        double vA = stat.getDistanceA() / stat.getTimeA();
        double vD = stat.getDistanceD() / stat.getTimeD();
        double percH = vH / vHRef * 100;
        double percA = vA / vARef * 100;
        double percD = vD / vDRef * 100;
        double meanPerc = percH * (1/3) + percA * 0.5 + percD * (1/6);
        if(meanPerc > 115) {
            last_ten_velos.add(1);
        } else {
            last_ten_velos.add(0);
        }

        if(last_ten_velos.size() > 9) {
            last_ten_velos.remove(0);
        }

        return last_ten_velos;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected boolean is_pause(Stream<ActivityTracker.Activity> act) {
        long pauses = act.filter(v -> v == ActivityTracker.Activity.Pause).count();
        if(pauses > 0){
            return true;
        }
        return false;
    }

    @TargetApi(24)
    protected void eval_curr_window(double[] baro_win, double[] baro_time, double[] long_win, double[] lat_win, double[] gps_time) {
        ActivityTracker tracker = new ActivityTracker(300);
        ActivityTracker.WindowStats stat = tracker.process(baro_win, baro_time, long_win, lat_win, gps_time);
        //curr_schritte = tracker.; TODO:
        curr_strecke = tracker.getDistanceH();
        curr_altitude = tracker.getDistanceA();
        curr_time_paused = tracker.getTimeP();
        ArrayList<Integer> last_ten_velos = new ArrayList<Integer>();
        last_ten_velos = eval_velocity(tracker, last_ten_velos, 4000, 400, 500);
        for(int i = 0; i < 10; i++) {
            if(last_ten_velos.get(i) == 1) {
                if (too_fast < 9) {
                    too_fast++;
                }
            } else if(too_fast > 0) {
                too_fast--;
            }
        }

        if(is_pause(tracker.streamActivities())) {
            too_fast = 0;
            last_ten_velos = new ArrayList<Integer>();
        }

        if(too_fast == 9) {
            recommendedPause = true;
        }

        energy_level = (too_fast+1) * 100;
    }

}