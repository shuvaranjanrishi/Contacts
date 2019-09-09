package com.example.asus.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "phone.db";
    static final int DATABASE_VERSION = 2;

    static final String TABLE_NAME = "phone_tbl";
    static final String ID = "id";
    static final String IMAGE = "image";
    static final String NAME = "name";
    static final String NUMBER = "number";

    String ORDER_BY = "name";

    static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ( "+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+IMAGE+" BLOB, "+NAME+" TEXT, "+NUMBER+" TEXT)";
    static final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME;

    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TABLE);
            Toast.makeText(context, "Database and Contact table Created", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(context, "MyOnCreate Error: "+e, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        try {
            db.execSQL(DROP_TABLE);
            Toast.makeText(context, "onUpgrade is Called", Toast.LENGTH_SHORT).show();
            onCreate(db);

        }catch (Exception e){
            Toast.makeText(context, "MyOnUpgrade Error: "+e, Toast.LENGTH_SHORT).show();
        }
    }

    //insert data into database
    public long insertData(ContactHolder values){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(IMAGE,values.image);
        contentValues.put(NAME,values.name);
        contentValues.put(NUMBER,values.number);

        long rowId = sqLiteDatabase.insert(TABLE_NAME,null,contentValues);

        return rowId;
    }

    public void setOrderBy(int id){
        if(id == 0){
            ORDER_BY = "name";
        }else if(id == 1){
            ORDER_BY = "number";
        }
    }

    //get all data from database
    public ArrayList<ContactHolder> getAllData(){

        ArrayList<ContactHolder> contactHolderArrayList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME,null,null,null,null,null,ORDER_BY,null);

        while(cursor.moveToNext()){

            int id = cursor.getInt(cursor.getColumnIndex(ID));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(IMAGE));
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            String number = cursor.getString(cursor.getColumnIndex(NUMBER));

            ContactHolder contactHolder = new ContactHolder(id,image,name,number);
            contactHolderArrayList.add(contactHolder);
        }

        return contactHolderArrayList;
    }


    //update data to database
    public void updateData(int contactId,byte[] image, String nameString,String numberString){

        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            String sql = "UPDATE "+TABLE_NAME+" SET "+IMAGE+" = ?, "+NAME+" = ?, "+NUMBER+" = ? WHERE "+ID+" = ?";
            SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);

            statement.bindBlob(1,image);
            statement.bindString(2,nameString);
            statement.bindString(3,numberString);
            statement.bindDouble(4,(double)contactId);

            statement.execute();
            sqLiteDatabase.close();

        }catch (Exception e){
            Toast.makeText(context, "MyUpdate Error", Toast.LENGTH_SHORT).show();
        }
    }

    //delete data from database
    public void deleteData(int contactId){

        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            String sql = "DELETE FROM "+TABLE_NAME+" WHERE "+ID+" = ?";
            sqLiteDatabase.execSQL(sql,new String[]{String.valueOf(contactId)});

        }catch (Exception e){
            Toast.makeText(context, "MyDelete Error", Toast.LENGTH_SHORT).show();
        }
    }
}
