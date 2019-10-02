import java.util.Arrays;
import java.lang.Math;
import java.awt.geom.Point2D;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.DoubleStream;
import java.util.ArrayList;

public class ActivityTracker
{
    public enum Activity
    {
        Ascending,
        Descending,
        Horizontal,
        Pause
    }

    public class WindowStats
    {
        public Activity activity;
        public double start;
        public double end;
        public double time;
        public double distanceA;
        public double distanceD;
        public double distanceH;
    }

    public static double SLOPE_THRESHOLD_ASCENDING = 2.0;
    public static double SLOPE_THRESHOLD_DESCENDING = -2.0;
    public static double DISTANCE_THRESHOLD_PAUSE = 10.0;

    private Activity lastActivity;
    private double lastAltitude;
    private double lastLongitude;
    private double lastLatitude;
    private double lastGpsTime;
    private double lastTime;
    private SumFilter filter;
    private boolean hasProcessedBefore;
    private double distanceA;
    private double distanceD;
    private double distanceH;

    private double accumulatedTimeA;
    private double accumulatedTimeD;
    private double accumulatedTimeH;
    private double accumulatedTimePause;

    private ArrayList<Activity> activities;
    private ArrayList<Double> distancesA;
    private ArrayList<Double> distancesD;
    private ArrayList<Double> distancesH; 

    private static double computeHaversineDistance(double firstLongitude, double firstLatitude, double secondLongitude, double secondLatitude)
    {
        firstLongitude = Math.toRadians(firstLongitude);
        firstLatitude = Math.toRadians(firstLatitude);
        secondLongitude = Math.toRadians(secondLongitude);
        secondLatitude = Math.toRadians(secondLatitude);

        double deltaLongitude = secondLongitude - secondLongitude;
        double deltaLatitude = secondLatitude - firstLatitude;

        double a = Math.pow(Math.sin(deltaLatitude * 0.5), 2)
                 + Math.cos(firstLatitude) * Math.cos(secondLatitude)
                 * Math.pow(Math.sin(deltaLongitude), 2);
        double c = 2.0 * Math.asin(Math.sqrt(a));
        double r = 6371;
        return c * r * 1000;
    }

    private static double computeAltitude(double baro)
    {
        return 44330 * (1.0 - Math.pow(baro / 1013.0, 0.1903));
    }

    public ActivityTracker(int filterSize)
    {
        filter = new SumFilter(filterSize);
        hasProcessedBefore = false;

        accumulatedTimeA = 0;
        accumulatedTimeD = 0;
        accumulatedTimeH = 0;
        accumulatedTimePause = 0;

        activities = new ArrayList<Activity>();
        distancesA = new ArrayList<Double>();
        distancesD = new ArrayList<Double>();
        distancesH = new ArrayList<Double>(); 
    }

    public double getTimeA() { return accumulatedTimeA; }
    public double getTimeD() { return accumulatedTimeD; }
    public double getTimeH() { return accumulatedTimeH; }
    public double getTimeP() { return accumulatedTimePause; }
    public double getDistanceA() { return distancesA.parallelStream().mapToDouble(box -> box.doubleValue()).sum(); }
    public double getDistanceD() { return distancesD.parallelStream().mapToDouble(box -> box.doubleValue()).sum(); }
    public double getDistanceH() { return distancesH.parallelStream().mapToDouble(box -> box.doubleValue()).sum(); }

    public Stream<Activity> streamActivities() { return activities.stream(); }
    public Stream<Double> streamDistancesA() { return distancesA.stream(); }
    public Stream<Double> streamDistancesD() { return distancesD.stream(); }
    public Stream<Double> streamDistancesH() { return distancesH.stream(); }

    private double computeHorizontalDistance(double[] longitude, double[] latitude, double[] gpsTime, double newAltitudeDelta, double oldAltitudeDelta, double windowTime)
    {
        if (longitude.length > 0)
        {
            var distance = IntStream.iterate(0, i -> i + 1)
                                    .limit(longitude.length - 1)
                                    .parallel()
                                    .mapToDouble(i -> computeHaversineDistance(longitude[i], latitude[i], longitude[i + 1], latitude[i + 1]))
                                    .sum();
            if (hasProcessedBefore)
            {
                distance += computeHaversineDistance(lastLongitude, lastLatitude, longitude[0], latitude[0]);
            }

            return Math.sqrt(distance * distance - Math.pow(oldAltitudeDelta - newAltitudeDelta, 2));
        }
        else
        {
            return distanceH;
        }
    }

    private Point2D.Double computeVerticalDistances(double[] altitude)
    {
        double[] altitudes;
        if (!hasProcessedBefore)
        {
            altitudes = new double[altitude.length + 1];
            altitudes[0] = lastAltitude;
            System.arraycopy(altitude, 0, altitudes, 1, altitude.length);
        }
        else
        {
            altitudes = altitude;
        }

        var deltaAltitude = IntStream.iterate(0, i -> i + 1)
                                     .limit(altitudes.length - 1)
                                     .parallel()
                                     .mapToDouble(i -> altitudes[i + 1] - altitudes[i])
                                     .toArray();

        double down = -Arrays.stream(deltaAltitude)
                             .parallel()
                             .filter(delta -> delta < 0)
                             .sum();
        double up = Arrays.stream(deltaAltitude)
                          .parallel()
                          .filter(delta -> delta > 0)
                          .sum();

        return new Point2D.Double(down, up);
    }

    public WindowStats process(double[] baro, double[] baroTime, double[] longitude, double[] latitude, double[] gpsTime)
    {
        var altitude = Arrays.stream(baro)
                             .map(value -> computeAltitude(filter.filter(value)))
                             .toArray();

        double oldAltitudeDelta = hasProcessedBefore ? distanceA - distanceD : 0.0;

        var verticalDistances = computeVerticalDistances(altitude);
        distanceD = verticalDistances.x;
        distanceA = verticalDistances.y;
        double newAltitudeDelta = distanceA - distanceD;
        double windowTime = baroTime[baroTime.length - 1] - baroTime[0];

        distanceH = computeHorizontalDistance(longitude, latitude, gpsTime, newAltitudeDelta, oldAltitudeDelta, windowTime);

        double slope = 0;
        if (distanceH > 0)
        {
            slope = newAltitudeDelta / distanceH * 100;
        }

        if (distanceH > DISTANCE_THRESHOLD_PAUSE)
        {
            if (slope > SLOPE_THRESHOLD_ASCENDING)
            {
                lastActivity = Activity.Ascending;
                accumulatedTimeA += windowTime;
            }
            else if (slope < SLOPE_THRESHOLD_DESCENDING)
            {
                lastActivity = Activity.Descending;
                accumulatedTimeD += windowTime;
            }
            else
            {
                lastActivity = Activity.Horizontal;
                accumulatedTimeH += windowTime;
            }
        }
        else
        {
            lastActivity = Activity.Pause;
            accumulatedTimeH += windowTime;
            accumulatedTimePause += windowTime;
        }

        lastAltitude = altitude[altitude.length - 1];
        lastTime = baroTime[baroTime.length - 1];
        if (longitude.length > 0)
        {
            lastLongitude = longitude[longitude.length - 1];
            lastLatitude = latitude[latitude.length - 1];
            lastGpsTime = gpsTime[gpsTime.length - 1];
        }

        activities.add(lastActivity);
        distancesA.add(distanceA);
        distancesD.add(distanceD);
        distancesH.add(distanceH);

        hasProcessedBefore = true;

        var stats = new WindowStats();
        stats.activity = lastActivity;
        stats.start = baroTime[0];
        stats.end = lastTime;
        stats.time = windowTime;
        stats.distanceA = distanceA;
        stats.distanceD = distanceD;
        stats.distanceH = distanceH;
        return stats;
    }
}