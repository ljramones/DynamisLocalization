package org.dynamislocalization.runtime;

import org.dynamis.core.exception.DynamisException;
import org.dynamis.event.EventBusBuilder;
import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.LocaleKey;
import org.dynamislocalization.api.value.MissingKeyBehavior;
import org.dynamislocalization.core.JsonStringTableLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalizationRuntimeTest {

    private static LocalizationRuntime runtime() {
        return LocalizationRuntime.builder()
            .initialLocale(LocaleDescriptor.EN_US)
            .supportedLocales(List.of(LocaleDescriptor.EN_US, LocaleDescriptor.FR_FR))
            .loader(new JsonStringTableLoader(Path.of("src/test/resources")))
            .namespace("dynamis.ui")
            .eventBus(EventBusBuilder.create().synchronous().build())
            .build();
    }

    @Test
    void serviceGetReturnsConfirmForEnglish() {
        LocalizationRuntime runtime = runtime();
        String text = runtime.service().get(LocaleKey.of("dynamis.ui:button.confirm"));
        assertEquals("Confirm", text);
    }

    @Test
    void switchLocaleToFrenchReturnsConfirmer() {
        LocalizationRuntime runtime = runtime();
        runtime.switchLocale(LocaleDescriptor.FR_FR);
        String text = runtime.service().get(LocaleKey.of("dynamis.ui:button.confirm"));
        assertEquals("Confirmer", text);
    }

    @Test
    void switchLocaleFiresLocaleChangedEventOnEventBus() {
        LocalizationRuntime runtime = runtime();
        AtomicReference<LocaleChangedEvent> captured = new AtomicReference<>();
        runtime.eventBus().subscribe(LocaleChangedEvent.class, captured::set);

        runtime.switchLocale(LocaleDescriptor.FR_FR);

        LocaleChangedEvent event = captured.get();
        assertEquals(LocaleDescriptor.EN_US, event.previous());
        assertEquals(LocaleDescriptor.FR_FR, event.current());
    }

    @Test
    void switchLocaleNotifiesLocaleChangeListener() {
        LocalizationRuntime runtime = runtime();
        AtomicReference<LocaleDescriptor> previous = new AtomicReference<>();
        AtomicReference<LocaleDescriptor> current = new AtomicReference<>();

        runtime.service().addLocaleChangeListener((prev, curr) -> {
            previous.set(prev);
            current.set(curr);
        });

        runtime.switchLocale(LocaleDescriptor.FR_FR);

        assertEquals(LocaleDescriptor.EN_US, previous.get());
        assertEquals(LocaleDescriptor.FR_FR, current.get());
    }

    @Test
    void switchLocaleSameLocaleDoesNotFireEvent() {
        LocalizationRuntime runtime = runtime();
        AtomicInteger count = new AtomicInteger(0);
        runtime.eventBus().subscribe(LocaleChangedEvent.class, event -> count.incrementAndGet());

        runtime.switchLocale(LocaleDescriptor.EN_US);

        assertEquals(0, count.get());
    }

    @Test
    void missingKeyReturnKeyBehavior() {
        LocalizationRuntime runtime = runtime();
        String value = runtime.service().get(
            LocaleKey.of("dynamis.ui:nonexistent"),
            MissingKeyBehavior.RETURN_KEY
        );
        assertEquals("dynamis.ui:nonexistent", value);
    }

    @Test
    void missingKeyReturnEmptyBehavior() {
        LocalizationRuntime runtime = runtime();
        String value = runtime.service().get(
            LocaleKey.of("dynamis.ui:nonexistent"),
            MissingKeyBehavior.RETURN_EMPTY
        );
        assertEquals("", value);
    }

    @Test
    void missingKeyThrowBehavior() {
        LocalizationRuntime runtime = runtime();
        assertThrows(
            DynamisException.class,
            () -> runtime.service().get(LocaleKey.of("dynamis.ui:nonexistent"), MissingKeyBehavior.THROW)
        );
    }

    @Test
    void getPluralOneReturnsOneForm() {
        LocalizationRuntime runtime = runtime();
        String value = runtime.service().getPlural(LocaleKey.of("dynamis.ui:items.count"), 1);
        assertEquals("{count} item", value);
    }

    @Test
    void getPluralFiveReturnsOtherForm() {
        LocalizationRuntime runtime = runtime();
        String value = runtime.service().getPlural(LocaleKey.of("dynamis.ui:items.count"), 5);
        assertEquals("{count} items", value);
    }
}
