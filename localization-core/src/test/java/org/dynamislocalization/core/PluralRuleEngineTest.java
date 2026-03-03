package org.dynamislocalization.core;

import org.dynamislocalization.api.value.LocaleDescriptor;
import org.dynamislocalization.api.value.PluralCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluralRuleEngineTest {

    @Test
    void englishRules() {
        LocaleDescriptor locale = LocaleDescriptor.EN_US;
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 1));
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 0));
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 2));
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 11));
    }

    @Test
    void frenchRules() {
        LocaleDescriptor locale = LocaleDescriptor.FR_FR;
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 0));
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 1));
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 2));
    }

    @Test
    void russianRules() {
        LocaleDescriptor locale = LocaleDescriptor.RU_RU;
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 1));
        assertEquals(PluralCategory.MANY, PluralRuleEngine.resolve(locale, 11));
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 21));
        assertEquals(PluralCategory.FEW, PluralRuleEngine.resolve(locale, 2));
        assertEquals(PluralCategory.MANY, PluralRuleEngine.resolve(locale, 12));
        assertEquals(PluralCategory.MANY, PluralRuleEngine.resolve(locale, 5));
    }

    @Test
    void arabicRules() {
        LocaleDescriptor locale = LocaleDescriptor.AR_SA;
        assertEquals(PluralCategory.ZERO, PluralRuleEngine.resolve(locale, 0));
        assertEquals(PluralCategory.ONE, PluralRuleEngine.resolve(locale, 1));
        assertEquals(PluralCategory.TWO, PluralRuleEngine.resolve(locale, 2));
        assertEquals(PluralCategory.FEW, PluralRuleEngine.resolve(locale, 5));
        assertEquals(PluralCategory.MANY, PluralRuleEngine.resolve(locale, 15));
    }

    @Test
    void japaneseRules() {
        LocaleDescriptor locale = LocaleDescriptor.JA_JP;
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 1));
        assertEquals(PluralCategory.OTHER, PluralRuleEngine.resolve(locale, 100));
    }
}
