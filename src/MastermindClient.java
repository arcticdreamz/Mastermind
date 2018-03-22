import java.net.*;
import java.util.ArrayList;

class MastermindClient {
	
	private static ArrayList<Game> currentgames = new ArrayList<Game>();

	
	public static void main (String argv[]){
			
		try{
			Socket clientsock = new Socket("localhost",2416);
			Game newGame = new Game(clientsock);
			currentgames.add(newGame);
		}
		catch(Exception e){e.printStackTrace();}
	}
	

}
