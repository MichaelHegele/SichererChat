package de.wi2012.mysql;

// Einbinden der benötigten Bibliotheken


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WI_ChatServer {
	
	public static void main(String[] args){
		
		// neuen Thread erstellen

		Thread tStart = new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					// Neuen Serversocket mit dem Name sSocket und dem Port 8001 öffnen 
					ServerSocket sSocket = new ServerSocket(8002);
					// Ausgabe, dass Start erfolgreich ist
					System.out.println("Server is running and listening...");
					while(true){
					
						
					
					// Bei eingehender Verbindung einen Socket mit dem Name s erstellen und annehmen
					Socket s = sSocket.accept();
					
					// Stream für eingehende Daten mit der Variable diStream öffnen und Daten des Sockets s annehmen
					
					
					ObjectInputStream inStream = new ObjectInputStream(s.getInputStream());
					
					String [] clientarray = (String[]) inStream.readObject();
					//wenn es eine registrierung ist
			        if(clientarray[0].equals("1"))
			        {
			    		String status = "error";
			    		
			    		if(DBConnection.insertName(clientarray[1], clientarray[2], clientarray[3]) == true)
			    		{
			            System.out.println("Neue Registrierung: User: " + clientarray[1] + " Password: " + clientarray[2] + " E-Mail: " + clientarray[3]);
			            System.out.println("Neue Tabelle:######################################");
			    		DBConnection.printNameList();
			    		System.out.println("###################################################");
			    		
			    		status = "sucess";
		    			

			    		}
			    		else
			    		{
			    			System.out.println("User oder E-Mail bereits vorhanden!");
							
			    			status = "error";
													
			    		}
		    			DataOutputStream oStream = new DataOutputStream(s.getOutputStream());
						
						oStream.writeUTF(status); 
							
							oStream.flush();
							oStream.close();	
			        
			        }
			       
			        //wenn es Nachrichten sind
			        if(clientarray[0].equals("2"))
			        {
					    System.out.println(clientarray[1] + ": " + clientarray[2]);
			      
			        }
							
					inStream.close();
	
		
					
				}
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// Wenn Start nicht erfolgreich, Fehlermeldung ausgeben
					System.out.println("Server not running, view Logfile!");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
						
		};
		tStart.start();
	}
}
