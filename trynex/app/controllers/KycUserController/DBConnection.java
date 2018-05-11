package controllers.KycUserController;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
	private static Connection conn;

	private DBConnection() {

	}

	public static Connection getConnectionString() {

		try {

			Class.forName("org.postgresql.Driver");

			conn = DriverManager.getConnection("jdbc:postgresql://18.222.97.182:5432/exchange", "postgres", "root");

		} catch (Exception e) {

		}
		return conn;

	}

	public static void closeConnection(Connection conn) {

		try {

			conn.close();

		} catch (Exception e) {

		}

	}

}
