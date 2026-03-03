package org.dynamislocalization.runtime;

import org.dynamis.core.exception.DynamisException;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamis.event.EventBus;
import org.dynamis.event.EventBusBuilder;
import org.dynamislocalization.api.LocalizationService;
import org.dynamislocalization.api.spi.StringTableLoader;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.format.LocaleAwareFormatter;
import org.dynamislocalization.format.ParameterSubstitutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level assembly for DynamisLocalization.
 *
 * Wires together: NamespaceRegistry, DefaultLocalizationService,
 * LocaleAwareFormatter, ParameterSubstitutor, and DynamisEvent EventBus.
 *
 * Usage:
 * <pre>
 *   LocalizationRuntime runtime = LocalizationRuntime.builder()
 *       .initialLocale(LocaleDescriptor.EN_US)
 *       .supportedLocales(List.of(LocaleDescriptor.EN_US, LocaleDescriptor.FR_FR))
 *       .loader(new JsonStringTableLoader(path))
 *       .namespace("dynamis.ui")
 *       .build();
 *
 *   String text = runtime.service().get(LocaleKey.of("dynamis.ui:button.confirm"));
 *   runtime.switchLocale(LocaleDescriptor.FR_FR);
 * </pre>
 */
public final class LocalizationRuntime {

    private static final DynamisLogger log = DynamisLogger.get(LocalizationRuntime.class);

    private final DefaultLocalizationService service;
    private final LocaleAwareFormatter formatter;
    private final ParameterSubstitutor substitutor;
    private final NamespaceRegistry namespaceRegistry;
    private final EventBus eventBus;

    private LocalizationRuntime(Builder builder) {
        this.eventBus = builder.eventBus != null
            ? builder.eventBus
            : EventBusBuilder.create().synchronous().build();
        this.namespaceRegistry = new NamespaceRegistry();

        for (String ns : builder.namespaces) {
            namespaceRegistry.registerNamespace(ns);
        }
        for (StringTableLoader loader : builder.loaders) {
            namespaceRegistry.registerLoader(loader);
        }

        this.service = new DefaultLocalizationService(
            builder.initialLocale,
            builder.supportedLocales,
            namespaceRegistry,
            builder.missingKeyBehavior,
            this.eventBus);

        this.formatter = new LocaleAwareFormatter(builder.initialLocale);
        this.substitutor = new ParameterSubstitutor(formatter);

        log.info("LocalizationRuntime initialized");
    }

    /** The primary localization service. Use this for all string lookups. */
    public LocalizationService service() {
        return service;
    }

    /** The locale-aware formatter for direct value formatting. */
    public LocaleAwareFormatter formatter() {
        return formatter;
    }

    /** The parameter substitutor for template rendering. */
    public ParameterSubstitutor substitutor() {
        return substitutor;
    }

    /** The namespace registry for dynamic namespace/loader registration. */
    public NamespaceRegistry namespaceRegistry() {
        return namespaceRegistry;
    }

    /** The event bus - exposes for external subscribers. */
    public EventBus eventBus() {
        return eventBus;
    }

    /**
     * Switches the active locale. Fires LocaleChangedEvent on the EventBus.
     * Thread-safe.
     */
    public void switchLocale(LocaleDescriptor locale) {
        service.switchLocale(locale);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LocaleDescriptor initialLocale = LocaleDescriptor.EN_US;
        private List<LocaleDescriptor> supportedLocales = new ArrayList<>();
        private List<StringTableLoader> loaders = new ArrayList<>();
        private List<String> namespaces = new ArrayList<>();
        private MissingKeyBehavior missingKeyBehavior = MissingKeyBehavior.RETURN_KEY;
        private EventBus eventBus;

        public Builder initialLocale(LocaleDescriptor locale) {
            this.initialLocale = locale;
            return this;
        }

        public Builder supportedLocales(List<LocaleDescriptor> locales) {
            this.supportedLocales = new ArrayList<>(locales);
            return this;
        }

        public Builder loader(StringTableLoader loader) {
            this.loaders.add(loader);
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespaces.add(namespace);
            return this;
        }

        public Builder missingKeyBehavior(MissingKeyBehavior behavior) {
            this.missingKeyBehavior = behavior;
            return this;
        }

        public Builder eventBus(EventBus bus) {
            this.eventBus = bus;
            return this;
        }

        public LocalizationRuntime build() {
            if (initialLocale == null) {
                throw new DynamisException("initialLocale must not be null");
            }
            if (supportedLocales.isEmpty()) {
                supportedLocales.add(initialLocale);
            }
            return new LocalizationRuntime(this);
        }
    }
}
