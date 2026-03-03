# DynamisLocalization — Agent Guidelines

## What This Repo Is
String tables, language switching, and locale-aware formatting for the Dynamis ecosystem.
Owns all player-facing text assets and provides the localization API that DynamisUI,
DynamisScripting, DynamisAI, and game-specific mods consume.

## Module Structure
| Module | Purpose |
|---|---|
| `localization-api` | Pure contracts and value types. Zero logic. |
| `localization-core` | Default implementations, JSON loading, plural rules engine. |
| `localization-format` | Locale-aware formatting — numbers, dates, currency, parameter substitution. |
| `localization-runtime` | Assembly, DynamisEvent integration, mod/content pack support. |

## Dependency Order
```
localization-api → localization-core → localization-format → localization-runtime
```
Build and install in this order.

## Conventions (must match DynamisCore baseline)
- groupId: `org.dynamislocalization`
- Package root: `org.dynamislocalization.*`
- Logging: `DynamisLogger` only — never SLF4J
- Exceptions: root in `DynamisException` from DynamisCore
- `module-info.java` required in every module
- JUnit 5.11.4, SpotBugs 4.9.8.2, maven-compiler-plugin 3.14.0, maven-surefire-plugin 3.5.2

## Dependency Boundary Rules
- `localization-api` — depends on `dynamis-core` only
- `localization-core` — depends on `localization-api` and `dynamis-core` only
- `localization-format` — depends on `localization-api` and `dynamis-core` only
- `localization-runtime` — depends on all above plus `dynamis-event`
- No circular dependencies. No implementation module may be imported by API.

## String Table Format
JSON files. One file per locale per namespace.
Filename convention: `<namespace>.<bcp47tag>.json` (e.g. `dynamis.ui.en-US.json`)
Keys are dot-separated strings. Plural forms use CLDR categories.

## Build Commands
```bash
cd DynamisCore && mvn install && cd ..
cd DynamisEvent && mvn install && cd ..
cd DynamisLocalization && mvn install
```

## Known Tooling Notes
- `mvn dependency:analyze` fails on JDK 25 bytecode. Use `mvn dependency:tree` instead.
- SpotBugs requires 4.9.8.2+ for JDK 25 bytecode analysis.
