package com.krayrr.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Shaikh on 15-02-2018.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String TAG = DbHelper.class.getSimpleName();
    public static final String DB_NAME = "Location.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_LOCATION = "location";
    public static final String COLUMN_LOCATION = "cordinate";


    public  static  final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + TABLE_LOCATION + "("
            + COLUMN_LOCATION + " TEXT );";




    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTIFICATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF Exist" + TABLE_LOCATION);
        onCreate(db);
    }
    public void addNewLocation(String location)
    {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_LOCATION + " =?",new String[] {location});
        // Cursor cursor=db.rawQuery("Select * from " + USER_TABLE_ADDICON + " where " + icon + " = " + COLUMN_ICON,null);
        System.out.println(cursor);


        if(cursor.getCount() <= 0){
            ContentValues values = new ContentValues();
            values.put(COLUMN_LOCATION, location);


            long id = db.insert(TABLE_LOCATION, null, values);
            db.close();

            Log.d(TAG, "User medicine history inserted " +id);}else {
            System.out.println("Allready Exisist");
            db.close();
        }
    }

    public Cursor getLocation(){
        //String selectQuery = "select * from " + USER_TABLE_ADDICON;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from "+ TABLE_LOCATION, null);
        return cursor;
    }
    public void deleteride() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOCATION, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }


    // Add Icon Method
}

