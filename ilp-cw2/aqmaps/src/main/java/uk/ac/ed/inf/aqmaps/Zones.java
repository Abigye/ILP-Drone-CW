package uk.ac.ed.inf.aqmaps;

import java.util.List;
import com.mapbox.geojson.*;

public class Zones {
	private final double MIN_LNG = -3.192473; // smallest longitude value of confinement area
	private final double MAX_LNG = -3.184319; // largest longitude value of confinement area
	private final double MIN_LAT = 55.942617; // smallest latitude value of confinement area
	private final double MAX_LAT = 55.946233; // largest latitude value of confinement area

	public static List<Polygon> buildings = ServerData.polygons; // holds the no fly zones


	
	/*
	 * checks if a point lies in the confinement area and returns true if that is
	 * the case and fals otherwise
	 */
	public boolean isPointInConfinementArea(Point p) {
		if (p.longitude() < MAX_LNG && p.longitude() > MIN_LNG && p.latitude() < MAX_LAT && p.latitude() > MIN_LAT) {
			return true;
		}
		return false;
	}

	/*
	 * Given three collinear points p, q, r, the function checks if point q lies on
	 * line segment 'pr'
	 */
	private boolean onLineSegment(Point p, Point q, Point r) {
		if (q.longitude() <= Math.max(p.longitude(), r.longitude())
				&& q.longitude() >= Math.min(p.longitude(), r.longitude())
				&& q.latitude() <= Math.max(p.latitude(), r.latitude())
				&& q.latitude() >= Math.min(p.latitude(), r.latitude())) {
			return true;
		}

		return false;
	}

	/*
	 * finds orientation of three points (p, q, r) and the following values 0 --> p,
	 * q and r are collinear 1 --> Clockwise 2 --> Counterclockwise
	 */
	private int findOrientation(Point p, Point q, Point r) {
		double value = (q.latitude() - p.latitude()) * (r.longitude() - q.longitude())
				- (q.longitude() - p.longitude()) * (r.latitude() - q.latitude());

		if (value == 0.0) {
			return 0; // collinear
		}

		return (value > 0.0) ? 1 : 2; // clock or counterclock wise
	}

	// The main function that returns true if line segment 'p1q1'
	// and 'p2q2' intersect.
	private boolean doLinesIntersect(Point p1, Point q1, Point p2, Point q2) {
		// Find the four orientations needed for general and special cases
		var o1 = findOrientation(p1, q1, p2); // infers int
		var o2 = findOrientation(p1, q1, q2); // infers int
		var o3 = findOrientation(p2, q2, p1); // infers int
		var o4 = findOrientation(p2, q2, q1); // infers int

		// General case
		if (o1 != o2 && o3 != o4) {
			return true;
		}
		// Special Cases
		// p1, q1 and p2 are collinear and p2 lies on segment p1q1
		if (o1 == 0 && onLineSegment(p1, p2, q1)) {
			return true;
		}

		// p1, q1 and q2 are collinear and q2 lies on segment p1q1
		if (o2 == 0 && onLineSegment(p1, q2, q1)) {
			return true;
		}

		// p2, q2 and p1 are collinear and p1 lies on segment p2q2
		if (o3 == 0 && onLineSegment(p2, p1, q2)) {
			return true;
		}

		// p2, q2 and q1 are collinear and q1 lies on segment p2q2
		if (o4 == 0 && onLineSegment(p2, q1, q2)) {
			return true;
		}

		return false; // Doesn't fall in any of the above cases
	}

	/* checks if a line segment pq intersects a polygon */
	private boolean doesLineIntersectsPolygon(Point p, Point q, Polygon polygon) {
		LineString outerPolyLines = polygon.outer(); // getting the outer perimeter of the polygon
		var outerPolyPoints = outerPolyLines
				.coordinates(); /*
								 * infers List(a list of the points that forms the perimeter of a polygon
								 */
		int n = outerPolyPoints.size();
		for (int i = 0; i < n; i++) {
			if (i < n - 1) {
				if (doLinesIntersect(p, q, outerPolyPoints.get(i), outerPolyPoints.get(i + 1))) {
					/*
					 * check if line segment formed from curPoint and nextPoint intersects any of
					 * the line segments formed from the ordered points in the list and return true
					 * if that is the case
					 */
					return true;
				} else {
					continue;
				}
			}
			if (i == n - 1) {
				if (doLinesIntersect(p, q, outerPolyPoints.get(i), outerPolyPoints.get(0))) {
					/*
					 * check if line segment formed from curPoint and nextPoint intersects line
					 * segment formed from the last point and first one and return true if that is
					 * the case
					 */
					return true;
				}
			}
		}
		return false; // no intersection at all
	}

	/*
	 * checks if a line segment pq intersects any of the no fly zones stored in
	 * building variable
	 */
	public boolean doesLineIntersectBuildings(Point p, Point q) {
		for (var poly : buildings) { // infers Polygon (the nofly zones stored in the buildings variable)
			if (doesLineIntersectsPolygon(p, q, poly)) {
				/*
				 * check if the line segment segment formed from curPoint and nextPoint
				 * intersects any of the no fly zone buildings, return true if that is the case
				 */
				return true;
			} else {
				continue;
			}
		}
		return false; // no intersection at all
	}
}
