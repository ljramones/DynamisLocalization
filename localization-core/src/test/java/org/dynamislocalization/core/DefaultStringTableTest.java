package org.dynamislocalization.core;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.api.value.PluralCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultStringTableTest {

    private static DefaultStringTable table() {
        Map<String, Object> entries = Map.of(
            "button.confirm", "Confirm",
            "items.count", Map.of("one", "{count} item", "other", "{count} items")
        );
        return new DefaultStringTable(LocaleDescriptor.EN_US, "dynamis.ui", entries);
    }

    @Test
    void getReturnsKnownString() {
        String value = table().get(new LocaleKey("dynamis.ui", "button.confirm"));
        assertEquals("Confirm", value);
    }

    @Test
    void missingReturnKeyReturnsFullyQualified() {
        String value = table().get(new LocaleKey("dynamis.ui", "missing"), MissingKeyBehavior.RETURN_KEY);
        assertEquals("dynamis.ui:missing", value);
    }

    @Test
    void missingReturnEmptyReturnsEmptyString() {
        String value = table().get(new LocaleKey("dynamis.ui", "missing"), MissingKeyBehavior.RETURN_EMPTY);
        assertEquals("", value);
    }

    @Test
    void missingThrowThrows() {
        assertThrows(
            DynamisException.class,
            () -> table().get(new LocaleKey("dynamis.ui", "missing"), MissingKeyBehavior.THROW)
        );
    }

    @Test
    void pluralOneReturnsOneForm() {
        String value = table().getPlural(new LocaleKey("dynamis.ui", "items.count"), 1);
        assertEquals("{count} item", value);
    }

    @Test
    void pluralFiveReturnsOtherForm() {
        String value = table().getPlural(new LocaleKey("dynamis.ui", "items.count"), 5);
        assertEquals("{count} items", value);
    }

    @Test
    void hasReportsPresence() {
        DefaultStringTable table = table();
        assertTrue(table.has(new LocaleKey("dynamis.ui", "button.confirm")));
        assertFalse(table.has(new LocaleKey("dynamis.ui", "does.not.exist")));
    }

    @Test
    void getPluralFormReturnsOptionalOne() {
        Optional<String> form = table().getPluralForm(
            new LocaleKey("dynamis.ui", "items.count"),
            PluralCategory.ONE
        );
        assertTrue(form.isPresent());
        assertEquals("{count} item", form.orElseThrow());
    }
}
