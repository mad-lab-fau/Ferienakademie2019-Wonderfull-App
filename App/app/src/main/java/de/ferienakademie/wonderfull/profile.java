package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ProfileWrapper profileDB = new ProfileWrapper(this);
        ProfileValues profile = profileDB.getProfile();

        TextView name = findViewById(R.id.profile_name);
        name.setText(profile.getSurname() + " " + profile.getName());

        TextView weight = findViewById(R.id.profile_weight);
        weight.setText(profile.getWeight() + " kg");

        TextView height = findViewById(R.id.profile_height);
        height.setText(profile.getSize() + " m");

        TextView diseases = findViewById(R.id.profile_diseases);
        diseases.setText(profile.getDiseases());

        TextView medication = findViewById(R.id.profile_medication);
        medication.setText(profile.getMedication());

        TextView allergies = findViewById(R.id.profile_allergies);
        allergies.setText(profile.getAllergies());

        TextView level = findViewById(R.id.profile_level);
        level.setText(profile.getFitness());


        TableLayout contactLayout = findViewById(R.id.profile_contacts);
        List<Contact> contacts = profileDB.getContacts();

        for(Contact c: contacts){
            TableRow contactRow = new TableRow(this);
            TextView contactName = new TextView(this);
            contactName.setText(c.getName() + ":");
            contactName.setPadding(10,10,10,10);
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

    public void onBackpressed(){
        startActivity(new Intent(this, MainActivity.class));
    }
}
