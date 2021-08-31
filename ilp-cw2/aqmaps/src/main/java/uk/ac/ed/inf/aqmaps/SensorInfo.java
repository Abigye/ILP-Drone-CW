package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class SensorInfo {
	private Point point;    	// an instance variable to hold sensor point
	private String location;	// an instance variable to hold sensor location
	private float battery;		// an instance variable to hold sensor battery level
	private String reading;		// an instance variable to hold air quality reading
	
	/*constructor for this class*/
	public SensorInfo(Point point,String location,float battery,String reading) {
		this.point=point;
		this.location =location;
		this.battery = battery;
		this.reading = reading;
	}
	
	/* gets sensor point */
	public Point getPoint(){
		return point;
	}
	
	/* gets sensor location */
	public String getLocation() {
		return location;
	}
	
	/* gets sensor battery level */
	public float getBattery() {
		return battery;
	}
	
	/* gets air quality reading the sensor holds */
	public String getReading() {
		return reading;
	}
}
