# EVCharge Calc (Android)

A lightweight mobile app to calculate EV charging energy requirements, built for electric vehicle owners whose cars lack built-in charge-limiting features.

## Why This App?

Many EVs (and older or basic models) do not allow setting a target charge level directly in the vehicle (e.g. stopping at 80% to preserve battery health).

If you need to configure your charging station or timer to stop at a specific amount, this app calculates the exact energy (kWh) required to reach your target State of Charge (SOC), taking battery degradation and charging losses into account.

## Features

- **SOC Input**: Set current and target SOC via sliders or direct numeric input.
- **Accurate Calculation**: Factors in usable battery capacity (kWh), State of Health (SOH degradation), and charging efficiency losses.
- **Quick Presets**: One-tap buttons for common target levels (Daily 80%, Top Up 90%, Road Trip 100%).
- **go-e Charger Integration**: Optionally push the calculated energy limit (Wh) directly to your local go-e Charger wallbox via HTTP API.
- **Persistent Settings**: Saves battery parameters and charger configuration locally.

## Platforms

- **Android**: Native Jetpack Compose & Material 3 ([evcharge-calc-android](https://github.com/sirdir1972/evcharge-calc-android))
- **iOS**: Native SwiftUI ([evcharge-calc-ios](https://github.com/sirdir1972/evcharge-calc-ios))

## License

MIT
