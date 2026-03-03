package org.dynamislocalization.core;

import org.dynamis.core.exception.DynamisException;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.api.value.PluralCategory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable string table backed by a pre-loaded map.
 * Plural forms are stored as nested maps keyed by CLDR category name (lowercase).
 */
public final class DefaultStringTable implements StringTable {

    private static final DynamisLogger log = DynamisLogger.get(DefaultStringTable.class);

    private final LocaleDescriptor locale;
    private final String namespace;
    private final Map<String, Object> entries;

    public DefaultStringTable(LocaleDescriptor locale,
                               String namespace,
                               Map<String, Object> entries) {
        this.locale = locale;
        this.namespace = namespace;
        this.entries = Collections.unmodifiableMap(entries);
    }

    @Override
    public LocaleDescriptor locale() { return locale; }

    @Override
    public String namespace() { return namespace; }

    @Override
    public String get(LocaleKey key, MissingKeyBehavior onMissing) {
        Object value = entries.get(key.key());
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof Map) {
            // Key exists but is a plural form — caller should use getPlural
            log.debug(String.format(
                "Key '%s' is a plural entry; use getPlural(). Falling back to 'other'.", key));
            @SuppressWarnings("unchecked")
            Map<String, String> forms = (Map<String, String>) value;
            String other = forms.get("other");
            if (other != null) return other;
        }
        return applyMissingBehavior(key, onMissing);
    }

    @Override
    public String getPlural(LocaleKey key, long count, MissingKeyBehavior onMissing) {
        Object value = entries.get(key.key());
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> forms = (Map<String, String>) value;
            PluralCategory category = PluralRuleEngine.resolve(locale, count);
            String form = forms.get(category.name().toLowerCase());
            if (form != null) return form;
            // Fallback to OTHER
            String other = forms.get("other");
            if (other != null) return other;
        }
        if (value instanceof String s) {
            return s; // Non-plural key used with getPlural — return it as-is
        }
        return applyMissingBehavior(key, onMissing);
    }

    @Override
    public boolean has(LocaleKey key) {
        return entries.containsKey(key.key());
    }

    @Override
    public Optional<String> getPluralForm(LocaleKey key, PluralCategory category) {
        Object value = entries.get(key.key());
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> forms = (Map<String, String>) value;
            return Optional.ofNullable(forms.get(category.name().toLowerCase()));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Object> rawEntries() {
        return entries;
    }

    private String applyMissingBehavior(LocaleKey key, MissingKeyBehavior behavior) {
        return switch (behavior) {
            case RETURN_KEY -> key.fullyQualified();
            case RETURN_EMPTY -> "";
            case THROW -> throw new DynamisException(
                String.format("Missing localization key '%s' in locale '%s'",
                    key.fullyQualified(), locale.bcp47Tag()));
        };
    }
}
