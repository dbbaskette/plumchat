package com.baskettecase.mcpserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ToolsServiceTest {

    private ToolsService toolsService;

    @BeforeEach
    void setUp() {
        toolsService = new ToolsService();
    }

    @DisplayName("Text Capitalization Tests")
    @Test
    void testCapitalizeText_normalText() {
        String result = toolsService.capitalizeText("hello world");
        assertEquals("Hello World", result);
    }

    @Test
    void testCapitalizeText_singleWord() {
        String result = toolsService.capitalizeText("hello");
        assertEquals("Hello", result);
    }

    @Test
    void testCapitalizeText_multipleSpaces() {
        String result = toolsService.capitalizeText("hello    world   test");
        assertEquals("Hello World Test", result);
    }

    @Test
    void testCapitalizeText_mixedCase() {
        String result = toolsService.capitalizeText("hELLo WoRLD");
        assertEquals("Hello World", result);
    }

    @Test
    void testCapitalizeText_emptyString() {
        String result = toolsService.capitalizeText("");
        assertEquals("", result);
    }

    @Test
    void testCapitalizeText_nullInput() {
        String result = toolsService.capitalizeText(null);
        assertNull(result);
    }

    @Test
    void testCapitalizeText_whitespaceOnly() {
        String result = toolsService.capitalizeText("   ");
        assertEquals("   ", result);
    }

    @DisplayName("Calculator Tests - Basic Operations")
    @ParameterizedTest
    @CsvSource({
        "5.0, 3.0, '+', 8.0",
        "5.0, 3.0, '-', 2.0", 
        "5.0, 3.0, '*', 15.0",
        "15.0, 3.0, '/', 5.0",
        "17.0, 5.0, '%', 2.0",
        "2.0, 3.0, '^', 8.0"
    })
    void testCalculate_basicOperations(double num1, double num2, String operator, double expected) {
        double result = toolsService.calculate(num1, num2, operator);
        assertEquals(expected, result, 0.0001);
    }

    @Test
    void testCalculate_divisionByZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolsService.calculate(10.0, 0.0, "/")
        );
        assertEquals("Division by zero is not allowed", exception.getMessage());
    }

    @Test
    void testCalculate_moduloByZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolsService.calculate(10.0, 0.0, "%")
        );
        assertEquals("Modulo by zero is not allowed", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"@", "#", "sin", "cos", "log", "√"})
    void testCalculate_unsupportedOperator(String operator) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolsService.calculate(5.0, 3.0, operator)
        );
        assertTrue(exception.getMessage().contains("Unsupported operator"));
    }

    @Test
    void testCalculate_nullOperator() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolsService.calculate(5.0, 3.0, null)
        );
        assertEquals("Operator cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCalculate_emptyOperator() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolsService.calculate(5.0, 3.0, "")
        );
        assertEquals("Operator cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCalculate_operatorWithSpaces() {
        double result = toolsService.calculate(5.0, 3.0, " + ");
        assertEquals(8.0, result, 0.0001);
    }

    @DisplayName("Calculator Tests - Edge Cases")
    @Test
    void testCalculate_negativeNumbers() {
        assertEquals(-8.0, toolsService.calculate(-5.0, -3.0, "+"), 0.0001);
        assertEquals(-2.0, toolsService.calculate(-5.0, -3.0, "-"), 0.0001);
        assertEquals(15.0, toolsService.calculate(-5.0, -3.0, "*"), 0.0001);
    }

    @Test
    void testCalculate_decimals() {
        assertEquals(8.7, toolsService.calculate(5.5, 3.2, "+"), 0.0001);
        assertEquals(17.6, toolsService.calculate(5.5, 3.2, "*"), 0.0001);
    }

    @Test
    void testCalculate_powerOfZero() {
        assertEquals(1.0, toolsService.calculate(5.0, 0.0, "^"), 0.0001);
        assertEquals(0.0, toolsService.calculate(0.0, 5.0, "^"), 0.0001);
    }
}