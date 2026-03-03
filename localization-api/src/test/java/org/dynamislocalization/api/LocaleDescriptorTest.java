package org.dynamislocalization.api;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleDescriptorTest {

    @Test
    void enUsTagMatches() {
        assertEquals("en-US", LocaleDescriptor.EN_US.bcp47Tag());
    }

    @Test
    void arSaIsRightToLeft() {
        assertTrue(LocaleDescriptor.AR_SA.rightToLeft());
    }

    @Test
    void enUsIsNotRightToLeft() {
        assertFalse(LocaleDescriptor.EN_US.rightToLeft());
    }

    @Test
    void blankTagThrows() {
        assertThrows(DynamisException.class, () -> new LocaleDescriptor(" ", "English", false));
    }

    @Test
    void blankDisplayNameThrows() {
        assertThrows(DynamisException.class, () -> new LocaleDescriptor("en-US", " ", false));
    }
}
