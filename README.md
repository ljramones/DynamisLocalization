# DynamisLocalization

String tables, language switching, and locale-aware formatting for the Dynamis ecosystem.
Owns all player-facing text assets and provides the localization API that DynamisUI,
DynamisScripting, DynamisAI, and game-specific mods consume.

## Modules

| Module | Purpose |
|---|---|
| `localization-api` | Pure contracts — interfaces, records, enums. No logic. |
| `localization-core` | JSON loading, plural rules engine (CLDR), default string table. |
| `localization-format` | Locale-aware number, currency, date, and parameter formatting. |
| `localization-runtime` | Assembly, DynamisEvent integration, mod/content pack support. |

## Quick Start

```java
LocalizationRuntime runtime = LocalizationRuntime.builder()
    .initialLocale(LocaleDescriptor.EN_US)
    .supportedLocales(List.of(LocaleDescriptor.EN_US, LocaleDescriptor.FR_FR))
    .loader(new JsonStringTableLoader(Path.of("assets/localization")))
    .namespace("dynamis.ui")
    .build();

String text = runtime.service().get(LocaleKey.of("dynamis.ui:button.confirm"));
runtime.switchLocale(LocaleDescriptor.FR_FR);
```

## String Table Format

Files: `<namespace>.<bcp47tag>.json` in your localization asset directory.

```json
{
  "locale": "en-US",
  "namespace": "dynamis.ui",
  "keys": {
    "button.confirm": "Confirm",
    "items.count": {
      "one": "{count} item",
      "other": "{count} items"
    }
  }
}
```

## Build

```bash
cd DynamisCore && mvn install && cd ..
cd DynamisEvent && mvn install && cd ..
cd DynamisLocalization && mvn install
```

## Tests

```bash
cd DynamisLocalization && mvn test
```

68 tests across 4 modules, 0 failures.

## Documentation

See [docs/integration-guide.md](docs/integration-guide.md) for full integration
examples covering DynamisUI, DynamisScripting, DynamisAI, and mod pack support.

## Supported Locales (built-in)

`en-US` · `fr-FR` · `de-DE` · `ja-JP` · `ar-SA` (RTL) · `ru-RU` · `zh-CN`

Additional locales: `new LocaleDescriptor(bcp47Tag, displayName, rtl)`.
