package calderon.android.bctransit_assistant.objects;

import java.util.List;

public class BusRoutes {
	private String created;
	private List<BusRoute> routes;

	/*
	 * Returns created timestamp
	 */
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created=created;
	}
	/*
	 * Returns list of bus routes
	 */
	public List<BusRoute> getRoutes() {
		return routes;
	}
	public void setRoutes(List<BusRoute> routes) {
		this.routes=routes;
	}
	
	
}
