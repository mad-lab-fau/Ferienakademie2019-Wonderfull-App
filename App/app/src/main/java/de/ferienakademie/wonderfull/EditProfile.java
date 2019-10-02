package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

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
        Log.d("EditProfile", "surname " + profile.getSurname());
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

        //Spinner niveau = findViewById(R.id.edit_select_level);
        //niveau.setSelection(profile.fitnessToInt());

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

        EditText size = findViewById(R.id.edit_size);
        profile.setSize(Float.parseFloat(size.getText().toString()));

        EditText weight = findViewById(R.id.edit_weight);
        profile.setWeight(Float.parseFloat(weight.getText().toString()));

        EditText diseases = findViewById(R.id.edit_diseases);
        profile.setDiseases(diseases.getText().toString());

        EditText allergies = findViewById(R.id.edit_allergies);
        profile.setAllergies(allergies.getText().toString());

        EditText medication = findViewById(R.id.edit_medication);
        profile.setMedication(medication.getText().toString());

       // Spinner niveau = findViewById(R.id.edit_select_level);
        //profile.setFitness(profile.intToFitness(niveau.getSelectedItemPosition()));

        // write values to database
        Log.d("EditProfile", "name: " + profile.getSurname());
        profileDB.setProfile(profile);

        startActivity(new Intent(this, profile.class));
    }

    public void addContact(View v){
        LinearLayout emergencyContactsLayout = findViewById(R.id.edit_current_contacts);
        TableLayout newContactTable = new TableLayout(this);

        TableRow contactNameRow = new TableRow(this);
        TextView contactName = new TextView(this);
        contactName.setText("Name: ");
    }

}
