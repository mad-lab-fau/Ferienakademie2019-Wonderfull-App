package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.List;

public class profile extends AppCompatActivity {

    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = this.getIntent();
        caller = intent.getStringExtra("caller");


        ProfileWrapper profileDB = new ProfileWrapper(this);
        ProfileValues profile = profileDB.getProfile();

        TextView name = findViewById(R.id.profile_name);
        if(profile.getSurname().isEmpty() && profile.getName().isEmpty()){
            name.setText(getResources().getString(R.string.profile_edit));
        }else{
            name.setText(profile.getSurname() + " " + profile.getName());
        }

        TextView weight = findViewById(R.id.profile_weight);
        if(profile.getWeight() == 0.0){
            weight.setText("-");

        } else{
            weight.setText(profile.getWeight() + " kg");
        }

        TextView height = findViewById(R.id.profile_height);
        if(profile.getSize() == 0){
            height.setText("-");
        } else{
            height.setText(profile.getSize() + " m");
        }

        TextView diseases = findViewById(R.id.profile_diseases);
        if(profile.getDiseases().isEmpty()){
            diseases.setText(getResources().getString(R.string.profile_none));
            diseases.setTextColor(getResources().getColor(R.color.grey));

        }else{
            diseases.setText(profile.getDiseases());
        }

        TextView medication = findViewById(R.id.profile_medication);
        if(profile.getMedication().isEmpty()){
            medication.setText(getResources().getString(R.string.profile_none));
            medication.setTextColor(getResources().getColor(R.color.grey));
        }else{
            medication.setText(profile.getMedication());
        }

        TextView allergies = findViewById(R.id.profile_allergies);
        if(profile.getAllergies().isEmpty()){
            allergies.setText(getResources().getString(R.string.profile_none));
            allergies.setTextColor(getResources().getColor(R.color.grey));
        }else{
            allergies.setText(profile.getAllergies());
        }

        TextView level = findViewById(R.id.profile_level);
        level.setText( getResources().getTextArray(R.array.level_spinner)[profile.fitnessToInt()]);


        TableLayout contactLayout = findViewById(R.id.profile_contacts);
        List<Contact> contacts = profileDB.getContacts();
        if(contacts.isEmpty()){
            TableRow contactRow = new TableRow(this);
            TextView contactName = new TextView(this);
            contactName.setText(getResources().getString(R.string.profile_none));

            contactName.setPadding(0,10,10,10);
            contactName.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
            contactName.setTextColor(getResources().getColor(R.color.grey));
            contactRow.addView(contactName);
            contactLayout.addView(contactRow);

        }else{
            for(Contact c: contacts){
                TableRow contactRow = new TableRow(this);
                TextView contactName = new TextView(this);
                contactName.setText(c.getName() + ":");
                contactName.setPadding(0,10,10,10);
                contactName.setTextAppearance(R.style.TextAppearance_AppCompat_Large);

                contactRow.addView(contactName);

                TextView contactPhone = new TextView(this);
                contactPhone.setText(c.getPhone());
                contactPhone.setPadding(0,10,10,10);
                contactPhone.setTextAppearance(R.style.TextAppearance_AppCompat_Large);

                contactRow.addView(contactPhone);

                contactLayout.addView(contactRow);
            }
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        startActivity(new Intent(this, EditProfile.class));
        return true;
    }

    @Override
    public void onBackPressed(){
        if (caller.compareTo("hike") == 0){
            startActivity(new Intent(this, HikeMainscreen.class));
        }else{
            startActivity(new Intent(this, MainActivity.class));
        }

    }
}
