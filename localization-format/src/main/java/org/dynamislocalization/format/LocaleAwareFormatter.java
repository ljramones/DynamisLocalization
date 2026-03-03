package org.dynamislocalization.format;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleDescriptor;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Locale;

/**
 * Formats values according to a locale's conventions.
 *
 * Supported format types (used in {param, type} substitution tokens):
 * - "number"   - locale-aware integer/decimal formatting
 * - "currency" - locale-aware currency formatting
 * - "date"     - locale-aware short date formatting
 * - "datetime" - locale-aware short date+time formatting
 * - "percent"  - locale-aware percentage formatting
 * - "filesize" - human-readable file size (1.2 MB) - not locale-sensitive, always English units
 */
public final class LocaleAwareFormatter {

    private final Locale locale;

    public LocaleAwareFormatter(LocaleDescriptor descriptor) {
        this.locale = Locale.forLanguageTag(descriptor.bcp47Tag());
    }

    /**
     * Formats a numeric value as a locale-aware number string.
     */
    public String formatNumber(Number value) {
        return NumberFormat.getNumberInstance(locale).format(value);
    }

    /**
     * Formats a numeric value as a locale-aware currency string.
     * Uses the locale's default currency.
     */
    public String formatCurrency(Number value) {
        return NumberFormat.getCurrencyInstance(locale).format(value);
    }

    /**
     * Formats a numeric value as a locale-aware percentage string.
     * Input: 0.0 to 1.0 (e.g. 0.75 -> "75%")
     */
    public String formatPercent(Number value) {
        return NumberFormat.getPercentInstance(locale).format(value);
    }

    /**
     * Formats a LocalDate as a locale-aware short date string.
     */
    public String formatDate(LocalDate date) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(locale)
            .format(date);
    }

    /**
     * Formats a LocalDateTime as a locale-aware short date+time string.
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(locale)
            .format(dateTime);
    }

    /**
     * Formats a byte count as a human-readable file size string.
     * Not locale-sensitive - uses SI units (KB, MB, GB).
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    /**
     * Formats a value by format type name.
     * Used by ParameterSubstitutor to resolve {param, type} tokens.
     */
    public String format(Object value, String formatType) {
        return switch (formatType.toLowerCase()) {
            case "number" -> {
                if (value instanceof Number n) yield formatNumber(n);
                yield String.valueOf(value);
            }
            case "currency" -> {
                if (value instanceof Number n) yield formatCurrency(n);
                yield String.valueOf(value);
            }
            case "percent" -> {
                if (value instanceof Number n) yield formatPercent(n);
                yield String.valueOf(value);
            }
            case "date" -> {
                if (value instanceof LocalDate d) yield formatDate(d);
                yield String.valueOf(value);
            }
            case "datetime" -> {
                if (value instanceof LocalDateTime dt) yield formatDateTime(dt);
                yield String.valueOf(value);
            }
            case "filesize" -> {
                if (value instanceof Number n) yield formatFileSize(n.longValue());
                yield String.valueOf(value);
            }
            default -> throw new DynamisException(
                String.format("Unknown format type '%s'. " +
                    "Supported: number, currency, percent, date, datetime, filesize",
                    formatType));
        };
    }

    /** Returns the underlying Java Locale. */
    public Locale javaLocale() {
        return locale;
    }
}
