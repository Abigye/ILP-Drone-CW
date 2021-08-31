package uk.ac.ed.inf.aqmaps;

import java.util.*;
import com.mapbox.geojson.*;

public class Movement {

	private static final double LINE_DIST = 0.0003; //holds the fixed distance for move
	public static List<SensorInfo> sensorInfoList = getSensorInfoList(); // holds sensors and  their details
	public List<Output> outputs = new ArrayList<>(); // holds the flight path taken

	/*gets sensors and their details*/
	private static List<SensorInfo> getSensorInfoList() {
		List<SensorInfo> sensorDetails = new ArrayList<SensorInfo>();
		for (AirQualityData data : ServerData.airQualitySensors) {
			Words word = ServerData.getLocationDetails(data.location);
			sensorDetails.add(
					new SensorInfo(Point.fromLngLat(word.getCoordinates().getLng(), word.getCoordinates().getLat()),
							data.location, data.battery, data.reading));
		}
		return sensorDetails;
	}
	
	/*calculates the distance between two points p1,p2*/
	private double calculateDistance(Point p1, Point p2) {
		double diffX = p1.longitude() - p2.longitude();
		double diffY = p1.latitude() - p2.latitude();
		double dis = (diffX * diffX) + (diffY * diffY);

		return Math.sqrt(dis);
	}

	/*calculates the next position given current position and angle*/
	private Point calculateNextPoint(Point p, double angle) {
		double nextLng = p.longitude() + (LINE_DIST * Math.cos(angle));
		double nextLat = p.latitude() + (LINE_DIST * Math.sin(angle));
		Point nextP = Point.fromLngLat(nextLng, nextLat);
		return nextP;
	}
	
	/* gets all the sensors*/
	private List<Point> getSensors() {
		var p = new ArrayList<Point>();
		for (SensorInfo s : sensorInfoList) {
			p.add(s.getPoint());
		}
		return p;
	}

	/*finds the nearest sensor */
	private Point findNearestSensor(Point curPoint, List<Point> points) {
		Map<Point, Double> map = new HashMap<>();
		for (Point q : points) {
			map.put(q, calculateDistance(curPoint, q));
		}

		double min = calculateDistance(curPoint, points.get(0));
		Point point = points.get(0);
		for (Point p : map.keySet()) {
			if (map.get(p) < min) {
				min = map.get(p);
				point = p;
			}
		}
		return point;
	}
	
	/*gets the details of a particular sensor*/
	private SensorInfo getDataReadingForPoint(Point p) {
		SensorInfo pointDetails = new SensorInfo(p, "null", 0.0f, "null");
		for (SensorInfo q : sensorInfoList) {
			if (p == q.getPoint()) {
				pointDetails = new SensorInfo(p, q.getLocation(), q.getBattery(), q.getReading());
			}

		}
		return pointDetails;

	}
	
	/* find the driection of travel of the drone*/
	private double findDirectionOfTravel(Point curPoint, Point nearestPoint) {
		Map<Double, Double> map = new TreeMap<>();
		/*
		 * for each angle btn 0 and 350, find next position using current position and
		 * the angle then find the distance between that next position and nearest
		 * sensor, store the angle and distance in a tree map
		 */
		for (double i = 0; i < 360.0; i += 10.0) {
			Point nextPoint = calculateNextPoint(curPoint, i);
			double distance = calculateDistance(nextPoint, nearestPoint);
			map.put(distance, i);
		}
		// find angle that gives minimum distance to nearest sensor point
		ArrayList<Double> angles = new ArrayList<Double>(map.keySet());
		double minDist = angles.get(0);
		return map.get(minDist);
	}

	/*controls the drone movement*/
	public List<Output> moveDrone() {
		List<Point> pointsRemaining = getSensors();

		Point startPoint = Point.fromLngLat(Double.parseDouble(App.getArgs()[4]), Double.parseDouble(App.getArgs()[3]));
		Point nearestPoint = findNearestSensor(startPoint, pointsRemaining); // nearest sensor to our startpoint
		Point dronePoint = startPoint; // current position of drone
		Point prevPoint = startPoint; // previous position of drone

		int moveCountLimit = 150;
		int moveCount = 0;
		double flightAngle = findDirectionOfTravel(dronePoint, nearestPoint); // direction of travel
		
		Point nextPoint = calculateNextPoint(dronePoint, flightAngle); // next poistion of the drone
		Zones  zones = new Zones();
		while (moveCount < moveCountLimit) {
			if (zones.isPointInConfinementArea(nextPoint) == true) {
				if(zones.doesLineIntersectBuildings(dronePoint, nextPoint) == false) {
					prevPoint = dronePoint;
					dronePoint = nextPoint;
					moveCount += 1;
					
					if(calculateDistance(dronePoint,nearestPoint)< 0.0002) {
					/*if the drone moves to a position within 0.0002 of a sensor, reading is taken
					and path is recorded*/
					
						outputs.add(new Output(moveCount, prevPoint.longitude(), prevPoint.latitude(), (int) flightAngle,
								dronePoint.longitude(), dronePoint.latitude(),
								getDataReadingForPoint(nearestPoint).getLocation(),
								getDataReadingForPoint(nearestPoint).getBattery(),
								getDataReadingForPoint(nearestPoint).getReading()));
						
						pointsRemaining.remove(nearestPoint); // removethat sensor from list of sensors
						
						if(pointsRemaining.size()<5) { /*if the size of our list of sensors is less than 5, add start point*/
							pointsRemaining.add(startPoint);
							/* if the drone is within 0.0003 from our start point, we stop moving */
							if(calculateDistance(dronePoint,startPoint) < LINE_DIST) {
								break;
							}
						}
						
						prevPoint = dronePoint;
						nearestPoint = findNearestSensor(dronePoint, pointsRemaining);
						flightAngle = findDirectionOfTravel(dronePoint, nearestPoint);
						nextPoint = calculateNextPoint(dronePoint, flightAngle);
						
					}else {/* if the drone moves to a position which is not within 0.0002 of a sensor, no reading is taken
					but path is recorded*/
						outputs.add(new Output(moveCount, prevPoint.longitude(), prevPoint.latitude(), (int) flightAngle,
								dronePoint.longitude(), dronePoint.latitude(),
								"null",0.0f, "null"));
						
						prevPoint = dronePoint;
						nearestPoint = findNearestSensor(dronePoint, pointsRemaining);
						flightAngle = findDirectionOfTravel(dronePoint, nearestPoint); 
						nextPoint = calculateNextPoint(dronePoint, flightAngle);		
					}

				}else { /* if the path to be taken lies in a no fly zone, find another direction
				 of travel that gets the drone away from no fly zone and find the next position*/
					flightAngle = getAngle(flightAngle, dronePoint);
					nextPoint = calculateNextPoint(dronePoint, flightAngle);	
				}
				
			}else {
				dronePoint = prevPoint; 						// go back to previous position
				moveCount += 1; 								// increase number of moves taken by 1
				pointsRemaining.remove(nearestPoint);		 	// remove that sensor
				if (pointsRemaining.isEmpty()) { /* if our list of sensor is empty, nearest sensor becomes start point */
					pointsRemaining.add(startPoint);

					// if the drone is within 0.0003 from our start point, we stop moving
					if (calculateDistance(dronePoint, startPoint) < LINE_DIST) {
						break;
					}
				} else { // if our list of sensor is empty is not empty
					nearestPoint = findNearestSensor(dronePoint, pointsRemaining); // find new nearest sensor
					flightAngle = findDirectionOfTravel(dronePoint, nearestPoint); // find new flight angle to that
					nextPoint = calculateNextPoint(dronePoint, flightAngle); // find next Point to move to.
				}
			}
				  			
		}

		return outputs;

	}

	/* gets an angle that allows the drone to move away from no fly zones*/
	private double getAngle(double flightAngle, Point curPoint) {
		double previous = flightAngle;
		Zones zones = new Zones();
		List<Double> angles = new ArrayList<>(); 

		for (double i = 0; i < 350; i += 10) {
			angles.add(i);
		}

		for (double j = -350; j < 0; j += 10) {
			angles.add(j);
		}

		for (int i = 0; i < angles.size(); i++) {
			if (flightAngle >= 0 && flightAngle <= 350) {
				/*calculate next point*/
				Point nextPoint = calculateNextPoint(curPoint, flightAngle);
				if (zones.doesLineIntersectBuildings(curPoint, nextPoint) == false) {
					/*check if path from current position to next point using the flight angle
					 * intersects a no fly zone, if not return the angle*/
					return flightAngle;

				} else { /* if there is an intersection, add other angles to it to change direction
				 and check again*/
					flightAngle = flightAngle + angles.get(i);
				}
			} else { /* if  flight angle is less than 0 or greater thann 350, set it back to its prevoius value
			and keep adding angles until an approriate angle is found */
				flightAngle = previous;
			}
		}
		return 0.0;

	}

}
