package org.dynamislocalization.api.value;

import org.dynamis.core.exception.DynamisException;

/**
 * Describes a supported locale.
 *
 * @param bcp47Tag    BCP-47 language tag e.g. "en-US", "fr-FR", "ar-SA"
 * @param displayName Human-readable name e.g. "English (United States)"
 * @param rightToLeft True for RTL scripts (Arabic, Hebrew)
 */
public record LocaleDescriptor(String bcp47Tag, String displayName, boolean rightToLeft) {

    public static final LocaleDescriptor EN_US =
        new LocaleDescriptor("en-US", "English (United States)", false);
    public static final LocaleDescriptor FR_FR =
        new LocaleDescriptor("fr-FR", "French (France)", false);
    public static final LocaleDescriptor DE_DE =
        new LocaleDescriptor("de-DE", "German (Germany)", false);
    public static final LocaleDescriptor JA_JP =
        new LocaleDescriptor("ja-JP", "Japanese (Japan)", false);
    public static final LocaleDescriptor AR_SA =
        new LocaleDescriptor("ar-SA", "Arabic (Saudi Arabia)", true);
    public static final LocaleDescriptor RU_RU =
        new LocaleDescriptor("ru-RU", "Russian (Russia)", false);
    public static final LocaleDescriptor ZH_CN =
        new LocaleDescriptor("zh-CN", "Chinese Simplified (China)", false);

    public LocaleDescriptor {
        if (bcp47Tag == null || bcp47Tag.isBlank()) {
            throw new DynamisException("LocaleDescriptor bcp47Tag must not be null or blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new DynamisException("LocaleDescriptor displayName must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return bcp47Tag;
    }
}
