/*
 * Copyright (C) 2018 Machine Learning and Data Analytics Lab, Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU).
 * <p>
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. If you reuse
 * this code you have to keep or cite this comment.
 */
package de.ferienakademie.wonderfull.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import de.fau.sensorlib.SensorCallback;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.sensors.AbstractSensor;
import de.fau.sensorlib.sensors.NilsPodCallback;

/**
 * Class handling the connection to the Android Background Service.
 */
public class BleServiceManager implements ServiceConnection {

    private static final String TAG = BleServiceManager.class.getSimpleName();

    private Context mContext;

    private Intent mServiceIntent;
    private boolean mServiceBound;
    private BleService mService;
    private BleService.ServiceHandler mServiceHandler;

    private SensorCallback mSensorCallback;
    private NilsPodCallback mNilsPodCallback;

    private ArrayList<SensorInfo> mSelectedSensors;

    /**
     * Creates a new instance.
     *
     * @param context the context in which the service runs
     */
    public BleServiceManager(Context context) {
        mContext = context;
        // initialize Intent to bind BleService
        mServiceIntent = new Intent(mContext, BleService.class);
    }

    /**
     * Sets the sensor callback.
     *
     * @param callback the sensor callback
     */
    public void setSensorCallback(SensorCallback callback) {
        mSensorCallback = callback;
    }


    public void setLoggingCallback(NilsPodCallback callback) {
        mNilsPodCallback = callback;
    }

    /**
     * Binds the Service and connects to the selected sensors.
     *
     * @param selectedSensors the selected sensors
     */
    public void sendConnectSensors(ArrayList<SensorInfo> selectedSensors) {
        mSelectedSensors = selectedSensors;

        // bind service and connect to sensors as soon as connection to service is established
        mContext.bindService(mServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    /**
     * Disconnects the sensors and unbinds the Service.
     */
    public void sendDisconnectSensors(ArrayList<AbstractSensor> sensors) {
        if (mService != null) {
            if (mServiceHandler != null) {
                for (AbstractSensor sensor : sensors) {
                    mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_DISCONNECT_SENSOR, sensor).sendToTarget();
                }
            }

            if (mServiceBound) {
                // unbind Service
                mContext.unbindService(this);
                mServiceBound = false;
            }
        }
    }

    /**
     * Tells the Service to start streaming sensor data.
     */
    public void sendStartStreaming(ArrayList<AbstractSensor> sensors) {
        if (mServiceHandler != null) {
            for (AbstractSensor sensor : sensors) {
                mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_START_STREAMING, sensor).sendToTarget();
            }
        }
    }

    /**
     * Tells the Service to stop streaming sensor data.
     */
    public void sendStopStreaming(ArrayList<AbstractSensor> sensors) {
        if (mServiceHandler != null) {
            for (AbstractSensor sensor : sensors) {
                mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_STOP_STREAMING, sensor).sendToTarget();
            }
        }
    }

    /**
     * Enables recording of sensor data to external storage.
     */
    public void sendEnableRecording(ArrayList<AbstractSensor> sensors) {
        if (mServiceHandler != null) {
            for (AbstractSensor sensor : sensors) {
                mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_ENABLE_RECORDING, sensor).sendToTarget();
            }
        }
    }

    /**
     * Disables recording of sensor data to external storage.
     */
    public void sendDisableRecording(ArrayList<AbstractSensor> sensors) {
        if (mServiceHandler != null) {
            for (AbstractSensor sensor : sensors) {
                mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_DISABLE_RECORDING, sensor).sendToTarget();
            }
        }
    }

    public void sendReset(AbstractSensor sensor) {
        if (mServiceHandler != null) {
            mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_RESET, sensor).sendToTarget();
        }
    }

    /**
     * Checks whether the service is currently bound.
     *
     * @return true if the server is bound, false otherwise
     */
    public boolean isServiceBound() {
        return mService != null && mServiceBound;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service connected!");
        mServiceBound = true;
        // get reference to service instance
        mService = ((BleService.BleServiceBinder) service).getService();
        mService.setSensorCallback(mSensorCallback);
        mService.setNilsPodCallback(mNilsPodCallback);

        mServiceHandler = mService.getHandler();

        for (SensorInfo sensorInfo : mSelectedSensors) {
            mServiceHandler.obtainMessage(BleService.ServiceHandler.MSG_CONNECT_SENSOR, sensorInfo).sendToTarget();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected!");
        mServiceBound = false;
        mService = null;
        mServiceHandler = null;
    }
}
