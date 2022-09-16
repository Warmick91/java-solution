package edu.damago.java.calc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.DoubleBinaryOperator;
import edu.damago.java.tool.BinaryOperators;


/**
 * Facade for the interactive calculator text application,
 * based on object-oriented programming techniques and exception handling.
 * @author Sascha Baumeister
 */
public class Calculator9App {

	/**
	 * Application entry point.
	 * @param args the runtime arguments
	 * @throws IOException 
	 */
	static public void main (final String[] args) throws IOException {
		final BufferedReader terminalSource = new BufferedReader(new InputStreamReader(System.in));
		BinaryOperators.POOL.put("<<", (l, r) -> (long) l << (int) r);	// l * 2^r
		BinaryOperators.POOL.put(">>", (l, r) -> (long) l >> (int) r);	// l / 2^r

		while (true) {
			System.out.print("> ");
			final String line = terminalSource.readLine().trim();
			final int delimiterPosition = line.indexOf(' ');
			final String command = (delimiterPosition == -1 ? line : line.substring(0, delimiterPosition)).trim();
			final String arguments = (delimiterPosition == -1 ? "" : line.substring(delimiterPosition + 1)).trim();

			try {
				switch (command.toLowerCase()) {
					case "calc":
						processCalcCommand(arguments);
						break;
					case "quit":
						System.out.println("Bye!");
						return;
					default:
						processHelpCommand(arguments);
						break;
				}
			} catch (final Exception e) {
				String message = e.getClass().getSimpleName();
				if (e.getMessage() != null) message += ": " + e.getMessage();
				System.err.println(message);

				// outlook: using the logging API to log the exceptions:
				// Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
			}
		}
	}


	static public void processHelpCommand (final String arguments) {
		System.out.println("Available commands:");
		System.out.println("- help: displays this help");
		System.out.println("- calc <operand> <operator> <operand>: calculates the given expression and displays the result");
		System.out.println("- quit: terminates this program\n");
		System.out.println("Supported operators: +, -, *, /, %, **, root, log, <<, >>");
	}

	
	static public void processCalcCommand (final String arguments) {
		final String[] parameters = arguments.split("\\s+");
		if (parameters.length != 3) throw new IllegalArgumentException("expecting exactly three command arguments: <left operator> <operator symbol> <right operator>!");

		// input:    parse/copy runtime arguments into local variables
		final String operatorSymbol = parameters[1].trim();
		double leftOperator, rightOperator;
		try {
			leftOperator = Double.parseDouble(parameters[0].trim());
		} catch (final NumberFormatException e) {
			leftOperator = Double.NaN;
		}
		try {
			rightOperator = Double.parseDouble(parameters[2].trim());
		} catch (final NumberFormatException e) {
			rightOperator = Double.NaN;
		}

		// process:  assign a new variable with the result of calling the calculate operation
		final DoubleBinaryOperator operator = BinaryOperators.POOL.get(operatorSymbol);
		if (operator == null) throw new IllegalArgumentException("operator symbol not supported!");
		final double result = operator.applyAsDouble(leftOperator, rightOperator);

		// output:   print the calculation and it's result to sysout
		System.out.format("%s %s %s = %s%n", leftOperator, operatorSymbol, rightOperator, result);
	}
}