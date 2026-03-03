package org.dynamislocalization.core;

import org.dynamis.core.exception.DynamisException;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal recursive descent JSON parser for string table files.
 *
 * Supports only the DynamisLocalization string table schema:
 * - Top-level object with metadata fields and a required "keys" object
 * - String values: {@code "key": "value"}
 * - Plural maps: {@code "key": {"one": "...", "other": "..."}}
 *
 * No external dependencies. Not a general-purpose JSON parser.
 * Handles: strings (with escape sequences), nested objects, whitespace.
 * Does not handle: arrays, numbers, booleans, null at value positions.
 */
public final class MinimalJsonParser {

    private final String input;
    private int pos;

    private MinimalJsonParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    private MinimalJsonParser() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses a string table JSON file and returns the entries map.
     * The JSON must have a top-level object containing a "keys" field.
     *
     * @param json raw JSON string
     * @return map of key to String (simple) or key to Map&lt;String,String&gt; (plural)
     */
    public static Map<String, Object> parseStringTable(String json) {
        if (json == null || json.isBlank()) {
            throw new DynamisException("String table JSON must not be null or blank");
        }
        MinimalJsonParser parser = new MinimalJsonParser(json);
        Map<String, Object> root = parser.parseObject();
        Object keysObj = root.get("keys");
        if (keysObj == null) {
            throw new DynamisException("String table JSON missing required 'keys' field");
        }
        if (!(keysObj instanceof Map)) {
            throw new DynamisException("String table JSON 'keys' field must be an object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> keys = (Map<String, Object>) keysObj;
        return keys;
    }

    /** Parses a JSON object and returns a Map of its entries. */
    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        skipWhitespace();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char next = peek();
            if (next == '}') {
                pos++;
                break;
            }
            if (next == ',') {
                pos++;
            } else {
                throw new DynamisException(
                    String.format("Expected ',' or '}' at position %d, got '%c'", pos, next));
            }
        }
        return map;
    }

    /** Parses a JSON value — string or object only (sufficient for string table schema). */
    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        if (c == '"') return parseString();
        if (c == '{') return parseObject();
        throw new DynamisException(
            String.format(
                "Unexpected character '%c' at position %d. " +
                "String table values must be strings or objects.", c, pos));
    }

    /** Parses a JSON string including escape sequence handling. */
    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= input.length()) break;
                char esc = input.charAt(pos++);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > input.length()) {
                            throw new DynamisException("Incomplete unicode escape at position " + pos);
                        }
                        String hex = input.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                    }
                    default -> throw new DynamisException(
                        String.format("Unknown escape sequence '\\%c' at position %d", esc, pos));
                }
            } else {
                sb.append(c);
            }
        }
        throw new DynamisException("Unterminated string starting before position " + pos);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= input.length()) {
            throw new DynamisException("Unexpected end of JSON input at position " + pos);
        }
        return input.charAt(pos);
    }

    private void expect(char expected) {
        char actual = peek();
        if (actual != expected) {
            throw new DynamisException(
                String.format("Expected '%c' at position %d, got '%c'", expected, pos, actual));
        }
        pos++;
    }
}
