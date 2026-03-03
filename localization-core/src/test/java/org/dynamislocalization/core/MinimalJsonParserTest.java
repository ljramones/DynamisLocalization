package org.dynamislocalization.core;

import org.dynamis.core.exception.DynamisException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimalJsonParserTest {

    @Test
    void parsesStringValues() {
        String json = """
            {
              \"keys\": {
                \"button.confirm\": \"Confirm\"
              }
            }
            """;

        Map<String, Object> keys = MinimalJsonParser.parseStringTable(json);
        assertEquals("Confirm", keys.get("button.confirm"));
    }

    @Test
    void parsesPluralMapValues() {
        String json = """
            {
              \"keys\": {
                \"items.count\": {
                  \"one\": \"{count} item\",
                  \"other\": \"{count} items\"
                }
              }
            }
            """;

        Map<String, Object> keys = MinimalJsonParser.parseStringTable(json);
        Object plural = keys.get("items.count");
        assertInstanceOf(Map.class, plural);
        @SuppressWarnings("unchecked")
        Map<String, String> forms = (Map<String, String>) plural;
        assertEquals("{count} item", forms.get("one"));
        assertEquals("{count} items", forms.get("other"));
    }

    @Test
    void missingKeysFieldThrows() {
        String json = "{\"locale\":\"en-US\"}";
        assertThrows(DynamisException.class, () -> MinimalJsonParser.parseStringTable(json));
    }

    @Test
    void nullInputThrows() {
        assertThrows(DynamisException.class, () -> MinimalJsonParser.parseStringTable(null));
    }

    @Test
    void blankInputThrows() {
        assertThrows(DynamisException.class, () -> MinimalJsonParser.parseStringTable("   "));
    }

    @Test
    void unicodeEscapeParses() {
        String json = """
            {
              \"keys\": {
                \"letter\": \"\\u0041\"
              }
            }
            """;

        Map<String, Object> keys = MinimalJsonParser.parseStringTable(json);
        assertEquals("A", keys.get("letter"));
    }

    @Test
    void nestedObjectReturnsMap() {
        String json = """
            {
              \"keys\": {
                \"items.count\": {
                  \"one\": \"one\",
                  \"other\": \"other\"
                }
              }
            }
            """;

        Map<String, Object> keys = MinimalJsonParser.parseStringTable(json);
        assertInstanceOf(Map.class, keys.get("items.count"));
    }
}
