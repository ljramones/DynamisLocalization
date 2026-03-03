package org.dynamislocalization.core;

import org.dynamis.core.logging.DynamisLogger;
import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.spi.StringTableLoader;
import org.dynamislocalization.api.value.LocaleDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loads string tables from JSON files.
 *
 * File naming convention: {@code <namespace>.<bcp47tag>.json}
 * Example: {@code dynamis.ui.en-US.json}
 *
 * Searches the configured base path. Classpath loading is also supported
 * by passing a classpath resource root as the base path.
 *
 * JSON parsing uses a minimal hand-written parser to avoid external
 * dependencies. Only supports flat string values and one-level-deep
 * plural maps (CLDR category name -> string).
 */
public final class JsonStringTableLoader implements StringTableLoader {

    private static final DynamisLogger log = DynamisLogger.get(JsonStringTableLoader.class);

    private final Path basePath;
    private final Set<String> supportedNamespaces;

    /**
     * @param basePath            directory to search for JSON files
     * @param supportedNamespaces namespaces this loader can serve; empty set means all
     */
    public JsonStringTableLoader(Path basePath, Set<String> supportedNamespaces) {
        this.basePath = basePath;
        this.supportedNamespaces = Set.copyOf(supportedNamespaces);
    }

    /** Convenience constructor that supports all namespaces. */
    public JsonStringTableLoader(Path basePath) {
        this(basePath, Set.of());
    }

    @Override
    public boolean supports(String namespace) {
        return supportedNamespaces.isEmpty() || supportedNamespaces.contains(namespace);
    }

    @Override
    public Optional<StringTable> load(LocaleDescriptor locale, String namespace) {
        String filename = namespace + "." + locale.bcp47Tag() + ".json";
        Path file = basePath.resolve(filename);

        if (!Files.exists(file)) {
            log.debug(String.format("String table not found: %s", file));
            return Optional.empty();
        }

        try {
            String json = Files.readString(file);
            Map<String, Object> entries = MinimalJsonParser.parseStringTable(json);
            log.debug(String.format(
                "Loaded string table: %s (%d keys)", filename, entries.size()));
            return Optional.of(new DefaultStringTable(locale, namespace, entries));
        } catch (IOException e) {
            log.warn(String.format("Failed to read string table file: %s — %s",
                file, e.getMessage()));
            return Optional.empty();
        }
    }
}
