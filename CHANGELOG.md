# Changelog

All notable changes to this project will be documented in this file.

## [1.1] - 2026-08-13 (versionCode 3)

### Added
- **Multi-language Support (9 Languages):**
  - Added full native translations for English, German (Deutsch), French (Français), Dutch (Nederlands), Spanish (Español), Italian (Italiano), Norwegian (Norsk Bokmål), Swedish (Svenska), and Portuguese (Português).
  - Added an in-app **Language Selector** in the Settings screen defaulting to the system language, allowing users to override the app language on the fly.
  - Added dynamic Compose runtime configuration switching (`CompositionLocalProvider`).
- **Enhanced Number Parsing for European Locales:**
  - Support for both comma (`,`) and decimal point (`.`) input formatting across all numeric fields (SOC, battery capacity, SOH, and charge losses).

### Changed
- **Compact Single-Screen UI:**
  - Streamlined the Main Screen layout to fit entirely on a single screen without requiring scrolling.
  - Relocated Quick Presets (`80%`, `90%`, `100%`) directly below the Target Charge slider with active-state visual indicators.
  - Preserved full-sized (48dp+) touch targets and large input text fields for optimal one-handed thumb ergonomics.
  - Prominent Result Hero card showing required energy in kWh, SOC delta badge, and loss calculations.
- **Edge-to-Edge and Navigation Bar Inset Handling:**
  - Configured `enableEdgeToEdge()` with proper `safeDrawingPadding()` to ensure bottom controls are never obscured by system navigation bars.
- **Charger IP Input Usability:**
  - Configured IP address input keyboard to `KeyboardType.Uri` so the period (`.`) key is always readily available.

---

## [1.0] - 2026-08-11 (versionCode 2)

### Added
- Privacy Policy document and Play Store compliance assets.
- Target SDK updated to Android 15 (API 35).
- Constraint validation and automatic adjustment for SOC inputs.
- go-eCharger HTTP API integration and energy limit automation.
