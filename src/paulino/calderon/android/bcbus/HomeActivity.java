/*
 * @author Paulino Calderon <paulino@calderonpale.com>
 */
package paulino.calderon.android.bcbus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;


public class HomeActivity extends Activity {
	private final String ACTIVITY_CLASS_NAME="HomeActivity";
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String cityPref = settings.getString("cityPref", "null");
        
        if(cityPref.equals("null")) {
        	Log.d(ACTIVITY_CLASS_NAME, "BCTransit community is not set");
        	alertSetupNeeded();
        } else {
        	Log.d(ACTIVITY_CLASS_NAME, "BCTransit community setting:"+cityPref);
        }
    	
        
    }
    /*
     * Alerts users additional setup is needed
     */
    public void alertSetupNeeded() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.firstrun_message)
        	.setTitle(R.string.firstrun_setup_title) 
        	.setIcon(R.drawable.ic_about)
        	.setCancelable(false)
       .setPositiveButton(R.string.firstrun_setup_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        	 launchSettings();
                 }
       })
       .setNegativeButton(R.string.firstrun_setup_cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                	 dialog.cancel();
                   }
               }
     );
        AlertDialog alert = builder.create();
        alert.show();    	
    }
    /*
     * Launches ScheduleActivity Intent
     */
    public void onScheduleClick(View v) {
    	try {
    		startActivity(new Intent(this, ScheduleActivity.class));
    	} catch (Exception e) {
    		Log.d("DEBUG","Cant start Schedule activity:"+e.toString());
    	}
    }
    
    /*
     * Launches ScheduleActivity Intent
     */
    public void onAboutClick(View v) {
    	showAboutDialog();
    }
    /*
     * Displays the about dialog
     */
    public void showAboutDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final SpannableString s = 
            new SpannableString(this.getText(R.string.about_message));
    	Linkify.addLinks(s, Linkify.WEB_URLS);
        builder.setMessage(s)
          .setTitle(R.string.title_about)
          .setIcon(R.drawable.ic_about)
          .setCancelable(false)
       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
         dialog.cancel();
                 }
       });
        AlertDialog alert = builder.create();
        alert.show();    	
    }
    /*
     * Launches PreferencesActivity Intent 
     */
    public void onSettingsClick(View v) {
    	try {
    		startActivity(new Intent(this, PreferencesActivity.class));
    	} catch (Exception e) {
    		Log.d("DEBUG","Cant start Settings activity:"+e.toString());
    	}
    }
    /*
     * Launches PreferencesActivity Intent
     */
    public void launchSettings() {
    	try {
    		startActivity(new Intent(this, PreferencesActivity.class));
    	} catch (Exception e) {
    		Log.d("DEBUG","Cant start Settings activity:"+e.toString());
    	}
    }
    
   
}