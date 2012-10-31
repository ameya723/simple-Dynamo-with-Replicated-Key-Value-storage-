package edu.buffalo.cse.cse486_586.simpledynamo;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class Server_Thread extends AsyncTask<String, String, String>{

	ServerSocket servSocket;
	static List nodeList;
	int portServer;
	Socket servAccepted;
	Context context;
	String temp_recv_req[];
	String recv_req[];
	String temp;
	String data;
	String least_hash;
	String high_hash;

	String listtosend;
	static String pno;
	static Boolean flagQ;
	static Cursor tempCursor;
	ContentValues values;
	static int count = 0;
	static String value;
	static List tempList;

	public Server_Thread(Context newContext) {
		// TODO Auto-generated constructor stub
		try {
			portServer = 10000;
			servSocket = new ServerSocket(portServer);
			Log.d("In", "Server");
			nodeList = new ArrayList<String>();
			tempList = new ArrayList<String>();
			this.context = newContext;

			tempList.add("5562");
			tempList.add("5556");
			tempList.add("5554");
			tempList.add("5558");
			tempList.add("5560");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		DynamoActivity.hash.append(recv_req[2] + "\n");


	}

	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub

		try {
			while (true) {
				Socket servAccepted = servSocket.accept();
				BufferedReader fromClient = new BufferedReader(
						new InputStreamReader(servAccepted.getInputStream()));
				temp = fromClient.readLine();

				Log.d("recvd msg is :", temp);
				recv_req = temp.split(":");


				if (recv_req[0].equals("Initial")) {
					String[] projection = { Database_for_CP.COLUMN_KEY,
							Database_for_CP.COLUMN_VAL };
					Cursor new_cursor = Content_Provider.query_dump(
							Content_Provider.cp_uri, projection, null, null, null);
					String return_cursor = "";
					if (new_cursor != null && new_cursor.getCount() != 0) {
						while (new_cursor.moveToNext()) {
							return_cursor += new_cursor.getString(new_cursor
									.getColumnIndexOrThrow(Database_for_CP.COLUMN_KEY));
							return_cursor += ",";
							return_cursor += new_cursor.getString(new_cursor
									.getColumnIndexOrThrow(Database_for_CP.COLUMN_VAL));
							return_cursor += ":";
						}
						return_cursor = return_cursor +"stop";
						PrintWriter tonext = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(servAccepted.getOutputStream())),
								true);
						tonext.println(return_cursor);
						Log.d("cursor is", return_cursor);
					}

				}
				if (recv_req[0].equals("List")) {
					int i = 1;
					while (!recv_req[i].equals("end")) {
						if (nodeList.contains(recv_req[i])) {
							Log.d("Already Present", recv_req[i]);
						} else {
							nodeList.add(recv_req[i]);
							Log.d("Added node is ", ":" + nodeList);
						}
						i++;
					}

				}

				if (recv_req[0].equals("Message")) {

					String recvdkey = recv_req[1];
					String recvdval = recv_req[2];
					int indexnext=0;
					String max = tempList.get(0).toString();
					Log.d("reached here ", ":" + recvdval);
					String newmsg = "Replicate:" + recvdkey + ":"
							+ recvdval + ":" + Content_Provider.portStr;
					String hashkey = Content_Provider.genHash(recvdkey);

					for (int i = 0; i < 5; i++) {
						if (Content_Provider.genHash(
								tempList.get(i).toString()).compareTo(
										hashkey) < 0) {
							max = tempList.get((i + 1) % 5).toString();
							Log.d("max is ", ":" + max);
						}
					}
					Log.d("final max is ", ":" + max);
					if (max.equals(Content_Provider.portStr)) {
						Log.d("cannot insert :", Content_Provider.portStr);
						int flagq = queryfromCP(recvdkey);
						if (flagq == -1) {
							try {
								//								PrintWriter toServer1 = new PrintWriter(
								//										new BufferedWriter(
								//												new OutputStreamWriter(
								//														servAccepted
								//																.getOutputStream())),
								//										true);
								//								toServer1.println(Integer.toString(count));
								insertintoCP(context, recvdkey, recvdval);

								Log.d("data sent is ", ":" + count);

							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (flagq == 1) {
							update(recvdkey, recvdval);
							Log.d("data sent is ", ":" + count);
							//						PrintWriter toServer1 = new PrintWriter(
							//								new BufferedWriter(new OutputStreamWriter(
							//											servAccepted.getOutputStream())),
							//									true);
							//							toServer1.println(Integer.toString(count));

						}
						indexnext = tempList.indexOf(Content_Provider.portStr);
						send_data(Integer.parseInt(tempList.get((indexnext + 1) % 5).toString()) * 2,newmsg);
						send_data(Integer.parseInt(tempList.get((indexnext + 2) % 5).toString()) * 2,newmsg);

					} else {
						int flag = send_data_with_timeout(
								Integer.parseInt(max) * 2, newmsg);
						Log.d("returned flag is  ", ":" + flag);
						if (flag == 0) {
							indexnext = tempList.indexOf(max);
							Log.d("new index is and to send is   ", ":"
									+ tempList.get((indexnext + 1) % 5) + ":"
									+ indexnext);
							if (tempList.get((indexnext+1)%5).equals(Content_Provider.portStr)) {
								Log.d("cannot insert :", Content_Provider.portStr);
								int flagq = queryfromCP(recvdkey);
								if (flagq == -1) {
									try {
										//										PrintWriter toServer1 = new PrintWriter(
										//												new BufferedWriter(
										//														new OutputStreamWriter(
										//																servAccepted
										//																		.getOutputStream())),
										//												true);
										//										toServer1.println(Integer.toString(count));
										insertintoCP(context, recvdkey, recvdval);

										Log.d("data sent is ", ":" + count);

									} catch (Exception e) {
										e.printStackTrace();
									}
								} else if (flagq == 1) {
									update(recvdkey, recvdval);
									Log.d("data sent is ", ":" + count);
									//								PrintWriter toServer1 = new PrintWriter(
									//										new BufferedWriter(new OutputStreamWriter(
									//													servAccepted.getOutputStream())),
									//											true);
									//									toServer1.println(Integer.toString(count));

								}
								indexnext = tempList.indexOf(Content_Provider.portStr);
								send_data(Integer.parseInt(tempList.get((indexnext + 1) % 5).toString()) * 2,newmsg);
								send_data(Integer.parseInt(tempList.get((indexnext + 2) % 5).toString()) * 2,newmsg);

							}
							else{
								send_data(Integer.parseInt(tempList.get((indexnext + 1) % 5).toString()) * 2,newmsg);

							}
							//								if (tempList.get((indexnext+2)%4).equals(Content_Provider.portStr)) {
							//									int flagq = queryfromCP(recvdkey);
							//									if (flagq == -1) {
							//										try {
							////											PrintWriter toServer1 = new PrintWriter(
							////													new BufferedWriter(
							////															new OutputStreamWriter(
							////																	servAccepted
							////																			.getOutputStream())),
							////													true);
							////											toServer1.println(Integer.toString(count));
							//											insertintoCP(context, recvdkey, recvdval);
							//
							//											Log.d("data sent is ", ":" + count);
							//
							//										} catch (Exception e) {
							//											e.printStackTrace();
							//										}
							//									} else if (flagq == 1) {
							//										update(recvdkey, recvdval);
							//										Log.d("data sent is ", ":" + count);
							////									PrintWriter toServer1 = new PrintWriter(
							////											new BufferedWriter(new OutputStreamWriter(
							////														servAccepted.getOutputStream())),
							////												true);
							////										toServer1.println(Integer.toString(count));
							//
							//									}
							//								}else{
							//								Log.d("cannot insert :", Content_Provider.portStr);
							//								indexnext = tempList.indexOf(Content_Provider.portStr);
							//								}



							int temp = send_data_with_timeout(
									Integer.parseInt(tempList.get(
											(indexnext + 1) % 5).toString()) * 2,
											newmsg);
							Log.d("quorum is", ":" + temp);
						} else {
							Log.d("quorum is", ":" + flag);
							Log.d("not found", "");
						}
					}

				}
				if (recv_req[0].equals("Replicate")) {
					String recvdkey = recv_req[1];
					String recvdval = recv_req[2];
					String porttosend = recv_req[3];
					int count = 0;
					int indexnext = tempList
							.indexOf(Content_Provider.portStr);
					PrintWriter tonext = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(servAccepted.getOutputStream())),
							true);
					tonext.println("true");
					Log.d("index is ", tempList.get(indexnext).toString());
					String data_send = "Insert:" + recvdkey + ":"
							+ recvdval;
					Log.d("inserting into:",
							tempList.get((indexnext + 1) % 5).toString());
					//					
					send_data(Integer.parseInt(tempList.get((indexnext + 1) % 5).toString()) * 2,data_send);
					send_data(Integer.parseInt(tempList.get((indexnext + 2) % 5).toString()) * 2,data_send);


					//			}
					//					// if(tempList.get((indexnext+1)%4).toString().equals(Content_Provider.portStr)){
					//					// Log.d("cannot send since my"," "+tempList.get((indexnext+1)%4).toString());
					//					// }else{
					//					// Log.d("inserting into:",tempList.get((indexnext+2)%4).toString());
					//					// int quor2 =
					//					// send_data_with_timeout(Integer.parseInt(tempList.get((indexnext+2)%4).toString())*2,
					//					// data_send);
					//					// if(quor2>0){
					//					// Log.d("Count ","Increased");
					//					// count++;
					//					// }
					//					// }
					int flagq = queryfromCP(recvdkey);
					if (flagq == -1) {
						try {
							//							PrintWriter toServer1 = new PrintWriter(
							//									new BufferedWriter(
							//											new OutputStreamWriter(
							//													servAccepted
							//															.getOutputStream())),
							//									true);
							//							toServer1.println(Integer.toString(count));
							insertintoCP(context, recvdkey, recvdval);

							Log.d("data sent is ", ":" + count);

						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (flagq == 1) {
						update(recvdkey, recvdval);
						Log.d("data sent is ", ":" + count);
						//					PrintWriter toServer1 = new PrintWriter(
						//							new BufferedWriter(new OutputStreamWriter(
						//										servAccepted.getOutputStream())),
						//								true);
						//						toServer1.println(Integer.toString(count));

					}
				}
				else
					if (recv_req[0].equals("Insert")) {
						String recvdkey = recv_req[1];
						String recvdval = recv_req[2];
						//					PrintWriter toServer1 = new PrintWriter(
						//							new BufferedWriter(new OutputStreamWriter(
						//									servAccepted.getOutputStream())), true);
						//					toServer1.println("true");
						//Log.d("insert",temp);
						//publishProgress();
						int flagq = queryfromCP(recvdkey);
						if (flagq == -1) {
							insertintoCP(context, recvdkey, recvdval);

							Log.d("inserted in insert ", ":" +recvdkey );
						}else{
							update(recvdkey, recvdval);
							Log.d("updated ", ":" + recvdkey );
						}
						// insertintoCP(context, recvdkey, recvdval);

					}else if(recv_req[0].equals("Query")){
						String selection = recv_req[1];
						//pno = recv_req[2];
						SQLiteQueryBuilder query = new SQLiteQueryBuilder();
						SQLiteDatabase db = Content_Provider.cp_database.getReadableDatabase();
						query.setTables(Database_for_CP.TABLE_NAME);
						query.setProjectionMap(Content_Provider.hash_map);
						Cursor qc = query.query(db,new String[] {
								Database_for_CP.COLUMN_VAL
						} , selection, null, null,
						null, null);
						//qc.setNotificationUri(getApplicationContext().getContentResolver(), uri);
						String output="";
						if (qc != null && qc.getCount() != 0) {
							while (qc.moveToNext()) {

								output= qc.getString(qc
										.getColumnIndexOrThrow(Database_for_CP.COLUMN_VAL));

							}
						}
						PrintWriter tonext = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(servAccepted.getOutputStream())),
								true);
						tonext.println(output);
						Log.d("count is  :", "" + qc.getCount());

					}



			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	public void send_data(int curr_portNo, String data_send) {
		// TODO Auto-generated method stub
		try {
			Log.d("data sent is ", ":" + data_send);
			Socket baseSocket = new Socket(Content_Provider.ipAdd, curr_portNo);
			PrintWriter toPeer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(baseSocket.getOutputStream())), true);
			toPeer.println(data_send);
			baseSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int send_data_with_timeout(int curr_portNo, String data_send) {
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
			String getquorum = temp_Buff1.readLine();
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



	private void insertintoCP(Context new_context, String key, String val) {
		// TODO Auto-generated method stub
		ContentValues new_value = new ContentValues();
		Log.d("values are:", key + ":" + "val");
		new_value.put(Database_for_CP.COLUMN_KEY, key);
		new_value.put(Database_for_CP.COLUMN_VAL, val);
		new_context.getApplicationContext();
		Uri uri = context.getContentResolver().insert(Content_Provider.cp_uri,
				new_value);
		SQLiteDatabase db = Content_Provider.cp_database.getWritableDatabase();
		long r_id = db.insert(Database_for_CP.TABLE_NAME, null, new_value);
		if (r_id > 0) {
			Uri new_uri = ContentUris.withAppendedId(Content_Provider.cp_uri,
					r_id);
			context.getApplicationContext().getContentResolver()
			.notifyChange(new_uri, null);
			uri = new_uri;
		}
		Log.d("Data Inserted : ", " " + new_value);
		// throw new IllegalArgumentException("Unknown " + uri);

	}

	private int queryfromCP(String toquery) {
		// TODO Auto-generated method stub
		String[] projection = { Database_for_CP.COLUMN_VAL };
		String selection = Database_for_CP.COLUMN_KEY + "=" + "'" + toquery
				+ "'";
		Cursor new_cursor = Content_Provider.query_dump(
				Content_Provider.cp_uri, projection, selection, null, null);
		Log.d("key is :", "" + toquery);

		String cursret = "";
		if (new_cursor != null && new_cursor.getCount() != 0) {
			while (new_cursor.moveToNext()) {

				cursret += new_cursor.getString(new_cursor
						.getColumnIndexOrThrow(Database_for_CP.COLUMN_VAL));

			}
			if (new_cursor.getCount() > 0) {
				return 1;
			} else {
				return -1;
			}
		} else {
			return -1;
		}

	}

	public int update(String toupdate, String value) {
		ContentValues new_value = new ContentValues();
		new_value.put(Database_for_CP.COLUMN_KEY, toupdate);
		new_value.put(Database_for_CP.COLUMN_VAL, value);
		String[] projection = { Database_for_CP.COLUMN_VAL };
		String selection = Database_for_CP.COLUMN_KEY + "=" + "'" + toupdate
				+ "'";
		int count = context.getContentResolver().update(
				Content_Provider.cp_uri, new_value, selection, null);

		Log.d("key is :", "" + toupdate);
		return 0;
	}



}
