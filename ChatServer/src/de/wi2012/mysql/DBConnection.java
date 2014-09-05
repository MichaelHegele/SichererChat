package de.wi2012.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

	private static Connection conn = null;

	// IP, Port, Name, User und Passwort der Datenbank
	private static String dbHost = "193.196.175.236";
	private static String dbPort = "3306";
	private static String database = "WWI2012a4";
	private static String dbUser = "WWI2012a4";
	private static String dbPassword = "WWI2012a4";

	private DBConnection() {
		try {
			// Datenbanktreiber laden..
			Class.forName("com.mysql.jdbc.Driver");

			// Verbindung zur Datenbank herstellen..
			conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
					+ dbPort + "/" + database + "?" + "user=" + dbUser + "&"
					+ "password=" + dbPassword);
		} catch (ClassNotFoundException e) {
			System.out.println("Treiber nicht gefunden");
		} catch (SQLException e) {
			System.out.println("Connect nicht moeglich " + e);
		}
	}

	private static Connection getInstance() {
		if (conn == null)
			new DBConnection();
		return conn;
	}

	public static void printNameList() {
		conn = getInstance();
		if (conn != null) {
			Statement query;

			try {
				// Anfrage erzeugen
				query = conn.createStatement();

				// Ergebnistabelle erzeugen und abholen
				String sql = "SELECT client_id, clientname, password, email "
						+ "FROM clients " + "ORDER BY client_id";
				ResultSet result = query.executeQuery(sql);

				// Ergebnissätze durchgehen und ausgeben
				while (result.next()) {
					String client_id = result.getString("client_id");
					String clientname = result.getString("clientname");
					String password = result.getString("password");
					String email = result.getString("email");
					System.out.println("ID: " + client_id + " Username: "
							+ clientname + " PW: " + password + " E-Mail: "
							+ email);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean checkUser(String client, String pass) {
		conn = getInstance();
		boolean checkit = false;
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				String sql = "SELECT * " + "FROM clients "
						+ "WHERE clientname = '" + client + "'"
						+ " AND password = '" + pass + "'";
				ResultSet result = query.executeQuery(sql);

				// Ergebnissätze durchgehen und Meldung ausgeben
				if (result.next()) {
					System.out.println("Login erfolgreich.");
					checkit = true;
				} else {
					System.out.println("User nicht registriert!");
					checkit = false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				checkit = false;
			}
		}
		return checkit;
	}

	public static boolean checkAESPkey(String sender, String receiver) {
		conn = getInstance();
		boolean checkit = false;
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				String client_one = "SELECT client_id FROM clients WHERE clientname = '"
						+ sender + "'";
				ResultSet client_1 = query.executeQuery(client_one);
				while (client_1.next()) {
					client_one = client_1.getString("client_id");
				}

				String client_two = "SELECT client_id FROM clients WHERE clientname = '"
						+ receiver + "'";
				ResultSet client_2 = query.executeQuery(client_two);
				while (client_2.next()) {
					client_two = client_2.getString("client_id");
				}

				String sessionkey = "SELECT sessionkey FROM chats "
						+ "WHERE client_one = '" + client_one
						+ "' AND client_two = '" + client_two + "' "
						+ "OR client_one = '" + client_two
						+ "' AND client_two = '" + client_one + "'";
				ResultSet session_key = query.executeQuery(sessionkey);
				while (session_key.next()) {
					sessionkey = session_key.getString("sessionkey");
				}

				if (sessionkey.equals("empty")) {
					checkit = false;
					return checkit;
				} else {
					checkit = true;
					return checkit;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				checkit = false;
			}
		}
		return checkit;
	}

	public static String getAESPkey(String sender, String receiver) {
		conn = getInstance();
		String sessionkey = "empty";
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				String client_one = "SELECT client_id FROM clients WHERE clientname = '"
						+ sender + "'";
				ResultSet client_1 = query.executeQuery(client_one);
				while (client_1.next()) {
					client_one = client_1.getString("client_id");
				}

				String client_two = "SELECT client_id FROM clients WHERE clientname = '"
						+ receiver + "'";
				ResultSet client_2 = query.executeQuery(client_two);
				while (client_2.next()) {
					client_two = client_2.getString("client_id");
				}

				sessionkey = "SELECT sessionkey FROM chats "
						+ "WHERE client_one = '" + client_one
						+ "' AND client_two = '" + client_two + "' "
						+ "OR client_one = '" + client_two
						+ "' AND client_two = '" + client_one + "'";
				ResultSet session_key = query.executeQuery(sessionkey);
				while (session_key.next()) {
					sessionkey = session_key.getString("sessionkey");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sessionkey;
	}

	public static String getPublicKey(String receiver) {
		conn = getInstance();
		String publicKey = null;
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				publicKey = "SELECT pkey FROM clients "
						+ "WHERE clientname = '" + receiver + "' ";
				ResultSet public_key = query.executeQuery(publicKey);
				while (public_key.next()) {
					publicKey = public_key.getString("pkey");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return publicKey;
	}

	public static boolean insertAESPkey(String sender, String receiver,
			String key) {
		conn = getInstance();
		boolean checkit = false;
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				String client_one = "SELECT client_id FROM clients WHERE clientname = '"
						+ sender + "'";
				ResultSet client_1 = query.executeQuery(client_one);
				while (client_1.next()) {
					client_one = client_1.getString("client_id");
				}

				String client_two = "SELECT client_id FROM clients WHERE clientname = '"
						+ receiver + "'";
				ResultSet client_2 = query.executeQuery(client_two);
				while (client_2.next()) {
					client_two = client_2.getString("client_id");
				}

				String chat_id = "SELECT chat_id FROM chats "
						+ "WHERE client_one = '" + client_one
						+ "' AND client_two = '" + client_two + "' "
						+ "OR client_one = '" + client_two
						+ "' AND client_two = '" + client_one + "'";
				ResultSet chat_idr = query.executeQuery(chat_id);
				while (chat_idr.next()) {
					chat_id = chat_idr.getString("chat_id");
				}

				// Update erzeugen
				String sql = "UPDATE chats SET sessionkey = '" + key
						+ "' WHERE chat_id = '" + chat_id + "'";
				PreparedStatement preparedStatement = conn
						.prepareStatement(sql);

				// SQL ausführen
				if (!key.equals("empty")) {
					preparedStatement.executeUpdate();
				} else {
					checkit = false;
				}
				checkit = true;
			} catch (SQLException e) {
				e.printStackTrace();
				checkit = false;
			}
		}
		return checkit;
	}

	public static boolean insertName(String clientname, String password,
			String email, String pkey) {
		conn = getInstance();
		boolean checkit = false;
		if (conn != null) {
			try {

				// Insert erzeugen (Fragezeichen werden später ersetzt)
				String sql = "INSERT INTO clients(clientname, password, email, pkey) "
						+ "VALUES(?, ?, ?, ?)";
				PreparedStatement preparedStatement = conn
						.prepareStatement(sql);

				// Erstes Fragezeichen durch "Username" Parameter ersetzen
				preparedStatement.setString(1, clientname);
				// Zweites Fragezeichen durch "Passwort" Parameter ersetzen
				preparedStatement.setString(2, password);
				// Drittes Fragezeichen durch "Email" Parameter ersetzen
				preparedStatement.setString(3, email);
				// Drittes Fragezeichen durch "PKey" Parameter ersetzen
				preparedStatement.setString(4, pkey);
				// SQL ausführen
				preparedStatement.executeUpdate();
				checkit = true;
			} catch (SQLException e) {
				e.printStackTrace();
				checkit = false;
			}
		}
		return checkit;
	}

	public static boolean insMSG(String sender, String receiver, String msg) {
		conn = getInstance();
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();

				String client_one = "SELECT client_id FROM clients WHERE clientname = '"
						+ sender + "'";
				ResultSet client_1 = query.executeQuery(client_one);
				while (client_1.next()) {
					client_one = client_1.getString("client_id");
				}

				String client_two = "SELECT client_id FROM clients WHERE clientname = '"
						+ receiver + "'";
				ResultSet client_2 = query.executeQuery(client_two);
				while (client_2.next()) {
					client_two = client_2.getString("client_id");
				}

				String chat_id = "SELECT chat_id FROM chats "
						+ "WHERE client_one = '" + client_one
						+ "' AND client_two = '" + client_two + "' "
						+ "OR client_one = '" + client_two
						+ "' AND client_two = '" + client_one + "'";
				ResultSet chat_idr = query.executeQuery(chat_id);
				while (chat_idr.next()) {
					chat_id = chat_idr.getString("chat_id");
				}

				String sql = "INSERT INTO messages(message, client_id_fk, chat_id_fk) "
						+ "VALUES(?, ?, ?)";
				PreparedStatement preparedStatement = conn
						.prepareStatement(sql);

				preparedStatement.setString(1, msg);
				preparedStatement.setString(2, client_one);
				preparedStatement.setString(3, chat_id);

				preparedStatement.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public static String[] getMSG(String sender, String receiver) {
		conn = getInstance();
		String nachrichten[] = new String[20];

		if (conn != null) {
			Statement query;
			Statement query1;

			nachrichten[0] = sender;
			nachrichten[1] = receiver;

			try {
				query = conn.createStatement();
				query1 = conn.createStatement();

				String client_one = "SELECT client_id FROM clients WHERE clientname = '"
						+ sender + "'";
				ResultSet client_1 = query.executeQuery(client_one);
				while (client_1.next()) {
					client_one = client_1.getString("client_id");
				}

				String client_two = "SELECT client_id FROM clients WHERE clientname = '"
						+ receiver + "'";
				ResultSet client_2 = query.executeQuery(client_two);
				while (client_2.next()) {
					client_two = client_2.getString("client_id");
				}

				String chat_id = "SELECT chat_id FROM chats "
						+ "WHERE client_one = '" + client_one
						+ "' AND client_two = '" + client_two + "' "
						+ "OR client_one = '" + client_two
						+ "' AND client_two = '" + client_one + "'";
				ResultSet chat_idr = query.executeQuery(chat_id);
				while (chat_idr.next()) {
					chat_id = chat_idr.getString("chat_id");
				}

				int i = 2;

				String sql = "SELECT * FROM messages WHERE chat_id_fk = '"
						+ chat_id + "' ORDER BY message_id DESC LIMIT 5;";
				String user_id = null;
				String user = null;
				ResultSet ausgabe = query.executeQuery(sql);

				while (ausgabe.next()) {
					sql = ausgabe.getString("message");
					user_id = ausgabe.getString("client_id_fk");
					user = "SELECT clientname FROM clients WHERE client_id = '"
							+ user_id + "'";
					ResultSet id = query1.executeQuery(user);

					while (id.next()) {
						user = id.getString("clientname");
					}

					nachrichten[i] = user;
					nachrichten[i + 1] = sql;
					i = i + 2;
				}
				return nachrichten;
			} catch (SQLException e) {
				e.printStackTrace();
				return nachrichten;
			}
		}
		return nachrichten;
	}
}
