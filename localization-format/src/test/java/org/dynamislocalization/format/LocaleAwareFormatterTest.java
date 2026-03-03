package org.dynamislocalization.format;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocaleAwareFormatterTest {

    @Test
    void formatNumberEnUs() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("1,234,567", formatter.formatNumber(1234567));
    }

    @Test
    void formatNumberDeDe() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.DE_DE);
        assertEquals("1.234.567", formatter.formatNumber(1234567));
    }

    @Test
    void formatPercentEnUs() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("75%", formatter.formatPercent(0.75));
    }

    @Test
    void formatFileSizeKilobytes() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("1.0 KB", formatter.formatFileSize(1024));
    }

    @Test
    void formatFileSizeMegabytes() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("1.0 MB", formatter.formatFileSize(1048576));
    }

    @Test
    void formatFileSizeBytes() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("512 B", formatter.formatFileSize(512));
    }

    @Test
    void genericFormatNumber() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertEquals("42", formatter.format(42, "number"));
    }

    @Test
    void unknownFormatTypeThrows() {
        LocaleAwareFormatter formatter = new LocaleAwareFormatter(LocaleDescriptor.EN_US);
        assertThrows(DynamisException.class, () -> formatter.format(42, "unknown"));
    }
}
