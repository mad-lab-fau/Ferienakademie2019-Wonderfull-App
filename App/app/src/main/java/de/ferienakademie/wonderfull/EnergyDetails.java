package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import de.ferienakademie.wonderfull.service.BleService;

public class EnergyDetails extends AppCompatActivity {

    private static boolean isTooFast
    (
            double distanceH, double distanceD, double distanceA,
            double timeH, double timeD, double timeA,
            double referenceVelocityH, double referenceVelocityD, double referenceVelocityA
    )
    {
        double velocityH = distanceH / timeH;
        double velocityD = distanceD / timeD;
        double velocityA = distanceA / timeA;

        double scoreH = velocityH / referenceVelocityH * 100;
        double scoreD = velocityD / referenceVelocityD * 100;
        double scoreA = velocityA / referenceVelocityA * 100;

        double score = scoreH / 3 + scoreA / 2 + scoreD / 6;
        return score > 115;
    }

    private StepCountCallback stepCallback = counter ->
    {
        TextView schritte_p = (TextView) findViewById(R.id.schritte_p);
        schritte_p.setText(Integer.toString(counter.getStepCount()));
    };

    private ActivityTracker.WindowStats[] lastWindows = new ActivityTracker.WindowStats[60];
    private int windowSize = 0;
    private int windowIndex = 0;

    private boolean[] speedEvaluations = new boolean[10];
    private int speedEvaluationSize = 0;
    private int speedEvaluationIndex = 0;

    @TargetApi(24)
    private Stream<ActivityTracker.WindowStats> getActivityWindow()
    {
        return windowSize >= lastWindows.length ? Arrays.stream(lastWindows) : Arrays.stream(lastWindows).limit(windowSize);
    }

    @TargetApi(24)
    private ActivityTrackingCallback activityCallback = (stats, tracker) ->
    {
        TextView hoehenmeter_p = (TextView) findViewById(R.id.hoehenmeter_p);
        hoehenmeter_p.setText(String.format("%.0f m",tracker.getDistanceA() - tracker.getDistanceD()));

        double distance = tracker.getDistanceH();
        TextView strecke_p = (TextView) findViewById(R.id.strecke_p);
        strecke_p.setText(String.format("%.0f %s", distance > 1000 ? distance / 1000 : distance, distance > 1000 ? "km" : "m"));

        double time = tracker.getTimeH() + tracker.getDistanceD() + tracker.getTimeA();
        TextView zeit_p = (TextView) findViewById(R.id.zeit_p);
        if (time > 60 * 60)
        {
            zeit_p.setText(String.format("%.0f h %.0f min", time / 3600, (time / 60) % 60 ));
        }
        else
        {
            zeit_p.setText(String.format("%.0f min", time / 60));
        }

        lastWindows[windowIndex] = stats;
        ++windowSize;

        if (++windowIndex == lastWindows.length)
        {
            double distanceH = getActivityWindow().mapToDouble(s -> s.distanceH).average().getAsDouble();
            double distanceD = getActivityWindow().mapToDouble(s -> s.distanceD).average().getAsDouble();
            double distanceA = getActivityWindow().mapToDouble(s -> s.distanceA).average().getAsDouble();

            double timeH = getActivityWindow().mapToDouble(s -> s.activity == ActivityTracker.Activity.Horizontal ? s.time : 0.0).average().getAsDouble();
            double timeD = getActivityWindow().mapToDouble(s -> s.activity == ActivityTracker.Activity.Descending ? s.time : 0.0).average().getAsDouble();
            double timeA = getActivityWindow().mapToDouble(s -> s.activity == ActivityTracker.Activity.Ascending ? s.time : 0.0).average().getAsDouble();

            double referenceH = 1.0;
            double referenceD = 1.0;
            double referenceA = 1.0; //TODO
            boolean wasTooFast = isTooFast(distanceH, distanceD, distanceA, timeH, timeD, timeA, referenceH, referenceD, referenceA);

            speedEvaluations[speedEvaluationIndex++] = wasTooFast;

            int count = 0;
            for (int i = 0; i < Math.min(speedEvaluationSize, speedEvaluations.length); i++)
            {
                if (speedEvaluations[i]) ++count;
            }

            this.setTitle(getResources().getString(R.string.energyLevel_headline) + ": " + 10 * (9 - count) + "%");

            windowIndex = 0;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_details);

        String energy_level = getIntent().getStringExtra("ENERGY_LEVEL");

        /*TextView herzfrequenz_p = (TextView) findViewById(R.id.herzfrequenz_p);
        herzfrequenz_p.append(" " + "80" + " Hz"); */

        TextView hoehenmeter_p = (TextView) findViewById(R.id.hoehenmeter_p);
        hoehenmeter_p.append("80" + " m");

        TextView strecke_p = (TextView) findViewById(R.id.strecke_p);
        strecke_p.append("\n2,5" + " km");

        TextView schritte_p = (TextView) findViewById(R.id.schritte_p);
        schritte_p.append("1042");

        TextView pause = (TextView) findViewById(R.id.pause);
        pause.append(" " + "15" + " min");

        TextView zeit_p = (TextView) findViewById(R.id.zeit_p);
        zeit_p.append("50" + " min");

        this.setTitle(getResources().getString(R.string.energyLevel_headline) + ": " + energy_level + "%");
    }

    @Override
    protected void onResume() {
        super.onResume();

        BleService.registerStepCounterCallback(stepCallback);
        BleService.registerActivityTrackingCallback(activityCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService.deregisterStepCounterCallback(stepCallback);
        BleService.deregisterActivityTrackingCallback(activityCallback);
    }
}
