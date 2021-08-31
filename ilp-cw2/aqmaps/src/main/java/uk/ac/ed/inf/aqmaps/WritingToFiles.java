package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mapbox.geojson.*;


public class WritingToFiles {
	private Movement movement = new Movement(); // instance of Movement class
	
	
	
	/* gets the color of the sensor using the air quality reading and the battery level*/
	private String getColor(String reading, float battery) {
		String rgbString = ""; 
		if (battery < 10.0f) {
			rgbString = "#000000";
		} else { // if battery is greater than 10.0
			if (Double.valueOf(reading) >= 0.0 && Double.valueOf(reading) < 32.0) {
				rgbString = "#00ff00";
			} else if (Double.valueOf(reading) >= 32.0 && Double.valueOf(reading) < 64.0) {
				rgbString = "#40ff00";
			} else if (Double.valueOf(reading) >= 64.0 && Double.valueOf(reading) < 96.0) {
				rgbString = "#80ff00";
			} else if (Double.valueOf(reading) >= 96.0 && Double.valueOf(reading) < 128.0) {
				rgbString = "#c0ff00";
			} else if (Double.valueOf(reading) >= 128.0 && Double.valueOf(reading) < 160.0) {
				rgbString = "#ffc000";
			} else if (Double.valueOf(reading) >= 160.0 && Double.valueOf(reading) < 192.0) {
				rgbString = "#ff8000";
			} else if (Double.valueOf(reading) >= 192.0 && Double.valueOf(reading) < 224.0) {
				rgbString = "#ff4000";
			} else if (Double.valueOf(reading) >= 224.0 && Double.valueOf(reading) < 256.0) {
				rgbString = "#ff0000";
			}
		}
		return rgbString;
	}

	/* gets the marker symbol of the sensor using the air quality reading and the battery level*/
	private String getMarkerSymbol(String reading, float battery) {
		String markerSymbol = ""; 
		if (battery < 10.0f) {
			markerSymbol = "cross";
		} else { // if battery greater than 10.0
			if (Double.valueOf(reading) >= 0.0 && Double.valueOf(reading) < 128.0) {
				markerSymbol = "lighthouse";
			} else if (Double.valueOf(reading) >= 128.0 && Double.valueOf(reading) < 256.0) {
				markerSymbol = "danger";
			}
		}
		return markerSymbol;
	}
	
	/* gets the sensors which were visited from the List of flight path taken */
	private List<Point> getVisitedPoints(List<Output> outputs) {
		List<Point> visited = new ArrayList<Point>();
		for (Output q : outputs) {
			for (SensorInfo p : Movement.sensorInfoList) {
				if (q.getLocation().equals(p.getLocation())) {
					/*
					 * checks if the what3words location of a flight path is same as that of a
					 * sensor if so, that sensor was visited so it is added to a list
					 */
					visited.add(p.getPoint());
				}

			}
		}
		return visited;

	}

	
	/*  create a feature coolection from the list of flight paths taken */
	private FeatureCollection createGeoJsonFeatureCollection(List<Output> outputs) {
		List<Feature> features = new ArrayList<Feature>();
		for (SensorInfo t :Movement.sensorInfoList) { // iterating through list of 33 points for a particular date
			Feature feature = Feature.fromGeometry((Geometry) t.getPoint());
			feature.addStringProperty("marker-size", "medium");
			feature.addStringProperty("location", t.getLocation());

			if (getVisitedPoints(outputs).contains(t.getPoint())) { //if a sensor pointis visited, do the following
															
				feature.addStringProperty("rgb-string", getColor(t.getReading(), t.getBattery()));
				feature.addStringProperty("marker-color", getColor(t.getReading(), t.getBattery()));
				feature.addStringProperty("marker-symbol", getMarkerSymbol(t.getReading(), t.getBattery()));

			} else { // if a sensor point is not visited, do the following 

				feature.addStringProperty("rgb-string", "#aaaaaa");
				feature.addStringProperty("marker-color", "#aaaaaa");
				feature.addStringProperty("marker-symbol", "");
			}

			features.add(feature); // add feature to our list of features
		}
		
		Output firstPathTaken = outputs.get(0);
		Point startPoint = Point.fromLngLat(firstPathTaken.getPrevLng(), firstPathTaken.getPrevLat());
		Point endPoint = Point.fromLngLat(firstPathTaken.getCurLng(), firstPathTaken.getCurLat());	 
		
		var listOfPoints = new ArrayList<Point>();
		listOfPoints.add(startPoint);
		listOfPoints.add(endPoint);

		for (int i=1;i<outputs.size(); i++) {												 
			listOfPoints.add(Point.fromLngLat(outputs.get(i).getCurLng(), outputs.get(i).getCurLat()));
		}
		
		/* create LineString from the list of points */
		LineString path = LineString.fromLngLats(listOfPoints);
		
		Feature f = Feature.fromGeometry((Geometry) path);
		features.add(f);													 
		
		FeatureCollection collections = FeatureCollection.fromFeatures(features); 
		return collections;
	}

	/*  converts feature collection to json and writes the result to a geojson file*/
	public void writeToGeoJsonFile() {
		try (FileWriter outputFile = new FileWriter(
				"readings-" + App.getArgs()[0] + "-" + App.getArgs()[1] + "-" + App.getArgs()[2] + ".geojson")) {
			outputFile.write(createGeoJsonFeatureCollection(movement.moveDrone()).toJson());
			outputFile.close();
			System.out.println("Successfully wrote to the geojson file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	/* writes the flight paths taken to a txt line by line */
	public void writeToOutputFile() {
		try (FileWriter outputFile = new FileWriter(
				"flightpath-" + App.getArgs()[0] + "-" + App.getArgs()[1] + "-" + App.getArgs()[2] + ".txt")) {
			for (Output output : movement.moveDrone()) {
				outputFile.write(output.toString() + "\n");
			}
			System.out.println("Successfully wrote to the flightpath file.");
			outputFile.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

}
