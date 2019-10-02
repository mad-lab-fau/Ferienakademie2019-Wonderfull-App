package de.ferienakademie.wonderfull;

import android.annotation.TargetApi;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class LocationSampler implements LocationListener
{
    public class Samples
    {
        public double[] longitude;
        public double[] latitude;
        public double[] time;
    }

    private Object lock = new Object();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> times = new ArrayList<>();

    @TargetApi(24)
    public Samples extractSamples()
    {
        synchronized (lock)
        {
            Samples samples = new Samples();
            samples.longitude = longitudes.stream().mapToDouble(box -> box.doubleValue()).toArray();
            samples.latitude = latitudes.stream().mapToDouble(box -> box.doubleValue()).toArray();
            samples.time = times.stream().mapToDouble(box -> box.doubleValue()).toArray();

            longitudes.clear();
            latitudes.clear();
            times.clear();

            return samples;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (lock)
        {
            double time = System.currentTimeMillis() / 1000.0;
            double lon = location.getLongitude();
            double lat = location.getLatitude();

            longitudes.add(lon);
            latitudes.add(lat);
            times.add(time);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
