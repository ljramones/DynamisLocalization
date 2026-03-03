package org.dynamislocalization.api;

import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.api.value.PluralCategory;

import java.util.Map;
import java.util.Optional;

/**
 * Read-only view of translated strings for a single locale and namespace.
 */
public interface StringTable {

    /** The locale this table serves. */
    LocaleDescriptor locale();

    /** The namespace this table belongs to. */
    String namespace();

    /**
     * Returns the translated string for the given key.
     * Applies {@link MissingKeyBehavior} if the key is absent.
     */
    String get(LocaleKey key, MissingKeyBehavior onMissing);

    /**
     * Returns the translated string for the given key using RETURN_KEY on missing.
     */
    default String get(LocaleKey key) {
        return get(key, MissingKeyBehavior.RETURN_KEY);
    }

    /**
     * Returns the plural form for a count, using CLDR plural category resolution.
     */
    String getPlural(LocaleKey key, long count, MissingKeyBehavior onMissing);

    default String getPlural(LocaleKey key, long count) {
        return getPlural(key, count, MissingKeyBehavior.RETURN_KEY);
    }

    /** Returns true if this table contains the given key. */
    boolean has(LocaleKey key);

    /** Returns the plural string for the given category directly, if present. */
    Optional<String> getPluralForm(LocaleKey key, PluralCategory category);

    /** Returns all keys in this table. */
    Map<String, Object> rawEntries();
}
