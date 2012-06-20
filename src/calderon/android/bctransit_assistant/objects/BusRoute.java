package calderon.android.bctransit_assistant.objects;

import java.util.List;

public class BusRoute {
    private String routeNumber;
    private String routeName;
    private String routeLatStart;
    private String routeLatEnd;
    private String routeLongStart;
    private String routeLongEnd;
    private List<BusStop> stops;
    /*
     * Return route name
     */
    public String getName() {
        return routeName;
    }
    /*
     * Return route number
     */
    public String getNumber() {
        return routeNumber;
    }
    /*
     * Return route latitude start point
     */
    public String getLatStart() {
        return routeLatStart;
    }
    /*
     * Return route latitude end point
     */
    public String getLatEnd() {
        return routeLatEnd;
    }
    /*
     * Return route longitude start point
     */
    public String getLongStart() {
        return routeLongStart;
    }
    /*
     * Return route longitude end point
     */
    public String getLongEnd() {
        return routeLongEnd;
    }
    /*
     * Returns list of busStops for this route
     */
    public List<BusStop> getStops() {
    	return stops;
    }
}