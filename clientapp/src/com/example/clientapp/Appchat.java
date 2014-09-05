package com.example.clientapp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import android.support.v7.app.ActionBarActivity;
import android.content.ContextWrapper;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

public class Appchat extends ActionBarActivity implements OnClickListener{

	// Benötigte Variablen deklarieren

	final String [] ip = new String[1];
	final int [] port = new int[1];
	final ContextWrapper c = new ContextWrapper(this);
	
	String [] nachrichten = new String[20];
	
	Handler handler = new Handler();	

	TextView cReader; // -> Nachrichtenausgabe in der App
	ScrollView scroller; // Scrollen
	
	// Textfelder in der App
	
	EditText cMessage; //-> Neue Variable des Typs EditText, Übergabe App an Programmcode
	EditText cUser; //-> User aus der App
	EditText cPW; //-> User aus der App	
	EditText cNewUser; //-> User aus der App
	EditText cNewPw; //-> User aus der App
	EditText cNewMail; //-> User aus der App
	
	// Buttons in der App
	
	Button bNewRegister; // Neue Registrierung Button
	Button bBack; // Zurück Button
	Button bLogin; // Login Button
	Button bRegister; // Neue Registrierung Button
	Button bUpdate; // Update Button
	Button bSend; //-> Neue Variable des Typs Button
	
	String cOut; // Variable für Ausgabe des Textes
	String user; //lokale App-Variable für den User
	String pw; //lokale App-Variable für das PW
	
	
	// onCreate wird bei Start der App ausgeführt..
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//IP und Port des Servers
		ip[0] = "193.196.175.236";
		port[0] = 8002;
		
		// Eine neue Instanz der App anlegen..
		super.onCreate(savedInstanceState);
		
		// Das erstellte Main-Layout "activity_login" laden..
		setContentView(R.layout.activity_login);
		
		// Verknüpfung zwischen den Layout Feldern und den lokalen Variablen über die IDs herstellen
		cUser = (EditText)findViewById(R.id.cUser);
		cPW = (EditText)findViewById(R.id.cPW);
		bLogin = (Button)findViewById(R.id.bLogin);
		bRegister = (Button)findViewById(R.id.bRegister);

		// Warten bis der Login Button ausgeführt wird
		bLogin.setOnClickListener(this);
		bRegister.setOnClickListener(this);		
	}
	
	// Dies passiert, wenn der jeweilige Button betätigt wird...
	@Override
	public void onClick(View v) {
		
		// Wenn der Login Button betätigt wird...
		if(v==bLogin){
			
			// Prüfen ob ein User eingegeben wurde, wenn nicht...
			if(cUser.getText().toString().trim().equals(""))
			{
				// Ausgabe, korrekten User eingeben
				messagebox("Bitte User eingeben!");
				
			}
			
			// wenn korrekter User eingegeben ist...			
			else
			{
				// Wenn User eingegeben, User und Passwort auslesen und den Variablen zuweisen
				user = cUser.getText().toString();
				pw = cPW.getText().toString();
				
				// neues Array anlegen, zur Überprüfung ob Kombination User + Passwort so in der Datenbank stehen
				final String[] checkarray = new String[5];
				
				// Passwort mit SHA256 hashen und mit dem user salten...
				pw = sha256(salt(user, pw));
				
				// In jedem Array wird auf Platz 0 eine Nummer mitgegeben, an dieser entscheidet der Server was zu tun ist...
				// Zuweisung des Users und gehashten Passworts an Stelle 1 und 2 des Arrays
				checkarray[0] = "3";
				checkarray[1] = user;
				checkarray[2] = pw;

				//neuen Thread erstellen, um Daten an Server zu übergeben und User + Passwort zu überprüfen
				Thread tCheckUser = new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						// neuen SSLSocketFactory, SSLSocket, ObjectOutputstream, DataInputStream erstellen						
						SSLSocketFactory ssf = null;
						SSLSocket s = null;
						ObjectOutputStream doStream = null;
						DataInputStream inStream = null;
				
						try {						
							//Verbindung zum Server IP/Port über den SSLSocket herstellen
							ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
							s = (SSLSocket) ssf.createSocket(ip[0] , port[0]);	
							
							//Alle verfügbaren Ciphersuites abrufen und den geeigneten verwenden..
							s.setEnabledCipherSuites(s.getSupportedCipherSuites());
							
							// neuen Schreibstream mit doStream erstellen und den Socket s zuweisen
							doStream = new ObjectOutputStream(s.getOutputStream());
							
							// Zuvor erstelltes Array an Socket / Server übergeben
							doStream.writeObject(checkarray);
							
							//Stream leeren					
							doStream.flush();
							
							// Daten vom Socket / Server holen
							inStream = new DataInputStream(s.getInputStream());
							
							// Der Variable checkit zuweisen -> gibt vom Server zurück, ob User / Passwort korrekt ist
							String checkit = inStream.readUTF();
							
							// Wenn User + Passwort korrekt:
							if(checkit.equals("true"))
							{
								runOnUiThread(new Runnable() {
								    @Override
								    public void run() {
								    	
								    	//wenn korrekt, dann login (siehe unten) ausführen...			
								    	login();
								            }
								    });    
								

							}
							
							// Wenn User + Passwort nicht korrekt:
							else
							{
								runOnUiThread(new Runnable() {
								    @Override
								    public void run() {
										// wenn inkorrekt, Meldung ausgeben:
								    	Toast.makeText(getApplicationContext(), "User nicht vorhanden oder Passwort falsch!",Toast.LENGTH_LONG).show(); 
								            }
								    });    
							}
							
							//Fehler abfangen und Streams / Socket schließen
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
						    if (doStream != null) {
						        try {
						            doStream.close();
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
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
						}						
					}		
				};
				// Thread starten
				tCheckUser.start();
			}
		}
		
		// Wenn Button Neu Registrieren gedrückt wird...		
		if(v==bRegister){
				
			// Neues Layout activity_register aktivieren
			setContentView(R.layout.activity_register);
			
			// Buttons initialisieren
			bNewRegister = (Button)findViewById(R.id.bNewRegister);
			bBack = (Button)findViewById(R.id.bBack);
			
			// Warten bis Button betätigt wird
			bBack.setOnClickListener(this);
			bNewRegister.setOnClickListener(this);
		}
		
		// Wenn Button Registrieren ausgeführt wird...
		if(v==bNewRegister){	
			
			// Textfelder anhand der ID zuweisen und mit lokalen Variablen verknüpfen
			cNewUser=(EditText)findViewById(R.id.cNewUser);
			cNewPw=(EditText)findViewById(R.id.cNewPw);
			cNewMail=(EditText)findViewById(R.id.cNewMail);
			
			// Prüfen ob bei der Registierung überall Daten angegeben wurden
			if(cNewUser.getText().toString().trim().equals("") || 
					cNewPw.getText().toString().trim().equals("")||
					cNewMail.getText().toString().trim().equals(""))
			{
				// Wenn nicht alle Daten angegeben wurden...
				messagebox("Bitte allen Daten angeben!");
			}
			// wenn in jedem Feld etwas steht:
			else
			{
				//Werte aus den Textfelder den lokalen Variablen zuweisen
				String newuser = cNewUser.getText().toString();
				String newpw = cNewPw.getText().toString();
				String newmail = cNewMail.getText().toString();
				
				// neue Variablen für Public und Private Key erstellen
				PublicKey pubkey;
				PrivateKey prkey;
				String Spubkey = null; 
				FileOutputStream fos = null;
				
				// Bei der Registrierung ein neues Keypair mit einem Public Key
				// und einem PrivateKey erstellen
				// Public Key wird in die Datenbank geschrieben, PrivateKey wird auf dem Gerät im
				// lokalen App Ordner abgelegt
				try {
					
					// Neues RSA1024 Keypair erzeugen
					KeyPair keyPair = encryptionRSA.generateKey();
					
					// Public und Private Key den lokalen Variablen zuweisen
					pubkey = keyPair.getPublic();
					prkey = keyPair.getPrivate();
					
					// PublicKey in String umwandeln
					Spubkey = encryptionRSA.publicKeyToString(pubkey);
					
					
					// Private Key lokal im Appverzeichnis ablegen -> Dateiname private.key
					PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(prkey.getEncoded());
					String defaultDataDir = c.getApplicationInfo().dataDir;
					String filename = defaultDataDir + "/private.key";
					fos = new FileOutputStream(filename);
					fos.write(pkcs8EncodedKeySpec.getEncoded());
					fos.close();
				    
				//Fehler vom Private Key speichern abfangen
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// Neues Array zur Registrierung anlegen
				final String[] registerarray = new String[5];
				
				// Passwort mit SHA256 hashen und dem angegeben User salten 
				newpw = sha256(salt(newuser, newpw));
				
				// Platz 0 im Array den Wert 1 angeben, damit Server weiß, was zu tun ist
				registerarray[0] = "1";
				// Werte der Registrierung dem Array zuweisen (User, Hashed + Salted Pw, Mail und Publickey)
				registerarray[1] = newuser;
				registerarray[2] = newpw;
				registerarray[3] = newmail;
				registerarray[4] = Spubkey;
				
				//neuen Thread erstellen, um Daten an Server zu übergeben und Registrierung durchzuführen
				Thread tRegister = new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt						
						SSLSocketFactory ssf = null;
						SSLSocket s = null;
						ObjectOutputStream oStream = null;
						DataInputStream inStream = null;
						
						try {
							// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
							ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
							s = (SSLSocket) ssf.createSocket(ip[0] , port[0]);	
							s.setEnabledCipherSuites(s.getSupportedCipherSuites());
							oStream = new ObjectOutputStream(s.getOutputStream());
							oStream.writeObject(registerarray);  
							oStream.flush();
							inStream = new DataInputStream(s.getInputStream());
							String status = inStream.readUTF();							
							Arrays.fill(registerarray, null);

							// Wenn der bei der Registrierung angegebene User oder E-Mail bereits vorhanden ist...
							if(status.equals("error"))
							{
								
								runOnUiThread(new Runnable() {
								    @Override
								    public void run() {
										Toast.makeText(getApplicationContext(), "Username oder Email existiert bereits!",Toast.LENGTH_LONG).show(); 
								            }
								    });             
							}	
							
							// Wenn Registrierung erfolgreich war...
							else
							{
								runOnUiThread(new Runnable() {
								    @Override
								    public void run() {
										bBack.performClick();
								    	Toast.makeText(getApplicationContext(), "Registrierung erfolgreich!",Toast.LENGTH_LONG).show(); 
								            }
								    });		
							}
							
						// Fehler abfangen und Streams / Sockets schließen	
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
						    if (oStream != null) {
						        try {
						            oStream.close();
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
						    if (s != null) {
						        try {
						            s.close();
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
						}
							
					}		
				};
				// Thread Registrierung ausführen
				tRegister.start();
			}
		}


		// Wenn der Zurück Button betätigt wird...
		if(v==bBack){
			
			// Layout auf das Loginfeld zurücksetzen
			setContentView(R.layout.activity_login);
			
			// Verknüpfung zwischen den Layout Feldern und den lokalen Variablen über die ID herstellen
			cUser = (EditText)findViewById(R.id.cUser);
			cPW = (EditText)findViewById(R.id.cPW);
			bLogin = (Button)findViewById(R.id.bLogin);
			bRegister = (Button)findViewById(R.id.bRegister);

			// Warten bis der Login oder Registrieren Button ausgeführt wird
			bLogin.setOnClickListener(this);
			bRegister.setOnClickListener(this);
			
			// Prüfen ob ein User eingegeben wurde..
			if(cUser.getText().toString().trim().equals(""))
			{
				messagebox("Bitte User eingeben!");
			}
			else
			{
				// Wenn User eingegeben, Werte auslesen und an lokale Variablen übergeben
				user = cUser.getText().toString();
				pw = cPW.getText().toString();
			}
		}
		
		// Wenn der Senden Button im Chat ausgeführt wird
		if(v==bSend)
		{						
			// Die eingegebene Nachricht des Nutzers der lokalen Variable cOut zuweisen
			cOut = cMessage.getText().toString();
			
			//updatemsg(user, user, cOut);
			

			// Geschriebenen Text löschen
			cMessage.setText(" ");
			
			// Array zum Senden der Nachricht anlegen
			final String[] sendarray = new String[5];
			
			// Dem Platz 0 des Arrays die Zahl 2 zuweisen, Server entscheidet anhand dieser was zu tun ist...
			sendarray[0] = "2";
			
			// Sender der Nachricht dem Array zuweisen
			sendarray[1] = user;
			
			// Empfänger der Nachricht - hart codiert
			// Hier wird später der Empfänger eingesetzt, 
			// zunächst aber hart codiert, da Userverwaltung noch nicht vorhanden
			if(user.equals("Jonas"))
			{
				sendarray[2] = "Andi";
			}
			else
			{
				sendarray[2] = "Jonas";
			}
			
			// Nachrichteninhalt mit AES 256 verschlüsseln
			try {
				cOut = encryptionAES.encrypt(cOut, getAESkeyValue(sendarray[2]));
			
			// Fehler bei der Verschlüsselung abfangen
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Dem Array die verschlüsselte Nachricht zuweisen
			sendarray[3] = cOut;

			//neuen Thread erstellen, um Daten an Server zu übergeben und Nachricht zu senden
			Thread tSend = new Thread()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt						
					SSLSocketFactory ssf = null;
					SSLSocket s = null;
					ObjectOutputStream doStream = null;
					
					try {
						
						// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
						ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
						s = (SSLSocket) ssf.createSocket(ip[0] , port[0]);	
						s.setEnabledCipherSuites(s.getSupportedCipherSuites());
						doStream = new ObjectOutputStream(s.getOutputStream());
						doStream.writeObject(sendarray);
						doStream.flush();
						
						
					// Fehler beim Socket erstellen abfangen
					// Socket und Streams schließen
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
					    if (doStream != null) {
					        try {
					            doStream.close();
					        } catch (IOException ex) {
					        	System.out.println("Error closing Stream");
					        }
					    }
					    if (s != null) {
					        try {
					            s.close();
					        } catch (IOException ex) {
					        	System.out.println("Error closing Stream");
					        }
					    }
					}						
				}		
			};
			
			// Senden Thread ausfüren
			tSend.start();
			
			// Ausgabe, dass die Nachricht versendet wurde!
			messagebox("Nachricht versendet!");			
		}
		
		// Wenn der Update Button ausgeführt wird
		if(v==bUpdate)
		{	
			// neues Array anlegen
			final String[] handshakearray = new String[5];
			
			// Dem Platz 0 des Array die Zahl 5 zuwesen, anhand dieser entscheidet der Server was zu tun ist
			handshakearray[0] = "5";
			
			// Sender der Nachricht zuweisen
			handshakearray[1] = user;
			
			// Empfänger der Nachricht - hart codiert,
			// dieser wird hier später bei bestehender Userbasis eingesetzt
			if(user.equals("Jonas"))
			{
				handshakearray[2] = "Andi";
			}
			else
			{
				handshakearray[2] = "Jonas";
			}
			
			// Prüfen ob das aktuelle Gerät auf dem die App läuft einen AES Key für die
			// aktuelle Chatsession besitzt
			String defaultDataDir = c.getApplicationInfo().dataDir;
			String filename = defaultDataDir + "/AES_" + handshakearray[2] + ".txt";
			java.io.File file = new java.io.File(filename);
			
			// Wenn kein AES Key vorhanden ist
			if(!file.exists())
			{					
				//neuen Thread erstellen, um Daten an Server zu übergeben und AES256 Key mit RSA1024 PublicKey verschlüsselt zu übertragen
				Thread thandshake = new Thread()
				{

					@Override
					public void run() {
						
						// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
						SSLSocketFactory ssf1 = null;
						SSLSocket s1 = null;
						SSLSocketFactory ssf2 = null;
						SSLSocket s2 = null;
						//Socket s = null;
						//Socket s2 = null;
						boolean check = false;
						ObjectOutputStream doStream = null;
						ObjectOutputStream doStream2 = null;
						ObjectInputStream in = null;
						String[] aescheck = null;
						
						// TODO Auto-generated method stub
						try {

							// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
							ssf1 = (SSLSocketFactory) SSLSocketFactory.getDefault();
							s1 = (SSLSocket) ssf1.createSocket(ip[0] , port[0]);	
							s1.setEnabledCipherSuites(s1.getSupportedCipherSuites());
							doStream = new ObjectOutputStream(s1.getOutputStream());
							doStream.writeObject(handshakearray);							
							// PublicKey verschlüsselten AES Key vom Server empfangen
							in = new ObjectInputStream(s1.getInputStream());
							aescheck = new String[2];
							aescheck = (String[]) in.readObject();
							doStream.flush();

							// Wenn kein AES Key auf dem Gerät, aber ein mit einem RSA Publickey verschlüsselter
							// AES Key in der Datenbank liegt...
							if(aescheck[0].equals("true"))
							{
								String SkeyValue = aescheck[1];
								
								// Den bei der Registrierung angelegten RSA Privatekey aus dem Dateisystem laden
								String defaultDataDir = c.getApplicationInfo().dataDir;
								String filename1 = defaultDataDir + "/private.key";
								File filename = new File(filename1);
								
								FileInputStream fis = new FileInputStream(filename);
								byte[] bPrivateKey = new byte[(int) filename.length()];
								fis.read(bPrivateKey);
								fis.close();
								
								// Aus dem Privatekey (Byte[]) einen PrivateKey erstellen								
								KeyFactory keyFactory = null;
								try {
									keyFactory = KeyFactory.getInstance("RSA");
								} catch (NoSuchAlgorithmException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}	
								PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bPrivateKey);
								PrivateKey prkey = null;
								try {
									prkey = keyFactory.generatePrivate(privateKeySpec);
								} catch (InvalidKeySpecException e1) {
									// TODO Auto-generated catch block
									Log.d("test","problemmmm");
									e1.printStackTrace();
								} 
								
								// Den RSA PublicKey verschlüsselten AES, welcher vom Server
								// in der Variable SkeyValue empfangen wurde
								// mit dem PrivateKey auf dem Gerät entschlüsseln und der Variable SKeyValue zuweisen
								SkeyValue = encryptionRSA.decrypt(SkeyValue, prkey);
								byte[] bkeyValue = SkeyValue.getBytes();
								
								// Den entschlüsselten AES Key im Dateisystem des Gerätes ablegen
								filename1 = defaultDataDir + "/AES_" + handshakearray[2] + ".txt";
								
								FileOutputStream out = null;
								try {
									out = new FileOutputStream(filename1);
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							    try {
									out.write(bkeyValue);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							    try {
									out.flush();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							    try {
									out.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							// Wenn der AES Key nicht auf dem Gerät ist und nicht in der Datenbank liegt
							else if(aescheck[0].equals("false") )
							{
								// siehe unten bei if(check == true)...
								check = true;
							}
							
						// Fehler abfangen, Streams und Sockets schließen						
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotFoundException e3) {
							// TODO Auto-generated catch block
							e3.printStackTrace();
						} finally {
						    if (doStream != null) {
						        try {
						            doStream.close();
						            
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
						    if (s1 != null) {
						        try {
						            s1.close();
						            
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
						    if (in != null) {
						        try {
						            in.close();
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
						}
						
						// Wenn der AES Key nicht auf dem Gerät ist und nicht in der Datenbank liegt
						if(check == true)
						{
							try{
							
							// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
							ssf2 = (SSLSocketFactory) SSLSocketFactory.getDefault();
							s2 = (SSLSocket) ssf2.createSocket(ip[0] , port[0]);	
							s2.setEnabledCipherSuites(s2.getSupportedCipherSuites());
							doStream2 = new ObjectOutputStream(s2.getOutputStream());
							
							// PublicKey des Empfängers, der zuvor in einem Array gespeichert wurde
							// der lokalen Variable Spubkey zuweisen
							String Spubkey = aescheck[1];
							
							// Den Publickey vom Datentyp String in den Datentyp PublicKey konvertieren
							PublicKey pubkey = encryptionRSA.stringToPublicKey(Spubkey);
							
							// Einen neuen AES256 Key generieren
							String SkeyValue = encryptionAES.RandomkeyValue(32);
							
							// Als Byte Array umwandeln
							byte[] keyValue = SkeyValue.getBytes();
							
							// Den generierten AES Key mit dem PublicKey des Empfängers verschlüsseln
							SkeyValue = encryptionRSA.encrypt(SkeyValue, pubkey);
							
							// Dem Array an der Stelle 0 die Zahl 6 zuweisen, anhand dieser Zahl entscheidet der Server was zu tun ist
							handshakearray[0] = "6";
							
							// Den RSA PublicKey verschlüsselten AES Key dem Array zuweisen...
							handshakearray[3] = SkeyValue;
							
							//... und an den Server übergeben
							doStream2.writeObject(handshakearray);
							doStream2.flush();
							
							// Den generierten AES auf dem Gerät ablegen
							String defaultDataDir = c.getApplicationInfo().dataDir;
							String filename = defaultDataDir + "/AES_" + handshakearray[2] + ".txt";
							
							FileOutputStream out = null;
							try {
								out = new FileOutputStream(filename);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    try {
								out.write(keyValue);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    try {
								out.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    try {
								out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						
						// Fehler abfangen, Sockets und Streams schließen
						
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} finally {
						    if (doStream2 != null) {
						        try {
						            doStream2.close();
						            
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }
						    if (s2 != null) {
						        try {
						            s2.close();
						            
						        } catch (IOException ex) {
						        	System.out.println("Error closing Stream");
						        }
						    }

						}
				}
					}		
				};
				
				// Thread Handshake starten
				thandshake.start();			
			}
				
			// Updatearray erstellen
			final String[] updatearray = new String[5];
			
			// Dem Array an Platz 0 die Zahl 4 zuweisen, anhand dieser entscheidet der Server was zu tun ist
			updatearray[0] = "4";
			
			// Sender der Nachricht zuweisen
			updatearray[1] = user;
			
			// Empfänger der Nachricht - hart codiert zuweisen
			// Wenn später Userbasis existiert wird dieser hier eingefügt
			if(user.equals("Jonas"))
			{
			updatearray[2] = "Andi";
			}
			else
			{
				updatearray[2] = "Jonas";
			}
			
			//neuen Thread erstellen, um Daten an Server zu übergeben und vorhandene Nachrichten zu erhalten / in der App auszugeben
			Thread tUpdate = new Thread()
			{

				@Override
				public void run() {
					
					// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt				
					SSLSocketFactory ssf = null;
					SSLSocket s = null;
					ObjectOutputStream doStream = null;
					ObjectInputStream in = null;
					
					// TODO Auto-generated method stub
					try {
						
						// Wird 1x beim Thread checkuser erklärt... Hier selber Schritt
						ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
						s = (SSLSocket) ssf.createSocket(ip[0] , port[0]);	
						s.setEnabledCipherSuites(s.getSupportedCipherSuites());
						doStream = new ObjectOutputStream(s.getOutputStream());
						doStream.writeObject(updatearray);			
						in = new ObjectInputStream(s.getInputStream());
						
						// Vorhandene Nachrichten für diese Chatpartner vom Server entgegennehmen
						nachrichten = (String[]) in.readObject();
						
						runOnUiThread(new Runnable() {
						    @Override
						    public void run() {
						    	
						    	for(int i=2;i<=10;i= i+2)
						    	{
									// Nachrichten an updatemsg übergeben, welche die Nachrichten in der App anzeigt (siehe unten)
						    		updatemsg(nachrichten[12-i],nachrichten[13-i],updatearray[2]);
						        }
						    	}
						    });  

						doStream.flush();
						
					// Fehler abfangen, Streams und Sockets schließen
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
					    if (doStream != null) {
					        try {
					            doStream.close();
					        } catch (IOException ex) {
					        	System.out.println("Error closing Stream");
					        }
					    }
					    if (s != null) {
					        try {
					            s.close();
					        } catch (IOException ex) {
					        	System.out.println("Error closing Stream");
					        }
					    }
					    if (in != null) {
					        try {
					            in.close();
					        } catch (IOException ex) {
					        	System.out.println("Error closing Stream");
					        }
					    }
					}
						
				}		
			};
			
			//Update Thread starten
			tUpdate.start();		
		}
	}
	

	// Wird automatisch generiert
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.appchat, menu);
		return true;
	}

	// Oben rechts in der App kann der Nutzer sich ausloggen
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		// Wenn der Nutzer oben rechts Logout auswählt, wird das Layout auf den Loginbildschirm zurückgesetzt
		if (id == R.id.action_settings) {
			try{
			setContentView(R.layout.activity_login);
			// Verknüpfung zwischen den Layout Feldern und den lokalen Variablen über die ID herstellen
			cUser = (EditText)findViewById(R.id.cUser);
			cPW = (EditText)findViewById(R.id.cPW);
			bLogin = (Button)findViewById(R.id.bLogin);
			bRegister = (Button)findViewById(R.id.bRegister);

			// Warten bis der Login Button ausgeführt wird
			bLogin.setOnClickListener(this);
			bRegister.setOnClickListener(this);
			// Prüfen ob ein User eingegeben wurde..
			if(cUser.getText().toString().trim().equals(""))
			{
				messagebox("Bitte User eingeben!");
			}
			else
			{
				// Wenn User eingegeben, Werte auslesen und an lokale Variablen übergeben
				user = cUser.getText().toString();
				pw = cPW.getText().toString();
				messagebox("Willkommen " + user + "!");			
				// Chat-Layout starten
				setContentView(R.layout.activity_appchat);			
				// Die oben erstellten Variablen mit dem Textfeld und Button im Layout "verknüpfen" (über die ID cMessage, bSend..)		
				bSend=(Button)findViewById(R.id.bSend);	
				bUpdate = (Button)findViewById(R.id.bUpdate);
				cMessage=(EditText)findViewById(R.id.cMessage);
				cReader=(TextView)findViewById(R.id.cReader);
				scroller=(ScrollView)findViewById(R.id.scroller);
				//Listener für den Button "bSend" aktivieren, wenn dieser ausgeführt wird, Methode onClick durchführen
				bSend.setOnClickListener(this);
				bUpdate.setOnClickListener(this);
			}
			
			}catch(Exception exception){
				messagebox("Bitte erst einloggen, um sich ausloggen zu können!");				
			}
		
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Dient dazu, Nachrichten innerhalb der App auszugeben
	public void messagebox(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();		
	}
	
	// Wenn User + Passwort korrekt ist, wird login() ausgeführt
	public void login()
	{
		// Willkommensnachricht für den Benutzer
		
		messagebox("Willkommen " + user + "!");			
		
		// Chat-Layout starten
		setContentView(R.layout.activity_appchat);			
		
		// Die oben erstellten Variablen mit dem Textfeld und Button im Layout "verknüpfen" (über die ID cMessage, bSend..)		
		bSend=(Button)findViewById(R.id.bSend);	
		bUpdate = (Button)findViewById(R.id.bUpdate);
		cMessage=(EditText)findViewById(R.id.cMessage);
		cReader=(TextView)findViewById(R.id.cReader);
		scroller=(ScrollView)findViewById(R.id.scroller);
		
		//Listener für den Button "bSend" aktivieren, wenn dieser ausgeführt wird, Methode onClick durchführen
		bSend.setOnClickListener(this);
		bUpdate.setOnClickListener(this);
		
		//updatetimer();
		// Beim Start 1x den Button Update ausführen, um alte Nachrichten zu laden
		bUpdate.performClick();
	}
	
	
	// Der updatetimer soll später mal in gewissen zeitlichen Abständen durchgeführt werden
	// Hier holt sich die App in zeitliche Abständen alle Nachrichten vom Server,
	// welche noch nicht in die App geladen wurden.
	// Mit diesem Schritt wird der Button Update überflüssig.
	// Um dies zu realisieren, müssen in der Datenbank noch Anpassungen vollzogen werden,
	// damit der Server weiß, welche Nachrichten noch nicht zugestellt sind.
	// So funktioniert der Updatetimer aber bereits.
	public void updatetimer()
	{
		Timer t = new Timer();
		

		t.schedule(new TimerTask() {  
					
					@Override    
					public void run() {
						
						// Hier der seperate Thread im Hintergrund
						final String[] updatearray = new String[5];
						
						//Nachricht ID = 4
						updatearray[0] = "4";
						// Sender der Nachricht
						updatearray[1] = user;
						//Empfänger der Nachricht - hart codiert
						if(user.equals("Jonas"))
						{
						updatearray[2] = "Andi";
						}
						else
						{
							updatearray[2] = "Jonas";
						}

						
						// neuen Thread erstellen, um mit einem Timer vorhandene Nachrichten zu holen
						// noch nicht komplett implementiert
						Thread tUpdateTimer = new Thread()
						{

							@Override
							public void run() {
								
								SSLSocketFactory ssf = null;
								SSLSocket s = null;
								ObjectOutputStream doStream = null;
								ObjectInputStream in = null;
								
								// TODO Auto-generated method stub
								try {
									//Verbindung zum Server IP/Port8001 über den Socket herstellen
									ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
									s = (SSLSocket) ssf.createSocket(ip[0] , port[0]);	
									
									//Alle verfügbaren Ciphersuites abrufen und den geeigneten verwenden..
									s.setEnabledCipherSuites(s.getSupportedCipherSuites());
									
									// neuen Schreibstream mit doStream erstellen und die erhaltenen Werte vom Socket s zuweisen
									doStream = new ObjectOutputStream(s.getOutputStream());
									
									//Daten in String umwandeln und ausgeben
									doStream.writeObject(updatearray);
									//Stream leeren und schließen / Socket s schließen
									
								
									in = new ObjectInputStream(s.getInputStream());
									
									nachrichten = (String[]) in.readObject();
									
									runOnUiThread(new Runnable() {
									    @Override
									    public void run() {
									    	
									    	for(int i=2;i<=6;i= i+2)
									    	{
									    		updatemsg(nachrichten[i],nachrichten[i+1],updatearray[2]);
									        }
									    	}
									    });  

									doStream.flush();	
									
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (NotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} finally {
								    if (doStream != null) {
								        try {
								            doStream.close();
								        } catch (IOException ex) {
								        	System.out.println("Error closing Stream");
								        }
								    }
								    if (s != null) {
								        try {
								            s.close();
								        } catch (IOException ex) {
								        	System.out.println("Error closing Stream");
								        }
								    }
								    if (in != null) {
								        try {
								            in.close();
								        } catch (IOException ex) {
								        	System.out.println("Error closing Stream");
								        }
								    }
								}
									
							}		
						};
						tUpdateTimer.start();						
					};
				}, 0L, 1L * 100000);
	}
	
	// Hier wird die empfangene Nachricht vom Server in der App angezeigt
	public void updatemsg(String sender, String msg, String empfänger)
	{
		// Wenn die Nachricht nicht leer ist
		if(msg != null)
		{			
		try {
			// Wird hier die Nachricht mit AES 256 entschlüsselt und
			// in der App ausgegeben
			cReader.append("(" + sender + "): " + encryptionAES.decrypt(msg, getAESkeyValue(empfänger)) + "\n\n");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      

		scroller.post(new Runnable() { 
		                public void run() { 
		                    scroller.smoothScrollTo(0, cReader.getBottom());
		                } 
		            }); 		
		}
		
		
	}
	
	// Hier werden Texte in SHA 256 gehashed
	public static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	// Hier werden Passwörter gesaltet
	public static String salt(String user, String password) {
		password = user + "Add some salt" + password;
		return password;
	}
	
	// Hier wird der AES aus dem Dateisystem gelesen und als byte Array zurückgegeben
	public byte[] getAESkeyValue(String user)
	{
		String defaultDataDir = c.getApplicationInfo().dataDir;
		String filename = defaultDataDir + "/AES_" + user + ".txt";
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		try {
			while ((nRead = fis.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			buffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] keyvalue = buffer.toByteArray();
		return keyvalue;
	}
	
}


