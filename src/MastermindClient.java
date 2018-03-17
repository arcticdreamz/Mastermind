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
	
	public static void main (){
			
		try{
			Socket clientsock = new Socket ("localhost",2416) ;
			
			while(true){
				//Reading from user
				InputStreamReader userIR = new InputStreamReader(System.in);
				BufferedReader userBR = new BufferedReader(userIR);
				String userInput;
				userInput = userBR.readLine();
				

				//Sending to server
				OutputStream clientOstream = clientsock.getOutputStream();
				clientOstream.write(userInput.getBytes());
				clientOstream.flush();
				
				//Getting response from server
				byte serverBytes[] = new byte[64];
				InputStream clientIstream = clientsock.getInputStream();
				int length = clientIstream.read(serverBytes);
				if(length <= 0){break;}
				//Convert to string
				String serverMessage = new String(serverBytes);
				//Truncate message
				serverMessage = serverMessage.substring(0, length);
				
				
				analyseMessage(serverMessage);
				
				
				
				
				
				System.out.println(new String(serverMessage));
			}
					
			clientsock.close();
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	private static void analyseMessage(String serverMessage){
		
		String code = serverMessage.substring(0, 2);
		String information = serverMessage.substring(2,serverMessage.length());
	
		switch(code){
		case "11":
			System.out.println("The game has started !");
			System.out.println("Input a combination consisting of " + Worker.getMaxColors()
							+ " of the following colors");
			for(colors value: colors.values()){
			    System.out.println(colors.name());
			}
	
			break;
		case "12":
			break;
		case "13":
			
			break;
			
		case "14":
			
			break;
		}	
	}
	
	
}
