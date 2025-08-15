# EVCharge Calc

A mobile app for calculating EV charging energy requirements and timing.

## Purpose

This app is designed for **Electric Vehicle owners whose cars do not have built-in charge limiting features**. Many older EVs or basic models don't allow you to set a target charge level (like 80%) directly in the vehicle. 

Instead, EV owners need to:
1. Calculate how much energy is needed to reach their desired charge level
2. Configure their EVSE (Electric Vehicle Supply Equipment/charging station) accordingly
3. Monitor and stop charging manually when the target is reached

## Features

- **Precise SOC Input**: Set current and target State of Charge (SOC) using sliders or direct text input
- **Energy Calculation**: Calculate exact kWh needed based on your battery specifications
- **Battery Configuration**: Customize settings for:
  - Battery capacity (kWh)
  - State of Health (SOH) - accounts for battery degradation
  - Charging losses - accounts for charging inefficiencies
- **Quick Presets**: Common charging scenarios (Daily 20-80%, Road Trip 20-100%, Top-up 60-90%)

## Why This App?

If your EV can automatically stop at 80% charge, you don't need this app. But if you have to manually configure your charging station or timer to stop at a specific energy amount, this app calculates exactly how much energy you need.

## Platforms

- iOS: SwiftUI native app
- Android: Jetpack Compose native app

## License

[Choose your license - MIT recommended]
