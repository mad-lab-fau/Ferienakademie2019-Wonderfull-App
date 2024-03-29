package de.ferienakademie.wonderfull;

import android.annotation.TargetApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class StepCounter
{
    public static double DEFAULT_MIN_DELTA_TIME = 0.2;
    public static double DEFAULT_MAX_DELTA_TIME = 5.0;

    private boolean hasProcessedBefore;
    private double maxThresh;
    private double minThresh;
    private double lastStepTime;
    private double lastTime;
    private double lastAcceleration;
    private final double minDeltaTime;
    private final double maxDeltaTime;
    private final SumFilter filter;

    private ArrayList<Double> stepHistory;

    
    static private double normalize(double accelerationX, double accelerationY, double accelerationZ)
    {
        return Math.sqrt(accelerationX * accelerationX
                       + accelerationY * accelerationY
                       + accelerationZ * accelerationZ);
    }

    public StepCounter(double minDeltaTime, double maxDeltaTime, int filterSize)
    {
        this.filter = new SumFilter(filterSize);
        this.minDeltaTime = minDeltaTime;
        this.maxDeltaTime = maxDeltaTime;
        this.lastAcceleration = this.getDynamicThreshold();

        this.stepHistory = new ArrayList<>();
        hasProcessedBefore = false;
    }
    
    public StepCounter()
    {
        this(DEFAULT_MIN_DELTA_TIME, DEFAULT_MAX_DELTA_TIME, SumFilter.DEFAULT_SIZE);
    }

    public double getMinThreshold() { return this.minThresh; }
    public double getDynamicThreshold() { return 0.5 * (this.maxThresh + this.minThresh); }
    public double getMaxThreshold() { return this.maxThresh; }
    public int getStepCount() { return stepHistory.size(); }

    @TargetApi(24)
    public double[] process(double[] accelerationX, double[] accelerationY, double[] accelerationZ, double[] accelerationTime)
    {
        // assert accelerationX.length == accelerationY.length ... == time.length

        // prepend last time to time
        double[] time;
        {
            time = new double[accelerationTime.length + 1];
            time[0] = this.lastTime;
            System.arraycopy(accelerationTime, 0, time, 1, accelerationTime.length);
        }

        // normalize and filter and prepend last (normalized) acceleration
        double[] data = DoubleStream.concat
                   (
                       DoubleStream.of(this.lastAcceleration),
                       IntStream.iterate(0, i -> i + 1)
                                .limit(accelerationX.length)
                                .mapToDouble(i -> filter.filter(normalize(accelerationX[i], accelerationY[i], accelerationZ[i])))
                   ).toArray();

        Point2D minmax = Arrays.stream(data)
                           .skip(2) // skip lastAcceleration (now preprended to data) and first data value (will be initial value for reduction)
                           .parallel()
                           .boxed()
                           .reduce
                           (
                                new Point2D(data[1], data[1]),
                                (acc, box) -> new Point2D(Math.min(acc.x, box.doubleValue()), Math.max(acc.y, box.doubleValue())),
                                (a, b) -> new Point2D(Math.min(a.x, b.x), Math.max(a.y, b.y))
                           );

        double[] steps = DoubleStream.concat
                    (
                        DoubleStream.of(this.lastStepTime), // prepend last step to steps
                        IntStream.iterate(0, i -> i + 1)
                                 .limit(data.length - 1)
                                 .parallel()
                                 .filter(i -> data[i] > data[i + 1] 
                                           && data[i] >= this.getDynamicThreshold() 
                                           && data[i + 1] < this.getDynamicThreshold())
                                 .mapToDouble(i -> 0.5 * (time[i] + time[i + 1])) // approximate step time with midpoint of time before and after threshold-crossing
                    ).toArray();
                
        double[] stepDeltas = IntStream.iterate(0, i -> i + 1)
                                  .limit(steps.length - 1)
                                  .parallel()
                                  .mapToDouble(i -> Math.abs(steps[i + 1] - steps[i]))
                                  .toArray();

        double[] stepTimes = IntStream.iterate(0, i -> i + 1)
                                 .limit(stepDeltas.length)
                                 .parallel()
                                 .filter(i -> this.minDeltaTime < stepDeltas[i] && stepDeltas[i] < this.maxDeltaTime)
                                 .mapToDouble(i -> steps[i + 1])
                                 .toArray();

        this.minThresh = minmax.x;
        this.maxThresh = minmax.y;
        this.lastTime = time[time.length - 1];
        this.lastAcceleration = data[data.length - 1];

        if (hasProcessedBefore && stepTimes.length > 0)
        {
            this.lastStepTime = stepTimes[stepTimes.length - 1];

            for (double stepTime: stepTimes)
            {
                stepHistory.add(stepTime);
            }
        }
        else if (!hasProcessedBefore)
        {
            lastStepTime = lastTime;
        }

        hasProcessedBefore = true;
        return stepTimes;
    }
}