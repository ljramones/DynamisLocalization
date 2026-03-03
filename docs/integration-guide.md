# DynamisLocalization — Integration Guide

## Overview

DynamisLocalization provides string tables, runtime locale switching, plural rules,
and locale-aware formatting for the Dynamis ecosystem. All consumers depend only on
`localization-api` and `localization-runtime` — never on `localization-core` or
`localization-format` directly.

## Maven Dependency

Add to your module's `pom.xml`:

```xml
<dependency>
    <groupId>org.dynamislocalization</groupId>
    <artifactId>localization-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.dynamislocalization</groupId>
    <artifactId>localization-runtime</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Add to your `module-info.java`:

```java
requires org.dynamislocalization.api;
requires org.dynamislocalization.runtime;
```

---

## Assembly (done once at startup)

```java
LocalizationRuntime runtime = LocalizationRuntime.builder()
    .initialLocale(LocaleDescriptor.EN_US)
    .supportedLocales(List.of(
        LocaleDescriptor.EN_US,
        LocaleDescriptor.FR_FR,
        LocaleDescriptor.DE_DE,
        LocaleDescriptor.JA_JP,
        LocaleDescriptor.AR_SA))
    .loader(new JsonStringTableLoader(Path.of("assets/localization")))
    .namespace("dynamis.ui")
    .namespace("dynamis.scripting")
    .namespace("dynamis.ai")
    .missingKeyBehavior(MissingKeyBehavior.RETURN_KEY)  // THROW in CI
    .eventBus(myEventBus)   // pass your DynamisEvent EventBus, or omit for a default
    .build();

LocalizationService loc = runtime.service();
```

---

## String Table File Format

File naming: `<namespace>.<bcp47tag>.json`
Location: `assets/localization/` (or any directory passed to `JsonStringTableLoader`)

```
assets/localization/
    dynamis.ui.en-US.json
    dynamis.ui.fr-FR.json
    dynamis.scripting.en-US.json
    dynamis.ai.en-US.json
    mod.mymod.en-US.json
```

File schema:

```json
{
  "locale": "en-US",
  "version": "1.0",
  "namespace": "dynamis.ui",
  "keys": {
    "button.confirm": "Confirm",
    "button.cancel": "Cancel",
    "items.count": {
      "one": "{count} item",
      "other": "{count} items"
    },
    "shop.balance": "Balance: {amount, currency}",
    "greeting": "Hello, {name}!"
  }
}
```

Plural forms use CLDR category names: `zero`, `one`, `two`, `few`, `many`, `other`.
Only the categories needed for the target locale are required. All locales fall back
to `other` if the resolved category is absent.

---

## Basic String Lookup

```java
LocaleKey confirmKey = LocaleKey.of("dynamis.ui:button.confirm");
String text = loc.get(confirmKey);  // "Confirm"
```

---

## Plural Forms

```java
LocaleKey itemsKey = LocaleKey.of("dynamis.ui:items.count");

String one   = loc.getPlural(itemsKey, 1);   // "{count} item"
String many  = loc.getPlural(itemsKey, 5);   // "{count} items"
```

Note: plural forms are returned as templates. Pass them through
`ParameterSubstitutor` to resolve `{count}`:

```java
ParameterSubstitutor sub = runtime.substitutor();
String rendered = sub.substitute(loc.getPlural(itemsKey, 5), Map.of("count", 5));
// "5 items"
```

---

## Parameter Substitution

```java
ParameterSubstitutor sub = runtime.substitutor();

// Simple substitution
String greeting = sub.substitute(
    loc.get(LocaleKey.of("dynamis.ui:greeting")),
    Map.of("name", "Aria"));
// "Hello, Aria!"

// Formatted substitution
String balance = sub.substitute(
    loc.get(LocaleKey.of("dynamis.ui:shop.balance")),
    Map.of("amount", 1234.50));
// "Balance: $1,234.50"  (en-US)
// "Balance: 1 234,50 €" (fr-FR, if switched)
```

Supported format types in `{param, type}` tokens:

| Type       | Input               | Example output (en-US) |
|------------|---------------------|------------------------|
| `number`   | `Number`            | `1,234,567`            |
| `currency` | `Number`            | `$1,234.50`            |
| `percent`  | `Number` (0.0–1.0)  | `75%`                  |
| `date`     | `LocalDate`         | `1/15/25`              |
| `datetime` | `LocalDateTime`     | `1/15/25, 3:30 PM`     |
| `filesize` | `Number` (bytes)    | `1.2 MB`               |

---

## Locale Switching

### Programmatic switch

```java
runtime.switchLocale(LocaleDescriptor.FR_FR);

// All subsequent loc.get() calls return French strings
String text = loc.get(LocaleKey.of("dynamis.ui:button.confirm")); // "Confirmer"
```

`switchLocale` is thread-safe. The active table set is replaced atomically.

### Listening for locale changes (pull model)

```java
loc.addLocaleChangeListener((previous, current) -> {
    log.info("Locale changed: " + previous.bcp47Tag() + " → " + current.bcp47Tag());
    rebuildUiStrings();
});
```

### Listening via DynamisEvent EventBus (push model — preferred for cross-module use)

```java
eventBus.subscribe(LocaleChangedEvent.class, event -> {
    LocaleDescriptor newLocale = event.current();
    reloadTtsVoiceProfile(newLocale);
});
```

---

## DynamisUI Integration

DynamisUI should hold a reference to `LocalizationService` injected at scene load.
On `LocaleChangedEvent`, rebind all text nodes:

```java
// At scene load
this.loc = localizationRuntime.service();
eventBus.subscribe(LocaleChangedEvent.class, e -> rebind());

// Rebind method
private void rebind() {
    confirmButton.setText(loc.get(LocaleKey.of("dynamis.ui:button.confirm")));
    cancelButton.setText(loc.get(LocaleKey.of("dynamis.ui:button.cancel")));
    // ... all UI text nodes
}
```

For RTL support, check `runtime.service().currentLocale().rightToLeft()` and apply
layout mirroring accordingly.

---

## DynamisScripting Integration

NPC dialogue templates live in the `dynamis.scripting` namespace. The scripting
layer retrieves the template for the current locale and passes it to the dialogue
system as a pre-localized string:

```java
// In the dialogue template resolver
LocaleKey templateKey = LocaleKey.of("dynamis.scripting:" + dialogueId);
String template = loc.get(templateKey);

// Substitute NPC-specific parameters
String line = substitutor.substitute(template, Map.of(
    "npcName", npc.displayName(),
    "playerName", player.displayName(),
    "gold", player.gold()
));
```

Subscribe to `LocaleChangedEvent` to invalidate any cached resolved templates:

```java
eventBus.subscribe(LocaleChangedEvent.class, e -> dialogueTemplateCache.invalidateAll());
```

---

## DynamisAI TTS Integration

DynamisAI needs the BCP-47 tag to select the correct TTS voice profile.
Subscribe to `LocaleChangedEvent` and update the active voice:

```java
eventBus.subscribe(LocaleChangedEvent.class, event -> {
    String bcp47 = event.current().bcp47Tag();
    ttsEngine.setVoiceLocale(bcp47);
    log.info("TTS voice locale updated to " + bcp47);
});
```

On startup, seed from the current locale:

```java
ttsEngine.setVoiceLocale(localizationRuntime.service().currentLocale().bcp47Tag());
```

---

## Mod / Content Pack Support

Mods register their own namespace and a loader pointing to their asset directory:

```java
NamespaceRegistry registry = runtime.namespaceRegistry();
registry.registerNamespace("mod.mymod");
registry.registerLoader(new JsonStringTableLoader(
    Path.of("mods/mymod/localization"),
    Set.of("mod.mymod")));

// After registration, look up mod strings normally
String modText = loc.get(LocaleKey.of("mod.mymod:some.key"));
```

Mod namespaces never conflict with `dynamis.*` namespaces by convention.
The `JsonStringTableLoader` scoped to `Set.of("mod.mymod")` will only serve
requests for that namespace — it will not interfere with other loaders.

---

## Missing Key Behavior

| Behavior     | Result                                    | When to use              |
|--------------|-------------------------------------------|--------------------------|
| `RETURN_KEY` | Returns `"namespace:key"` (default)       | Production               |
| `RETURN_EMPTY` | Returns `""`                            | UI layout testing        |
| `THROW`      | Throws `DynamisException`                 | CI / translation QA      |

Set per-service default at build time:
```java
.missingKeyBehavior(MissingKeyBehavior.THROW)  // catch missing keys early
```

Override per-call:
```java
loc.get(key, MissingKeyBehavior.RETURN_EMPTY);
```

---

## Supported Locales (built-in descriptors)

| Constant                    | BCP-47    | RTL   |
|-----------------------------|-----------|-------|
| `LocaleDescriptor.EN_US`    | `en-US`   | false |
| `LocaleDescriptor.FR_FR`    | `fr-FR`   | false |
| `LocaleDescriptor.DE_DE`    | `de-DE`   | false |
| `LocaleDescriptor.JA_JP`    | `ja-JP`   | false |
| `LocaleDescriptor.AR_SA`    | `ar-SA`   | true  |
| `LocaleDescriptor.RU_RU`    | `ru-RU`   | false |
| `LocaleDescriptor.ZH_CN`    | `zh-CN`   | false |

Additional locales: construct `new LocaleDescriptor(bcp47Tag, displayName, rtl)`.
