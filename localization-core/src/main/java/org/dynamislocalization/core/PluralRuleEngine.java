package org.dynamislocalization.core;

import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.PluralCategory;

/**
 * CLDR-based plural category resolution.
 *
 * Implements plural rules for the supported locale set.
 * Rules source: https://unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html
 *
 * Supported locales and their rule sets:
 *
 * English, German, Dutch, Swedish, Norwegian, Danish, Finnish (and most Germanic/Nordic):
 *   one: n == 1
 *   other: everything else
 *
 * French, Portuguese (Brazil):
 *   one: n == 0 or n == 1
 *   other: everything else
 *
 * Russian, Ukrainian, Serbian, Croatian, Bosnian:
 *   one:  n % 10 == 1 and n % 100 != 11
 *   few:  n % 10 in 2..4 and n % 100 not in 12..14
 *   many: n % 10 == 0 or n % 10 in 5..9 or n % 100 in 11..14
 *   other: everything else (decimals)
 *
 * Arabic:
 *   zero:  n == 0
 *   one:   n == 1
 *   two:   n == 2
 *   few:   n % 100 in 3..10
 *   many:  n % 100 in 11..99
 *   other: everything else
 *
 * Japanese, Chinese, Korean, Thai, Vietnamese (and most East Asian):
 *   other: always (no plural distinctions)
 *
 * Polish:
 *   one:  n == 1
 *   few:  n % 10 in 2..4 and n % 100 not in 12..14
 *   many: n % 10 == 1 and n % 100 != 11, or n % 10 in 5..9 or n % 10 == 0
 *   other: everything else (decimals)
 *
 * Czech, Slovak:
 *   one:  n == 1
 *   few:  n in 2..4
 *   many: (decimals)
 *   other: everything else
 */
public final class PluralRuleEngine {

    private PluralRuleEngine() {}

    /**
     * Resolves the CLDR plural category for a given count in a given locale.
     *
     * @param locale the locale to apply rules for
     * @param count  the numeric count (non-negative)
     * @return the appropriate PluralCategory
     */
    public static PluralCategory resolve(LocaleDescriptor locale, long count) {
        String tag = locale.bcp47Tag().toLowerCase();
        String lang = tag.contains("-") ? tag.substring(0, tag.indexOf('-')) : tag;

        return switch (lang) {
            case "fr", "pt" -> frenchRules(count);
            case "ru", "uk", "sr", "hr", "bs" -> russianRules(count);
            case "ar" -> arabicRules(count);
            case "ja", "zh", "ko", "th", "vi", "id", "ms", "tr" -> PluralCategory.OTHER;
            case "pl" -> polishRules(count);
            case "cs", "sk" -> czechRules(count);
            default -> englishRules(count);
        };
    }

    // one: n == 1; other: everything else
    private static PluralCategory englishRules(long n) {
        return n == 1 ? PluralCategory.ONE : PluralCategory.OTHER;
    }

    // one: n == 0 or n == 1; other: everything else
    private static PluralCategory frenchRules(long n) {
        return (n == 0 || n == 1) ? PluralCategory.ONE : PluralCategory.OTHER;
    }

    // Russian/Slavic rules
    private static PluralCategory russianRules(long n) {
        long mod10 = n % 10;
        long mod100 = n % 100;
        if (mod10 == 1 && mod100 != 11) return PluralCategory.ONE;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return PluralCategory.FEW;
        if (mod10 == 0 || (mod10 >= 5 && mod10 <= 9) || (mod100 >= 11 && mod100 <= 14))
            return PluralCategory.MANY;
        return PluralCategory.OTHER;
    }

    // Arabic rules
    private static PluralCategory arabicRules(long n) {
        long mod100 = n % 100;
        if (n == 0) return PluralCategory.ZERO;
        if (n == 1) return PluralCategory.ONE;
        if (n == 2) return PluralCategory.TWO;
        if (mod100 >= 3 && mod100 <= 10) return PluralCategory.FEW;
        if (mod100 >= 11 && mod100 <= 99) return PluralCategory.MANY;
        return PluralCategory.OTHER;
    }

    // Polish rules
    private static PluralCategory polishRules(long n) {
        long mod10 = n % 10;
        long mod100 = n % 100;
        if (n == 1) return PluralCategory.ONE;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return PluralCategory.FEW;
        return PluralCategory.MANY;
    }

    // Czech/Slovak rules
    private static PluralCategory czechRules(long n) {
        if (n == 1) return PluralCategory.ONE;
        if (n >= 2 && n <= 4) return PluralCategory.FEW;
        return PluralCategory.OTHER;
    }
}
