package edu.damago.java.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Facade for the text line input based World application
 * using object-oriented programming techniques and exception handling.
 * @author Sascha Baumeister
 */
public class WorldApp {
	static private enum DatabaseType { MYSQL, MARIA_DB, ORACLE_DB, MS_ACCESS, ODBC }

	/**
	 * Application entry point.
	 * @param args the runtime arguments
	 * @throws IOException if there is an I/O related problem
	 * @throws SQLException if there is an SQL related problem
	 */
	static public void main (final String[] args) throws IOException, SQLException {
		final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
		final Connection jdbcConnection = newJdbcConnection(DatabaseType.MYSQL);
		System.out.println("Connected: " + jdbcConnection.getMetaData().getDatabaseProductName());

		while (true) {
			System.out.print("> ");
			final String line = consoleReader.readLine().trim();
			final int delimiterPosition = line.indexOf(' ');
			final String command = (delimiterPosition == -1 ? line : line.substring(0, delimiterPosition)).trim();
			final String arguments = (delimiterPosition == -1 ? "" : line.substring(delimiterPosition + 1)).trim();

			try {
				switch (command.toLowerCase()) {
					case "quit":
						System.out.println("bye!");
						return;
					case "query-countries":
						processQueryCountriesCommand(jdbcConnection, arguments);
						break;
					case "query-languages":
						processQueryLanguagesCommand(jdbcConnection, arguments);
						break;
					case "query-cities":
						processQueryCitiesCommand(jdbcConnection, arguments);
						break;
					default:
						processHelpCommand(arguments);
						break;
				}
			} catch (final Exception e) {
				e.printStackTrace();
				// processHelpCommand(arguments);
			}
		}
	}


	/**
	 * Displays the available commands.
	 * @param arguments the arguments
	 */
	static public void processHelpCommand (final String arguments) {
		System.out.println("List of available commands:");
		System.out.println("- help: displays this help");
		System.out.println("- query-countries: displays all countries as JSON");
		System.out.println("- query-languages <country-id>: displays all languages of a given country as JSON");
		System.out.println("- query-cities <country-id>: displays all cities of a given country as JSON, with capitals marked as such");
		System.out.println("- quit: terminates this program");
	}


	/**
	 * Queries and filters the persistent countries, and displays them as JSON.
	 * @param jdbcConnection the JDBC connection
	 * @param arguments the arguments
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an SQL related problem
	 */
	static private void processQueryCountriesCommand (final Connection jdbcConnection, final String arguments) throws NullPointerException, SQLException {
		// TODO: query and display all countries as JSON
	}


	/**
	 * Queries the languages of the given country, and displays them as JSON.
	 * @param jdbcConnection the JDBC connection
	 * @param arguments the arguments
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an SQL related problem
	 */
	static private void processQueryLanguagesCommand (final Connection jdbcConnection, final String arguments) throws NullPointerException, SQLException {
		// TODO: parse country-id from the given arguments-text, query and display as JSON
	}


	/**
	 * Queries the cities of the given country, and displays them as JSON.
	 * The capital city is additionally marked as such.
	 * @param jdbcConnection the JDBC connection
	 * @param arguments the arguments
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an SQL related problem
	 */
	static private void processQueryCitiesCommand (final Connection jdbcConnection, final String arguments) throws NullPointerException, SQLException {
		// TODO: parse country-id from the given arguments-text, query and display as JSON;
		//       join each city with its respective country to detect capitals,
		//       but solely display city information!
	}


	/**
	 * Connects to the given database type, and returns the resulting JDBC connection.
	 * @param databaseType the database type
	 * @return the JDBC connection created
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an SQL related problem
	 */
	static private Connection newJdbcConnection (final DatabaseType type) throws NullPointerException, SQLException {
		switch (type) {
			case MYSQL:
				return DriverManager.getConnection("jdbc:mysql://localhost:3306/world", "root", "root");
			case MARIA_DB:
				return DriverManager.getConnection("jdbc:mariadb://localhost:3306/world", "root", "root");
			case ORACLE_DB:
				return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "changeit");
			case MS_ACCESS:
				return DriverManager.getConnection("jdbc:ucanaccess://world.accdb", "Administrator", "damago");
			case ODBC:
				return DriverManager.getConnection("jdbc:odbc:world", "Administrator", "damago");
			default:
				throw new AssertionError();
		}
	}
}