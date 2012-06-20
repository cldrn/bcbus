package calderon.android.bctransit_assistant.objects;

import java.util.List;

public class BusStop {
	private String name;
	private String latitude;
	private String longitude;
	private int category;
	private int direction;
	private List<BusSchedule> schedules;
	
	/*
	 * Returns bus stop name
	 */
	public String getName() {
		return name;
	}
	/*
	 * Returns bus stop latitude
	 */
	public String getLatitude() {
		return latitude;
	}
	/*
	 * Returns bus stop longitude
	 */
	public String getLongitude() {
		return longitude;
	}
	/*
	 * Returns bus stop category
	 */
	public int getCategory() {
		return category;
	}
	/*
	 * Returns bus stop direction
	 */
	public int getDirection() {
		return direction;
	}
	/*
	 * Returns list of BusSchedule objects for the bus stop
	 */
	public List<BusSchedule> getSchedules() {
		return schedules;
	}
}
