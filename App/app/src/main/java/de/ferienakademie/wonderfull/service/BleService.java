/*
 * Copyright (C) 2018 Machine Learning and Data Analytics Lab, Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU).
 * <p>
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. If you reuse
 * this code you have to keep or cite this comment.
 */
package de.ferienakademie.wonderfull.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.fau.sensorlib.BleSensorManager;
import de.fau.sensorlib.SensorCallback;
import de.fau.sensorlib.SensorDataProcessor;
import de.fau.sensorlib.SensorException;
import de.fau.sensorlib.SensorFactory;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.enums.SensorMessage;
import de.fau.sensorlib.sensors.AbstractNilsPodSensor;
import de.fau.sensorlib.sensors.AbstractNilsPodSensor.NilsPodOperationState;
import de.fau.sensorlib.sensors.AbstractSensor;
import de.fau.sensorlib.sensors.NilsPodCallback;
import de.fau.sensorlib.sensors.NilsPodSensor;
import de.fau.sensorlib.sensors.Recordable;
import de.fau.sensorlib.sensors.Resettable;
import de.fau.sensorlib.sensors.logging.Session;
import de.fau.sensorlib.sensors.logging.SessionDownloader;
import de.ferienakademie.wonderfull.ActivityTracker;
import de.ferienakademie.wonderfull.ActivityTrackingCallback;
import de.ferienakademie.wonderfull.HeightChangeCallback;
import de.ferienakademie.wonderfull.LocationSampler;
import de.ferienakademie.wonderfull.OnFallDetectionCallback;
import de.ferienakademie.wonderfull.StepCountCallback;
import de.ferienakademie.wonderfull.StepCounter;

import static de.fau.sensorlib.sensors.NilsPodSensor.*;


/**
 * Background Service for handling Bluetooth Low Energy connection to Sensors
 */
public class BleService extends Service {

    private static final String TAG = BleService.class.getSimpleName();

    private static final int NOTIFICATION_UPDATE_INTERVAL = 5000;

    private IBinder mBinder = new BleServiceBinder();

    /**
     * Callbacks for communication Service -> Activity
     */
    private SensorCallback mSensorCallback;

    private NilsPodCallback mNilsPodActivityCallback;

    private OnFallDetectionCallback mFallDetectionCallback;

    /**
     * Handler for communication Activity -> Service
     */
    private ServiceHandler mServiceHandler = new ServiceHandler(this);

    private InternalHandler mInternalHandler;

    /**
     * List containing sensors that are currently attached to this Service
     */
    private ArrayList<AbstractSensor> mAttachedSensors = new ArrayList<>();

    private ConcurrentLinkedQueue<AbstractSensor> mConnectingQueue = new ConcurrentLinkedQueue<>();

    private double fs;
    private int window_size;


    private ArrayList<Double> baroBuffer = new ArrayList<>();
    private ArrayList<Double> acc_xBuffer = new ArrayList<>();
    private ArrayList<Double> acc_yBuffer = new ArrayList<>();
    private ArrayList<Double> acc_zBuffer = new ArrayList<>();

    public static void registerStepCounterCallback(StepCountCallback callback) {
        stepCountCallbacks.add(callback);
    }

    private static HashSet<StepCountCallback> stepCountCallbacks = new HashSet<>();

    public static boolean deregisterStepCounterCallback(StepCountCallback callback) {
        return stepCountCallbacks.remove(callback);
    }

    public static void registerHeightChangedCallback(HeightChangeCallback callback) {
        heightChangeCallbacks.add(callback);
    }

    public static boolean deregisterHeightChangedCallback(HeightChangeCallback callback) {
        return heightChangeCallbacks.remove(callback);
    }

    private static HashSet<HeightChangeCallback> heightChangeCallbacks = new HashSet<>();

    public static void registerActivityTrackingCallback(ActivityTrackingCallback callback) {
        activityTrackingCallbacks.add(callback);
    }

    public static boolean deregisterActivityTrackingCallback(ActivityTrackingCallback callback) {
        return activityTrackingCallbacks.remove(callback);
    }

    private static HashSet<ActivityTrackingCallback> activityTrackingCallbacks = new HashSet<>();

    private LocationSampler locationSampler;
    private StepCounter stepCounter = new StepCounter();
    private ActivityTracker activityTracker = new ActivityTracker(8);

    private ArrayList<Entry> heightHistory = new ArrayList<>();
    private int stepCounterWindowIndex = 0;
    private int stepCounterWindowSize;
    private double[] accBufferX;
    private double[] accBufferY;
    private double[] accBufferZ;
    private double[] timeBuffer;
    private double[] barometerBuffer;
    private int timeIndex = 0;

    private boolean fall = false;
    double height = 0;

    public boolean getFall() {
        return fall;
    }

    public void setFall(boolean answer) {
        fall = answer;
    }


    /**
     * Sensor Data Processor for receiving information about connection state changes and new data
     * from the sensor devices
     */
    private SensorDataProcessor mSensorDataProcessor = new SensorDataProcessor() {

        private int counter = 0;

        public double computeHeight(double baro) {
            return 44330 * (1.0 - (Math.pow((baro / 1013.0), 0.1903)));
        }

        public double computeMean(ArrayList<Double> buffer) {
            int zähler = 0;
            double mean = 0;
            for (int k = 0; k < buffer.size(); k++) {
                mean += buffer.get(k);
                zähler++;
            }

            mean = mean / zähler;
            return mean;
        }

        @TargetApi(24)
        @Override
        public void onNewData(SensorDataFrame data) {
            // TODO data from the sensor enters the service HERE! => here you can implement
            //  your algorithms (and pass the results to the activity in a similar way)
            NilsPodDataFrame df = (NilsPodDataFrame) data;

            fs = df.getOriginatingSensor().getSamplingRate();
            //Log.d("SensorActivty", "Fs: " + Double.toString(fs));
            window_size = (int) (8.2 * fs);
            stepCounterWindowSize = (int) (1.0 * fs);
            if (timeBuffer == null || stepCounterWindowSize != timeBuffer.length) {
                stepCounterWindowIndex = 0;
                timeBuffer = new double[stepCounterWindowSize];
                accBufferX = new double[stepCounterWindowSize];
                accBufferY = new double[stepCounterWindowSize];
                accBufferZ = new double[stepCounterWindowSize];
                barometerBuffer = new double[stepCounterWindowSize];
            }

            if (counter >= window_size) {
                // call method to compute mean here
                double mean = computeMean(baroBuffer);
                height = computeHeight(mean);

                mFallDetectionCallback.onNewHeightData(df.getTimestamp(), height);
                //Log.e(TAG, "class: " + acc_xBuffer.to.getClass());
                if (fall_detection.fall_detections((Double[]) acc_xBuffer.toArray(new Double[acc_xBuffer.size()]), (Double[]) acc_yBuffer.toArray(new Double[acc_xBuffer.size()]), (Double[]) acc_zBuffer.toArray(new Double[acc_xBuffer.size()]), fs)) {
                    mFallDetectionCallback.onFallDetected(System.currentTimeMillis());


                }
                //boolean falling = fall_detection.fall_detections((Double[]) acc_xBuffer.toArray(new Double[acc_xBuffer.size()]), (Double[]) acc_yBuffer.toArray(new Double[acc_xBuffer.size()]), (Double[]) acc_zBuffer.toArray(new Double[acc_xBuffer.size()]), fs);
                //Log.d("detect fall", Boolean.toString(falling));

                baroBuffer.subList(0, (int) (window_size / 2 + 1)).clear();
                acc_xBuffer.subList(0, (int) (window_size / 2 + 1)).clear();
                acc_yBuffer.subList(0, (int) (window_size / 2 + 1)).clear();
                acc_zBuffer.subList(0, (int) (window_size / 2 + 1)).clear();
                //Log.d("delete half of list", Integer.toString(acc_xBuffer.size()));

                counter = acc_xBuffer.size();
            } else {
                baroBuffer.add(df.getBarometricPressure());
                acc_xBuffer.add(df.getAccelX() / 2048);
                acc_yBuffer.add(df.getAccelY() / 2048);
                acc_zBuffer.add(df.getAccelZ() / 2048);
                counter++;
                //Log.d("SensorActivity", Integer.toString(acc_xBuffer.size()));

            }

            accBufferX[stepCounterWindowIndex] = df.getAccelX();
            accBufferY[stepCounterWindowIndex] = df.getAccelY();
            accBufferZ[stepCounterWindowIndex] = df.getAccelZ();
            barometerBuffer[stepCounterWindowIndex] = df.getBarometricPressure();
            timeBuffer[stepCounterWindowIndex] = System.currentTimeMillis() / 1000.0;

            if (++stepCounterWindowIndex == stepCounterWindowSize) {
                stepCounter.process(accBufferX, accBufferY, accBufferZ, timeBuffer);

                for (StepCountCallback callback : stepCountCallbacks) {
                    callback.onStepsChanged(stepCounter);
                }

                LocationSampler.Samples samples = locationSampler.extractSamples();
                ActivityTracker.WindowStats stats = activityTracker.process(barometerBuffer, timeBuffer, samples.longitude, samples.latitude, samples.time);

                for (ActivityTrackingCallback callback : activityTrackingCallbacks) {
                    callback.onActivity(stats, activityTracker);
                }

                double baro = Arrays.stream(barometerBuffer).average().getAsDouble();
                double time = 0.5 * (timeBuffer[timeBuffer.length - 1] + timeBuffer[0]);
                heightHistory.add(new Entry((float) ++timeIndex, (float) computeHeight(baro)));

                for (HeightChangeCallback callback : heightChangeCallbacks) {
                    callback.onHeightChanged(heightHistory);
                }

                stepCounterWindowIndex = 0;
            }

            // send sensor data to activity
            if (mSensorCallback != null) {
                mSensorCallback.onDataReceived(data);
            }

        }

        @Override
        public void onSensorCreated(AbstractSensor sensor) {
            Log.d(TAG, "onSensorCreated");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            counter = 0;
        }

        @Override
        public void onConnected(final AbstractSensor sensor) {
            Log.d(TAG, "onConnected");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            // send message to activity
            if (mSensorCallback != null) {
                mSensorCallback.onSensorConnected(sensor);
            }

            if (mSensorCallback != null && allSensorsConnected()) {
                mSensorCallback.onAllSensorsConnected();
            }

            // re-broadcast operation state after sensor is connected
            if (mSensorCallback != null && sensor instanceof AbstractNilsPodSensor) {
                mSensorCallback.onMessageReceived(sensor, SensorMessage.OPERATION_STATE_CHANGED, ((AbstractNilsPodSensor) sensor).getOperationState().toString());
            }

            try {
                if (!mConnectingQueue.isEmpty()) {
                    mConnectingQueue.poll().connect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(AbstractSensor sensor) {
            Log.d(TAG, "onDisconnected");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            // send message to activity
            if (mSensorCallback != null) {
                mSensorCallback.onSensorDisconnected(sensor);
            }

            if (mSensorCallback != null && allSensorsDisconnected()) {
                mSensorCallback.onAllSensorsDisconnected();
            }

        }

        @Override
        public void onConnectionLost(AbstractSensor sensor) {
            Log.d(TAG, "onConnectionLost");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            // send message to activity
            if (mSensorCallback != null) {
                mSensorCallback.onSensorConnectionLost(sensor);
            }
            // remove from list of attached sensors
            mAttachedSensors.remove(sensor);
            if (mSensorCallback != null && mAttachedSensors.isEmpty()) {
                mSensorCallback.onAllSensorsDisconnected();
            }
        }

        @Override
        public void onStartStreaming(AbstractSensor sensor) {
            Log.d(TAG, "onStartStreaming");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            if (mSensorCallback != null) {
                // send message to activity
                mSensorCallback.onStartStreaming(sensor);
            }
            if (mSensorCallback != null && allSensorsStreaming()) {
                mSensorCallback.onAllSensorsStreaming();
            }
        }

        @Override
        public void onStopStreaming(AbstractSensor sensor) {
            Log.d(TAG, "onStopStreaming");
            mInternalHandler.sendEmptyMessage(InternalHandler.MSG_CONNECTION_STATE_CHANGE);
            if (mSensorCallback != null) {
                // send message to activity
                mSensorCallback.onStopStreaming(sensor);
            }
        }

        @Override
        public void onSamplingRateChanged(AbstractSensor sensor, double newSamplingRate) {
            Log.d(TAG, "onSamplingRateChanged: " + newSamplingRate + " Hz");
            mSensorCallback.onMessageReceived(sensor, SensorMessage.SAMPLING_RATE_CHANGED);
        }

        @Override
        public void onNotify(AbstractSensor sensor, Object notification) {
            if (notification instanceof SensorMessage) {
                SensorMessage msg = (SensorMessage) notification;
                Log.d(TAG, sensor + " new message: " + msg);
                switch (msg) {
                    case BATTERY_LEVEL_CHANGED:
                        mSensorCallback.onMessageReceived(sensor, msg, sensor.getBatteryLevel());
                }
            } else if (notification instanceof SensorException) {
                SensorException e = (SensorException) notification;
                e.printStackTrace();

                Toast.makeText(BleService.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                switch (e.getExceptionType()) {
                    case readStateError:
                        disconnectSensor(sensor);
                        break;
                }
            }
        }
    };

    private NilsPodCallback mNilsPodOperationStateCallback = new NilsPodCallback() {

        @Override
        public void onSessionListRead(NilsPodSensor nilsPodSensor, List<Session> list) {

        }

        @Override
        public void onClearSessions(NilsPodSensor nilsPodSensor) {

        }

        @Override
        public void onSessionDownloadStarted(NilsPodSensor nilsPodSensor, SessionDownloader sessionDownloader) {

        }

        @Override
        public void onSessionDownloadProgress(NilsPodSensor nilsPodSensor, SessionDownloader sessionDownloader) {

        }

        @Override
        public void onSessionDownloadFinished(NilsPodSensor nilsPodSensor, SessionDownloader sessionDownloader) {

        }

        @Override
        public void onSensorConfigChanged(NilsPodSensor nilsPodSensor) {

        }

        @Override
        public void onOperationStateChanged(AbstractNilsPodSensor sensor, NilsPodOperationState operationState) {
            Log.d(TAG, "operation state changed: " + sensor + " | " + operationState);
            mNilsPodActivityCallback.onOperationStateChanged(sensor, operationState);
        }
    };

    public void setFallDetectionCallback(OnFallDetectionCallback callback) {
        mFallDetectionCallback = callback;
    }


    /**
     * Handler for communication Activity -> Service
     */
    public static class ServiceHandler extends Handler {

        /**
         * Message to initialize sensor connecting routine
         */
        public static final int MSG_CONNECT_SENSOR = 0x00;
        /**
         * Message to initialize sensor disconnecting routine
         */
        public static final int MSG_DISCONNECT_SENSOR = 0x01;
        /**
         * Message to initialize sensor start streaming routine
         */
        public static final int MSG_START_STREAMING = 0x02;
        /**
         * Message to initialize sensor stop streaming routine
         */
        public static final int MSG_STOP_STREAMING = 0x03;
        /**
         * Message to enable data recording to smartphone
         */
        public static final int MSG_ENABLE_RECORDING = 0x04;
        /**
         * Message to disable data recording to smartphone
         */
        public static final int MSG_DISABLE_RECORDING = 0x05;
        /**
         * Message to send command to sensor
         */
        public static final int MSG_SEND_COMMAND = 0x10;

        /**
         * Message to reset sensor
         */
        public static final int MSG_RESET = 0xF0;


        private BleService mService;

        public ServiceHandler(BleService service) {
            mService = service;
        }

        private BleService getService() {
            return mService;
        }

        @Override
        public void handleMessage(Message msg) {
            AbstractSensor sensor = null;
            if (msg.obj instanceof AbstractSensor) {
                sensor = (AbstractSensor) msg.obj;
            }
            switch (msg.what) {
                case MSG_CONNECT_SENSOR:
                    getService().connectSensor((SensorInfo) msg.obj);
                    break;
                case MSG_DISCONNECT_SENSOR:
                    getService().disconnectSensor(sensor);
                    break;
                case MSG_START_STREAMING:
                    getService().startStreaming(sensor);
                    break;
                case MSG_STOP_STREAMING:
                    getService().stopStreaming(sensor);
                    break;
                case MSG_ENABLE_RECORDING:
                    getService().enableDataRecording(sensor);
                    break;
                case MSG_DISABLE_RECORDING:
                    getService().disableDataRecording(sensor);
                    break;
                case MSG_RESET:
                    getService().reset(sensor);
                    break;
            }
        }
    }

    private static class InternalHandler extends Handler {

        private ArrayList<AbstractSensor> mAttachedSensors;

        public InternalHandler(ArrayList<AbstractSensor> attachedSensors) {
            mAttachedSensors = attachedSensors;
        }

        private static final int MSG_CONNECTION_STATE_CHANGE = 0x0F;

        private ArrayList<AbstractSensor> getAttachedSensors() {
            return mAttachedSensors;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECTION_STATE_CHANGE:
                    Log.d(TAG, "Sensor connection state change: " + getAttachedSensors());
                    break;
            }
        }
    }


    private void connectSensor(SensorInfo sensorInfo) {
        // create sensor references and add to list of attached sensors
        AbstractSensor sensor = SensorFactory.getSensorInstance(sensorInfo, BleService.this, mSensorDataProcessor);
        if (sensor instanceof AbstractNilsPodSensor) {
            ((AbstractNilsPodSensor) sensor).addNilsPodCallback(mNilsPodOperationStateCallback);
        }
        mAttachedSensors.add(sensor);
        mConnectingQueue.add(sensor);

        // notifies activity that known sensors have been found
        mSensorCallback.onScanResult(true);
        try {
            /*for (AbstractSensor sensor : mAttachedSensors) {
                sensor.connect();
            }*/
            if (!mConnectingQueue.isEmpty()) {
                (mConnectingQueue.poll()).connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectSensor(AbstractSensor sensor) {
        if (mAttachedSensors.contains(sensor)) {
            sensor.disconnect();
        }
    }

    private void startStreaming(AbstractSensor sensor) {
        if (mAttachedSensors.contains(sensor)) {
            sensor.startStreaming();
        }
    }

    private void stopStreaming(AbstractSensor sensor) {
        if (mAttachedSensors.contains(sensor)) {
            sensor.stopStreaming();
        }
    }

    private void enableDataRecording(AbstractSensor sensor) {
        if (sensor instanceof Recordable) {
            ((Recordable) sensor).setRecorderEnabled();
        }
    }

    private void disableDataRecording(AbstractSensor sensor) {
        if (sensor instanceof Recordable) {
            ((Recordable) sensor).setRecorderDisabled();
        }
    }

    private void reset(AbstractSensor sensor) {
        if (sensor instanceof Resettable) {
            ((Resettable) sensor).reset();
        }
    }


    @TargetApi(23)
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind!");

        mAttachedSensors.clear();
        mInternalHandler = new InternalHandler(mAttachedSensors);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationSampler = new LocationSampler();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "LOCATION PERMISSION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5.0f, locationSampler);
        }

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        //mAttachedSensors.clear();
        BleSensorManager.cancelRunningScans();
        mBinder = null;
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public ServiceHandler getHandler() {
        return mServiceHandler;
    }

    /**
     * Sets the sensor callback.
     *
     * @param callback the sensor callback
     */
    public void setSensorCallback(SensorCallback callback) {
        mSensorCallback = callback;
    }

    public void setNilsPodCallback(NilsPodCallback callback) {
        mNilsPodActivityCallback = callback;
    }

    private boolean allSensorsConnected() {
        if (mAttachedSensors.isEmpty()) {
            return false;
        }

        boolean ret = true;
        for (SensorInfo sensorInfo : mAttachedSensors) {
            ret &= sensorInfo.isConnected();
        }
        return ret;
    }

    private boolean allSensorsStreaming() {
        if (mAttachedSensors.isEmpty()) {
            return false;
        }

        boolean ret = true;
        for (SensorInfo sensorInfo : mAttachedSensors) {
            ret &= sensorInfo.isStreaming();
        }
        return ret;
    }

    private boolean allSensorsDisconnected() {
        if (mAttachedSensors.isEmpty()) {
            return false;
        }

        boolean ret = true;
        for (SensorInfo sensorInfo : mAttachedSensors) {
            ret &= !sensorInfo.isConnected();
        }
        return ret;
    }


    /**
     * Inner class representing the Binder between {@link BleService}
     * and {@link android.app.Activity}
     */
    public class BleServiceBinder extends Binder {

        public BleService getService() {
            return BleService.this;
        }
    }
}
