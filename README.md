# Daily Wisdom - Native Android App

A native Android application that displays daily inspirational quotes with features for saving favorites, daily notifications, and search functionality.

## Features

- **Random Quotes**: Get a new inspirational quote with a single tap
- **Quote of the Day**: A special quote that changes daily
- **Favorites**: Save your favorite quotes for later reference
- **Search**: Search through all quotes or just your favorites
- **Notifications**: Daily notifications to inspire your day
- **Copy to Clipboard**: Easily share quotes with others
- **Dark Theme**: Beautiful dark theme for comfortable reading

## Technical Details

- Built with Kotlin
- MVVM Architecture
- Room Database for local storage
- LiveData and ViewModel for reactive UI
- AdMob integration for banner and rewarded ads
- WorkManager for scheduling notifications

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Update the AdMob IDs in `AdManager.kt` with your own IDs
4. Build and run the app

## Project Structure

- **data/**: Contains database, repository, and model classes
- **viewmodel/**: Contains ViewModel classes for each screen
- **adapter/**: Contains RecyclerView adapters
- **notification/**: Contains notification handling logic
- **ads/**: Contains AdMob integration
- **worker/**: Contains WorkManager implementation

## License

This project is licensed under the MIT License - see the LICENSE file for details.
