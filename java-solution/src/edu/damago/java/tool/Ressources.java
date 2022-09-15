package edu.damago.java.tool;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Facade providing operations related to resources.   
 * @author Sascha Baumeister
 */
public class Ressources {

	/**
	 * Returns a new URL based on the given text.
	 * @param text the text
	 * @return the URL created
	 * @throws NullPointerException if the given text is {@code null}
	 * @throws IllegalArgumentException if the given text does not represent a valid URL
	 */
	static public URL newURL (final String text) throws NullPointerException, IllegalArgumentException {
		try {
			return new URL(text);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
