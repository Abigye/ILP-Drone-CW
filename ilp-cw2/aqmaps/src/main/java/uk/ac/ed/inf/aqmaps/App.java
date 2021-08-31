package uk.ac.ed.inf.aqmaps;

import java.util.List;


public class App {

	private static String[] savedArgs = new String[7]; //stores arguments from commandline

	/*returns the saved command line arguments in a String array form */
	public static String[] getArgs() { 
		return savedArgs;
	}

	public static void main(String[] args) {
		savedArgs = args;
		
		Movement droneMovement = new Movement ();
		
		List<Output> result = droneMovement.moveDrone();

		for (Output out : result) {
			System.out.println(out);
		}

		WritingToFiles writing = new WritingToFiles();

		writing.writeToOutputFile();
		writing.writeToGeoJsonFile();
	
		


	}

}
