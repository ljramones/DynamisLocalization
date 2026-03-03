package org.dynamislocalization.api.value;

/**
 * Controls what happens when a key is not found in the string table.
 */
public enum MissingKeyBehavior {
    /** Return the raw key string as the value. Default. */
    RETURN_KEY,
    /** Return an empty string. */
    RETURN_EMPTY,
    /** Throw a DynamisException. Use in development/CI to catch missing translations. */
    THROW
}
