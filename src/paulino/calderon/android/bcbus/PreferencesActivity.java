package paulino.calderon.android.bcbus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import calderon.android.bctransit_assistant.objects.BusRoute;
import calderon.android.bctransit_assistant.objects.BusSchedule;
import calderon.android.bctransit_assistant.objects.BusStop;
import calderon.android.bctransit_assistant.objects.BusTime;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;


public class PreferencesActivity extends PreferenceActivity {
	private static String JSON_DATA_IDS="http://calder0n.com/bcbus_assistant/victoria_ids.txt";
	private String JSON_DATA_BASEPATH="http://calder0n.com/bcbus_assistant/victoria_";
	
	private DBAdapter db_link;
	private static String CLASS_ACTIVITY_NAME="HomeActivity";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        // Get the custom preference
        Preference customPref = (Preference) findPreference("updatePref");
        customPref
                        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                                public boolean onPreferenceClick(Preference preference) {
                                		new UpdateDataTask().execute();
                                        return true;
                                }

                        });
        
        
    }
    /*
     * This class takes care of updating in bg the database
     */
    private class UpdateDataTask extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(PreferencesActivity.this);

    	protected void onPreExecute() {
    		this.dialog.setMessage("Updating bus schedules");
    		this.dialog.show();
    	}

		@Override
		protected String doInBackground(String... params) {
          
            switch(NetChecker.checkNet(getApplicationContext())){
            case CONN_MISSING:
                    //User should enable wifi/3g etc
                    Log.d("HomeActivity","NETCHECKER: User should enable wifi/3g");
                    break;
            case NONET:
                    //User has enabled network, but it's not working
                    Log.d("HomeActivity","NETCHECKER: Network not working");
                    break;
            case FAULTYURL:
                    //Url of the server is faulty
                    Log.d("HomeActivity","NETCHECKER: Server is not reachable");
                    break;
            case NETOK:
                    //Maybe the problem has been solved since the exception was thrown. Retry the last method that threw the exception
                    Log.d("HomeActivity","NETCHECKER: NET OK Start download");
                   
                       
                       //Parsing json now
				
                      parse();
                       
                       //Log.d("HomeActivity",json_data);

            }
	        
			return null;
		}

		protected void onPostExecute(final String result) {
			if (this.dialog.isShowing()) 
				this.dialog.dismiss();
			
			SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("scheduleDownloaded","Yes");
	        editor.commit();

		}

		/*
		 * Parses the json InputStream and inserts the information to the database
		 */
    	public void parse() {
    		Gson gson = new Gson();
    		try { //Try to open a database connection
				db_link=new DBAdapter(PreferencesActivity.this);
				db_link.open();	
				db_link.initTransaction();
			} catch(Exception e){
				Log.w(CLASS_ACTIVITY_NAME, "Cannot open database connection:"+e.toString());
			}
    		//String ret="";
    		BusRoute route;
 
    		SQLiteStatement route_insert_stmnt = db_link.compileInsertRouteStmnt();
    		SQLiteStatement stop_insert_stmnt = db_link.compileInsertStopStmnt();
    		SQLiteStatement schedule_insert_stmnt = db_link.compileInsertScheduleStmnt();
    		
    		try {
    			db_link.deleteAll();
    			String ids = FileDownloader.downloadIDFile();
    			String[] ids_arr;
    			ids_arr=ids.split(" ");
    			Log.d(CLASS_ACTIVITY_NAME,"File ids found ("+ids_arr.length+"):"+ids);
    			for(int i=0;i<ids_arr.length;i++){
    			
    			
				
					//Reader r= new InputStreamReader(json_string);
					Reader r = new InputStreamReader(FileDownloader.downloadFile(JSON_DATA_BASEPATH+ids_arr[i]+".json"));
					route = gson.fromJson(r,BusRoute.class);
					
		
	            
	                String route_number = route.getNumber();
	                String route_name = route.getName();
	                String route_start_lat = route.getLatStart();
	                String route_end_lat = route.getLatEnd();
	                String route_start_long = route.getLongStart();
	                String route_end_long = route.getLongEnd();
	                /*
	                ret += "------------\n";
	                ret += "Number:" + route_number + "\n";
	                ret += "Name:" + route_name + "\n";
	                ret += "Start latitude:" + route_start_lat + "\n";
	                ret += "End latitude:" + route_end_lat + "\n";
	                ret += "Start longitude:" + route_start_long + "\n";
	                ret += "End longitude:" + route_end_long + "\n\n";
	                */
	                route_insert_stmnt.bindString(1, route_number);
	                route_insert_stmnt.bindString(2, route_name);
	                route_insert_stmnt.bindString(3, route_start_lat);
	                route_insert_stmnt.bindString(4, route_start_long);
	                route_insert_stmnt.bindString(5, route_end_lat);
	                route_insert_stmnt.bindString(6, route_end_long);
	                
	                route_insert_stmnt.executeInsert();
	                
	                //db_link.insertBusRoute(route_number, route_name, route_start_lat, route_end_lat, route_start_long, route_end_long);
	                
	                int bus_route_id = db_link.lastInsertID();
	                //System.gc();
			
	                //parse stops
	                for(BusStop stop : route.getStops())
	                {
	                	String stop_name = stop.getName();
	                	String stop_category = String.valueOf(stop.getCategory());
	                	String stop_direction = String.valueOf(stop.getDirection());
	                	String stop_latitude = stop.getLatitude();
	                	String stop_longitude = stop.getLongitude();
	                	
	                	stop_insert_stmnt.bindString(1, stop_latitude);
	                	stop_insert_stmnt.bindString(2, stop_longitude);
	                	stop_insert_stmnt.bindString(3, stop_category);
	                	stop_insert_stmnt.bindString(4, String.valueOf(bus_route_id));
	                	stop_insert_stmnt.bindString(5, stop_name);
	                	stop_insert_stmnt.bindString(6, stop_direction);
	                	
	                	stop_insert_stmnt.executeInsert();
	                	/*
		                ret += "------------\n";
		                ret += "Name:" + stop_name + "\n";
		                ret += "Category:" + stop_category + "\n";
		                ret += "Direction:" + stop_direction + "\n";
		                ret += "Longitude:" + stop_latitude + "\n";
		                ret += "Latitude:" + stop_longitude + "\n\n";
		                */
		                //db_link.insertBusStop(stop_name, stop_category, stop_direction, stop_latitude, stop_longitude, String.valueOf(bus_route_id));
		                int bus_stop_id = db_link.lastInsertID();
		                //System.gc();
		                //parse schedules
		                for(BusSchedule schedule : stop.getSchedules())
		                {
		                	String schedule_day = String.valueOf(schedule.getDay());
		                	/*
		                	ret += "------------\n";
			                ret += "Day:" + schedule_day + "\n\n";
			              */
			                //parse times
			                for(BusTime time : schedule.getTimes())
			                {
			                	String schedule_time = time.getTime();
			                	/*
			                	ret += "------------\n";
			                	ret += "Time:" + schedule_time + "\n";
			                	*/
			                	schedule_insert_stmnt.bindString(1, String.valueOf(bus_stop_id));
			                	schedule_insert_stmnt.bindString(2, schedule_time);
			                	schedule_insert_stmnt.bindString(3, schedule_day);
			                	
			                	schedule_insert_stmnt.executeInsert();
			                	//db_link.insertBusSchedule(String.valueOf(bus_stop_id), schedule_time, schedule_day);
			                	//System.gc();
			                }
		                }
	                }
	            }
			} catch (JsonParseException e) {
				Log.d(CLASS_ACTIVITY_NAME, "JSONException:"+e.toString());
				e.printStackTrace();
			} finally {
			       db_link.setTransactionOK();		
			}
    		db_link.finishTransaction();
    		db_link.close();
    		//return null;
    	}

    }
 
    /*
     * Helper class to download binary files
     */
    public static class FileDownloader {
    	public static String downloadIDFile() {
    		String ids=null;
    		DefaultHttpClient httpClient = new DefaultHttpClient();
            URI uri;
            InputStream data = null;
            try {
                uri = new URI(JSON_DATA_IDS);
                HttpGet method = new HttpGet(uri);
                HttpResponse response = httpClient.execute(method);
                data = response.getEntity().getContent();      
                
                InputStreamReader reader = new InputStreamReader(data);
                BufferedReader buffer = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String cur;
                while ((cur = buffer.readLine()) != null) {
                sb.append(cur);
                }
                ids=sb.toString();
                data.close();
			} catch (Exception e) {
				Log.d("HomeActivity", "Malformed URL:"+e.toString());
				return null;		
			}
    		return ids;
    	}
    	public static InputStream downloadFile(String url) {
    		Log.d(CLASS_ACTIVITY_NAME,"Downloading file:"+url);
    		DefaultHttpClient httpClient = new DefaultHttpClient();
            URI uri;
            InputStream data = null;
            try {
                uri = new URI(url);
                HttpGet method = new HttpGet(uri);
                HttpResponse response = httpClient.execute(method);
                data = response.getEntity().getContent();      
			} catch (Exception e) {
				Log.d("HomeActivity", "Malformed URL:"+e.toString());
				return null;
			} 
			//data.close();
    		return data;
    	}
    }
}
