import java.io.*;
import java.net.*;
import java.util.Arrays;

class MastermindClient {
	public static void main (String argv[]){
			
		try {
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
				byte serverMessage[] = new byte[64];

				InputStream clientIstream = clientsock.getInputStream();
				
				if(clientIstream.read(serverMessage) <= 0){
					System.out.println("Client broken out");
					break;
				}
				
				System.out.println(new String(serverMessage));
			}
					
			clientsock.close();
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}