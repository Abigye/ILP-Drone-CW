package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

public class ServerData {
	public static  List<Polygon> polygons = getNoFlyZones();  						// holds no fly zones building
	public static  List<AirQualityData> airQualitySensors = getAirQualityDataList(); // holds air quality data of the sensors

	
	/*sends a request to server to get all no fly zones and puts them in a list which is returned*/
	private static List<Polygon> getNoFlyZones() {
		 // holds the no fly zones buildings
		var noFlyZones = new ArrayList<Polygon>();
		var client = HttpClient.newHttpClient();

		// HttpClient assumes that it is a GET request by default
		var request = HttpRequest.newBuilder().uri(URI.create("http://localhost:"+App.getArgs()[6]+"/buildings/no-fly-zones.geojson"))
				.build();
		try {
			// The response object is of class HttpResponse<String>
			var response = client.send(request, BodyHandlers.ofString());

			// obtaining the FeatureCollection from the body of the response
			var collection = FeatureCollection.fromJson(response.body());

			var listOfFeatures = collection.features();
			for (Feature f : listOfFeatures) {
				noFlyZones.add((Polygon) f.geometry());

			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return noFlyZones;
	}
	
	/* sends a request to server to get all sensors and puts them in a list*/
	private static List<AirQualityData> getAirQualityDataList() {
		var sensorList = new ArrayList<AirQualityData> ();
		
		var client = HttpClient.newHttpClient();

		// HttpClient assumes that it is a GET request by default
		var request = HttpRequest.newBuilder().uri(URI.create("http://localhost:"+App.getArgs()[6]+"/maps/" + App.getArgs()[2] + "/"
				+ App.getArgs()[1] + "/" + App.getArgs()[0] + "/" + "air-quality-data.json")).build();
		try {
			// getting the response object of class HttpResponse<String>
			var response = client.send(request, BodyHandlers.ofString());

			Type listType = new TypeToken<ArrayList<AirQualityData>>() {
			}.getType();

			// Using the ”fromJson(String, Type)” method
			sensorList = new Gson().fromJson(response.body(), listType);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return sensorList;
	}

	/*given the what3words location of sensor, this method gets all information about that location and returns it */
	public static Words getLocationDetails(String location) {
			String[] st = location.split("\\."); // splits the location into 3 parts
			Words word = new Words();
		
			var client = HttpClient.newHttpClient();

			// HttpClient assumes that it is a GET request by default
			var request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:"+App.getArgs()[6]+"/words/" + st[0] + "/" + st[1] + "/" + st[2] + "/details.json"))
					.build();
			try {
				// get the response object of class HttpResponse<String>
				var response = client.send(request, BodyHandlers.ofString());

				// Using the ”fromJson(String, Class)” method
				 word = new Gson().fromJson(response.body(), Words.class);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			
		
			return word;
		
	}

}
