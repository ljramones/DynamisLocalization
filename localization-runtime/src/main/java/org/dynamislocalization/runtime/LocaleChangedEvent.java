package org.dynamislocalization.runtime;

import org.dynamis.core.event.EngineEvent;
import org.dynamis.core.event.EventPriority;
import org.dynamis.core.exception.DynamisException;
import org.dynamislocalization.api.value.LocaleDescriptor;

/**
 * Fired on the DynamisEvent EventBus when the active locale changes.
 * Subscribers (DynamisUI, DynamisAI TTS, etc.) react to this event
 * to reload their locale-dependent state.
 */
public record LocaleChangedEvent(
        LocaleDescriptor previous,
        LocaleDescriptor current) implements EngineEvent {

    public LocaleChangedEvent {
        if (previous == null) throw new DynamisException("previous must not be null");
        if (current == null) throw new DynamisException("current must not be null");
    }

    @Override
    public EventPriority priority() {
        return EventPriority.HIGH;
    }
}
