package edu.damago.java.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.damago.java.tool.JSON;


/**
 * Facade for the text line input based World application
 * using object-oriented programming techniques and exception handling.
 * @author Sascha Baumeister
 */
public class World1App {
	static private enum DatabaseType { MYSQL, MARIA_DB, ORACLE_DB, MS_ACCESS, ODBC }

	static private final String QUERY_COUNTRIES = "SELECT * FROM Country ORDER BY longCode";
	static private final String QUERY_LANGUAGES = "SELECT * FROM CountryLanguage WHERE countryCode=? ORDER BY language";
	static private final String QUERY_CITIES = "SELECT City.*, cityIdentity=capitalReference AS capital FROM City LEFT OUTER JOIN Country ON countryCode=longCode WHERE countryCode=? ORDER BY name";


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
		System.out.println("- query-countries <JSON>: displays any countries matching the given filter criteria as JSON");
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
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(QUERY_COUNTRIES)) {
			try (ResultSet tableCursor = jdbcStatement.executeQuery()) {
				final ResultSetMetaData metaData = tableCursor.getMetaData();
				final List<Map<String,Object>> rowMaps = new ArrayList<>();

				while (tableCursor.next()) {
					final Map<String,Object> rowMap = new HashMap<>();
					rowMaps.add(rowMap);

					for (int index = 1; index <= metaData.getColumnCount(); ++index)
						rowMap.put(metaData.getColumnLabel(index), tableCursor.getObject(index));
				}

				System.out.println("Countries:");
				for (final Map<String,Object> rowMap : rowMaps)
					System.out.println(JSON.stringify(rowMap));
			}
		}
	}


	/**
	 * Queries the languages of the given country, and displays them as JSON.
	 * @param jdbcConnection the JDBC connection
	 * @param arguments the arguments
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an SQL related problem
	 */
	static private void processQueryLanguagesCommand (final Connection jdbcConnection, final String arguments) throws NullPointerException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(QUERY_LANGUAGES)) {
			jdbcStatement.setString(1, arguments);

			try (ResultSet tableCursor = jdbcStatement.executeQuery()) {
				final ResultSetMetaData metaData = tableCursor.getMetaData();
				final List<Map<String,Object>> rowMaps = new ArrayList<>();

				while (tableCursor.next()) {
					final Map<String,Object> rowMap = new HashMap<>();
					rowMaps.add(rowMap);

					for (int index = 1; index <= metaData.getColumnCount(); ++index)
						rowMap.put(metaData.getColumnLabel(index), tableCursor.getObject(index));
				}

				System.out.println("Languages of country: " + arguments);
				for (final Map<String,Object> rowMap : rowMaps)
					System.out.println(JSON.stringify(rowMap));
			}
		}
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
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(QUERY_CITIES)) {
			jdbcStatement.setString(1, arguments);

			try (ResultSet tableCursor = jdbcStatement.executeQuery()) {
				final ResultSetMetaData metaData = tableCursor.getMetaData();
				final List<Map<String,Object>> rowMaps = new ArrayList<>();

				while (tableCursor.next()) {
					final Map<String,Object> rowMap = new HashMap<>();
					rowMaps.add(rowMap);

					for (int index = 1; index <= metaData.getColumnCount(); ++index)
						rowMap.put(metaData.getColumnLabel(index), tableCursor.getObject(index));

					// rowMap.put("capital", tableCursor.getBoolean(metaData.getColumnCount()));
				}

				for (final Map<String,Object> rowMap : rowMaps)
					rowMap.put("capital", rowMap.get("capital").equals(1L));

				System.out.println("Cities of country: " + arguments);
				for (final Map<String,Object> rowMap : rowMaps)
					System.out.println(JSON.stringify(rowMap));
			}
		}
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
				return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "root");
			case MS_ACCESS:
				return DriverManager.getConnection("jdbc:ucanaccess://world.accdb", "Administrator", "damago");
			case ODBC:
				return DriverManager.getConnection("jdbc:odbc:world", "Administrator", "damago");
			default:
				throw new AssertionError();
		}
	}
}