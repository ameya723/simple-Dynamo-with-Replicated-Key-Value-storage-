package edu.buffalo.cse.cse486_586.simpledynamo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database_for_CP extends SQLiteOpenHelper{


	private static final String DATABASE_NAME = "CP_Database4";
	private static final int DATABASE_VERSION = 1;

	public static  String TABLE_NAME = "Key_pair";
	public static String COLUMN_KEY = "provider_key";
	public static  String COLUMN_VAL = "provider_value";
	
	public Database_for_CP(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_KEY
				+ " TEXT NOT NULL, " + COLUMN_VAL + " TEXT NOT NULL);");
		Log.d("DB", "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_KEY
				+ "TEXT PRIMARY KEY, " + COLUMN_VAL + " TEXT NOT NULL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		onCreate(db);
	}
	
	


	
	

}
