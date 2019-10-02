package de.ferienakademie.wonderfull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EditProfile extends AppCompatActivity {

    private ProfileWrapper profileDB;
    private List<TableLayout> addedContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // get database and values
        profileDB = new ProfileWrapper(this);
        ProfileValues profile = profileDB.getProfile();

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

        Spinner niveau = findViewById(R.id.edit_select_level);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.level_spinner));
        niveau.setAdapter(adapter);

        niveau.setSelection(profile.fitnessToInt());

        // insert contacts into edit screen
        loadContacts();



    }

    private void loadContacts(){

        List<Contact> contacts = profileDB.getContacts();

        LinearLayout emergencyContactsLayout = findViewById(R.id.edit_current_contacts);
        emergencyContactsLayout.removeAllViews();

        if(!contacts.isEmpty()) {

            TableLayout emergencyContactsTable = new TableLayout(this);
            emergencyContactsTable.setColumnStretchable(0, true);
            for (Contact c : contacts) {
                TableRow tr = new TableRow(this);

                TextView contactView = new TextView(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER_VERTICAL;
                contactView.setLayoutParams(lp);
                int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                contactView.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                contactView.setText(c.getName());
                contactView.setPadding(margin, margin, margin, margin);
                tr.addView(contactView);


                Button minus = new Button(this);
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
                TableRow.LayoutParams params = new TableRow.LayoutParams(width, width);
                params.gravity = Gravity.CENTER;
                params.weight = 1.0f;
                params.setMargins(0, 0, 0, 0);
                Log.d("EditContact","Name: " + c.getName() + " id: " + c.getId());
                minus.setLayoutParams(params);
                minus.setId(c.getId());
                minus.setPadding(0, 0, 0, 0);
                minus.setGravity(Gravity.CENTER);
                minus.setOnClickListener(this::removeContact);
                minus.setBackground(getResources().getDrawable(R.drawable.round_button));
                minus.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                minus.setTextColor(getResources().getColor(R.color.white));
                minus.setText(getResources().getText(R.string.edit_minus));
                tr.addView(minus);
                emergencyContactsTable.addView(tr);

            }

            emergencyContactsLayout.addView(emergencyContactsTable);
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

        Spinner niveau = findViewById(R.id.edit_select_level);
        profile.setFitness(profile.intToFitness(niveau.getSelectedItemPosition()));

        // write values to database
        profileDB.setProfile(profile);



        // get possibly new emergency contacts
        for(TableLayout tl:addedContacts){
            TableRow tr = (TableRow) tl.getChildAt(0);
            EditText et = (EditText) tr.getChildAt(1);
            Contact contact = new Contact();
            String text = et.getText().toString();

            contact.setName(text);

            tr = (TableRow) tl.getChildAt(1);
            et = (EditText) tr.getChildAt(1);
            text = et.getText().toString();
            if (text.isEmpty()){
                continue;
            }
            contact.setPhone(text);

            profileDB.insertContact(contact);


        }

        startActivity(new Intent(this, profile.class));
    }

    public void addContact(View v){
        LinearLayout emergencyContactsLayout = findViewById(R.id.edit_current_contacts);
        TableLayout newContactTable = new TableLayout(this);
        newContactTable.setColumnStretchable(1, true);

        TableRow contactNameRow = new TableRow(this);
        TextView contactName = new TextView(this);
        contactName.setText(getResources().getString(R.string.name));
        contactName.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        contactName.setPadding(10, 10, 10, 10);
        contactNameRow.addView(contactName);

        EditText editContactName = new EditText(this);
        editContactName.setMaxLines(1);
        editContactName.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        editContactName.setPadding(0,0,0,10);
        contactNameRow.addView(editContactName);
        newContactTable.addView(contactNameRow);

        TableRow contactPhoneRow = new TableRow(this);
        TextView contactPhone = new TextView(this);
        contactPhone.setText(getResources().getString(R.string.edit_phone));
        contactPhone.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        contactPhone.setPadding(10,10,10,10);
        contactPhoneRow.addView(contactPhone);

        EditText editContactPhone = new EditText(this);
        editContactPhone.setMaxLines(1);
        editContactPhone.setPadding(0,0,0,10);
        editContactPhone.setInputType(InputType.TYPE_CLASS_NUMBER);
        editContactPhone.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        contactPhoneRow.addView(editContactPhone);
        newContactTable.addView(contactPhoneRow);


        emergencyContactsLayout.addView(newContactTable);

        addedContacts.add(newContactTable);

    }

    public void removeContact(View v){

        int id = v.getId();
        Contact contact = new Contact();
        contact.setId(id);
        profileDB.deleteContact(contact);

        loadContacts();
    }

}
