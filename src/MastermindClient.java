import java.io.*;
import java.net.*;
import java.util.Arrays;

class MastermindClient {
	
	private static enum colors {
		  RED,
		  BLUE,
		  YELLOW,
		  GREEN,
		  WHITE,
		  BLACK;
	}
	
	private static OutputStream clientOstream;
	private static InputStream clientIstream;
	private static BufferedReader userBR;
	
	public static void main (String argv[]){
			
		try{
			Socket clientsock = new Socket("localhost",2416);
			clientOstream = clientsock.getOutputStream();
			clientIstream = clientsock.getInputStream();
			
			//First message, prompt user to play the game
			System.out.println("Do you want to play a round of Mastermind [Y/N] ?");
			userBR = new BufferedReader(new InputStreamReader(System.in));
			String userInput = userBR.readLine();
			
			if(userInput.equals("Y")){
				
				//Start the game
				sendMessage("10");
				boolean playGame = true;
				
				while(playGame){
					
					//Getting response from server
					byte serverBytes[] = new byte[64];
					int length = clientIstream.read(serverBytes);
					if(length <= 0){
						playGame = false;
						break;
					}
					String serverMessage = new String(serverBytes);
					serverMessage = serverMessage.substring(0, length);
					
					analyseMessage(serverMessage);
								

					System.out.println("What do you want to do ?\n");
					System.out.println("1) Choose a color combination");
					System.out.println("2) See already played combination");
					System.out.println("3) Quit\n");
					System.out.print("Your choice: ");
					
					userInput = userBR.readLine();
					
					switch(userInput){
						case "1":
							String colorinput = chooseColors();
							sendMessage(colorinput);
							break;
						case "2":
							sendMessage("12");
							break;
						case "3":
							playGame = false;
							break;							
					}					
				}
			}
			
			System.out.println("Shutting down.");
			clientsock.close();
			userBR.close();
		
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	private static String chooseColors() throws Exception {
		
		System.out.println("Input a combination consisting of " +
				Worker.getMaxColors()+ " of the following colors");

		for(colors color: colors.values()){
			System.out.println(color.name());
		}
		
		System.out.println("Separate each of them with spaces");
		
		
		String userinput = userBR.readLine();
		String[] inputcolors = userinput.split(" ");
		String message = "12";
		for(String color: inputcolors){
			switch(color){
			case "RED":
				message += colors.RED.ordinal();
				break;
				
			case "BLUE":
				message += colors.BLUE.ordinal();
				break;
				
			case "YELLOW":
				message += colors.YELLOW.ordinal();
				break;
				
			case "GREEN":
				message += colors.GREEN.ordinal();
				break;
				
			case "WHITE":
				message += colors.WHITE.ordinal();
				break;
				
			case "BLACK":
				message += colors.BLACK.ordinal();
				break;
			}
		}
		
		return message;
	}

	private static void sendMessage(String userinput) throws Exception {		
		//Sending to server
		clientOstream.write(userinput.getBytes());
		clientOstream.flush();
	}



	private static void analyseMessage(String serverMessage) throws Exception{
		
		//Separating the message in two parts
		String code = serverMessage.substring(0, 2);
		String information = serverMessage.substring(2,serverMessage.length());
		
		String won = String.format("%d0",Worker.getMaxColors());

		switch(code){
		case "11":
			System.out.println("The game has started !");
			break;
			
		case "12":
			if(information.equals(won)){
				System.out.println("Congratulations, you won !");
			}
			else{
				String result = String.format("You placed %c colors right and %c " +
						"others are not at the correct place",
						information.charAt(0),
						information.charAt(1));
				
				System.out.println(result);
			}
			
			break;
			
		case "13":
			
			char nbguesses = information.charAt(0);
			System.out.println("You have made " + nbguesses +" number of guesses");
			if(nbguesses > 0){
				System.out.println("These are :");	
			}
			for(int i = 1; i < information.length(); i += 6){
				String guess = information.substring(i, i + 6);
				String combination = guess.substring(0,4);
				char placedright = guess.charAt(4);
				char ispresent= guess.charAt(5);

				System.out.println("Guess " + ((i-1)/6 +1) + ": " + combination +
									" Result: " + placedright + " " + ispresent);				
			}
			
			break;
			
		case "14":
			System.out.println("Wrong request");	
			break;
		}
		
	}
	
}
