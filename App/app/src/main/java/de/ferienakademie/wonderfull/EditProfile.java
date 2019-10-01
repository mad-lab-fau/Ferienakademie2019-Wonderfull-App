package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.ferienakademie.wonderfull.ProfileWrapper;

public class EditProfile extends AppCompatActivity {

    private ProfileWrapper profileDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // get database and values
        profileDB = new ProfileWrapper(this);
        ProfileValues profile = profileDB.getProfile();
        List<Contact> contacts = profileDB.getContacts();

        // insert profile values into edit screen
        EditText surname = findViewById(R.id.edit_surname);
        surname.setText(profile.getSurname());

        EditText name = findViewById(R.id.edit_name);
        name.setText(profile.getName());

        EditText size = findViewById(R.id.edit_size);
        size.setText(profile.getSizeString());

        EditText weight = findViewById(R.id.edit_weight);
        weight.setText(profile.getWeightString());

        EditText diseases = findViewById(R.id.edit_diseases);
        diseases.setText(profile.getDiseases());

        EditText medication = findViewById(R.id.edit_medication);
        medication.setText(profile.getMedication());

        EditText allergies = findViewById(R.id.edit_allergies);
        allergies.setText(profile.getAllergies());

        // insert contacts into edit screen
        LinearLayout emergencyContactsLayout = findViewById(R.id.edit_current_contacts);
        for(Contact c: contacts){
            TextView contactView = new TextView(this);
            contactView.setText(c.getName());
            emergencyContactsLayout.addView(contactView);
        }


    }

    public void saveProfile(View v){

        ProfileValues profile = new ProfileValues();

        // read values
        EditText surname = findViewById(R.id.edit_surname);
        profile.setSurname(surname.getText().toString());

        EditText name = findViewById(R.id.edit_name);
        profile.setName(name.getText().toString());

        // write values to database
        profileDB.setProfile(profile);
    }

    public void addContact(View v){

    }

}
