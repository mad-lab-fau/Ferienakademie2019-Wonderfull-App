/*
 * Copyright (C) 2018 Machine Learning and Data Analytics Lab, Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU).
 * <p>
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. If you reuse
 * this code you have to keep or cite this comment.
 */
package de.ferienakademie.wonderfull.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
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

    private ArrayList<Double> baroBuffer = new ArrayList<>(100);
    private ArrayList<Double> acc_xBuffer = new ArrayList<>(100);
    private ArrayList<Double> acc_yBuffer = new ArrayList<>(100);
    private ArrayList<Double> acc_zBuffer = new ArrayList<>(100);

    private boolean fall = false;

    public boolean getFall() {
      return fall;
    }

    public void setFall(boolean answer){
        fall = answer;
    }


    /**
     * Sensor Data Processor for receiving information about connection state changes and new data
     * from the sensor devices
     */
    private SensorDataProcessor mSensorDataProcessor = new SensorDataProcessor() {

        private int counter = 0;
        public double computeMean(ArrayList<Double> buffer){
            double mean = 0;
            for (int k = 0; k < buffer.size() ; k++) {
                mean += buffer.get(k);
                counter++;
            }

            mean = mean / counter;
            return mean;
        }
        @Override
        public void onNewData(SensorDataFrame data) {
            // TODO data from the sensor enters the service HERE! => here you can implement
            //  your algorithms (and pass the results to the activity in a similar way)
            NilsPodDataFrame df = (NilsPodDataFrame) data;

            fs = df.getOriginatingSensor().getSamplingRate();
            window_size = (int)(8.2*fs);


            if (counter == window_size) {
                // call method to compute mean here
                double mean = computeMean(baroBuffer);
                double height = 44330 * (1.0 - (Math.pow((mean / 1013.0), 0.1903)));

                fall = fall_detection.fall_detections((Double[]) acc_xBuffer.toArray(),  (Double[]) acc_yBuffer.toArray(), (Double[]) acc_zBuffer.toArray(), fs);
                baroBuffer.subList(0, (int)(window_size/2+1)).clear();
                acc_xBuffer.subList(0, (int)(window_size/2+1)).clear();
                acc_yBuffer.subList(0, (int)(window_size/2+1)).clear();
                acc_zBuffer.subList(0, (int)(window_size/2+1)).clear();

                counter = acc_xBuffer.size();
            } else {
                baroBuffer.add(df.getBarometricPressure());
                acc_xBuffer.add(df.getAccelX());
                acc_yBuffer.add(df.getAccelY());
                acc_zBuffer.add(df.getAccelZ());
                counter++;

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

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind!");

        mAttachedSensors.clear();
        mInternalHandler = new InternalHandler(mAttachedSensors);

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
