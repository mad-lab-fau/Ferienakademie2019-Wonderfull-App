package de.ferienakademie.wonderfull;

import android.annotation.TargetApi;

import java.util.Arrays;

public class SumFilter
{
    public static int DEFAULT_SIZE = 4;

    private final double[] buffer;
    private int index;
    private final int size;
    private int usedCapacity;

    public SumFilter(int size)
    {
        this.size = size;
        this.buffer = new double[size];
        this.index = 0;
        this.usedCapacity = 0;
    }

    public SumFilter()
    {
        this(DEFAULT_SIZE);
    }

    @TargetApi(24)
    public double filter(double sample)
    {
        if (this.usedCapacity < this.size)
        {
            this.buffer[this.usedCapacity++] = sample;
            return Arrays.stream(this.buffer)
                         .limit(this.usedCapacity)
                         .sum() / (double)this.usedCapacity;
        }
        else
        {
            this.buffer[this.index] = sample;
            this.index = (this.index + 1) % this.size;
            return Arrays.stream(this.buffer)
                         .sum() / (double)this.size;
        }
    }
}