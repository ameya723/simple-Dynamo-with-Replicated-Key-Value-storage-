package edu.buffalo.cse.cse486_586.simpledynamo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Content_Provider extends ContentProvider {
	static Context context;
	String base1 = "5554";
	String base2 = "5556";
	static String portStr;
	static String predecessor;
	static String successor;
	static String least_pred;
	static String high_succssr;
	static String ipAdd = "10.0.2.2";
	static String myhash;
	static int myportno;
	static int seqno = 0;
	static String toSend;
	TelephonyManager tel;
	static String returnC = "";
	public static Database_for_CP cp_database;
	ContentValues curr_values;
	int portno;
	List tempList1;
	String getquorum;
	


	public static final Uri cp_uri = Uri
			.parse("content://edu.cse.cse486_586.simpledht.provider/CP_Database");

	public Content_Provider() {
		cp_database = new Database_for_CP(getContext());

		// context = this.getContext();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		curr_values = values;
		Thread send_data = new Thread(new Data_sent(), "client thread");
		send_data.start();
		return uri;

	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.d("In", "CP");
		cp_database = new Database_for_CP(getContext());
		new Server_Thread(this.getContext()).execute("hi");
		tel = (TelephonyManager) getContext().getSystemService(
				Context.TELEPHONY_SERVICE);

		portStr = tel.getLine1Number().substring(
				tel.getLine1Number().length() - 4);
		myportno = Integer.parseInt(portStr) * 2;
		tempList1 = new ArrayList<String>();
				Thread clientT = new Thread(new ClientThread(), "client thread");
				clientT.start();
		tempList1.add("5562");
		tempList1.add("5556");
		tempList1.add("5554");
		tempList1.add("5558");
		tempList1.add("5560");


		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		try {
			String j[];
			String max = tempList1.get(0).toString();
			Log.d("selection is", selection);
			j = selection.split("'");
			Log.d("key is:", "" + j[1]);
			String hashkey = Content_Provider.genHash(j[1]);

			String newmsg = "Query:" + selection;
			for (int i = 0; i < 5; i++) {
				if (Content_Provider.genHash(tempList1.get(i).toString())
						.compareTo(hashkey) < 0) {
					max = tempList1.get((i + 1) % 5).toString();
				}
			}
			Log.d("max is :", j + ":" + max);
			if (max.equals(portStr)) {
				SQLiteQueryBuilder query = new SQLiteQueryBuilder();
				SQLiteDatabase db = cp_database.getReadableDatabase();
				query.setTables(Database_for_CP.TABLE_NAME);
				query.setProjectionMap(hash_map);
				Cursor qc = query.query(db, projection, selection,
						selectionArgs, null, null, null);
				qc.setNotificationUri(getContext().getContentResolver(), uri);
				if (qc.getCount() > 0) {
					return qc;
				}
			} else {

				int flag = send_data_with(Integer.parseInt(max) * 2, newmsg);
				if (flag == 1) {
					String[] columns = new String[1];
					columns[0] = Database_for_CP.COLUMN_VAL;
					MatrixCursor matrix = new MatrixCursor(columns);
					matrix.addRow(new Object[] { getquorum });
					return matrix;
				} else if (flag == 0) {
					int indexnext = tempList1.indexOf(max);
					if (tempList1.get((indexnext + 1) % 5).equals(
							Content_Provider.portStr)) {
						SQLiteQueryBuilder query = new SQLiteQueryBuilder();
						SQLiteDatabase db = cp_database.getReadableDatabase();
						query.setTables(Database_for_CP.TABLE_NAME);
						query.setProjectionMap(hash_map);
						Cursor qc = query.query(db, projection, selection,
								selectionArgs, null, null, null);
						qc.setNotificationUri(
								getContext().getContentResolver(), uri);
						if (qc.getCount() > 0) {
							return qc;
						}
					} else {
						send_data_with(
								Integer.parseInt(tempList1.get(
										(indexnext + 1) % 5).toString()) * 2,
										newmsg);
						String[] columns = new String[1];
						columns[0] = Database_for_CP.COLUMN_VAL;
						MatrixCursor matrix = new MatrixCursor(columns);
						matrix.addRow(new Object[] { getquorum });
						Log.d("returned flag is  ", ":" + flag);
						return matrix;

					}
				}
			}

		} catch (Exception e) {

		}
		return null;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = Content_Provider.cp_database.getWritableDatabase();
		long r_id = db.update(Database_for_CP.TABLE_NAME, values, selection,
				null);
		if (r_id > 0) {
			Uri new_uri = ContentUris.withAppendedId(Content_Provider.cp_uri,
					r_id);
			getContext().getContentResolver().notifyChange(new_uri, null);
			uri = new_uri;
		}
		Log.d("Data Inserted : ", " " + values);
		// TODO Auto-generated method stub
		return 0;
	}

	public static Cursor query_dump(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		SQLiteDatabase db = cp_database.getReadableDatabase();
		query.setTables(Database_for_CP.TABLE_NAME);
		query.setProjectionMap(hash_map);
		Cursor qc = query.query(db, projection, selection, selectionArgs, null,
				null, null);

		// qc.setNotificationUri(uri);
		return qc;
	}

	public static HashMap<String, String> hash_map;
	static {
		hash_map = new HashMap<String, String>();
		hash_map.put(Database_for_CP.COLUMN_KEY, Database_for_CP.COLUMN_KEY);
		hash_map.put(Database_for_CP.COLUMN_VAL, Database_for_CP.COLUMN_VAL);
	}

	static public String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public int send_data_with(int curr_portNo, String data_send) {
		// TODO Auto-generated method stub
		Socket baseSocket;
		int send_data = 0;
		try {
			Log.d("waiting ", ":" + curr_portNo);
			baseSocket = new Socket(Content_Provider.ipAdd, curr_portNo);

			BufferedReader temp_Buff1 = new BufferedReader(
					new InputStreamReader(baseSocket.getInputStream()));
			PrintWriter toPeer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(baseSocket.getOutputStream())), true);
			toPeer.println(data_send);
			Log.d("waiting ", ":" + curr_portNo);
			getquorum = temp_Buff1.readLine();
			if (getquorum.equals(null)) {
				Log.d("inside null condition ", ":" + getquorum);
				baseSocket.close();
				return 0;

			} else {
				Log.d("recvd is", getquorum);

				baseSocket.close();
				return 1;
			}

		} catch (Exception e) {
			return 0;
		}

	}

	public Uri insertin(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = cp_database.getWritableDatabase();
		long r_id = db.insert(Database_for_CP.TABLE_NAME, null, values);
		if (r_id > 0) {
			Uri new_uri = ContentUris.withAppendedId(Content_Provider.cp_uri,
					r_id);
			getContext().getApplicationContext().getContentResolver()
			.notifyChange(new_uri, null);
			uri = new_uri;
		}
		return uri;

	}


	public class ClientThread implements Runnable {
		String handshake;
		String tempno;
		String getkey[];
		String getkey1[];
		public void run() {
			// TODO Auto-generated method stub
			try {
				int i=0;
				handshake = "Initial" + ":" + portStr;
				portno = Integer.parseInt(base1) * 2;
				int indexofsucc=tempList1.indexOf(portStr);
				Log.d("index of succ is ",""+indexofsucc);
				int flagcurr = send_data_with(Integer.parseInt(tempList1.get((indexofsucc+1)%5).toString())*2, handshake);
				Log.d("flag is  ",""+flagcurr);
				if(flagcurr==1){
					if(getquorum!=null){
						String revdrecovery[] = getquorum.split(":");
						Log.d("value is ",revdrecovery[0]);
						while(!revdrecovery[i].equals("stop")){
							Log.d("value is ",revdrecovery[0]);
							getkey=revdrecovery[i].split(",");
							addtoCP(getkey[0],getkey[1]);
							Log.d("key is",getkey[0]);
							i++;
						}

					}
				}
				i=0;
				indexofsucc=indexofsucc-1;
				if(indexofsucc<0){
					indexofsucc=4;
				}
				Log.d("index of pred is",":"+tempList1.get(indexofsucc));
				int flagcu = send_data_with(Integer.parseInt(tempList1.get((indexofsucc)%5).toString())*2, handshake);
				Log.d("flag is  ",""+flagcurr);
				if(flagcu==1){
					if(getquorum!=null){
						String revdrecovery1[] = getquorum.split(":");
						Log.d("value is ",revdrecovery1[0]);
						while(!revdrecovery1[i].equals("stop")){
							Log.d("value is ",revdrecovery1[0]);
							getkey1=revdrecovery1[i].split(",");
							addtoCP(getkey1[0],getkey1[1]);
							Log.d("key is",getkey1[0]);
							i++;
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void addtoCP(String str,String str1) {
		if(str.equals("0")){
			if(portStr.equals("5560") || portStr.equals("5562") || portStr.equals("5556")){
				insertintial(str,str1);
			}

		}else if(str.equals("1")){
			if(portStr.equals("5558") || portStr.equals("5560") || portStr.equals("5562")){
				insertintial(str,str1);
			}

		}else if(str.equals("2")){
			if(portStr.equals("5562") || portStr.equals("5556") || portStr.equals("5554")){
				insertintial(str,str1);
			}

		}else if(str.equals("3")){
			if(portStr.equals("5558") || portStr.equals("5560") || portStr.equals("5562")){
				insertintial(str,str1);
			}

		}else if(str.equals("4")){
			if(portStr.equals("5556") || portStr.equals("5554") || portStr.equals("5558")){
				insertintial(str,str1);
			}

		}else if(str.equals("5")){
			if(portStr.equals("5560") || portStr.equals("5562") || portStr.equals("5556")){
				insertintial(str,str1);
			}

		}else if(str.equals("6")){
			if(portStr.equals("5560") || portStr.equals("5562") || portStr.equals("5556")){
				insertintial(str,str1);
			}

		}else if(str.equals("7")){
			if(portStr.equals("5558") || portStr.equals("5560") || portStr.equals("5562")){
				insertintial(str,str1);
			}

		}else if(str.equals("8")){
			if(portStr.equals("5562") || portStr.equals("5556") || portStr.equals("5554")){
				insertintial(str,str1);
			}


		}else if(str.equals("9")){
			if(portStr.equals("5562") || portStr.equals("5556") || portStr.equals("5554")){
				insertintial(str,str1);
			}

		}

	}



	public void insertintial(String substr,String substr1){
		ContentValues new_value = new ContentValues();
		Log.d("values are:", substr + ":" + substr1);
		new_value.put(Database_for_CP.COLUMN_KEY, substr);
		new_value.put(Database_for_CP.COLUMN_VAL, substr1);
		getContext().getApplicationContext();
		Uri uri = insertin(cp_uri,
				new_value);
		//Log.d("Data Inserted : ", " " + new_value);
		//Log.d("Data Inserted : ", " " + new_value);
	}

	public class Data_sent implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			try {

				//
				toSend = "Message:"
						+ curr_values.get(Database_for_CP.COLUMN_KEY) + ":"
						+ curr_values.get(Database_for_CP.COLUMN_VAL);

				Log.d("message is ", ": " + toSend);

				Socket newSocket = new Socket(ipAdd,
						Integer.parseInt(portStr) * 2);
				PrintWriter tonext = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(newSocket.getOutputStream())),
						true);
				tonext.println(toSend);
				newSocket.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
