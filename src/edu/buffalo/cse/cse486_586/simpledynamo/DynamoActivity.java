package edu.buffalo.cse.cse486_586.simpledynamo;


import com.cse.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DynamoActivity extends Activity {
	static TextView hash;
	Button get;
	Button dump;
	Button put1;
	Button put2;
	Button put3;
	Cursor new_cursor;
	Context myContext;
	String tempval="";
	Handler handlerMain = new Handler();
	static String returnC="";


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		hash = (TextView) findViewById(R.id.chatMsg);
		hash.setMovementMethod(new ScrollingMovementMethod());

		get = (Button) findViewById(R.id.getButton);
		dump = (Button) findViewById(R.id.dumpButton);
		put1 = (Button) findViewById(R.id.put1Button);
		put2 = (Button) findViewById(R.id.put2Button);
		put3 = (Button) findViewById(R.id.put3Button);
		Log.d("In","main");

		put1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub


				Log.d("Query now","lets c");
				Thread send_data = new Thread(new InsertFunction1(), "client thread");
				send_data.start();

			}
		});
		put2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub


				Log.d("Query now","lets c");
				Thread send_data = new Thread(new InsertFunction2(), "client thread");
				send_data.start();

			}
		});
		put3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub


				Log.d("Query now","lets c");
				Thread send_data = new Thread(new InsertFunction3(), "client thread");
				send_data.start();

			}
		});

		get.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub


				Log.d("Query now","lets c");
				Thread send_data = new Thread(new getFunction(), "client thread");
				send_data.start();

			}
		});

		dump.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub
				String[] projection = { 
						Database_for_CP.COLUMN_VAL };
				Cursor new_cursor = Content_Provider.query_dump(
						Content_Provider.cp_uri,
						projection,null , null, null);

				String return_cursor = "";
				if (new_cursor != null && new_cursor.getCount() != 0) {
					while (new_cursor.moveToNext()) {

						return_cursor += new_cursor.getString(new_cursor
								.getColumnIndexOrThrow(Database_for_CP.COLUMN_VAL));
						return_cursor += "\n";
					}
					hash.setText("Dump:" + "\n");

					hash.append(return_cursor.toString());
					Log.d("cursor is", return_cursor);
				} else {
					hash.setText("No data Present");
					Log.d("data not found", "");
				}


			}
		});

	}
	public class InsertFunction1 implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			int seqno=0;
			Log.d("i m here","but nt working");
			for (int j = 0; j < 10; j++) {
				ContentValues new_value = new ContentValues();
				new_value.put(Database_for_CP.COLUMN_KEY, seqno);
				new_value.put(Database_for_CP.COLUMN_VAL, "put1"+seqno);
				Uri uri = getContentResolver().insert(Content_Provider.cp_uri,
						new_value);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				seqno++;
			}
			seqno=0;
		}

	}

	Runnable clientHandler = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			hash.append(tempval.toString()+"\n");
		}
	};

	public class InsertFunction2 implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			int seqno=0;
			for (int j = 1; j < 10; j++) {
				ContentValues new_value = new ContentValues();
				new_value.put(Database_for_CP.COLUMN_KEY, seqno);
				new_value.put(Database_for_CP.COLUMN_VAL, "put2"+seqno);
				Uri uri = getContentResolver().insert(Content_Provider.cp_uri,
						new_value);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				seqno++;
			}
			seqno=0;
		}

	}

	public class InsertFunction3 implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			int seqno=0;
			for (int j = 0; j < 10; j++) {
				ContentValues new_value = new ContentValues();
				new_value.put(Database_for_CP.COLUMN_KEY, seqno);
				new_value.put(Database_for_CP.COLUMN_VAL, "put3"+seqno);
				Uri uri = getContentResolver().insert(Content_Provider.cp_uri,
						new_value);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				seqno++;
			}
			seqno=0;
		}

	}
	public class getFunction implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			for (int j = 0; j < 10; j++) {
				String[] projection = { Database_for_CP.COLUMN_VAL };
				String selection = Database_for_CP.COLUMN_KEY+"=" + "'" +Integer.toString(j)+"'";

				new_cursor = getContentResolver().query(
						Content_Provider.cp_uri, projection, selection,
						null, null);
				tempval="";
				if (new_cursor != null && new_cursor.getCount() != 0) {
					while (new_cursor.moveToNext()) {

						tempval= new_cursor.getString(new_cursor
								.getColumnIndexOrThrow(Database_for_CP.COLUMN_VAL));

					}
				}
				handlerMain.post(clientHandler);

				Log.d("To print tempval is ",":"+tempval);
			}

		}
	}
}

