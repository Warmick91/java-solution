package edu.damago.java.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Facade providing JSON parse and stringify (marshal and unmarshal) operations.
 * This class's design is equivalent to JavaScript's JSON singleton.  
 * @author Sascha Baumeister
 */
public class JSON {

	/**
	 * Prevents external instantiation.
	 */
	private JSON () {}


	/**
	 * Returns the JSON representation for the given object.
	 * @param object the object, or {@code null}
	 * @return the JSON representation
	 * @throws IllegalArgumentException if the object, or one of it's constituents, is neither
	 *         {@code null} nor an instance of Boolean, Number, String, Map, List, or an array
	 */
	static public String stringify (final Object object) {
		if (object == null | object instanceof Boolean | object instanceof Number) return Objects.toString(object);
		final StringBuilder builder = new StringBuilder();

		if (object instanceof Object[] | object instanceof List) {
			final Object[] array = object instanceof Object[]
				? (Object[]) object
				: ((List<?>) object).toArray();

			builder.append("[");
			for (int index = 0; index < array.length; ++index) {
				if (index > 0) builder.append(", ");
				builder.append(stringify(array[index]));
			}
			builder.append("]");

			return builder.toString();
		}

		if (object instanceof Map) {
			builder.append("{");

			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) object;
			for (final String key : map.keySet()) {
				builder.append(stringify(key));
				builder.append(": ");
				builder.append(stringify(map.get(key)));
				builder.append(", ");
			}

			if (!map.isEmpty()) builder.delete(builder.length() - 2, builder.length());
			builder.append("}");
			return builder.toString();
		}

		return "'" + object.toString().replace("'", "\\'").replace("\"", "\\\"").replace("\t", "\\t").replace("\n", "\\n") + "'";
	}


	/**
	 * Returns an object parsed from the given JSON representation.
	 * @param json the JSON representation
	 * @return the object, or null
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the argument is not valid JSON
	 * @throws ClassCastException if the result is either not compatible to, or implicitly cast
	 * 			to something different from either Object, Boolean, Number (Long or Double),
	 * 			String, Map<String,Object>, or Object[]
	 */
	@SuppressWarnings("unchecked")
	static public <T> T parse (String json) throws NullPointerException, IllegalArgumentException, ClassCastException {
		if (json.trim().isEmpty()) throw new IllegalArgumentException(json);
		json = json.trim();

		switch (json.charAt(0)) {
			case 'n': case 'u':
				return (T) parseVoid(json);				
			case 't': case 'f':
				return (T) parseBoolean(json);				
			case '"': case '\'':
				return (T) parseString(json);
			case '[':
				return (T) parseArray(json);
			case '{':
				return (T) parseMap(json);
			default:
				return (T) parseNumber(json);
		}
	}


	/**
	 * Converts the given JSON representation into the content of a newly created map.
	 * @param json the JSON representation
	 * @return the map
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON map
	 */
	static private Map<String,Object> parseMap (final String json) throws NullPointerException, IllegalArgumentException {
		if (json.length() < 2 | !json.startsWith("{") | !json.endsWith("}")) throw new IllegalArgumentException(json);

		final Map<String,Object> map = new HashMap<>();
		for (int index = 1, startIndex; index < json.length(); ++index) {
			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;
			if (json.charAt(index) == '}') break;
			if (json.charAt(index) != '"' & json.charAt(index) != '\'') throw new IllegalArgumentException(json);

			startIndex = index;
			index = matchingDelimiterPosition(json, startIndex) + 1;
			final String key = parseString(json.substring(startIndex, index));

			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;
			if (json.charAt(index++) != ':') throw new IllegalArgumentException(json);
			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;

			startIndex = index;
			if (json.charAt(index) == '{' | json.charAt(index) == '[' | json.charAt(index) == '"' | json.charAt(index) == '\'')
				index = matchingDelimiterPosition(json, startIndex) + 1;
			else
				while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != '}') index += 1;

			final Object value = parse(json.substring(startIndex, index).trim());
			map.put(key, value);

			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;
			if (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != '}') throw new IllegalArgumentException(json);
		}

		return map;
	}


	/**
	 * Converts the given JSON representation into the content of a newly created array.
	 * @param json the JSON representation
	 * @return the array
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON array
	 */
	static private Object[] parseArray (final String json) throws NullPointerException, IllegalArgumentException {
		if (json.length() < 2 | !json.startsWith("[") | !json.endsWith("]")) throw new IllegalArgumentException(json);

		final List<Object> list = new ArrayList<>();
		for (int index = 1; index < json.length(); ++index) {
			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;
			if (json.charAt(index) == ']') break;

			final int startIndex = index;
			if (json.charAt(index) == '{' | json.charAt(index) == '[' | json.charAt(index) == '"' | json.charAt(index) == '\'')
				index = matchingDelimiterPosition(json, startIndex) + 1;
			else
				while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != ']') index += 1;

			final Object element = parse(json.substring(startIndex, index).trim());
			list.add(element);

			while (index < json.length() && Character.isWhitespace(json.charAt(index))) index += 1;
			if (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != ']') throw new IllegalArgumentException(json);
		}

		return list.toArray();
	}


	/**
	 * Converts the given JSON representation into the content of a newly created string.
	 * @param json the JSON representation
	 * @return the string
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON string
	 */
	static private String parseString (final String json) throws NullPointerException, IllegalArgumentException {
		if (json.length() < 2 | !((json.startsWith("\"") & json.endsWith("\"")) | (json.startsWith("'") & json.endsWith("'")))) throw new IllegalArgumentException(json);

		return json.substring(1, json.length() - 1).replace("\\'", "'").replace("\\\"", "\"").replace("\\t", "\t").replace("\\n", "\n");
	}


	/**
	 * Converts the given JSON representation into the content of a newly created numeric value.
	 * @param json the JSON representation
	 * @return the numeric value, either of type {@code Long} or {@code Double}
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON number
	 */
	static private Number parseNumber (final String json) throws NullPointerException, IllegalArgumentException {
		try {
			return Long.parseLong(json);
		} catch (final NumberFormatException e) {
			return Double.parseDouble(json);
		}
	}


	/**
	 * Converts the given JSON representation into the content of a newly created boolean value.
	 * @param json the JSON representation
	 * @return the Boolean value
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON boolean
	 */
	static private Boolean parseBoolean (final String json) throws NullPointerException, IllegalArgumentException {
		if (json.equals("true")) return true;
		if (json.equals("false")) return false;
		throw new IllegalArgumentException(json);
	}


	/**
	 * Converts the given JSON representation into {@code null}.
	 * @param json the JSON representation
	 * @return the {@code null} value
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given JSON does not represent a valid JSON void
	 */
	static private Void parseVoid (final String json) throws NullPointerException, IllegalArgumentException {
		if (!json.equals("null") & !json.equals("undefined")) throw new IllegalArgumentException(json);

		return null;
	}


	/**
	 * Returns the position of the stop delimiter matching the start delimiter at the given position.
	 * @param json the JSON representation
	 * @param startPosition the start delimiter position
	 * @return the matching stop delimiter position
	 * @throws NullPointerException if the given JSON argument is {@code null}
	 * @throws IllegalArgumentException if the given start position is out of range,
	 *			or if the given JSON doesn't contain a matching stop delimiter
	 */
	static private int matchingDelimiterPosition (final String json, final int startPosition) throws NullPointerException, IllegalArgumentException {
		if (startPosition < 0 | startPosition >= json.length()) throw new IllegalArgumentException(json);
		final char startDelimiter = json.charAt(startPosition);

		if (startDelimiter == '"' | startDelimiter == '\'') {
			for (int index = startPosition + 1; index < json.length(); ++index) {
				final char character = json.charAt(index);
				if (character == startDelimiter & json.charAt(index - 1) != '\\') return index;
			}
		} else if (startDelimiter == '{' | startDelimiter == '[') {
			for (int index = startPosition + 1; index < json.length(); ++index) {
				final char character = json.charAt(index);
				if ((startDelimiter == '{' & character == '}') | (startDelimiter == '[' & character == ']')) return index;

				if (character == '{' | character == '[' | character == '"' | character == '\'')
					index = matchingDelimiterPosition(json, index);
			}
		}

		throw new IllegalArgumentException(json);
	}
}