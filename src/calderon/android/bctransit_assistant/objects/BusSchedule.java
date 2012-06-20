package calderon.android.bctransit_assistant.objects;

import java.util.List;

public class BusSchedule {
	private String day;
	private List<BusTime> times;
	/*
	 * Returns day of this bus schedule
	 */
	public String getDay() {
		return day;
	}
	/*
	 * Returns list of schedule times for this schedule
	 */
	public List<BusTime> getTimes() {
		return times;
	}
}
