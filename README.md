# SafeSync

SafeSync is a personal safety Android application built with Kotlin and Jetpack Compose. It aims to provide peace of mind to users and their loved ones by offering a suite of safety features. In today's world, knowing that your friends and family are safe is more important than ever. SafeSync addresses this by providing a simple and effective way to stay connected and request help in case of an emergency.

## Features

*   **Real-time Location Sharing:** Allows users to share their live location with a pre-selected list of trusted contacts. The location is updated in real-time, providing an accurate view of the user's whereabouts. This is useful for letting family know you're on your way home, or for friends to find each other in a crowded place.

*   **Emergency SOS:** A prominent SOS button allows the user to immediately send an emergency alert to all their safety contacts. This alert includes a predefined message and a link to the user's current location on Google Maps, ensuring that help can be dispatched quickly and accurately.

*   **Contact Management:** A dedicated screen for managing safety contacts. Users can easily add new contacts from their phone's address book, or enter them manually. Existing contacts can be edited or removed. This ensures the user's emergency contact list is always up-to-date.

*   **User Profile & Authentication:** Secure user authentication using Firebase Authentication (Email/Password and Google Sign-In). Users can manage their profile information, including their name and phone number, ensuring their identity is correctly associated with their safety alerts.

*   **Material Design 3 & Jetpack Compose:** The entire UI is built using Jetpack Compose and follows the Material Design 3 guidelines, providing a modern, clean, and intuitive user experience that is consistent with the latest Android platform conventions.

*   **Firebase Integration:** Leverages the power of the Firebase suite. Firebase Authentication for user management, and Firebase Realtime Database to store user data, contacts, and location information in a synchronized, real-time manner.

*   **Google Play Services:** Integrates with Google Play Services for robust and battery-efficient location tracking and authentication.

## How It Works

SafeSync is built around a client-server architecture using Firebase as the backend.

*   **Frontend:** The Android app is built with modern Android development techniques. The UI is purely declarative, written in Jetpack Compose. The architecture follows the recommended guide, using ViewModels to separate logic from UI and to manage the UI state.

*   **Backend:** Firebase Authentication handles user sign-up and sign-in securely. Firebase Realtime Database is used to store all application data, such as user profiles, contact lists, and shared location data, which are synchronized in real-time across devices.

*   **Location Services:** The app uses the Fused Location Provider API from Google Play Services to get the device's location efficiently and accurately, with minimal impact on battery life.

