package org.dynamislocalization.api;

import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;

import java.util.List;
import java.util.Optional;

/**
 * Primary API for all DynamisLocalization consumers.
 *
 * <p>Usage:
 * <pre>
 *   String text = service.get(LocaleKey.of("dynamis.ui:button.confirm"));
 *   String count = service.getPlural(LocaleKey.of("dynamis.ui:items.count"), 3);
 * </pre>
 */
public interface LocalizationService {

    /**
     * Returns the current active locale.
     */
    LocaleDescriptor currentLocale();

    /**
     * Switches the active locale. Fires a locale change event to all registered listeners.
     * Thread-safe.
     */
    void switchLocale(LocaleDescriptor locale);

    /**
     * Returns the translated string for the given key in the current locale.
     */
    String get(LocaleKey key);

    String get(LocaleKey key, MissingKeyBehavior onMissing);

    /**
     * Returns the plural form for the given count in the current locale.
     */
    String getPlural(LocaleKey key, long count);

    String getPlural(LocaleKey key, long count, MissingKeyBehavior onMissing);

    /**
     * Returns the StringTable for a given namespace in the current locale.
     * Empty if the namespace is not loaded.
     */
    Optional<StringTable> tableFor(String namespace);

    /**
     * Returns all supported locales registered with this service.
     */
    List<LocaleDescriptor> supportedLocales();

    /**
     * Registers a listener to be notified on locale switches.
     */
    void addLocaleChangeListener(LocaleChangeListener listener);

    void removeLocaleChangeListener(LocaleChangeListener listener);
}
