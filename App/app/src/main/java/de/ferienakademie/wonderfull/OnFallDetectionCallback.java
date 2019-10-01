package de.ferienakademie.wonderfull;

public interface OnFallDetectionCallback {

    void onFallDetected(double timestamp);

    void onNewHeightData(double timestamp, double height);
}
