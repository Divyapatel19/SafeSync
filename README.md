# Emergency SOS, Safety & Location Tracking App
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

## Screenshots
| Login Screen | Register Screen                                                       |
|---------------|-----------------------------------------------------------------------|
| <img src="screenshot/login.jpg" width="250" height="400"> | <img src="screenshot/register.jpg" width="250" height="400">          |
| Home Screen | Alone Mode                                                            |
| <img src="screenshot/home.jpg" width="250" height="400"> | <img src="screenshot/alonemode.jpg" width="250" height="400">         |
| Feature Screen | Emergency SOS Screen                                                  |
| <img src="screenshot/feture.jpg" width="250" height="400"> | <img src="screenshot/emergencysos.jpg" width="250" height="400">      |
| Profile Screen | Contact Screen                                                        |
| <img src="screenshot/profile.jpg" width="250" height="400"> | <img src="screenshot/manage%20contact%20.jpg" width="250" height="400"> |
| Edit Profile Screen | Medical Info Screen                                                   |
| <img src="screenshot/edit%20profile.jpg" width="250" height="400"> | <img src="screenshot/medical%20infor.jpg" width="250" height="400">   |
| Privacy & Safety Screen | Help & Support Screen                                                 |
| <img src="screenshot/privacy%20and%20saty.jpg" width="250" height="400"> | <img src="screenshot/help%20and%20support.jpg" width="250" height="400"> |


## How It Works
SafeSync is built around a client-server architecture using Firebase as the backend.
*   **Frontend:** The UI is built entirely with **Jetpack Compose**, providing a reactive and modern user interface. The app follows Google's recommended architecture, using **ViewModels** to manage UI state and interact with the backend.
*   **Backend:** **Firebase** is used for all backend needs. **Firebase Authentication** handles secure user login, while **Firebase Realtime Database** stores user data, contacts, and location information, ensuring all data is synchronized in real-time.
*   **Location Services:** The app utilizes the **Fused Location Provider API** from Google Play Services for efficient and accurate location tracking with minimal battery impact.



