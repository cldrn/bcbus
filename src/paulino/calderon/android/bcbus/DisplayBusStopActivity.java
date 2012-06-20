/**
 * 
 */
package paulino.calderon.android.bcbus;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;



import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author pcp
 *
 */
public class DisplayBusStopActivity extends ListActivity {
	private DBAdapter db_link;
	public int stopID;
	private ListView lv1;
	public int scheduleDay=-1;
	private static String CLASS_ACTIVITY_NAME="DisplayBusStopActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   
		   
		   Bundle stopIDBundle = getIntent().getExtras();
		   String stopIDStr=stopIDBundle.getString("busStopID");
		   stopID=Integer.valueOf(stopIDStr);
		   try {
				db_link=new DBAdapter(DisplayBusStopActivity.this);
				db_link.open();
			} catch(Exception e){
				Log.w("DisplayScheduleActivity","DB Connection failed:"+e.toString());
			}	
			String stopName=db_link.getBusStopName(stopID);
			db_link.close();

			setTitle(stopName);
		   setContentView(R.layout.home_schedule);
		   lv1=getListView();
		   Log.d(CLASS_ACTIVITY_NAME,"Bus Stop ID:"+stopIDStr);
		   new SelectDataTask().execute(stopIDStr);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    SubMenu sub = menu.addSubMenu(0,0,0, "Change day").setIcon(android.R.drawable.ic_menu_today);
	    sub.add(0,11,0,"Weekday");
	    sub.add(0,12,0,"Saturday");
	    sub.add(0,13,0,"Sunday");
	    
	    sub.setGroupCheckable(0, true, true);
	    
	    
		switch(scheduleDay)
		{
		case 7:
			 sub.getItem(2).setChecked(true);
			break;
		case 6:
			 sub.getItem(1).setChecked(true);
			break;
		default:
			sub.getItem(0).setChecked(true);
		}
	    
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.d(CLASS_ACTIVITY_NAME, "MenuItem direction:"+item.getItemId());
	    // Handle item seCOLOR_MENU_GROUPlection
	    switch (item.getItemId()) {
		case 11:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			scheduleDay=1;
			break;
		case 12:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			scheduleDay=6;
			
			break;
		case 13:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			scheduleDay=7;
			break;
		
	    default:
	    	scheduleDay=-1;
	        //return super.onOptionsItemSelected(item);
	    }
	    if(item.getItemId()>10)
	    	new SelectDataTask().execute(String.valueOf(stopID));
	    return true;
	}
	private class SelectDataTask extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(DisplayBusStopActivity.this);
    	private ArrayList<String> listview_arr=new ArrayList<String>();
    	
    	protected void onPreExecute() {
    		this.dialog.setMessage("Loading schedules");
    		this.dialog.show();
    	}

		@Override
		protected String doInBackground(String... params) {
			int stopID=Integer.valueOf(params[0]);
			Log.d(CLASS_ACTIVITY_NAME,"doInBackground stopID:"+params[0]);
			try {
				db_link=new DBAdapter(DisplayBusStopActivity.this);
				db_link.open();
			} catch(Exception e){
				Log.w(CLASS_ACTIVITY_NAME,"DB Connection failed:"+e.toString());
			}			
		
			int day=-1;
			if(scheduleDay==-1) {
				/*
				 * 1 - sunday
				 * 7 - saturday in gregoria, but here i start mon - 1 sun - 7
				 * 
				 * Dumb conversion to my current format
				 */
				GregorianCalendar newCal = new GregorianCalendar( );
				day = newCal.get( Calendar.DAY_OF_WEEK );
				Log.d(CLASS_ACTIVITY_NAME, "day:"+day);
				if(day>1 && day<=6) {
					scheduleDay=1;
				} else {
					switch(day)
					{
					case 7:
						scheduleDay=6;
						break;
					case 1:
						scheduleDay=7;
						break;
					}
				}
			} 
			Log.d(CLASS_ACTIVITY_NAME, "Today is:"+scheduleDay);
			List<String> names = db_link.selectScheduleByBusStop(stopID,scheduleDay);
		
			db_link.close();
			
			
	        for (String name : names) {
	        	Log.d(CLASS_ACTIVITY_NAME, "New schedule:"+name);
	        	String [] route=name.split("-");
	        	String listview_text=route[1];
	        	listview_arr.add(listview_text);
	        }

			return null;
		}

		protected void onPostExecute(final String result) {
	
			
			lv1.setAdapter(new ArrayAdapter<String>(DisplayBusStopActivity.this, R.layout.item_list, (String[])listview_arr.toArray(new String[listview_arr.size()])));

			if (this.dialog.isShowing()) 
				this.dialog.dismiss();
			
			CharSequence text = "Press menu to change the day";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(DisplayBusStopActivity.this, text, duration);
			toast.show();
			
		}


    }
}
