import java.net.*;
import java.util.ArrayList;

public class MastermindServer {
	
	//public static ArrayList<Socket> PlayersArray = new ArrayList<Socket>();
	
	public static void main ( String argv [ ] ) throws Exception {
		
		ServerSocket serversock = new ServerSocket(2416);
		System.out.println("Server is running");
		
		try{
			while (true){
				Socket sock = serversock.accept();
				System.out.println("Connection accepted");

				Thread t1 = new Thread(new Worker(sock));
				t1.start();			
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			serversock.close();
		}
	}
}


