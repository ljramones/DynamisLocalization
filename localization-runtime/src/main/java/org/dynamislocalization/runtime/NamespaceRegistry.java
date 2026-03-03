package org.dynamislocalization.runtime;

import org.dynamis.core.exception.DynamisException;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.spi.StringTableLoader;
import org.dynamislocalization.api.value.LocaleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks registered namespaces and their loaders.
 *
 * Namespaces are registered by game code and mod packs.
 * Loaders are tried in registration order - first match wins.
 *
 * Thread-safe: namespace registration may happen at any time
 * (mod packs register during scene load).
 */
public final class NamespaceRegistry {

    private static final DynamisLogger log = DynamisLogger.get(NamespaceRegistry.class);

    private final CopyOnWriteArrayList<StringTableLoader> loaders = new CopyOnWriteArrayList<>();
    private final Set<String> registeredNamespaces = ConcurrentHashMap.newKeySet();

    /**
     * Registers a loader. Loaders are tried in registration order.
     */
    public void registerLoader(StringTableLoader loader) {
        if (loader == null) throw new DynamisException("loader must not be null");
        loaders.add(loader);
        log.debug(String.format("Registered StringTableLoader: %s",
            loader.getClass().getSimpleName()));
    }

    /**
     * Declares a namespace as known to the registry.
     * A namespace must be registered before tables can be loaded for it.
     */
    public void registerNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            throw new DynamisException("namespace must not be null or blank");
        }
        registeredNamespaces.add(namespace);
        log.debug(String.format("Registered namespace: %s", namespace));
    }

    /**
     * Attempts to load a string table for the given locale and namespace.
     * Tries loaders in registration order. Returns empty if no loader can serve it.
     */
    public Optional<StringTable> load(LocaleDescriptor locale, String namespace) {
        for (StringTableLoader loader : loaders) {
            if (!loader.supports(namespace)) continue;
            Optional<StringTable> table = loader.load(locale, namespace);
            if (table.isPresent()) {
                log.debug(String.format("Loaded table for %s/%s via %s",
                    namespace, locale.bcp47Tag(), loader.getClass().getSimpleName()));
                return table;
            }
        }
        return Optional.empty();
    }

    /** Returns all registered namespace names. */
    public Set<String> namespaces() {
        return Collections.unmodifiableSet(registeredNamespaces);
    }

    /** Returns all registered loaders (unmodifiable view). */
    public List<StringTableLoader> loaders() {
        return Collections.unmodifiableList(loaders);
    }
}
