package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;

/**
 * Value object representing a parsed and validated condition expression.
 * 
 * This class wraps a SpEL expression and provides metadata about its complexity and the properties
 * it references for validation and optimization purposes.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConditionExpression {

    /**
     * The expression string
     */
    private String expression;

    /**
     * Complexity score for performance monitoring
     */
    private Integer complexityScore;

    /**
     * Properties referenced in expression
     */
    private Set<String> referencedProperties;

    /**
     * List of logical operators found in expression
     */
    private Set<String> logicalOperators;

    /**
     * List of comparison operators found in expression
     */
    private Set<String> comparisonOperators;

    /**
     * Maximum nesting depth of parentheses
     */
    private Integer maxNestingDepth;

    /**
     * Whether expression contains complex operations (ternary, regex, etc.)
     */
    private Boolean hasComplexOperations;

    /**
     * Creates a ConditionExpression from a raw expression string
     * 
     * @param expression The raw expression string
     * @return ConditionExpression with parsed expression and metadata
     */
    public static ConditionExpression fromString(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        int complexityScore = calculateComplexity(expression);
        Set<String> referencedProperties = extractReferencedProperties(expression);
        Set<String> logicalOperators = extractLogicalOperators(expression);
        Set<String> comparisonOperators = extractComparisonOperators(expression);
        Integer maxNestingDepth = calculateMaxNestingDepth(expression);
        Boolean hasComplexOperations = detectComplexOperations(expression);

        return ConditionExpression.builder().expression(expression).complexityScore(complexityScore)
                .referencedProperties(referencedProperties).logicalOperators(logicalOperators)
                .comparisonOperators(comparisonOperators).maxNestingDepth(maxNestingDepth)
                .hasComplexOperations(hasComplexOperations).build();
    }

    /**
     * Calculates complexity score based on expression characteristics
     */
    private static int calculateComplexity(String expression) {
        int score = 0;

        // Count logical operators (check longer operators first to avoid conflicts)
        int andCount = countOccurrences(expression, "AND");
        int orCount = countOccurrences(expression, "OR");
        int notCount = countOccurrences(expression, "NOT");
        score += andCount * 2 + orCount * 2 + notCount * 1;

        // Count comparison operators (check longer operators first)
        int notEqualsCount = countOccurrences(expression, "!=");
        int greaterEqualCount = countOccurrences(expression, ">=");
        int lessEqualCount = countOccurrences(expression, "<=");

        // Count == and > and < (check after removing longer operators)
        String tempExpr = expression.replace("!=", "").replace(">=", "").replace("<=", "");
        int equalsCount = countOccurrences(tempExpr, "==");
        int greaterCount = countOccurrences(tempExpr, ">");
        int lessCount = countOccurrences(tempExpr, "<");

        score += notEqualsCount + greaterEqualCount + lessEqualCount + equalsCount + greaterCount
                + lessCount;

        // Count nested parentheses
        int openParens = countOccurrences(expression, "(");
        int closeParens = countOccurrences(expression, ")");
        score += (openParens + closeParens) * 1;

        return Math.max(score, 1); // Minimum score of 1
    }

    private static int countOccurrences(String str, String substr) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substr, index)) != -1) {
            count++;
            index += substr.length();
        }
        return count;
    }

    /**
     * Extracts property references from expression
     */
    private static Set<String> extractReferencedProperties(String expression) {
        Set<String> properties = new HashSet<>();

        // Simple regex to find context.property references
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("context\\.(\\w+)\\.(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String category = matcher.group(1);
            String property = matcher.group(2);
            properties.add("context." + category + "." + property);
        }

        return properties;
    }

    /**
     * Extracts logical operators from expression
     */
    private static Set<String> extractLogicalOperators(String expression) {
        Set<String> operators = new HashSet<>();

        if (expression.contains("AND"))
            operators.add("AND");
        if (expression.contains("OR"))
            operators.add("OR");
        if (expression.contains("NOT"))
            operators.add("NOT");

        return operators;
    }

    /**
     * Extracts comparison operators from expression
     */
    private static Set<String> extractComparisonOperators(String expression) {
        Set<String> operators = new HashSet<>();

        if (expression.contains("=="))
            operators.add("==");
        if (expression.contains("!="))
            operators.add("!=");
        if (expression.contains(">="))
            operators.add(">=");
        if (expression.contains("<="))
            operators.add("<=");
        if (expression.contains(">"))
            operators.add(">");
        if (expression.contains("<"))
            operators.add("<");

        return operators;
    }

    /**
     * Calculates maximum nesting depth of parentheses
     */
    private static Integer calculateMaxNestingDepth(String expression) {
        int maxDepth = 0;
        int currentDepth = 0;

        for (char c : expression.toCharArray()) {
            if (c == '(') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == ')') {
                currentDepth--;
            }
        }

        return maxDepth;
    }

    /**
     * Detects complex operations in expression
     */
    private static Boolean detectComplexOperations(String expression) {
        // Check for ternary operators
        if (expression.contains("?") && expression.contains(":")) {
            return true;
        }

        // Check for regex matching
        if (expression.contains("matches")) {
            return true;
        }

        // Check for collection operations
        if (expression.contains(".?[") || expression.contains(".^[")) {
            return true;
        }

        // Check for null safety operators
        if (expression.contains("?.") || expression.contains("?:")) {
            return true;
        }

        // Check for method calls on variables
        if (expression.matches(".*\\w+\\..*\\(.*\\).*")) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "ConditionExpression{" + "expression='" + expression + '\'' + ", complexityScore="
                + complexityScore + ", referencedProperties=" + referencedProperties + '}';
    }
}
