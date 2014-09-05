package de.wi2012.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	
	 
	
	 private static Connection conn = null;
	 
	  // Hostname
	  private static String dbHost = "193.196.175.236";
	 
	  // Port -- Standard: 3306
	  private static String dbPort = "3306";
	 
	  // Datenbankname
	  private static String database = "WWI2012a4";
	 
	  // Datenbankuser
	  private static String dbUser = "WWI2012a4";
	 
	  // Datenbankpasswort
	  private static String dbPassword = "WWI2012a4";
	  
	  private DBConnection() {
	    try {
	 
	      // Datenbanktreiber für ODBC Schnittstellen laden.
	      // Für verschiedene ODBC-Datenbanken muss dieser Treiber
	      // nur einmal geladen werden.
	      Class.forName("com.mysql.jdbc.Driver");
	 
	      // Verbindung zur ODBC-Datenbank 'sakila' herstellen.
	      // Es wird die JDBC-ODBC-Brücke verwendet.
	      conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
	          + dbPort + "/" + database + "?" + "user=" + dbUser + "&"
	          + "password=" + dbPassword);

	    } catch (ClassNotFoundException e) {
	      System.out.println("Treiber nicht gefunden");
	    } catch (SQLException e) {
	      System.out.println("Connect nicht moeglich " + e);
	    }
	  }
	  
	  private static Connection getInstance()
	  {
	    if(conn == null)
	      new DBConnection();
	    return conn;
	  }
	  
	  public static void printNameList()
	  {
		  conn = getInstance();
		  
		    if(conn != null)
		    {
		      // Anfrage-Statement erzeugen.
		      Statement query;
		      try {
		        query = conn.createStatement();
		 
		        // Ergebnistabelle erzeugen und abholen.
		        String sql = "SELECT client_id, clientname, password, email " +
                        "FROM clients " +
                        "ORDER BY client_id";
		        ResultSet result = query.executeQuery(sql);
		 
		        // Ergebnissätze durchfahren.
		        while (result.next()) {
		          String client_id = result.getString("client_id"); // Alternativ: result.getString(1);
		          String clientname = result.getString("clientname"); // Alternativ: result.getString(2);
		          String password = result.getString("password");
		          String email = result.getString("email");

		          System.out.println("ID: " + client_id + " Username: " + clientname + " PW: " + password + " E-Mail: " + email);
		        }
		      } catch (SQLException e) {
		        e.printStackTrace();
		      }
		    }
	  }
	  
	  public static boolean insertName(String clientname, String password, String email)
	  {
	    conn = getInstance();
	 
	    if(conn != null)
	    {
	      try {
	 
	    	  
	        // Insert-Statement erzeugen (Fragezeichen werden später ersetzt).
	        String sql = "INSERT INTO clients(clientname, password, email) " +
	                     "VALUES(?, ?, ?)";
	        PreparedStatement preparedStatement = conn.prepareStatement(sql);

	        // Erstes Fragezeichen durch "Username" Parameter ersetzen
	        preparedStatement.setString(1, clientname);
	        // Zweites Fragezeichen durch "Passwort" Parameter ersetzen
	        preparedStatement.setString(2, password);
	        // Drittes Fragezeichen durch "Email" Parameter ersetzen
	        preparedStatement.setString(3, email);
	        // SQL ausführen.
	        preparedStatement.executeUpdate();
	        
	       return true;
	 

	      } catch (SQLException e) {
	        //e.printStackTrace();
	        //System.out.println("Name bereits vergeben!!");
	        
	        return false;
	        
	      }
	    }
		return false;
	  }
	
}
