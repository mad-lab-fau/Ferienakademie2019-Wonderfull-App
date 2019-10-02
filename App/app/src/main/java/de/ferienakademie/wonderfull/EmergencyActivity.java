package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EmergencyActivity extends AppCompatActivity {

    private String emergencyText = "%s wird in %d Sekunden angerufen.";
    private int time = 30;
    private Timer timer;
    private ProfileWrapper profileDB;


    private String number = "12345";
    private String contact = "Wonderfull";
    private String smsText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        final TextView textview = (TextView) findViewById(R.id.emergency_call_in);

        profileDB = new ProfileWrapper(this);
        List<Contact> contacts = profileDB.getContacts();
        if (!contacts.isEmpty()){
            number = contacts.get(0).getPhone();
            contact = contacts.get(0).getName();
        }

        ProfileValues profile = profileDB.getProfile();
        String name = profile.getSurname() + " " + profile.getName();
        smsText = String.format(getResources().getString(R.string.emergency_SMS), name);

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendSMS();
                        }
                    });

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
        timer.cancel();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            Log.d("EmergencyActivity", "Asking for permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, 100);
        }

        Log.d("EmergencyActivtity", "I will send an SMS now!");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, smsText, null, null);

        Context context = getApplicationContext();
        String text = String.format("SMS was send to %s", contact);
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);


    }

    public void onBackpressed(){
        timer.cancel();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }



}
