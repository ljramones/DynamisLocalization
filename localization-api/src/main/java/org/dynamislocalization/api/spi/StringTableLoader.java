package org.dynamislocalization.api.spi;

import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.value.LocaleDescriptor;

import java.util.Optional;

/**
 * SPI for loading string tables from any source (classpath, filesystem, network).
 * Implement and register to support custom loading strategies or mod packs.
 */
public interface StringTableLoader {

    /**
     * Attempts to load the string table for the given locale and namespace.
     * Returns empty if this loader cannot serve the request.
     *
     * @param locale    the target locale
     * @param namespace the namespace to load (e.g. "dynamis.ui")
     * @return the loaded table, or empty if not available from this source
     */
    Optional<StringTable> load(LocaleDescriptor locale, String namespace);

    /**
     * Returns true if this loader can potentially serve the given namespace.
     * Used by NamespaceRegistry to short-circuit loader chains.
     */
    boolean supports(String namespace);
}
