/**
 * 
 */
package paulino.calderon.android.bcbus;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Paulino Calderon <paulino@calderonpale.com>
 *
 */
public class DisplayScheduleActivity extends ListActivity {
	private static final String ACTIVITY_CLASS_NAME="DisplaySchedule";
	private DBAdapter db_link;
	private ListView lv1;
	private String [] position_ids_arr;
	public int routeID; // stores route id in database, not the route number
	public int routeDir=-1;
	public List<String> routeDirections;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   Bundle routeIDBundle = getIntent().getExtras();
		   String routeIDStr=routeIDBundle.getString("routeID");
		   try {
				db_link=new DBAdapter(DisplayScheduleActivity.this);
				db_link.open();
			} catch(Exception e){
				Log.w("DisplayScheduleActivity","DB Connection failed:"+e.toString());
			}	
			String routeName=db_link.getRouteName(Integer.valueOf(routeIDStr));
			db_link.close();
		  setTitle(routeName);
		   setContentView(R.layout.home_schedule);
		   
		   

		   lv1=getListView();
		   Log.d("DisplayScheduleActivity","Route ID:"+routeIDStr);
		   this.routeID= Integer.valueOf(routeIDStr);
		   //this.routeDir= Integer.valueOf(routeDir);
		   new SelectDataTask().execute(routeIDStr);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			db_link=new DBAdapter(DisplayScheduleActivity.this);
			db_link.open();
		} catch(Exception e){
			Log.w("DisplayScheduleActivity","DB Connection failed:"+e.toString());
		}		
		int defaultDirection;
			defaultDirection = db_link.selectBusStopDefaultDirection(routeID);
			db_link.close();
			Log.d(ACTIVITY_CLASS_NAME, "Default dir:"+defaultDirection);
	    SubMenu sub = menu.addSubMenu(0,0,0, "Change direction").setIcon(android.R.drawable.ic_menu_compass);
	    int x=10;
	    int c_index=0;
	    for(String routeDir : routeDirections) {
	    	/*
	    	 *   case "NORTH": 
        $direction=1; 
    break; 
    case "NORTHEAST": 
        $direction=2; 
    break; 
    case "EAST": 
        $direction=3; 
    break; 
    case "SOUTHEAST": 
        $direction=4; 
    break; 
    case "SOUTH": 
        $direction=5; 
    break; 
    case "SOUTHWEST": 
        $direction=6; 
    break; 
    case "WEST": 
        $direction=7; 
    break; 
    case "NORTHWEST": 
        $direction=8; 

	    	 * 
	    	 */
	    	
	    	Log.d(ACTIVITY_CLASS_NAME,"Direction option:"+routeDir);
	    	if (String.valueOf(defaultDirection).equals(routeDir)) {
	    		sub.add(0,Integer.valueOf(routeDir)+10,0,getDirStringByCode(Integer.valueOf(routeDir)));
	    		c_index=x;
	    		
	    	} else	{
	    		sub.add(0,Integer.valueOf(routeDir)+10,0,getDirStringByCode(Integer.valueOf(routeDir)));
	    	}
	    	x++;
	    }
	    sub.setGroupCheckable(0, true, true);
	    sub.getItem(c_index-10).setChecked(true);
	    
	    return true;
	}
	public String getDirStringByCode(int direction) {
		String dir=null;
		switch(direction) {
		case 1:
			dir="North";
			break;
		case 2:
			dir="North East";
			break;
		case 3:
			dir="East";
			break;
		case 4:
			dir="South East";
			break;
		case 5:
			dir="South";
			break;
		case 6:
			dir="South West";
			break;
		case 7:
			dir="West";
			break;
		case 8:
			dir="North West";
			break;
		default:
			dir="N/A";
		}
		return dir;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.d(ACTIVITY_CLASS_NAME, "MenuItem direction:"+item.getItemId());
	    // Handle item seCOLOR_MENU_GROUPlection
	    switch (item.getItemId()) {
		case 11:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			routeDir=1;
			break;
		case 12:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			routeDir=2;
			
			break;
		case 13:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			routeDir=3;
			break;
		case 14:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			routeDir=4;
			break;
		case 15:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
			routeDir=5;
			break;
		case 16:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
				routeDir=6;
			break;
		case 17:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
				routeDir=7;
			break;
		case 18:
			if (item.isChecked()) item.setChecked(false);
	    	  else item.setChecked(true);
				routeDir=8;
			break;
		
	    default:
	    	routeDir=-1;
	        //return super.onOptionsItemSelected(item);
	    }
	    if(item.getItemId()>10)
	    	new SelectDataTask().execute(String.valueOf(routeID));
	    return true;
	}
	private class SelectDataTask extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(DisplayScheduleActivity.this);
    	private ArrayList<String> listview_arr=new ArrayList<String>();
    	
    	protected void onPreExecute() {
    		this.dialog.setMessage("Loading bus stops");
    		this.dialog.show();
    	}

		@Override
		protected String doInBackground(String... params) {
			int routeID=Integer.valueOf(params[0]);
			Log.d("DisplayScheduleActivity","doInBackground routeID:"+params[0]);
			try {
				db_link=new DBAdapter(DisplayScheduleActivity.this);
				db_link.open();
			} catch(Exception e){
				Log.w("DisplayScheduleActivity","DB Connection failed:"+e.toString());
			}		
			int defaultDirection;
			if(routeDir==-1){
				defaultDirection = db_link.selectBusStopDefaultDirection(routeID);
				routeDir= defaultDirection;
			} else {
				defaultDirection = routeDir;
			}
			List<String> directions = db_link.getRouteDirections(routeID);
			routeDirections = directions;
			Log.d("DisplayScheduleActivity", "Default direction:"+defaultDirection);
			List<String> names =db_link.selectAllBusStopsByDirection(defaultDirection, routeID);
			db_link.close();
			position_ids_arr = new String[names.size()];
			int i=0;
	        for (String name : names) {
	        	Log.d("DisplayScheduleActivity", "New bus stop:"+name);
	        	String [] route=name.split(":");
	        	String listview_text=route[5];
	        	listview_arr.add(listview_text);
	        	position_ids_arr[i]=route[0];
	        	i++;
	        }

			return null;
		}

		protected void onPostExecute(final String result) {
			
			
			lv1.setAdapter(new ArrayAdapter<String>(DisplayScheduleActivity.this, R.layout.item_list, (String[])listview_arr.toArray(new String[listview_arr.size()])));

			lv1.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					String itemContent = (String)lv1.getItemAtPosition(position);
					
					Log.d("ScheduleActivity","getItemAtPosition:"+itemContent);
					Log.d("ScheduleActivity", "pos:"+position+" id:"+id);
					
					
					Intent intent = new Intent(DisplayScheduleActivity.this, DisplayBusStopActivity.class);
					intent.putExtra("busStopID",position_ids_arr[position]);
					startActivity(intent);
					
				}
				});
			if (this.dialog.isShowing()) 
				this.dialog.dismiss();
			
			//Context context = getApplicationContext();
			CharSequence text = "Press menu to change route directions";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(DisplayScheduleActivity.this, text, duration);
			toast.show();
		}


    }
}
