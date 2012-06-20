/*
 * @author Paulino Calderon <paulino@calderonpale.com>
 */
package paulino.calderon.android.bcbus;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.content.ContentValues;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DBAdapter {
	private final Context context; 
	private BusDBHelper DBHelper;
	private SQLiteDatabase db;

	private static final String DATABASE_NAME = "bctransit_assistant.db";
	private static final int DATABASE_VERSION = 3;
	private static final String BUS_STOPS_TABLE = "bus_stops";
	private static final String BUS_ROUTES_TABLE = "bus_routes";
	private static final String BUS_SCHEDULES_TABLE = "bus_schedules";
	
	
	
	public DBAdapter(Context ctx) 
	{
		this.context = ctx;
        DBHelper = new BusDBHelper(context);
    }
	/*
	 * This class manages the communication with SQLite. 
	 */
	private static class BusDBHelper extends SQLiteOpenHelper {
		   
		   BusDBHelper(Context context) {
		       super(context, DATABASE_NAME, null, DATABASE_VERSION);
		 
		   }
		   
		      /*
		       * onCreate(SQLiteDatabase db)
		       * Initializes and populates database. 
		       * Tables bus_stops, bus_routes and bus_schedules are created and populated.
		       * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
		       */
		      public void onCreate(SQLiteDatabase db) {
		    	  
		    	  //Create bus_stops table
		    	  db.execSQL("CREATE TABLE " + BUS_STOPS_TABLE +
		         	"(id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL, category INTEGER, bus_route_id INTEGER, name TEXT, direction INTEGER)");
		    	  //Create bus_routes table
		    	  db.execSQL("CREATE TABLE " + BUS_ROUTES_TABLE +
		       		"(id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER, name TEXT, start_lat REAL, start_long REAL, end_lat REAL, end_long REAL)");
		    	  //Create bus_schedules table
		    	  db.execSQL("CREATE TABLE " + BUS_SCHEDULES_TABLE +
		       		"(id INTEGER PRIMARY KEY AUTOINCREMENT, bus_stop_id INTEGER, time TEXT, day INTEGER)");
		    	 
		      }

		      /*
		       * Function performed when database got upgraded. Drops old tables and runs onCreate rutine
		       * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
		       */
		      @Override
		      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		         Log.d("Example", "Upgrading database, this will drop tables and recreate.");
		         db.execSQL("DROP TABLE IF EXISTS " + BUS_STOPS_TABLE);
		         db.execSQL("DROP TABLE IF EXISTS " + BUS_ROUTES_TABLE);
		         db.execSQL("DROP TABLE IF EXISTS " + BUS_SCHEDULES_TABLE);
		         onCreate(db);
		         
		         
		      }
		      
	}
	/*
	 * Opens the database connection	      
	 */
	public DBAdapter open() throws SQLException 
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	public void initTransaction() {
		db.beginTransaction();
	}
	public void finishTransaction() {
		db.endTransaction();
	}
	public void setTransactionOK() {
		db.setTransactionSuccessful();
	}
	
	public SQLiteStatement compileInsertRouteStmnt() {
		String sql = "insert into "+BUS_ROUTES_TABLE+" (number,name,start_lat,start_long,end_lat,end_long) values (?,?,?,?,?,?)";
		SQLiteStatement insertStmnt=db.compileStatement(sql);
		
		return insertStmnt;
	}
	public SQLiteStatement compileInsertStopStmnt() {
		String sql = "insert into "+BUS_STOPS_TABLE+" (latitude, longitude, category, bus_route_id, name, direction) values (?,?,?,?,?,?)";
		SQLiteStatement insertStmnt=db.compileStatement(sql);
		
		return insertStmnt;
	}
	public SQLiteStatement compileInsertScheduleStmnt() {
		String sql = "insert into "+BUS_SCHEDULES_TABLE+" (bus_stop_id, time, day) values (?,?,?)";
		SQLiteStatement insertStmnt=db.compileStatement(sql);
		
		return insertStmnt;
	}
	/*
	 * Returns id from last insert - helper
	 */
	public int lastInsertID() {
		int id=0;
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid()",null);
		if (cursor.moveToFirst())
				id=Integer.valueOf(cursor.getString(0));
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return id;
	}
	/*
	 * Returns directions
	 */
	public List<String> getRouteDirections(int routeID) {
		List<String> directions_list= new ArrayList<String>();

		Cursor cursor = db.rawQuery("SELECT DISTINCT direction FROM "+BUS_STOPS_TABLE+" WHERE bus_route_id="+routeID,null);
		
		if (cursor.moveToFirst()) {
			do {
				directions_list.add(cursor.getString(0)); 
		    } while (cursor.moveToNext());
		}
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return directions_list;
	}
	/*
	 * Insert bus route - helper
	 */
	public void insertBusRoute(String number, String name, String start_lat, String end_lat, String start_long, String end_long) {

		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("name", name);
		values.put("start_lat", start_lat);
		values.put("start_long", start_long);
		values.put("end_lat",end_lat);
		values.put("end_long",end_long);
	
		try {
			db.insertOrThrow(BUS_ROUTES_TABLE, null, values);
			Log.d("DBAdapter","Bus route was added succesfully");
		} catch (Exception e) {
			Log.d("DBAdapter","Insert bus route failed:"+e.toString());
		}
	}
	/*
	 * Inserts bus stop - helper
	 */
	public void insertBusStop(String stop_name, String stop_category, String stop_direction, String stop_latitude, String stop_longitude, String busRouteId) {
	
		ContentValues values = new ContentValues();
		values.put("name", stop_name);
		values.put("latitude", stop_latitude);
		values.put("longitude", stop_longitude);
		values.put("category", stop_category);
		values.put("bus_route_id", busRouteId);
		values.put("direction",stop_direction);
		
		try {
			db.insertOrThrow(BUS_STOPS_TABLE, null, values);
			Log.d("DBAdapter","Bus stop was added succesfully");
		} catch (Exception e) {
			Log.d("DBAdapter","Insert bus stop failed:"+e.toString());
		}
	}
	/*
	 * Inserts bus schedule - helper
	 */
	public void insertBusSchedule (String bus_stop_id, String time, String day) {
		
		ContentValues values = new ContentValues();
		values.put("bus_stop_id", bus_stop_id);
		values.put("time", time);
		values.put("day", day);
		if(time!="-") {
			try {
				db.insertOrThrow(BUS_SCHEDULES_TABLE, null, values);
				Log.d("DBAdapter","Bus schedule was added succesfully");
			} catch (Exception e) {
				Log.d("DBAdapter","Insert bus schedule failed:"+e.toString());
			}
		}
	}
	/*
	* populateDummyData(SQLiteDatabase db)
	* Populates tables with dummy data for testing purposes
	*/
	
	public void populateDummyData() {
		Log.d("SQLITE","Populating database with dummy data.");
		    	  
		//Inserting test row
		ContentValues dummyValues = new ContentValues();
		dummyValues.put("number", "4");
		dummyValues.put("name", "UVIC/DOWNTOWN VIA HILLSIDE");
		dummyValues.put("start_lat", "48.471588");
		dummyValues.put("start_long", "-123.353245");
		dummyValues.put("end_lat","48.471588");
		dummyValues.put("end_long","-123.346089");
		
		try {
			db.insertOrThrow(BUS_ROUTES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("Example","Populating dummy data failed:"+e.toString());
		}
		
		dummyValues = new ContentValues();
		dummyValues.put("number", "14");
		dummyValues.put("name", "DOWNTOWN");
		dummyValues.put("start_lat", "48.471588");
		dummyValues.put("start_long", "-123.353245");
		dummyValues.put("end_lat","48.471588");
		dummyValues.put("end_long","-123.346089");
		
		try {
			db.insertOrThrow(BUS_ROUTES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("Example","Populating dummy data failed:"+e.toString());
		}
		
		dummyValues = new ContentValues();
		dummyValues.put("number", "31");
		dummyValues.put("name", "ROYAL OAK");
		dummyValues.put("start_lat", "48.471588");
		dummyValues.put("start_long", "-123.353245");
		dummyValues.put("end_lat","48.471588");
		dummyValues.put("end_long","-123.346089");
		
		try {
			db.insertOrThrow(BUS_ROUTES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("Example","Populating dummy data failed:"+e.toString());
		}
		    	  
		dummyValues = new ContentValues();
		dummyValues.put("latitude", "48.471696");
		dummyValues.put("longitude", "-123.34585");
		dummyValues.put("category", "1");
		dummyValues.put("bus_route_id", "1");
		dummyValues.put("name","Hillside at Quadra");
		dummyValues.put("direction","1");
		try {
			db.insertOrThrow(BUS_STOPS_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("SQLITE","Populating dummy data failed."+e.toString());
		}
		
		dummyValues = new ContentValues();
		dummyValues.put("bus_stop_id", "1");
		dummyValues.put("time", "06:00");
		dummyValues.put("day", "1");
		try {
			db.insertOrThrow(BUS_SCHEDULES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("SQLITE","Populating dummy data failed."+e.toString());
		}
		dummyValues = new ContentValues();
		dummyValues.put("bus_stop_id", "1");
		dummyValues.put("time", "07:00");
		dummyValues.put("day", "1");
		try {
			db.insertOrThrow(BUS_SCHEDULES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("SQLITE","Populating dummy data failed."+e.toString());
		}
		dummyValues = new ContentValues();
		dummyValues.put("bus_stop_id", "1");
		dummyValues.put("time", "08:00");
		dummyValues.put("day", "1");
		try {
			db.insertOrThrow(BUS_SCHEDULES_TABLE, null, dummyValues);
		} catch (Exception e) {
			Log.d("SQLITE","Populating dummy data failed."+e.toString());
		}
		
	}

	/*
	 * Closes the database connection.
	 */
	public void close() 
	{
		DBHelper.close();
	}
		      
	/*
	 * Returns a JSON list with all the bus routes in the database
	 */
	public List<String> selectAllBusRoutes() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = db.query(BUS_ROUTES_TABLE, 
				new String[] { "id", "number","name","start_lat","start_long","end_lat","end_long" }, 
		        null, null, null, null, "number asc");
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0)+":"+cursor.getString(1)+":"+cursor.getString(2)+":"
		                		+cursor.getString(3)+":"+cursor.getString(4)+":"+cursor.getString(5)+":"+cursor.getString(6)); 
		    } while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
			return list;
		}
	

	/*
	* Returns JSON like data containing all the bus stops in the database
	*/
	public List<String> selectAllBusStops() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = db.query(BUS_STOPS_TABLE, 
				new String[] { "id","latitude","longitude","category","bus_route_id","name"}, 
		        null, null, null, null, "id desc");
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0)+":"+cursor.getString(1)+":"+cursor.getString(2)+":"
		                		+cursor.getString(3)+":"+cursor.getString(4)+":"+cursor.getString(5)); 
		    } while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return list;
	}
	/*
	* Returns JSON like data containing all the bus stops in the database
	*/
	public List<String> selectAllBusStopsByDirection(int direction, int routeID) {
		List<String> list = new ArrayList<String>();
		Cursor cursor = db.query(BUS_STOPS_TABLE, 
				new String[] { "id","latitude","longitude","category","bus_route_id","name"}, 
		        "direction="+direction+" and bus_route_id="+routeID, null, null, null, "id desc");
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0)+":"+cursor.getString(1)+":"+cursor.getString(2)+":"
		                		+cursor.getString(3)+":"+cursor.getString(4)+":"+cursor.getString(5)); 
		    } while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		return list;
	}
	/*r 
	 * Returns integer code representing default direction for bus route
	 * Directions integer codes:
	 * 1:NORTH
	 * 2:NORTHEAST
	 * 3:EAST
	 * 4:SOUTHEAST
	 * 5:SOUTH
	 * 6:SOUTHWEST
	 * 7:WEST
	 * 8:NORTHWEST
	 */
	public int selectBusStopDefaultDirection(int routeID) {
		int direction=-1;
		Cursor cursor = db.query(BUS_STOPS_TABLE, new String[]{"direction"},"bus_route_id="+routeID,null,null,null, null);
		
		if(cursor.moveToFirst())
				direction=Integer.valueOf(cursor.getString(0));

		if(cursor != null && !cursor.isClosed()) 
			cursor.close();
		
		return direction;
	}

	/*
	* Returns bus schedule filtered by bus stop and day
	*/
	
	public List<String> selectScheduleByBusStop(int busStopId, int day) {
		String day_str=null;
		switch(day){
		case 1:
			day_str = "Weekday";
			break;
		case 6:
			day_str = "SAT";
		break;
		case 7:
			day_str = "SUN";
		break;
		}
		List<String> list = new ArrayList<String>();
		Cursor cursor = db.query(BUS_SCHEDULES_TABLE, 
				new String[] { "id","time","day","bus_stop_id" }, 
		        "bus_stop_id="+busStopId+" and day='"+day_str+"'", null, null, null, "id asc");
		    	  
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0)+"-"+cursor.getString(1)+"-"+cursor.getString(2)+"-"
		                		+cursor.getString(3)); 
		    } while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	/*
	 * getRouteName
	 * Returns the route name/title given a bus stop ID
	 */
	public String getRouteName(int busStopId) {
		Log.d("DEBUG","getRouteName:"+busStopId);
		String routeName=null;
		Cursor cursor = db.query(BUS_ROUTES_TABLE, new String[] {"name"}, "id="+busStopId, null, null, null, null);
		
		if(cursor.moveToFirst())
			routeName=cursor.getString(0);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return routeName;
	}
	/*
	 * getRouteName
	 * Returns the route name/title given a bus stop ID
	 */
	public String getBusStopName(int busStopId) {
		Log.d("DEBUG","getBusStopName:"+busStopId);
		String routeName=null;
		Cursor cursor = db.query(BUS_STOPS_TABLE, new String[] {"name"}, "id="+busStopId, null, null, null, null);
		
		if(cursor.moveToFirst())
			routeName=cursor.getString(0);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return routeName;
	}
	/*
	 * deleteAll tables
	 */
	public void deleteAll() {
		db.delete(BUS_STOPS_TABLE, null, null);
		db.delete(BUS_ROUTES_TABLE, null, null);
		db.delete(BUS_SCHEDULES_TABLE, null, null);
	}
}
