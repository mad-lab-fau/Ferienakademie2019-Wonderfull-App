package de.ferienakademie.wonderfull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ProfileWrapper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ProfileDatabase.db";
    private static final String EMERGENCY_TABLE_NAME = "emergency_contacts";
    private static final String PROFILE_TABLE_NAME="profile";
    private final String ID = "42";
    private int id = 0;


    public ProfileWrapper(Context context){
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("ProfileWrapper", "onCreate!");

        String createTable = "CREATE TABLE IF NOT EXISTS " + EMERGENCY_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "name TEXT, phone TEXT)";
        sqLiteDatabase.execSQL(createTable);


        createTable = "CREATE TABLE IF NOT EXISTS " + PROFILE_TABLE_NAME + " ( id INTEGER PRIMARY KEY," + ProfileValues.SURNAME + " TEXT, " +
                ProfileValues.NAME + " TEXT, " + ProfileValues.SIZE + " REAL, " + ProfileValues.WEIGHT + " REAL, " +
                ProfileValues.DISEASES + " TEXT," + ProfileValues.MEDICATION + " TEXT, " + ProfileValues.ALLERGIES
                + " TEXT," + ProfileValues.FITNESS + " TEXT );";

        sqLiteDatabase.execSQL(createTable);

        // insert some values which are later updated


        ProfileValues profile = new ProfileValues();

        ContentValues values = new ContentValues();
        values.put("id", ID);
        values.put(ProfileValues.SURNAME, profile.getSurname());
        values.put(ProfileValues.NAME, profile.getName());
        values.put(ProfileValues.WEIGHT, profile.getWeight());
        values.put(ProfileValues.SIZE, profile.getSize());
        values.put(ProfileValues.DISEASES, profile.getDiseases());
        values.put(ProfileValues.MEDICATION, profile.getMedication());
        values.put(ProfileValues.ALLERGIES, profile.getAllergies());
        values.put(ProfileValues.FITNESS, profile.getFitness());
        sqLiteDatabase.insert(PROFILE_TABLE_NAME, null, values);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        Log.d("onUpgrade", "update requested");
        //onCreate(sqLiteDatabase);
    }

    public void insertContact(Contact contact){
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("name", contact.getName());
        values.put("phone", contact.getPhone());
        db.insert(EMERGENCY_TABLE_NAME, null, values);
        db.close();

    }

    public List<Contact> getContacts(){
        List<Contact> contacts = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + EMERGENCY_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                Contact contact = new Contact();
                contact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id"))));
                contact.setName(cursor.getString(cursor.getColumnIndex("name")));
                contact.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
                contacts.add(contact);
            }while (cursor.moveToNext());
        }

        db.close();
        cursor.close();
        return contacts;
    }

    public void deleteContact(Contact contact){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EMERGENCY_TABLE_NAME, "_id=?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    public void setProfile(ProfileValues profile){
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("EditProfile", "name: " + profile.getSurname());

        ContentValues values = new ContentValues();
        values.put(ProfileValues.SURNAME, profile.getSurname());
        values.put(ProfileValues.NAME, profile.getName());
        values.put(ProfileValues.WEIGHT, profile.getWeight());
        values.put(ProfileValues.SIZE, profile.getSize());
        values.put(ProfileValues.DISEASES, profile.getDiseases());
        values.put(ProfileValues.MEDICATION, profile.getMedication());
        values.put(ProfileValues.ALLERGIES, profile.getAllergies());
        values.put(ProfileValues.FITNESS, profile.getFitness());

        db.update(PROFILE_TABLE_NAME, values, "id=?", new String[]{ID});
        db.close();

    }

    public ProfileValues getProfile(){
        SQLiteDatabase db =this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + PROFILE_TABLE_NAME;

        ProfileValues profile = new ProfileValues();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            profile.setSurname(cursor.getString(cursor.getColumnIndex(ProfileValues.SURNAME)));
            profile.setName(cursor.getString(cursor.getColumnIndex(ProfileValues.NAME)));
            profile.setWeight(cursor.getFloat(cursor.getColumnIndex(ProfileValues.WEIGHT)));
            profile.setSize(cursor.getFloat(cursor.getColumnIndex(ProfileValues.SIZE)));
            profile.setDiseases(cursor.getString(cursor.getColumnIndex(ProfileValues.DISEASES)));
            profile.setMedication(cursor.getString(cursor.getColumnIndex(ProfileValues.MEDICATION)));
            profile.setAllergies(cursor.getString(cursor.getColumnIndex(ProfileValues.ALLERGIES)));
            profile.setFitness(cursor.getString(cursor.getColumnIndex(ProfileValues.FITNESS)));
        }
        db.close();
        cursor.close();

        Log.d("EditProfile", "surname get : " + profile.getSurname());


        return profile;
    }
}
