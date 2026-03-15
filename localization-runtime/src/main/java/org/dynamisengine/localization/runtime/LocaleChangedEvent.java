package org.dynamisengine.localization.runtime;

import org.dynamisengine.core.event.EngineEvent;
import org.dynamisengine.core.event.EventPriority;
import org.dynamisengine.core.exception.DynamisException;
import org.dynamisengine.localization.api.value.LocaleDescriptor;

/**
 * Fired on the DynamisEvent EventBus when the active locale changes.
 * Subscribers (DynamisUI, DynamisAI TTS, etc.) react to this event
 * to reload their locale-dependent state.
 */
public record LocaleChangedEvent(
        LocaleDescriptor previous,
        LocaleDescriptor current,
        long timestamp) implements EngineEvent {

    public LocaleChangedEvent(LocaleDescriptor previous, LocaleDescriptor current) {
        this(previous, current, System.nanoTime());
    }

    public LocaleChangedEvent {
        if (previous == null) throw new DynamisException("previous must not be null");
        if (current == null) throw new DynamisException("current must not be null");
    }

    @Override
    public EventPriority priority() {
        return EventPriority.HIGH;
    }
}
