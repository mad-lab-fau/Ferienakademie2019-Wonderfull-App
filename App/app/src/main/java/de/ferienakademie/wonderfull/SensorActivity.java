package de.ferienakademie.wonderfull;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.fau.sensorlib.BleSensorManager;
import de.fau.sensorlib.SensorCallback;
import de.fau.sensorlib.SensorEventGenerator;
import de.fau.sensorlib.SensorFoundCallback;
import de.fau.sensorlib.SensorInfo;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.enums.SensorMessage;
import de.fau.sensorlib.enums.SensorState;
import de.fau.sensorlib.sensors.AbstractNilsPodSensor;
import de.fau.sensorlib.sensors.AbstractSensor;
import de.fau.sensorlib.sensors.NilsPodCallback;
import de.fau.sensorlib.sensors.NilsPodSensor;
import de.fau.sensorlib.sensors.logging.Session;
import de.fau.sensorlib.sensors.logging.SessionDownloader;
import de.fau.sensorlib.widgets.OnStreamingFooterClickListener;
import de.fau.sensorlib.widgets.SensorInfoBar;
import de.fau.sensorlib.widgets.SensorPickerDialog;
import de.fau.sensorlib.widgets.StatusBar;
import de.fau.sensorlib.widgets.StreamingFooter;
import de.ferienakademie.wonderfull.service.BleServiceManager;

public class SensorActivity extends AppCompatActivity implements OnStreamingFooterClickListener, SensorCallback, NilsPodCallback, OnFallDetectionCallback {

    private static final String TAG = SensorActivity.class.getSimpleName();

    private StatusBar mStatusBar;
    private SensorInfoBar mSensorInfoBar;

    private TextView mBaroTextView;

    private DecimalFormat mDf = new DecimalFormat("#.##");

    private StreamingFooter mStreamingFooter;
    private SensorEventGenerator mSensorEventGenerator;

    private BleServiceManager mServiceManager;

    private ArrayList<SensorInfo> mSelectedSensors = new ArrayList<>();
    private ArrayList<AbstractSensor> mConnectedSensors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mStatusBar = findViewById(R.id.status_bar);
        mSensorInfoBar = findViewById(R.id.sensor_info_bar);

        mBaroTextView = findViewById(R.id.tv_barometer);
        mBaroTextView.setText(getString(R.string.string_barometer, mDf.format(0.0)));

        mStreamingFooter = findViewById(R.id.streaming_footer);
        mStreamingFooter.setOnStreamingFooterClickListener(this);

        mServiceManager = new BleServiceManager(this);
        mServiceManager.setSensorCallback(this);
        mServiceManager.setLoggingCallback(this);
        mServiceManager.setFallDetectionCallback(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSensorEventGenerator == null) {
            mSensorEventGenerator = new SensorEventGenerator();
            mSensorEventGenerator.addSensorEventListener(mStatusBar);
            mSensorEventGenerator.addSensorEventListener(mSensorInfoBar);
            mSensorEventGenerator.addSensorEventListener(mStreamingFooter);
        }
    }

    public void connectSensors() {
        mServiceManager.sendConnectSensors(mSelectedSensors);
    }

    public void disconnectSensors() {
        mServiceManager.sendDisconnectSensors(mConnectedSensors);
    }

    public void startStreaming() {
        mServiceManager.sendStartStreaming(mConnectedSensors);
    }

    public void stopStreaming() {
        mServiceManager.sendStopStreaming(mConnectedSensors);
    }

    private boolean shouldShowSensorPicker(boolean fabOpen) {
        if (!mServiceManager.isServiceBound() && !fabOpen) {
            // no service bound and FAB is closed, so show sensor picker (default state)
            return true;
        } else if (mSensorEventGenerator.getState() == SensorState.CONNECTION_LOST) {
            // don't show sensor picker if connection to sensor was recently lost
            return false;
        } else {
            // show sensor picker when FAB button is in "closed" state and all sensors are disconnected
            return !fabOpen && (mSensorEventGenerator.getState() == SensorState.DISCONNECTED);
        }
    }

    private void showSensorPicker() {
        try {
            if (BleSensorManager.enableBluetooth(this) == BleSensorManager.BT_ENABLED) {
                if (BleSensorManager.checkBtLePermissions(this, true) ==
                        BleSensorManager.PERMISSIONS_GRANTED) {
                    // clear all previously selected sensors
                    mSelectedSensors.clear();
                    mConnectedSensors.clear();

                    SensorPickerDialog fragment = new SensorPickerDialog();
                    // set callback if sensor was found
                    fragment.setSensorFoundCallback(new SensorFoundCallback() {
                        @Override
                        public boolean onKnownSensorFound(SensorInfo sensor) {
                            Log.d(TAG, "Known sensor: " + sensor.getDeviceName() + ", " +
                                    sensor.getDeviceClass());

                            mSelectedSensors.add(sensor);
                            return false;
                        }
                    });
                    fragment.setDialogDismissCallback(dialog -> {
                        if (mSelectedSensors != null && mSelectedSensors.size() > 0) {
                            SensorActivity.this.connectSensors();
                        } else {
                            mStreamingFooter.reset();
                        }
                    });

                    fragment.show(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BleSensorManager.REQUEST_ENABLE_BT) {
            switch (resultCode) {
                // if Bluetooth was enabled, no further action
                case RESULT_OK:
                    showSensorPicker();
                    break;
                // if Bluetooth wasn't enabled, continue in demo mode
                case RESULT_CANCELED:
                    Toast.makeText(this, "Please enable Bluetooth to scan for sensors!",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case BleSensorManager.REQUEST_BT_PERMISSIONS:
                if (grantResults.length == 0) {
                    Toast.makeText(this, "Permissions required!", Toast.LENGTH_SHORT).show();
                } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    String msg = "";
                    if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0]) ||
                            Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[0])) {
                        msg = "Android versions >= 6.0 need location access for scanning " +
                                "for BLE devices!";
                    }
                    if (!msg.equals("")) {
                        Toast.makeText(this, msg,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showSensorPicker();
                }
                break;
        }
    }

    @Override
    public void onFabClicked(boolean fabOpen) {
        if (shouldShowSensorPicker(fabOpen)) {
            showSensorPicker();
        }
    }

    @Override
    public void onStartStopButtonClicked() {
        if (mSensorEventGenerator.getState() == SensorState.STREAMING) {
            stopStreaming();
        } else {
            startStreaming();
        }

    }

    @Override
    public void onDisconnectButtonClicked() {
        disconnectSensors();
    }

    @Override
    public void onScanResult(boolean sensorFound) {
        if (sensorFound) {
            mSensorEventGenerator.setState(SensorState.CONNECTING);
        } else {
            mServiceManager.sendDisconnectSensors(mConnectedSensors);
        }
    }

    @Override
    public void onStartStreaming(AbstractSensor abstractSensor) {

    }

    @Override
    public void onStopStreaming(AbstractSensor abstractSensor) {
        mSensorEventGenerator.setState(SensorState.CONNECTED);
    }

    @Override
    public void onMessageReceived(AbstractSensor sensor, Object... message) {
        if (message[0] instanceof SensorMessage) {
            SensorMessage msg = (SensorMessage) message[0];
            switch (msg) {
                case BATTERY_LEVEL_CHANGED:
                    mSensorEventGenerator.broadcastMessage(sensor, SensorMessage.BATTERY_LEVEL_CHANGED);
                    break;
            }
        }
    }

    @Override
    public void onSensorConnected(AbstractSensor sensor) {
        mSensorEventGenerator.broadcastStateEvent(sensor, SensorState.CONNECTED);
        mConnectedSensors.add(sensor);
        if (sensor instanceof NilsPodSensor) {
            ((NilsPodSensor) sensor).updateRtc();
        }
    }

    @Override
    public void onSensorDisconnected(AbstractSensor abstractSensor) {

    }

    @Override
    public void onSensorConnectionLost(AbstractSensor abstractSensor) {
        Toast.makeText(this, "Sensor Connection Lost", Toast.LENGTH_SHORT).show();
        mSensorEventGenerator.setState(SensorState.CONNECTION_LOST);
    }

    @Override
    public void onDataReceived(SensorDataFrame data) {
        // TODO  HERE: raw sensor data from the service
        if (mSensorEventGenerator.getState() != SensorState.STREAMING) {
            return;
        } else {
            // update text view every 100th sample
            if (((int) data.getTimestamp()) % 100 == 0) {
                mBaroTextView.setText(getString(R.string.string_barometer, mDf.format(((NilsPodSensor.NilsPodDataFrame) data).getBarometricPressure())));
            }
        }
    }

    @Override
    public void onAllSensorsDisconnected() {
        mSensorEventGenerator.setState(SensorState.DISCONNECTED);
        invalidateOptionsMenu();
    }

    @Override
    public void onAllSensorsConnected() {
        mSensorEventGenerator.setState(SensorState.CONNECTED);

        invalidateOptionsMenu();
    }

    @Override
    public void onAllSensorsStreaming() {
        mSensorEventGenerator.setState(SensorState.STREAMING);
    }

    @Override
    public void onAllSensorsLogging() {

    }

    @Override
    public void onSensorConfigChanged(NilsPodSensor sensor) {

    }

    @Override
    public void onSessionListRead(NilsPodSensor sensor, List<Session> sessionList) {

    }

    @Override
    public void onClearSessions(NilsPodSensor sensor) {

    }

    @Override
    public void onSessionDownloadStarted(NilsPodSensor sensor, SessionDownloader sessionDownloader) {

    }

    @Override
    public void onSessionDownloadProgress(NilsPodSensor sensor, SessionDownloader sessionDownloader) {

    }

    @Override
    public void onSessionDownloadFinished(NilsPodSensor sensor, SessionDownloader sessionDownloader) {

    }

    @Override
    public void onOperationStateChanged(AbstractNilsPodSensor sensor, AbstractNilsPodSensor.NilsPodOperationState operationState) {
        mSensorEventGenerator.broadcastMessage(sensor, SensorMessage.OPERATION_STATE_CHANGED,
                operationState.toString());
    }

    @Override
    public void onFallDetected(double timestamp) {
        // THIS IS CALLED FROM THE SERVICE WHEN FALL WAS DETECTED
        // TODO HANDLE FALL, e.g. CALL EMERGENCY, START TIME OUT ETC.
    }

    @Override
    public void onNewHeightData(double timestamp, double height) {
        // TODO UPDATE PLOT
    }
}
