package org.dynamislocalization.api.value;

import org.dynamis.core.exception.DynamisException;

/**
 * Fully qualified key into a string table.
 * Format: {@code namespace:key} e.g. {@code "dynamis.ui:button.confirm"}.
 */
public record LocaleKey(String namespace, String key) {

    public LocaleKey {
        if (namespace == null || namespace.isBlank()) {
            throw new DynamisException("LocaleKey namespace must not be null or blank");
        }
        if (key == null || key.isBlank()) {
            throw new DynamisException("LocaleKey key must not be null or blank");
        }
    }

    /**
     * Parses a fully qualified key string of the form {@code "namespace:key"}.
     */
    public static LocaleKey of(String fullyQualified) {
        if (fullyQualified == null || fullyQualified.isBlank()) {
            throw new DynamisException("LocaleKey fullyQualified must not be null or blank");
        }
        int sep = fullyQualified.indexOf(':');
        if (sep <= 0 || sep == fullyQualified.length() - 1) {
            throw new DynamisException(
                "LocaleKey must be of the form 'namespace:key', got: " + fullyQualified);
        }
        return new LocaleKey(fullyQualified.substring(0, sep), fullyQualified.substring(sep + 1));
    }

    /** Returns the fully qualified form {@code "namespace:key"}. */
    public String fullyQualified() {
        return namespace + ":" + key;
    }

    @Override
    public String toString() {
        return fullyQualified();
    }
}
