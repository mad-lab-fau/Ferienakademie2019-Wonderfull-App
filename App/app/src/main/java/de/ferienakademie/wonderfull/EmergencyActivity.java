package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.telephony.SmsManager;

import java.util.Timer;
import java.util.TimerTask;

public class EmergencyActivity extends AppCompatActivity {

    private String emergencyText = "%s wird in %d Sekunden angerufen.";
    private int time = 30;
    private Timer timer;
    private String phoneNumber = "01781336385";
    private String smsText = "Test";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        final TextView textview = (TextView) findViewById(R.id.emergency_call_in);
        final String contact = "John Doe"; //TODO get emergency contact
        textview.setText(String.format(emergencyText, contact, 30));
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                time -= 1;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(String.format(emergencyText, contact, time));
                    }
                });

                if (time == 0){
                    timer.cancel();
                    sendSMS();

                }


            }
        }, 1000, 1000);


    }

    public void fineButtonClick(View v){
        timer.cancel();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    public void sosButtonClick(View v){
        sendSMS();
    }

    private void sendSMS(){
        //SmsManager smsManager = SmsManager.getDefault(); TODO
        //smsManager.sendTextMessage(phoneNumber, null, smsText, null, null);
    }



}
