package org.dynamislocalization.api;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocaleKeyTest {

    @Test
    void parsesFullyQualifiedKey() {
        LocaleKey key = LocaleKey.of("dynamis.ui:button.confirm");
        assertEquals("dynamis.ui", key.namespace());
        assertEquals("button.confirm", key.key());
    }

    @Test
    void fullyQualifiedRoundTrips() {
        String fullyQualified = LocaleKey.of("dynamis.ui:button.confirm").fullyQualified();
        assertEquals("dynamis.ui:button.confirm", fullyQualified);
    }

    @Test
    void nullInputThrows() {
        assertThrows(DynamisException.class, () -> LocaleKey.of(null));
    }

    @Test
    void missingSeparatorThrows() {
        assertThrows(DynamisException.class, () -> LocaleKey.of("nocorectseparator"));
    }

    @Test
    void blankNamespaceThrows() {
        assertThrows(DynamisException.class, () -> LocaleKey.of(":key"));
    }

    @Test
    void blankKeyThrows() {
        assertThrows(DynamisException.class, () -> LocaleKey.of("ns:"));
    }

    @Test
    void sameFieldsAreEqual() {
        LocaleKey a = new LocaleKey("dynamis.ui", "button.confirm");
        LocaleKey b = new LocaleKey("dynamis.ui", "button.confirm");
        assertEquals(a, b);
    }
}
