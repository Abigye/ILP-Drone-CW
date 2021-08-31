package uk.ac.ed.inf.aqmaps;

public class Words {
	private String country;
	
	private Square square;
	private static class Square{
		private Lnglat southwest;
		private Lnglat northeast;
	}
	
	public static class Lnglat{
		private double lng;
		private double lat;
		
		public double getLng() { /*gets longitude*/
			return this.lng;
		}
		
		public double getLat() { /*gets latitude*/
			return this.lat;
		}
		
	}
	private String nearestPlace;
	private Lnglat coordinates;  // holds the coordinates of a sensor
	private String words;		 // holds location (what3words) of a sensor
	private String language;
	private String map;
	
	public Lnglat getCoordinates() { /*gets the coorinates of a sensor*/
		return this.coordinates;
	}
	
	public String getWord() { /*gets the what3words location*/
		return this.words;
	}
	
	

}
