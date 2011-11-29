package uk.ac.cam.db538.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database {
	private Connection connection;

	public Database(String databasePath) throws SQLException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException ex) {
			throw new SQLException(ex);
		}

		// open connection to database
		connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ databasePath, "SA", "");

		Statement delayStmt = connection.createStatement();
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		} // Always update data on disk
		finally {
			delayStmt.close();
		}

		// turn transactions on
		connection.setAutoCommit(false);

		// create new table "messages"
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"
					+ "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			// System.out
			// .println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}

		// create new table "statistics"
		boolean firstTimeStatistics = true;
		sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE statistics(key VARCHAR(255), value INT)");
		} catch (SQLException e) {
			firstTimeStatistics = false;
			// System.out
			// .println("Warning: Database table \"statistics\" already exists.");
		} finally {
			sqlStmt.close();
		}

		// insert rows
		if (firstTimeStatistics) {
			String stmt = "INSERT INTO statistics(key, value) VALUES ('Total messages', 0)";
			PreparedStatement insertMessage = connection.prepareStatement(stmt);
			try {
				insertMessage.executeUpdate();
			} finally { // Notice use of finally clause here to finish statement
				insertMessage.close();
			}

			stmt = "INSERT INTO statistics(key, value) VALUES ('Total logins', 0)";
			insertMessage = connection.prepareStatement(stmt);
			try {
				insertMessage.executeUpdate();
			} finally { // Notice use of finally clause here to finish statement
				insertMessage.close();
			}
		}

		// commit
		connection.commit();
	}

	public void close() throws SQLException {
		connection.close();
	}

	public void increaseLogins() throws SQLException {
		String stmt = "UPDATE statistics SET value = value + 1 WHERE key='Total logins'";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.executeUpdate();
		} finally { // Notice use of finally clause here to finish statement
			insertMessage.close();
		}

		connection.commit();
	}

	public void addMessage(RelayMessage m) throws SQLException {
		// insert row
		String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.setString(1, m.getFrom()); // set value of first "?"
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, m.getCreationTime().getTime());
			insertMessage.executeUpdate();
		} finally { // Notice use of finally clause here to finish statement
			insertMessage.close();
		}

		stmt = "UPDATE statistics SET value = value + 1 WHERE key='Total messages'";
		insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.executeUpdate();
		} finally { // Notice use of finally clause here to finish statement
			insertMessage.close();
		}

		connection.commit();
	}

	public List<RelayMessage> getRecent() throws SQLException {
		List<RelayMessage> result = new LinkedList<RelayMessage>();

		// query
		String stmt = "SELECT nick,message,timeposted FROM messages "
				+ "ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next())
					result.add(0,
							new RelayMessage(rs.getString(1), rs.getString(2),
									new Date(rs.getLong(3))));
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
		}

		return result;
	}

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException {
		if (args.length != 1) {
			System.err
					.println("Usage: java uk.ac.cam.db538.fjava.tick5.Database <database name>");
			return;
		}

		// open connection to database
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ args[0], "SA", "");

		Statement delayStmt = connection.createStatement();
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		} // Always update data on disk
		finally {
			delayStmt.close();
		}

		// turn transactions on
		connection.setAutoCommit(false);

		// create new table "messages"
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"
					+ "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			System.out
					.println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}

		// insert row
		String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.setString(1, "Alastair"); // set value of first "?"
			insertMessage.setString(2, "Hello, Andy");
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
		} finally { // Notice use of finally clause here to finish statement
			insertMessage.close();
		}

		// commit
		connection.commit();

		// query
		stmt = "SELECT nick,message,timeposted FROM messages "
				+ "ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next())
					System.out.println(rs.getString(1) + ": " + rs.getString(2)
							+ " [" + rs.getLong(3) + "]");
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
		}

		// close connection
		connection.close();
	}
}
