import java.io.*;
import java.net.*;
import java.util.*;


public class Worker implements Runnable {
	//Length of the secret combination
	
	//All possible colors in the game
	private  enum colors {
		  RED,
		  BLUE,
		  YELLOW,
		  GREEN,
		  WHITE,
		  BLACK;
	}
	
	private Socket workersock;
	private OutputStream serverOstream;
	private InputStream serverIstream;

	// Number of occurrence of a color in the secret combination
	//Example : "1545" --> [0 1 0 0 1 2]
	private int[] colorOccurrence = new int[colors.values().length];
	private int[] secretCombination = new int[4];
	private int NBGUESS;
	private int nbExchanges = 0;
	private ArrayList<String> previousExchanges = new ArrayList<String>(NBGUESS);

	
	//Constructor
	Worker(Socket sock){workersock = sock;}
	
	
	public void run(){
		
		try {
			serverOstream = workersock.getOutputStream();
			serverIstream = workersock.getInputStream();
			
			while (true) {
				//Read message from inputStream
				byte[] incomingMessage = new byte[64];
				//Timout of 1 minute
				workersock.setSoTimeout(60000);
				int length = serverIstream.read(incomingMessage);
				
				if(length <= 0){
					System.out.println("Connection with Client " + workersock.getPort() + " lost");
					break;
				}
				
				//Convert it to string
				String clientMessage = new String(incomingMessage);
				System.out.println("Client " + workersock.getPort() + ": " + clientMessage);
				
				//Starting new game ("10")
				if(clientMessage.startsWith("10")){
					startGame();
				}
				
				//List previous exchanges ("12")
				else if(clientMessage.startsWith("12") && length == 2){
					StringBuilder builder = new StringBuilder("13");
					//Add the number of previous exchanges
					builder.append(nbExchanges);
					
					//Iterate through previous guesses and append them to string
					Iterator<String> iter = previousExchanges.iterator();
					while (iter.hasNext()){
						builder.append(iter.next());
					}
					//Send the previous exchanges to the client
					sendMessage(builder.toString());
				}
				//Guess a combination (ex: "121345")
				else if(clientMessage.startsWith("12") && length == (2 + 4)){
					String guessedcombination = clientMessage.substring(2, length);
					guessCombination(guessedcombination);
				}
				//Unknown message
				else{
					sendMessage("14");	
				}
			}
			
			
		}catch(SocketException e){
			System.err.println("The connection timed out");
		}		
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				workersock.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	private void startGame() throws Exception{
		NBGUESS = 2;
		//Choose a given amount of colors for the secret combination
		Random rand = new Random();
		
		for(int i =0; i < 4 ;i++) {
			int randomColor = rand.nextInt(colors.values().length);
			secretCombination[i] = randomColor;
			colorOccurrence[randomColor]++;
		}
		
		//Output the secret combination to the server console
		System.out.println(Arrays.toString(secretCombination));
		
		//Tell the client the game started
		sendMessage("11");
	
	}
	
	
	private void guessCombination(String guessedcombination) throws Exception{
		int length = guessedcombination.length();
		int isPresent = 0;
		int isRightplace = 0;
		
		NBGUESS--;
			

		//Copy the colorOccurence array
		int[] temp_colorOccurence = Arrays.copyOf(colorOccurrence,colorOccurrence.length);
		
		//Check each color at a time
		for(int i = 0; i < length; i++){
			
			//Extract the color and convert it to int
			int guessedcolor = Character.getNumericValue(guessedcombination.charAt(i));
			
			//If color is at the right place
			if(guessedcolor == secretCombination[i]){
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
