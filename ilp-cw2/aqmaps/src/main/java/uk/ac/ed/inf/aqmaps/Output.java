package uk.ac.ed.inf.aqmaps;



public class Output {
	private int move;  			// holds  the move number for a path taken
	private double prevLng;		// holds the longitude of previous position of drone
	private double prevLat;		// holds the latitude of previous position of drone
	private int angle;          // holds the angle for a taken path
	private double curLng; 		// holds the longitude of drone's current position
	private double curLat;		// holds the latitude of drone's current position
	private String location;	// holds location (what3words) of the visited sensor
	private float battery;		// holds the battery level of the visited sensor
	private String reading;		// holds the air quality reading 

	/* constructor for class */
	public Output(int move, double prevLng, double prevLat, int angle, double curLng, double curLat, String location,
			float battery, String reading) {
		this.move = move;
		this.prevLng = prevLng;
		this.prevLat = prevLat;
		this.angle = angle;
		this.curLng = curLng;
		this.curLat = curLat;
		this.location = location;
		this.battery = battery;
		this.reading = reading;
	}
	
	/* gets sensor's battery level */
	public double getBattery() {
		return battery;
	}
	
	/* gets air quality reading */
	public String getReading() {
		return reading;
	}
	
	/* gets longitude of drone's previous position */
	public double getPrevLng() {
		return prevLng;
	}
	
	/* gets latitude of drone's previous position */
	public double getPrevLat() {
		return prevLat;
	}
	
	/* gets longitude of drone's current position */
	public double getCurLng() {
		return curLng;
	}
	
	/* gets latitude of drone's current position */
	public double getCurLat() {
		return curLat;
	}

	/* gets what3words location of visited sensor */
	public String getLocation() {
		return location;
	}
	
	/* returns the expected format of the flight path in a string form */
	@Override
	public String toString() {
		return String.format("%d,%f,%f,%d,%f,%f,%s", this.move, this.prevLng, this.prevLat, this.angle, this.curLng,
				this.curLat, this.location);

	}

}
