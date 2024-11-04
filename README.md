# MarketData
An Android application providing real-time market data and historical price charts for different market assets, powered by the Fintacharts platform.

## Features
- **Real-Time Market Data**: Displays live market prices and time for selected assets, updating automatically with new data from the WebSocket API (simulated data).
- **Historical Price Chart**: Shows 30-day historical price data in a daily interval chart, allowing users to analyze asset behavior over time (real data). Built using the **MPAndroidChart** library, the chart supports zooming and panning for detailed data exploration.
- **Symbol Subscription**: Users can select and subscribe to a market asset symbol to view corresponding market data.

## Implementation Details
The app is built using the following technologies:
- **Kotlin** for development
- **Retrofit** for REST API calls
- **OkHttp WebSocket** for real-time data streaming
- **MPAndroidChart** for interactive charting
- **Kotlin Coroutines** for asynchronous processing
- **MVVM Architecture** for clean code organization

## Screenshots

|      Main View       |       Select Symbol       |
| :-------------------: | :-----------------------: |
| ![Main View](app/screenshots/MainView.png) | ![Select Symbol](app/screenshots/SelectSymbol.png) |

## Installation
1. Clone the repository: `git clone https://github.com/abetx10/MarketData.git`
2. Open the project in Android Studio
3. Run the app on an emulator or physical device

**Note**: Use the provided Fintacharts API credentials (in the test environment) for testing the app.
