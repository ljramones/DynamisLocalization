package org.dynamisengine.localization.api;

import org.dynamisengine.localization.api.value.LocaleDescriptor;

/**
 * Notified when the active locale changes.
 */
@FunctionalInterface
public interface LocaleChangeListener {

    /**
     * Called after the locale switch is complete and new tables are loaded.
     *
     * @param previous the locale before the switch
     * @param current  the locale after the switch
     */
    void onLocaleChanged(LocaleDescriptor previous, LocaleDescriptor current);
}
