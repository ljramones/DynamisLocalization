package org.dynamisengine.localization.runtime;

import org.dynamisengine.core.event.EventPriority;
import org.dynamisengine.core.exception.DynamisException;
import org.dynamisengine.localization.api.value.LocaleDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocaleChangedEventTest {

    @Test
    void fieldsAccessibleAndCorrect() {
        LocaleChangedEvent event = new LocaleChangedEvent(LocaleDescriptor.EN_US, LocaleDescriptor.FR_FR);
        assertEquals(LocaleDescriptor.EN_US, event.previous());
        assertEquals(LocaleDescriptor.FR_FR, event.current());
    }

    @Test
    void nullPreviousThrows() {
        assertThrows(DynamisException.class, () -> new LocaleChangedEvent(null, LocaleDescriptor.FR_FR));
    }

    @Test
    void nullCurrentThrows() {
        assertThrows(DynamisException.class, () -> new LocaleChangedEvent(LocaleDescriptor.EN_US, null));
    }

    @Test
    void priorityIsHigh() {
        LocaleChangedEvent event = new LocaleChangedEvent(LocaleDescriptor.EN_US, LocaleDescriptor.FR_FR);
        assertEquals(EventPriority.HIGH, event.priority());
    }
}
