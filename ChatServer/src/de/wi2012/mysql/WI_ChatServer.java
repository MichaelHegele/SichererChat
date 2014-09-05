package de.wi2012.mysql;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class WI_ChatServer {

	public static void main(String[] args) {

		// Hier wird ein neuer Server Thread erstellt..
		Thread tStart = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// Neuen SSLSocket und die benötigten Streams initialisieren..
				SSLServerSocketFactory ssf = null;
				SSLServerSocket ss = null;
				SSLSocket s = null;
				ObjectInputStream inStream = null;
				DataOutputStream dout = null;
				ObjectOutputStream outStream = null;

				try {
					// Neuen SSLServersocket erstellen, der auf dem Port 8002
					// abhört..
					ssf = (SSLServerSocketFactory) SSLServerSocketFactory
							.getDefault();
					ss = (SSLServerSocket) ssf.createServerSocket(8002);

					// Alle verfügbaren Ciphersuites abrufen und geeigneten
					// verwenden..
					ss.setEnabledCipherSuites(ss.getSupportedCipherSuites());

					// Ausgabe, dass Start erfolgreich war..
					System.out
							.println("Server started... Waiting for incoming Clients on port "
									+ ss.getLocalPort());

					while (true) {
						// Bei eingehender Verbindung auf dem Port einen
						// SSLSocket mit dem Name s erstellen und annehmen
						s = (SSLSocket) ss.accept();
						
						//Ausgewählte Ciphersuit ausgeben
						//System.out.println("Session ciphersuite is: "	+ s.getSession().getCipherSuite());

						// Objekte vom Socket entgegennehmen..
						inStream = new ObjectInputStream(s.getInputStream());

						// ..und einem Array clientarray zuweisen
						String[] clientarray = (String[]) inStream.readObject();

						// Anhand der ersten Zahl im Array entscheidet der
						// Server, was er zu tun hat:
						// Wenn es eine 1 ist -> Registrierung
						if (clientarray[0].equals("1")) {
							String status = "error";

							// Daten der Registrierung in der Datenbank einfügen
							if (DBConnection.insertName(clientarray[1],
									clientarray[2], clientarray[3],
									clientarray[4]) == true) {
								System.out.println("Neue Registrierung: User: "
										+ clientarray[1] + " Password: "
										+ clientarray[2] + " E-Mail: "
										+ clientarray[3] + " Public-Key: "
										+ clientarray[4]);
								System.out
										.println("Neue Tabelle:######################################");
								DBConnection.printNameList();
								System.out
										.println("###################################################");

								// Wenn erfolgreich
								status = "sucess";
							}
							// Wenn nicht erfolgreich...
							else {
								System.out
										.println("User oder E-Mail bereits vorhanden!");
								status = "error";
							}
							// String zurückschreiben, ob Registrierung
							// erfolgreich war oder nicht
							dout = new DataOutputStream(s.getOutputStream());

							dout.writeUTF(status);
							dout.flush();
						}
						// Wenn es eine 2 ist -> Nachrichten
						if (clientarray[0].equals("2")) {
							// Nachrichten entgegennehmen und in die Datenbank
							// einfügen
							System.out.println(clientarray[1] + ": "
									+ clientarray[3]);
							DBConnection.insMSG(clientarray[1], clientarray[2],
									clientarray[3]);
						}
						// Wenn es eine 3 ist -> Login
						if (clientarray[0].equals("3")) {
							boolean checkuser = DBConnection.checkUser(
									clientarray[1], clientarray[2]);
							String check = "false";

							// Wenn User + Pw ok ist
							if (checkuser == true) {
								check = "true";
								dout = new DataOutputStream(s.getOutputStream());
								// String mit true an die App zurückgeben
								dout.writeUTF(check);
								dout.flush();
							}
							// Wenn Login nicht ok ist
							else {
								check = "false";
								dout = new DataOutputStream(s.getOutputStream());
								// String mit false an die App zurückgeben
								dout.writeUTF(check);
								dout.flush();
							}
						}
						// Wenn es eine 4 ist -> Nachrichten für 2 bestimmte
						// Clients abrufen
						if (clientarray[0].equals("4")) {
							// Nachrichten für die 2 Clients aus der DB holen
							String nachrichten[] = DBConnection.getMSG(
									clientarray[1], clientarray[2]);
							outStream = new ObjectOutputStream(
									s.getOutputStream());
							// und an die App zurückschreiben
							outStream.writeObject(nachrichten);
							outStream.flush();
						}
						// Wenn es eine 5 ist -> Check ob mit PublicKey
						// verschlüsselter AES-Key in der DB vorhanden ist
						if (clientarray[0].equals("5")) {
							String AESPkey = DBConnection.getAESPkey(
									clientarray[1], clientarray[2]);
							String[] aescheck = new String[2];

							// Wenn kein AES Key vorhanden ist -> RSA Public Key
							// zurückgeben
							if (AESPkey.equals("empty")) {
								aescheck[0] = "false";
								aescheck[1] = DBConnection
										.getPublicKey(clientarray[2]);
								System.out.println("PublicKey: " + aescheck[1]);
							}
							// Wenn ein AES Key vorhanden ist -> AES Key
							// zurückgeben
							else {
								aescheck[0] = "true";
								aescheck[1] = AESPkey;
								System.out.println("AES-Key: " + aescheck[1]);
							}
							outStream = new ObjectOutputStream(
									s.getOutputStream());
							outStream.writeObject(aescheck);
							outStream.flush();
						}
						// Wenn es eine 6 ist -> Mit Public-Key verschlüsselter
						// AES-Key in DB schreiben
						if (clientarray[0].equals("6")) {
							boolean checkinsert = DBConnection.insertAESPkey(
									clientarray[1], clientarray[2],
									clientarray[3]);
							String insert = "false";
							if (checkinsert == true) {
								insert = "true";
							} else {
								insert = "false";
							}
							dout = new DataOutputStream(s.getOutputStream());
							dout.writeUTF(insert);
							dout.flush();
						}
					}
					// Fehler abfangen und Socket / Streams schließen
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// Wenn Start nicht erfolgreich, Fehlermeldung ausgeben
					System.out.println("Server not running, view Logfile!");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (s != null) {
						try {
							s.close();
						} catch (IOException ex) {
							System.out.println("Error closing Stream");
						}
					}
					if (inStream != null) {
						try {
							inStream.close();
						} catch (IOException ex) {
							System.out.println("Error closing Stream");
						}
					}
					if (outStream != null) {
						try {
							outStream.close();
						} catch (IOException ex) {
							System.out.println("Error closing Stream");
						}
					}
					if (dout != null) {
						try {
							dout.close();
						} catch (IOException ex) {
							System.out.println("Error closing Stream");
						}
					}
				}
			}
		};
		// Server Thread starten
		tStart.start();
	}
}
