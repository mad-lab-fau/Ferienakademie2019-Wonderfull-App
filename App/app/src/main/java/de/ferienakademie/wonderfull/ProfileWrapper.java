package de.ferienakademie.wonderfull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import de.ferienakademie.wonderfull.Contact;

public class ProfileWrapper extends SQLiteOpenHelper {

    private SQLiteDatabase profileDatabase;

    private static final String DATABASE_NAME = "ProfileDatabase";
    private static final String TABLE_NAME = "emergency_contacts";

    public ProfileWrapper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, phone TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);

        onCreate(sqLiteDatabase);
    }

    public void insertContact(String name, String phone){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(name, phone);
        db.insert(DATABASE_NAME, null, values);
        db.close();

    }

    public List<Contact> getContacts(){
        List<Contact> contacts = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + DATABASE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                Contact contact = new Contact();
                contact.setId(cursor.getColumnIndex("id"));
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
        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }
}
