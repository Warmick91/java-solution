package edu.damago.java.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Facade for additional JDBC related operations for
 * accessing relational databases in a weakly typed way.
 * @author Sascha Baumeister
 */
public class RelationalDatabases {
	static private final Map<ResultSet, ResultSetMetaData> META_DATA_CACHE = new WeakHashMap<>();


	/**
	 * Returns whether or not a given SQL type is numeric. 
	 * @param type an SQL type
	 * @return whether or not the given SQL type is numeric
	 * @see java.sql.Types
	 */
	static public boolean isNumericType (final int type) {
		return type == Types.BIT | type == Types.TINYINT | type == Types.SMALLINT | type == Types.INTEGER | type == Types.BIGINT | type == Types.FLOAT | type == Types.DOUBLE | type == Types.REAL | type == Types.DECIMAL | type == Types.NUMERIC;
	}


	/**
	 * Returns the current row content of the given result set as a row map. 
	 * @param resultSet the result set
	 * @return the row map
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an JDBC related problem
	 */
	static public Map<String,Object> toRowMap (final ResultSet resultSet) throws NullPointerException, SQLException {
		ResultSetMetaData metaData;
		synchronized (META_DATA_CACHE) {
			metaData = META_DATA_CACHE.get(resultSet);
			if (metaData == null) META_DATA_CACHE.put(resultSet, metaData = resultSet.getMetaData());
		}

		final Map<String,Object> rowMap = new HashMap<>();
		for (int index = 1, columnCount = metaData.getColumnCount(); index <= columnCount; ++index)
			rowMap.put(metaData.getColumnLabel(index), resultSet.getObject(index));

		return rowMap;
	}


	/**
	 * Returns the content of the given result set as a list of table row maps. 
	 * @param resultSet the result set
	 * @return the row maps
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an JDBC related problem
	 */
	static public List<Map<String,Object>> toRowMaps (final ResultSet resultSet) throws NullPointerException, SQLException {
		final List<Map<String,Object>> rowMaps = new ArrayList<>();
		while (resultSet.next())
			rowMaps.add(toRowMap(resultSet));

		return rowMaps;
	}


	/**
	 * Returns the content of the given result set as a list of table row beans. 
	 * @param <T> the row bean type
	 * @param resultSet the result set
     * @param rowMapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each result set row
	 * @return the row beans
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an JDBC related problem
	 */
	static public <T> List<T> toRowBeans (final ResultSet resultSet, final Function<ResultSet,T> rowMapper) throws NullPointerException, SQLException {
		final List<T> rowMaps = new ArrayList<>();

		try {
			while (resultSet.next())
				rowMaps.add(rowMapper.apply(resultSet));
		} catch (final UncheckedSQLException e) {
			throw e.getCause();
		}

		return rowMaps;
	}


	/**
	 * Returns the content of the given result set as a sequential stream of table row maps. 
	 * @param resultSet the result set
	 * @return the iterable of row maps
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws SQLException if there is an JDBC related problem
	 */
	static public Stream<Map<String,Object>> toRowMapStream (final ResultSet resultSet) throws NullPointerException, SQLException {
		final Iterator<Map<String,Object>> iterator = new RowMapIterator(resultSet);
		final Iterable<Map<String,Object>> iterable = () -> iterator;
		return StreamSupport.stream(iterable.spliterator(), false);
	}


	/**
	 * Executes the given SQL query and returns the resulting table row maps.
	 * @param jdbcConnection the JDBC connection
	 * @param sql the query SQL
	 * @param parameters the query parameter values as a var-arg array 
	 * @return the resulting row maps
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the number of given parameters does not
	 * 			equal the number of question marks within the given SQL
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public List<Map<String,Object>> executeQuery (final Connection jdbcConnection, final String sql, final Object... parameters) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql)) {
			if (jdbcStatement.getParameterMetaData().getParameterCount() != parameters.length) throw new IllegalArgumentException();
			for (int index = 0; index < parameters.length; ++index)
				jdbcStatement.setObject(index + 1, parameters[index]);

			try (ResultSet tableCursor = jdbcStatement.executeQuery()) {
				return toRowMaps(tableCursor);
			}
		}
	}


	/**
	 * Executes the given SQL query and returns the resulting table row beans.
	 * @param <T> the row bean type
	 * @param jdbcConnection the JDBC connection
	 * @param sql the query SQL
     * @param rowMapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each result set row
	 * @param parameters the query parameter values as a var-arg array 
	 * @return the resulting row beans
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the number of given parameters does not
	 * 			equal the number of question marks within the given SQL
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public <T> List<T> executeQuery (final Connection jdbcConnection, final String sql, final Function<ResultSet,T> rowMapper, final Object... parameters) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql)) {
			if (jdbcStatement.getParameterMetaData().getParameterCount() != parameters.length) throw new IllegalArgumentException();
			for (int index = 0; index < parameters.length; ++index)
				jdbcStatement.setObject(index + 1, parameters[index]);

			try (ResultSet tableCursor = jdbcStatement.executeQuery()) {
				return toRowBeans(tableCursor, rowMapper);
			}
		}
	}


	/**
	 * Executes the given SQL insert, update, or delete statement, and returns
	 * the number of modified table rows.
	 * @param jdbcConnection the JDBC connection
	 * @param sql the modifying SQL
	 * @param parameters the parameter values as a var-arg array 
	 * @return the number of modified rows
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the number of given parameters does not
	 * 			equal the number of question marks within the given SQL
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public long executeChange1 (final Connection jdbcConnection, final String sql, final Object... parameters) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql)) {
			if (jdbcStatement.getParameterMetaData().getParameterCount() != parameters.length) throw new IllegalArgumentException();
			for (int index = 0; index < parameters.length; ++index)
				jdbcStatement.setObject(index + 1, parameters[index]);

			jdbcStatement.executeUpdate();
			return jdbcStatement.getLargeUpdateCount();
		}
	}


	/**
	 * Executes the given SQL insert or update statement, and returns
	 * the number of modified table rows.
	 * @param <T> the row bean type
	 * @param jdbcConnection the JDBC connection
	 * @param sql the modifying SQL
     * @param parameterMapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> binary consumer
     *               to copy the given object's fields into the given statement's parameters 
	 * @param object the row bean to map into statement parameters
	 * @return the number of modified rows
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public <T> long executeChange1 (final Connection jdbcConnection, final String sql, final BiConsumer<PreparedStatement,T> parameterMapper, final T object) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql)) {
			parameterMapper.accept(jdbcStatement, object);

			jdbcStatement.executeUpdate();
			return jdbcStatement.getLargeUpdateCount();
		}
	}


	/**
	 * Executes the given SQL insert or update statement, and returns the resulting
	 * auto-generated field values. Note that the resulting values are undefined if
	 * any of these fields is set to a value differing from zero!
	 * @param jdbcConnection the JDBC connection
	 * @param sql the insert SQL
	 * @param parameters the query parameter values as a var-arg array 
	 * @return the resulting auto-generated values, for each modified row
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws IllegalArgumentException if the number of given parameters does not
	 * 			equal the number of question marks within the given SQL
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public long[][] executeChange2 (final Connection jdbcConnection, final String sql, final Object... parameters) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			if (jdbcStatement.getParameterMetaData().getParameterCount() != parameters.length) throw new IllegalArgumentException();
			for (int index = 0; index < parameters.length; ++index)
				jdbcStatement.setObject(index + 1, parameters[index]);

			final int rowCount = jdbcStatement.executeUpdate();
			try (ResultSet keyCursor = jdbcStatement.getGeneratedKeys()) {
				final int columnCount = keyCursor.getMetaData().getColumnCount();
				final long[][] generatedValues = new long[rowCount][columnCount];

				for (int rowIndex = 0; keyCursor.next(); ++rowIndex)
					for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex)
						generatedValues[rowIndex][columnIndex] = keyCursor.getLong(columnIndex + 1);

				return generatedValues;
			}
		}
	}


	/**
	 * Executes the given SQL insert or update statement, and returns the resulting
	 * auto-generated field values. Note that the resulting values are undefined if
	 * any of these fields is set to a value differing from zero!
	 * @param <T> the row bean type
	 * @param jdbcConnection the JDBC connection
	 * @param sql the insert SQL
     * @param parameterMapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a> binary consumer
     *               to copy the given object's fields into the given statement's parameters 
	 * @param object the row bean to map into statement parameters
	 * @return the resulting auto-generated values, for each modified row
	 * @throws NullPointerException if any of the given arguments is {@code null}
	 * @throws SQLException if there is an SQL related problem 
	 */
	static public <T> long[][] executeChange2 (final Connection jdbcConnection, final String sql, final BiConsumer<PreparedStatement,T> parameterMapper, final T object) throws NullPointerException, IllegalArgumentException, SQLException {
		try (PreparedStatement jdbcStatement = jdbcConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			parameterMapper.accept(jdbcStatement, object);

			final int rowCount = jdbcStatement.executeUpdate();
			try (ResultSet keyCursor = jdbcStatement.getGeneratedKeys()) {
				final int columnCount = keyCursor.getMetaData().getColumnCount();
				final long[][] generatedValues = new long[rowCount][columnCount];

				for (int rowIndex = 0; keyCursor.next(); ++rowIndex)
					for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex)
						generatedValues[rowIndex][columnIndex] = keyCursor.getLong(columnIndex + 1);

				return generatedValues;
			}
		}
	}


	/**
	 * Instances of this class adapt JDBC result sets into row map iterators.
	 */
	static private class RowMapIterator implements Iterator<Map<String,Object>> {
		private final ResultSet delegate;
		private Boolean hasNext;
		private SQLException problem;


		/**
		 * Initializes a new instance based on the given delegate.
		 * @param delegate the table cursor to delegate to
		 * @throws NullPointerException if the given argument is {@code null}
		 * @throws SQLException if there is an SQL related problem
		 */
		public RowMapIterator (final ResultSet delegate) throws NullPointerException, SQLException {
			this.delegate = delegate;
			this.hasNext = null;
			this.problem = null;
		}


		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext () {
			if (this.hasNext == null) {
				try {
					this.hasNext = this.delegate.next();
				} catch (final SQLException e) {
					this.hasNext = true;
					this.problem = e;
				}
			}

			return this.hasNext;
		}


		/**
		 * {@inheritDoc}
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public Map<String, Object> next () {
			try {
				if (this.hasNext != Boolean.TRUE | this.problem != null) {
					final NoSuchElementException exception = new NoSuchElementException();
					if (this.problem != null) exception.initCause(this.problem);
					throw exception;
				}
			} finally {
				this.hasNext = null;
				this.problem = null;
			}

			try {
				return toRowMap(this.delegate);
			} catch (final SQLException e) {
				final NoSuchElementException exception = new NoSuchElementException();
				exception.initCause(e);
				throw exception;
			}
		}
	}
}