package de.ferienakademie.wonderfull;

public interface ActivityTrackingCallback
{
    void onActivity(ActivityTracker.WindowStats stats, ActivityTracker tracker);
}
