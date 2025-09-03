package com.baskettecase.mcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

	/**
	 * Capitalize the first letter of each word in the input text
	 * @param text Input text to capitalize
	 * @return Text with first letter of each word capitalized
	 */
	@Tool(description = "Capitalize the first letter of each word in the input text")
	public String capitalizeText(String text) {
		if (text == null || text.trim().isEmpty()) {
			return text;
		}
		
		String[] words = text.trim().split("\\s+");
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < words.length; i++) {
			if (i > 0) {
				result.append(" ");
			}
			
			String word = words[i];
			if (word.length() > 0) {
				result.append(Character.toUpperCase(word.charAt(0)));
				if (word.length() > 1) {
					result.append(word.substring(1).toLowerCase());
				}
			}
		}
		
		return result.toString();
	}

	/**
	 * Perform basic mathematical operations on two numbers
	 * @param number1 First number
	 * @param number2 Second number  
	 * @param operator Mathematical operator (+, -, *, /, %, ^)
	 * @return Result of the mathematical operation
	 * @throws IllegalArgumentException if operator is not supported or division by zero
	 */
	@Tool(description = "Perform basic mathematical operations. Supported operators: +, -, *, /, %, ^ (power)")
	public double calculate(double number1, double number2, String operator) {
		if (operator == null || operator.trim().isEmpty()) {
			throw new IllegalArgumentException("Operator cannot be null or empty");
		}
		
		String op = operator.trim();
		
		return switch (op) {
			case "+" -> number1 + number2;
			case "-" -> number1 - number2;
			case "*" -> number1 * number2;
			case "/" -> {
				if (number2 == 0) {
					throw new IllegalArgumentException("Division by zero is not allowed");
				}
				yield number1 / number2;
			}
			case "%" -> {
				if (number2 == 0) {
					throw new IllegalArgumentException("Modulo by zero is not allowed");
				}
				yield number1 % number2;
			}
			case "^" -> Math.pow(number1, number2);
			default -> throw new IllegalArgumentException("Unsupported operator: " + op + 
				". Supported operators are: +, -, *, /, %, ^");
		};
	}
}