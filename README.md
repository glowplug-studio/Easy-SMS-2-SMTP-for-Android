# Easy SMS 2 SMTP for Android

**Easy SMS 2 SMTP for Android** is an efficient and lightweight Android application that automatically listens for incoming SMS messages and instantly forwards them to an email address of your choice using an SMTP server. 

Whether you need to archive your messages, forward two-factor authentication (2FA) codes to a secondary device, or monitor a remote Android phone, this app silently does the job in the background while being incredibly battery efficient.

## Key Features

- **Automatic Forwarding**: The app listens for SMS messages at the system level. It wakes up only when an SMS arrives, processes the forwarding, and shuts back down to conserve battery.
- **Modern UI**: Built with Jetpack Compose, the app features a clean, polished Material 3 design exclusively in Dark Mode.
- **Simple Controls**: A prominent "Big Switch" on the main screen lets you instantly toggle the forwarding service ON or OFF.
- **Detailed Logs**: View a history of incoming messages right on the home screen. Logs include the sender's number, message snippet, forwarded email address, and a success/failure indicator.
- **Status Indicators**: Logs show a yellow pending icon while SMTP delivery is in progress, green on success, and red on failure. Failed items expose detailed error output with a convenient "Copy" button for troubleshooting.
- **Test Mode**: A dedicated "Test" button in the settings allows you to verify your SMTP configuration before you actually receive an SMS. It sends a "Hello World" email and provides immediate feedback.
- **No Background Drain**: It does not run a continuous background service. It is triggered by Android SMS broadcasts and processes work only when needed.

## Configuration & Setup

To use the app, you need to configure your SMTP settings. Tap the **Settings (Gear Icon)** in the top right corner of the app and provide the following:

1. **SMTP Host**: The outgoing mail server of your email provider (e.g., `smtp.gmail.com`).
2. **SMTP Port**: The port used for secure connections (e.g., `465` for SSL).
3. **SMTP Username**: Your email address or login username.
4. **SMTP Password**: Your email password.
    - *Note for Gmail Users:* If you have 2-Step Verification enabled on your Google Account, your standard password will not work. You must generate an **[App Password](https://support.google.com/accounts/answer/185833)** and paste it into this field.
5. **From Email**: The email address the forwarded message will appear to come from (usually the same as your username).
6. **To Email**: The destination email address where you want to receive the forwarded SMS.
7. **Subject Template**: The subject line for the forwarded emails. You can use the `{SENDER}` placeholder to dynamically insert the phone number of the person who sent the SMS (e.g., `New SMS from {SENDER}`).

After entering your details, you can tap **Test** to verify the connection or **Save** to apply the settings.

## Permissions Required

Upon first launch, the app will request the following permissions to function correctly:
- **Receive SMS**: Required to intercept incoming text messages and read the message body from that broadcast.

## Building the Project

This project is built using modern Android development tools:
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Database**: Room (for persisting message logs)
- **Preferences**: DataStore (for storing SMTP settings locally)
- **Email**: JavaMail API for Android

To build the app:
1. Clone or download the repository.
2. Open the project in **Android Studio**.
3. Sync the project with Gradle files.
4. Build and Run the app on an emulator or physical device running Android 7.0 (API 24) or higher.

---

*Developed by [Glowplug](https://glowplug.studio) in Thailand.*

## 🤖 AI Generated
This application was entirely scaffolded and developed with the assistance of **Gemini 3 Pro Preview**, demonstrating the capabilities of advanced AI models in autonomous software engineering, full-stack Android development, and modern UI implementation using Jetpack Compose.
