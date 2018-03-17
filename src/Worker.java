import java.io.*;
import java.net.*;
import java.util.*;


public class Worker implements Runnable {
	private final int MAXCOLORS = 4; //Must be smaller than 30
	private int NBGUESS = 1; //Number of guesses allowed
	//All possible colors in the game
	private  enum colors {
		  red,
		  blue,
		  yellow,
		  green,
		  white,
		  black;
	}
	
	private Socket workersock;
	private OutputStream serverOstream;
	private InputStream serverIstream;
	
	// Number of occurrence of a color in the secret combination
	// Follows the same order as the colors enum.
	private int[] colorOccurrence = new int[colors.values().length];
	private int[] secretCombination = new int[MAXCOLORS];
	private boolean gameStarted = false;
	private ArrayList<String> previousExchanges = new ArrayList<String>(NBGUESS);
	private int nbExchanges = 0;
	private byte[] startBytes = "10".getBytes(); 
	
	
	
	//Constructor
	Worker(Socket sock){workersock = sock;}
	
	
	public void run(){
		
		try {
			serverOstream = workersock.getOutputStream();
			serverIstream = workersock.getInputStream();
			
			while (true) {
				//Read message from inputStream
				byte[] incomingMessage = new byte[64];
				int length = serverIstream.read(incomingMessage);
				
				if(length <=0 ){
					System.out.println("Server broke out");
					break;
				}
				
				//Convert it to string
				String clientMessage = new String(incomingMessage);
				System.out.println(clientMessage);
				
				//Starting new game ("10")
				if(clientMessage.equals("10") && gameStarted==false){
					gameStarted = startGame();
				}
				
				//List previous exchanges ("12")
				else if(clientMessage.equals("12")){
					StringBuilder builder = new StringBuilder("13");
					builder.append(nbExchanges);
					
					Iterator<String> iter = previousExchanges.iterator();
					while (iter.hasNext()){
						builder.append(iter.next());
					}
					//Send the previous exchanges to the client
					sendMessage(builder.toString());
				}
				//Guess a combination (ex: "121345")
				else if(clientMessage.startsWith("12") && length == (2 + MAXCOLORS)){
					guessCombination(clientMessage);
				}
				//Unknown message
				else{
					sendMessage("14");	
				}
				
				/*Sending to the client 
				serverOstream.write(incomingMessage,0,length);
				serverOstream.flush();
				*/
				
			}
			
			workersock.close();
			
		//acknowledge end of connection
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean startGame() throws Exception{
		//Choose a given amount of colors for the secret combination
		Random rand = new Random();
		
		for(int i =0; i < MAXCOLORS ;i++) {
			int randomColor = rand.nextInt(colors.values().length);
			secretCombination[i] = randomColor;
			colorOccurrence[randomColor]++;
		}
		
		//Output the secret combination to the server console
		System.out.println(Arrays.toString(secretCombination));
		
		//Tell the client the game started
		sendMessage("11");
	
		return true;	
	}
	
	
	private void guessCombination(String guessedcombination) throws Exception{
		int length = guessedcombination.length();
		int isPresent = 0;
		int isRightplace = 0;
		
		NBGUESS--;
		
		//Copy the colorOccurence array
		int[] temp_colorOccurence = Arrays.copyOf(colorOccurrence,colorOccurrence.length);
		
		//Check each color at a time
		for(int i = 0; i < length-2; i++){
			
			char guessedcolor = guessedcombination.charAt(i+2);
			
			//If color is at the right place
			if(guessedcolor == (char) secretCombination[i]){
				isRightplace++;
				
			//If the color is present somewhere in the secret combination
			}else if(temp_colorOccurence[guessedcolor] > 0){
				
				isPresent++;
				temp_colorOccurence[guessedcolor]--;
			}
		}
		
		//Create a string for the client-server exchange and add it to a list
		String exchange = String.format("%s%d%d",guessedcombination,isRightplace,isPresent);
		previousExchanges.add(exchange);
		nbExchanges++;
		
		//Send the result of the guess to the client
		String guessResult = String.format("12%d%d",isRightplace,isPresent);
		sendMessage(guessResult);
	}
	
	
	
	//Sending to the client
	private void sendMessage(String message) throws Exception{
		byte[] sendingMessage = new byte[64];
		sendingMessage = message.getBytes();
		serverOstream.write(sendingMessage);
		serverOstream.flush();
	}
}
