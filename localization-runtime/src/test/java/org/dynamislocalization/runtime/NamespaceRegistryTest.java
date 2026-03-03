package org.dynamislocalization.runtime;

import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.spi.StringTableLoader;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.api.value.PluralCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamespaceRegistryTest {

    @Test
    void registeredNamespaceAppears() {
        NamespaceRegistry registry = new NamespaceRegistry();
        registry.registerNamespace("dynamis.ui");
        assertTrue(registry.namespaces().contains("dynamis.ui"));
    }

    @Test
    void loaderRegisteredAndReturned() {
        NamespaceRegistry registry = new NamespaceRegistry();
        StringTableLoader loader = new EmptyLoader();
        registry.registerLoader(loader);
        assertEquals(1, registry.loaders().size());
        assertEquals(loader, registry.loaders().getFirst());
    }

    @Test
    void nullLoaderThrows() {
        NamespaceRegistry registry = new NamespaceRegistry();
        assertThrows(DynamisException.class, () -> registry.registerLoader(null));
    }

    @Test
    void blankNamespaceThrows() {
        NamespaceRegistry registry = new NamespaceRegistry();
        assertThrows(DynamisException.class, () -> registry.registerNamespace(" "));
    }

    @Test
    void loadReturnsEmptyWhenNoLoaderSupportsNamespace() {
        NamespaceRegistry registry = new NamespaceRegistry();
        registry.registerNamespace("dynamis.ui");
        registry.registerLoader(new UnsupportedLoader());

        Optional<StringTable> table = registry.load(LocaleDescriptor.EN_US, "dynamis.ui");
        assertTrue(table.isEmpty());
    }

    @Test
    void loadReturnsTableWhenMatchingLoaderExists() {
        NamespaceRegistry registry = new NamespaceRegistry();
        registry.registerNamespace("dynamis.ui");
        registry.registerLoader(new StubLoader());

        Optional<StringTable> table = registry.load(LocaleDescriptor.EN_US, "dynamis.ui");
        assertTrue(table.isPresent());
        assertEquals("dynamis.ui", table.orElseThrow().namespace());
    }

    private static final class EmptyLoader implements StringTableLoader {
        @Override
        public Optional<StringTable> load(LocaleDescriptor locale, String namespace) {
            return Optional.empty();
        }

        @Override
        public boolean supports(String namespace) {
            return true;
        }
    }

    private static final class UnsupportedLoader implements StringTableLoader {
        @Override
        public Optional<StringTable> load(LocaleDescriptor locale, String namespace) {
            return Optional.empty();
        }

        @Override
        public boolean supports(String namespace) {
            return false;
        }
    }

    private static final class StubLoader implements StringTableLoader {
        @Override
        public Optional<StringTable> load(LocaleDescriptor locale, String namespace) {
            return Optional.of(new StubStringTable(locale, namespace));
        }

        @Override
        public boolean supports(String namespace) {
            return "dynamis.ui".equals(namespace);
        }
    }

    private static final class StubStringTable implements StringTable {
        private final LocaleDescriptor locale;
        private final String namespace;

        private StubStringTable(LocaleDescriptor locale, String namespace) {
            this.locale = locale;
            this.namespace = namespace;
        }

        @Override
        public LocaleDescriptor locale() {
            return locale;
        }

        @Override
        public String namespace() {
            return namespace;
        }

        @Override
        public String get(LocaleKey key, MissingKeyBehavior onMissing) {
            return "";
        }

        @Override
        public String getPlural(LocaleKey key, long count, MissingKeyBehavior onMissing) {
            return "";
        }

        @Override
        public boolean has(LocaleKey key) {
            return false;
        }

        @Override
        public Optional<String> getPluralForm(LocaleKey key, PluralCategory category) {
            return Optional.empty();
        }

        @Override
        public Map<String, Object> rawEntries() {
            return Map.of();
        }
    }
}
