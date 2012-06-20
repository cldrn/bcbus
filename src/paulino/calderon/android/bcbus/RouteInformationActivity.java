/**
 * Schedule Activity
 * @author Paulino Calderon <paulino@calderonpale.com>
 */
package paulino.calderon.android.bcbus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import calderon.android.bctransit_assistant.objects.BusRoute;
import calderon.android.bctransit_assistant.objects.BusSchedule;
import calderon.android.bctransit_assistant.objects.BusStop;
import calderon.android.bctransit_assistant.objects.BusTime;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity displays a ListView containing the bus stop schedules 
 * @author Paulino Calderon <paulino@calderonpale.com>
 */
public class RouteInformationActivity extends ListActivity {
	private final static String CLASS_ACTIVITY_NAME="RouteInformationActivity";
	private DBAdapter db_link;
	private ListView lv1;
	private ArrayList<String> listview_arr=new ArrayList<String>();
	private String [] position_ids_arr;
	
	private static String JSON_DATA_IDS="http://calder0n.com/bcbus_assistant/victoria_ids.txt";
	private String JSON_DATA_BASEPATH="http://calder0n.com/bcbus_assistant/victoria_";
	
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_schedule);
        lv1=getListView();
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String cityPref = settings.getString("cityPref", "null");
        String mapsDownloaded = settings.getString("mapsDownloaded", "null");
        
        
        if(mapsDownloaded.equals("null") && !cityPref.equals("null")) {//no data yet, download it
        	alertDownloadRequired();
        } else if (cityPref.equals("null")) { //no community defined yet       	
        	alertSetupNeeded();        	
        } else {//loads bus routes from db in the background        
        	new SelectDataTask().execute();
        }
    }
    /*
     * 
     */
    public void alertSetupNeeded() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.firstrun_message)
          .setTitle("BCBus setup")
          .setIcon(R.drawable.ic_about)
          .setCancelable(false)
       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        	 launchHome();
                 }
       });
        AlertDialog alert = builder.create();
        alert.show();    	
    }
    public void alertDownloadRequired() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.firstrun_message_maps)
          .setTitle("BCBus Maps")
          .setIcon(R.drawable.ic_about)
          .setCancelable(false)
       .setPositiveButton("Download now", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        	 	new UpdateDataTask().execute();
                 }
       });
        AlertDialog alert = builder.create();
        alert.show();
    }
    /*
     * This class loads the available bus routes from the database
     */
    private class SelectDataTask extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(RouteInformationActivity.this);

    	protected void onPreExecute() {
    		this.dialog.setMessage("Loading bus routes");
    		this.dialog.show();
    	}

		@Override
		protected String doInBackground(String... params) {
			try { //Try to open a database connection
				db_link=new DBAdapter(RouteInformationActivity.this);
				db_link.open();	
			} catch(Exception e){
				Log.w(CLASS_ACTIVITY_NAME, "Cannot open database connection:"+e.toString());
			}
			
			List<String> names =db_link.selectAllBusRoutes();
			position_ids_arr = new String[names.size()];//Maps item positijsonon to routeID
	
			int i=0;
	        for (String name : names) {
	        	String [] route=name.split(":");
	        	String listview_text=route[2];
	        	position_ids_arr[i]=route[0];
	        	Log.d(CLASS_ACTIVITY_NAME,"Record added to position to id array:"+position_ids_arr[i]);
	        	Log.d(CLASS_ACTIVITY_NAME, "New bus route:"+listview_text);
	        	listview_arr.add(listview_text);
	        	i++;
	        }
	        db_link.close();
	        
			return null;
		}

		protected void onPostExecute(final String result) {
			
			lv1.setAdapter(new ArrayAdapter<String>(RouteInformationActivity.this, R.layout.item_list,
					(String[])listview_arr.toArray(new String[listview_arr.size()])));
			
			lv1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				String itemContent = (String)lv1.getItemAtPosition(position);
				
				Log.d(CLASS_ACTIVITY_NAME,"getItemAtPosition:"+itemContent);
				Log.d(CLASS_ACTIVITY_NAME, "pos:"+position+" id:"+id);
				
				Intent intent = new Intent(RouteInformationActivity.this, DisplayRouteInfoActivity.class);
				intent.putExtra("routeID",position_ids_arr[position]);
				startActivity(intent);
				
			}
			});
			
			if (this.dialog.isShowing()) 
				this.dialog.dismiss();

		}


    }
    
    /*
     * This class takes care of updating in bg the database
     */
    private class UpdateDataTask extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(RouteInformationActivity.this);

    	protected void onPreExecute() {
    		this.dialog.setMessage("Downloading route maps");
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
                    Log.d("HomeActivity","NETCHECKER: NET OK Start download");
                    parse();
                    }
	        
			return null;
		}

		protected void onPostExecute(final String result) {
			if (this.dialog.isShowing()) 
				this.dialog.dismiss();
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(RouteInformationActivity.this);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("routeMapsDownloaded","Yes");
	        editor.commit();
	        
			new SelectDataTask().execute();

		}

		/*
		 * Parses the json InputStream and inserts the information to the database
		 */
    	public void parse() {
    		Gson gson = new Gson();
    		try { //Try to open a database connection
				db_link=new DBAdapter(RouteInformationActivity.this);
				db_link.open();	
				db_link.initTransaction();
			} catch(Exception e){
				Log.w(CLASS_ACTIVITY_NAME, "Cannot open database connection:"+e.toString());
			}
			
    		
    		try {
    			String ids = FileDownloader.downloadIDFile();
    			String[] ids_arr;
    			ids_arr=ids.split(" ");
    			Log.d(CLASS_ACTIVITY_NAME,"File ids found ("+ids_arr.length+"):"+ids);
    			for(int i=0;i<ids_arr.length;i++){
    				
		                
	                
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
    	public byte[] downloadPNG(String image_url) {
    		byte[] byte_arr=null;
    		DefaultHttpClient mHttpClient = new DefaultHttpClient();
    		
    		HttpGet mHttpGet = new HttpGet(image_url);

    		HttpResponse mHttpResponse;
			try {
				mHttpResponse = mHttpClient.execute(mHttpGet);
				if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

		    		  HttpEntity entity = mHttpResponse.getEntity();
		    		  if ( entity != null) {
		    		      byte_arr= EntityUtils.toByteArray(entity);
		    		  }
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
   		return byte_arr;
    	}
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
    /*
     * Launches HomeActivity Intent
     */
    public void launchHome() {
    	try {
    		startActivity(new Intent(this, HomeActivity.class));
    	} catch (Exception e) {
    		Log.d("DEBUG","Cant start Settings activity:"+e.toString());
    	}
    }
    
}

