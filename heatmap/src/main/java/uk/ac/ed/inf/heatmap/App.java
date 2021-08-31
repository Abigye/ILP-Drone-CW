package uk.ac.ed.inf.heatmap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.FileWriter;
import com.mapbox.geojson.*;



public class App {
	
	//list to store prediction data for later use 
	public static List <String> predictionContent;
	

	/**
     * Load all lines from the specified file into a list and
     * save them for later parsing with the parseFileContent method.
     * 
     * This method has to be called before the parseFileContent method
     * can be executed successfully.
     * 
     * @param fileName file path with prediction data
     * @return true if  data could be loaded successfully, false otherwise
     * @throws NullPointerException if the given file name is null
     */
	public static boolean loadFile(Path fileName) {
		Objects.requireNonNull(fileName, "Given filename must not be null");
		boolean success = false;

		// read file contents into predictionContent
		try {
			predictionContent = Files.readAllLines(fileName);
			success = true;
		} catch (IOException | SecurityException e) {
			System.err.println("ERROR: Reading file content failed: " + e);
		}

		return success;

	}


	/** Parse file content loaded previously with the loadFile 
	 * method into a 2D array called grid.
	 * @return grid 
	 **/
	public static int[][] parseFileContent() {
		int[][] grid = new int[10][10];

		for (int i = 0; i < predictionContent.size(); i++) {
			String[] array = predictionContent.get(i).split(",");
			for (int j = 0; j < array.length; j++) {
				grid[i][j] = Integer.parseInt(array[j].trim());
			}
		}
		return grid;

	}
	
	
	/** calculates the longitude difference when given two points with same latitude
	 * @param point1 first point to be used
	 * @param point2 second point to be used 
	 * @throws throws an illegal Argument Exception when points given do not have same latitudes
	 * @return the absolute of the longitude difference between the  two points 
	 * */
	public static double lngDifference(Point point1, Point point2) {
		if (point1.latitude() != point2.latitude()) {
			throw new IllegalArgumentException("points should have same latitude");
		}

		return Math.abs(point1.longitude() - point2.longitude());

	}
	
	
	/** calculates the latitude difference when given two points with same longitude
	 * @param point1 first point to be used
	 * @param point2 second point to be used 
	 * @throws throws an illegal Argument Exception when points given do not have same longitude
	 * @return the absolute of the latitude difference between the two points 
	 * */
	public static double latDifference(Point point1, Point point2) {
		
		if (point1.longitude() != point2.longitude()) {
			throw new IllegalArgumentException("points should have same longitude");
		}

		return Math.abs(point1.latitude() - point2.latitude());
	}

	
	/**
	 * returns the rgbstring of a number
	 * @param num the number whose colour is to be determined
	 * @return the colour of the number when of falls within a particular range 
	 ***/
	public static String getColor(int num) {
		String rgbString = "";
		if (num >= 0 && num < 32) {
			rgbString = "#00ff00";
		} else if (num >= 32 && num < 64) {
			rgbString = "#40ff00";
		} else if (num >=64 && num < 96) {
			rgbString = "#80ff00";
		} else if (num >= 96 && num < 128) {
			rgbString = "#c0ff00";
		} else if (num >= 128 && num < 160) {
			rgbString = "#ffc000";
		} else if (num >= 160 && num < 192) {
			rgbString = "#ff8000";
		} else if (num >= 192 && num < 224) {
			rgbString = "#ff4000";
		} else if (num >= 224 && num < 256) {
			rgbString = "#ff0000";
		}
		return rgbString;
	}


	public static void main(String[] args) {
		
		loadFile(Paths.get(args[0])); // reading file from command line 
		
		int n = predictionContent.size(); // size of the prediction file (10)

		int[][] grid1 = parseFileContent();

		Point point1 = Point.fromLngLat(-3.192473, 55.946233); //Forest Hill
		Point point2 = Point.fromLngLat(-3.184319, 55.946233); //KFC
		Point point3 = Point.fromLngLat(-3.184319, 55.942617); // Buccleuch St.bus Stop
		//Point point4 = Point.fromLngLat(-3.192473, 55.942617); //Top of meadows


		double lngDiff = lngDifference(point1, point2) / n;  // width of one polygon
		double latDiff = latDifference(point2, point3) / n;  // height of one polygon


		List <Feature> listOfFeatures = new ArrayList <>();
		
		//longitude of starting point (Forest Hill points) for creating the polygons 
		double baseLng = point1.longitude();
		
		// latitude of  starting point for creating the polygons
		double baseLat = point1.latitude();  
		
		
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				List <Point> pl = getPoints(lngDiff, latDiff, baseLng, baseLat, row, col);
				var listOfPl = new ArrayList <List<Point>>();
				listOfPl.add(pl);

				Polygon polygon = Polygon.fromLngLats(listOfPl);

				Feature f = getFeature(grid1[row][col], polygon);

				listOfFeatures.add(f);
			}

		}

		FeatureCollection featureCollection = FeatureCollection.fromFeatures(listOfFeatures);
			//		System.out.println(featureCollection.toJson());
			//		featureCollection.toJson();
		
		//writing to a file 
		try {
		      FileWriter outputFile = new FileWriter("heatmap.geojson");
		      outputFile.write(featureCollection.toJson());
		      outputFile.close();
		      System.out.println("Successfully wrote to the file.");
		    } 
		catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		  

	}
	

	/**
	 * creates a list of points which will be used to create a polygon later
	 * @param lngDiff  the width of one  polygon
	 * @param latDiff  the height of one polygon
	 * @param baseLng  the longitude of the starting point
	 * @param baseLat  the latitude of the starting point 
	 * @param row      number to be multiplied by latDiff to get the latitude part of next point 
	 * @param col      number to be multiplied by lngDiff to get the longitude part of the next point
	 * @return list of points to create a polygon
	 * */
	private static List <Point> getPoints(double lngDiff, double latDiff, double baseLng, double baseLat, int row, int col) {
		Point p1 = Point.fromLngLat(baseLng + col * lngDiff, baseLat - row * latDiff);
		Point p2 = Point.fromLngLat(baseLng + (col + 1) * lngDiff, baseLat - row * latDiff);
		Point p3 = Point.fromLngLat(baseLng + (col + 1) * lngDiff, baseLat - (row + 1) * latDiff);
		Point p4 = Point.fromLngLat(baseLng + col * lngDiff, baseLat - (row + 1) * latDiff);

		List <Point> pl = new ArrayList <>();
		pl.add(p1);
		pl.add(p2);
		pl.add(p3);
		pl.add(p4);
		pl.add(p1);
		return pl;
	}
	
	
	/**
	 * creates a feature for a polygon and adds properties to it
	 * @param num     the number which will be used to get the rgbString property of the polygon 
	 *                using the getColor method 
	 * @param polygon the geometry (polygon) whose properties are to be added
	 * @return the feature of the polygon
	 * 
	 **/
	private static Feature getFeature(int num, Geometry polygon) {
		Feature f = Feature.fromGeometry(polygon);
		f.addNumberProperty("fill-opacity", 0.75);
		f.addStringProperty("rgb-string", getColor(num));
		f.addStringProperty("fill", getColor(num));
		return f;
	}
}
