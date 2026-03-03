package org.dynamislocalization.runtime;

import org.dynamis.core.exception.DynamisException;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamis.event.EventBus;
import org.dynamislocalization.api.LocaleChangeListener;
import org.dynamislocalization.api.LocalizationService;
import org.dynamislocalization.api.StringTable;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation of LocalizationService.
 *
 * Thread-safe locale switching via AtomicReference on the active table set.
 * Reads are always lock-free. Writes (switchLocale) are atomic swaps.
 */
public final class DefaultLocalizationService implements LocalizationService {

    private static final DynamisLogger log =
        DynamisLogger.get(DefaultLocalizationService.class);

    private final NamespaceRegistry namespaceRegistry;
    private final List<LocaleDescriptor> supportedLocales;
    private final MissingKeyBehavior defaultMissingBehavior;
    private final EventBus eventBus;

    /** Active locale - switched atomically. */
    private final AtomicReference<LocaleDescriptor> currentLocale;

    /** Active string tables - replaced atomically on locale switch. */
    private final AtomicReference<Map<String, StringTable>> activeTables;

    private final CopyOnWriteArrayList<LocaleChangeListener> listeners =
        new CopyOnWriteArrayList<>();

    public DefaultLocalizationService(
            LocaleDescriptor initialLocale,
            List<LocaleDescriptor> supportedLocales,
            NamespaceRegistry namespaceRegistry,
            MissingKeyBehavior defaultMissingBehavior,
            EventBus eventBus) {
        this.currentLocale = new AtomicReference<>(initialLocale);
        this.supportedLocales = List.copyOf(supportedLocales);
        this.namespaceRegistry = namespaceRegistry;
        this.defaultMissingBehavior = defaultMissingBehavior;
        this.eventBus = eventBus;
        this.activeTables = new AtomicReference<>(loadAllTables(initialLocale));
        log.info(String.format("LocalizationService initialized with locale %s",
            initialLocale.bcp47Tag()));
    }

    @Override
    public LocaleDescriptor currentLocale() {
        return currentLocale.get();
    }

    @Override
    public void switchLocale(LocaleDescriptor locale) {
        if (locale == null) throw new DynamisException("locale must not be null");
        LocaleDescriptor previous = currentLocale.getAndSet(locale);
        if (previous.equals(locale)) return;

        Map<String, StringTable> newTables = loadAllTables(locale);
        activeTables.set(newTables);

        log.info(String.format("Locale switched: %s -> %s",
            previous.bcp47Tag(), locale.bcp47Tag()));

        LocaleChangedEvent event = new LocaleChangedEvent(previous, locale);
        if (eventBus != null) {
            eventBus.publish(event);
        }
        for (LocaleChangeListener listener : listeners) {
            listener.onLocaleChanged(previous, locale);
        }
    }

    @Override
    public String get(LocaleKey key) {
        return get(key, defaultMissingBehavior);
    }

    @Override
    public String get(LocaleKey key, MissingKeyBehavior onMissing) {
        return tableFor(key.namespace())
            .map(t -> t.get(key, onMissing))
            .orElseGet(() -> applyMissing(key, onMissing));
    }

    @Override
    public String getPlural(LocaleKey key, long count) {
        return getPlural(key, count, defaultMissingBehavior);
    }

    @Override
    public String getPlural(LocaleKey key, long count, MissingKeyBehavior onMissing) {
        return tableFor(key.namespace())
            .map(t -> t.getPlural(key, count, onMissing))
            .orElseGet(() -> applyMissing(key, onMissing));
    }

    @Override
    public Optional<StringTable> tableFor(String namespace) {
        return Optional.ofNullable(activeTables.get().get(namespace));
    }

    @Override
    public List<LocaleDescriptor> supportedLocales() {
        return supportedLocales;
    }

    @Override
    public void addLocaleChangeListener(LocaleChangeListener listener) {
        if (listener != null) listeners.add(listener);
    }

    @Override
    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    private Map<String, StringTable> loadAllTables(LocaleDescriptor locale) {
        Map<String, StringTable> tables = new ConcurrentHashMap<>();
        for (String namespace : namespaceRegistry.namespaces()) {
            namespaceRegistry.load(locale, namespace)
                .ifPresent(table -> tables.put(namespace, table));
        }
        return tables;
    }

    private String applyMissing(LocaleKey key, MissingKeyBehavior behavior) {
        return switch (behavior) {
            case RETURN_KEY -> key.fullyQualified();
            case RETURN_EMPTY -> "";
            case THROW -> throw new DynamisException(
                String.format("No string table for namespace '%s' in locale '%s'",
                    key.namespace(), currentLocale().bcp47Tag()));
        };
    }
}
