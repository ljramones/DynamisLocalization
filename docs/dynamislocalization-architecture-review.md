# DynamisLocalization Architecture Review

Date: 2026-03-10  
Scope: Deep boundary ratification for `DynamisLocalization` (review/documentation only)

## 1. Repo Overview

Observed modules:

- `localization-api`
- `localization-core`
- `localization-format`
- `localization-runtime`

Observed implementation shape:

- `localization-api` defines localization contracts and value types (`LocalizationService`, `StringTable`, `StringTableLoader`, `LocaleDescriptor`, `LocaleKey`, `MissingKeyBehavior`, `PluralCategory`).
- `localization-core` implements JSON-backed table loading and plural rules (`JsonStringTableLoader`, `DefaultStringTable`, `PluralRuleEngine`, `MinimalJsonParser`).
- `localization-format` provides locale-aware formatting and template substitution (`LocaleAwareFormatter`, `ParameterSubstitutor`).
- `localization-runtime` assembles service/runtime wiring, namespace/loader registry, locale switching, and `DynamisEvent` integration (`LocalizationRuntime`, `DefaultLocalizationService`, `NamespaceRegistry`, `LocaleChangedEvent`).

Dependency signals from poms/module-info:

- Depends on `dynamis-core` across modules.
- `localization-runtime` depends on `dynamis-event`.
- No direct dependencies on `DynamisContent`, `DynamisSession`, `DynamisWorldEngine`, `DynamisUI`, or `DynamisAssetPipeline`.

## 2. Strict Ownership Statement

### What DynamisLocalization should own

- Runtime locale-aware lookup and retrieval of localized text resources.
- Localization contracts for keys/namespaces/locales/string tables.
- Runtime locale switch semantics and locale-change notifications.
- Localization-specific formatting/pluralization and parameter substitution behavior.
- Runtime caching/active-table replacement behavior inside localization service scope.

### What is appropriate for this repo today

- Generic `StringTableLoader` SPI and runtime namespace/loader composition.
- JSON string table loading as one runtime loader implementation.
- CLDR-style plural category resolution and locale-based formatting utilities.
- Emitting localization-specific runtime events (`LocaleChangedEvent`) for consumers.

### What it must never own

- Session/profile authority for user preference persistence (it may consume locale input; it must not own account/profile/session state).
- Runtime content authority (catalog identity, global content manifests, content cache policy outside localization tables).
- Build-time content pipeline ownership and asset baking/packaging.
- World authority, render authority, gameplay orchestration, or scripting policy.

## 3. Dependency Rules

### Allowed dependencies for DynamisLocalization

- `DynamisCore` for common exceptions/logging/event interfaces.
- `DynamisEvent` for generic event transport integration (`EventBus` publish/subscribe).
- JDK localization/formatting primitives.

### Forbidden dependencies for DynamisLocalization

- World/session authority repos (`DynamisWorldEngine`, `DynamisSession`) as ownership dependencies.
- Render/GPU subsystems (`DynamisLightEngine`, `DynamisGPU`) beyond consumer usage.
- Build-time pipeline authority (`DynamisAssetPipeline`) in runtime localization modules.

### Who may depend on DynamisLocalization

- UI, scripting, AI, feature systems that need localized runtime text lookup/formatting.
- Runtime orchestration layers that need locale-switch notifications.

### Who should not be represented inside DynamisLocalization

- Session persistence logic (save/load of locale preferences belongs in `DynamisSession`).
- Global runtime content catalog ownership (belongs in `DynamisContent`).
- Build/packaging tooling (belongs in `DynamisAssetPipeline`).

## 4. Public vs Internal Boundary

### Canonical public surface (recommended)

- `localization-api` should be the stable long-term contract surface.
- From runtime, preferred external entry point is `LocalizationRuntime` + `LocalizationService`.
- `LocaleChangedEvent` is a valid public integration event for cross-module consumers.

### Internal/implementation areas (should remain internal)

- `localization-core` concrete parser/loader/table internals (`MinimalJsonParser`, `DefaultStringTable`, parser implementation details).
- Runtime assembly internals such as loader-chain ordering behavior and active-table swap strategy.

### Boundary concern

- `localization-core`, `localization-format`, and `localization-runtime` all export implementation packages via `module-info.java`.
- This broad export surface can freeze concrete implementation types prematurely, instead of keeping most consumers on `localization-api` (+ minimal runtime facade).

## 5. Policy Leakage / Overlap Findings

## Major clean boundaries confirmed

- No direct world/session/render authority code present.
- No build-time pipeline orchestration inside this repo.
- Localization responsibilities are concretely implemented and cohesive: key contracts, loading, formatting, runtime switching, and event notification.

## Policy leakage / overlap risks

- **DynamisContent overlap risk (moderate):** `JsonStringTableLoader` reads directly from filesystem paths and runtime namespace registration acts as local content resolution. This is acceptable for now, but global runtime content ownership should remain in `DynamisContent`.
- **DynamisSession overlap risk (moderate):** runtime supports locale switching but intentionally does not persist locale preference. Keep persistence boundary in `DynamisSession`; Localization should consume provided locale state.
- **DynamisAssetPipeline overlap risk (low-to-moderate):** localization JSON schema and parser exist here; avoid growing this into build-time localization packaging/validation authority.
- **DynamisUI / DynamisScripting overlap risk (low):** integration docs include usage patterns; keep them as consumer examples, not ownership of UI/scripting policy.

## 6. Ratification Result

**Judgment: ratified with constraints**

Why:

- The repository is strongly aligned with runtime localization authority and does not currently absorb world/session/render ownership.
- Main constraints are boundary discipline around runtime content loading and avoiding expansion into persistence or pipeline generation.
- Public module exports are broader than ideal, creating future API-freezing risk for concrete implementation classes.

## 7. Recommended Next Step

1. Keep localization as runtime text/locale authority only.
2. In upcoming boundary reviews, explicitly ratify handoff lines:
   - `DynamisSession` owns persisted locale preference state.
   - `DynamisContent` owns broader runtime content catalog/resolution authority.
   - `DynamisAssetPipeline` owns build-time generation/packaging of localization artifacts.
3. Next repo to review: **DynamisUI** (direct major consumer where localization-policy/presentation boundaries are likely to blur).

---

This document is a boundary-ratification review artifact. It does not perform refactors in this pass.
