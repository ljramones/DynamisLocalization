package org.dynamislocalization.format;

import org.dynamislocalization.api.value.LocaleDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterSubstitutorTest {

    private static ParameterSubstitutor substitutor() {
        return new ParameterSubstitutor(new LocaleAwareFormatter(LocaleDescriptor.EN_US));
    }

    @Test
    void simpleTokenReplaced() {
        String out = substitutor().substitute("Hello, {name}!", Map.of("name", "Alex"));
        assertEquals("Hello, Alex!", out);
    }

    @Test
    void formattedNumberTokenReplaced() {
        String out = substitutor().substitute("Count: {count, number}", Map.of("count", 1234567));
        assertEquals("Count: 1,234,567", out);
    }

    @Test
    void unknownParamRetained() {
        String out = substitutor().substitute("Hello, {missing}!", Map.of("name", "Alex"));
        assertEquals("Hello, {missing}!", out);
    }

    @Test
    void nullTemplateReturnsEmptyString() {
        String out = substitutor().substitute(null, Map.of("name", "Alex"));
        assertEquals("", out);
    }

    @Test
    void emptyParamsReturnsTemplateUnchanged() {
        String out = substitutor().substitute("Hello, {name}!", Map.of());
        assertEquals("Hello, {name}!", out);
    }

    @Test
    void multipleTokensAllReplaced() {
        String out = substitutor().substitute(
            "Hello, {name}. You have {count, number} coins.",
            Map.of("name", "Alex", "count", 12345)
        );
        assertEquals("Hello, Alex. You have 12,345 coins.", out);
    }

    @Test
    void currencyTokenReplaced() {
        String out = substitutor().substitute("Balance: {balance, currency}", Map.of("balance", 1234.50));
        assertEquals("Balance: $1,234.50", out);
    }

    @Test
    void templateWithNoTokensUnchanged() {
        String out = substitutor().substitute("No tokens here.", Map.of("name", "Alex"));
        assertEquals("No tokens here.", out);
    }
}
