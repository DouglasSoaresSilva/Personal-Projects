# △ Gravity Falls Cipher Encoder

A **Kotlin + Jetpack Compose** Android app that encodes and decodes the 3 classic ciphers from the TV series *Gravity Falls* (by Alex Hirsch), with aesthetics inspired by [thisisnotawebsitedotcom.com](https://thisisnotawebsitedotcom.com/).

## 📋 Table of Contents

- [Theme System](#-automatic-dark--light-theme)
- [Implemented Ciphers](#-implemented-ciphers)
- [Project Structure](#-project-structure)
- [Installation & Setup](#-installation--setup)
- [Features](#-features)
- [Quick Tests](#-quick-tests)

---

## 🎨 Automatic Dark & Light Theme

The app **automatically follows your device's system theme** via `isSystemInDarkTheme()` — there is **no manual toggle button**:

### Dark Mode (Bill's Terminal)
- **Background**: Black (`#000000`)
- **Text**: Phosphor green (`#00FF41`)
- **Effect**: CRT terminal aesthetic with scanlines
- **Activated when**: Device is in dark mode

### Light Mode (Dipper's Journal)
- **Background**: Parchment (`#F3EAD3`)
- **Text**: Dark brown
- **Highlights**: Journal red and burned gold
- **Activated when**: Device is in light mode

**How to test:** Change your device/emulator theme in Settings → Display → Theme, and the app will update automatically without restart.

---

## 🔐 Implemented Ciphers

| Cipher | How It Works | Example |
|---|---|---|
| **Caesar** | Each letter shifted 3 positions. Encode = +3 (A→D), Decode = −3 (D→A) | `WELCOME TO GRAVITY FALLS` ⇄ `ZHOFRPH WR JUDYLWB IDOOV` |
| **Atbash** | Reversed alphabet: A↔Z, B↔Y, C↔X… (symmetric) | `WELCOME` ⇄ `DVOXLNV` |
| **A1Z26** | Letter → number: A=1 … Z=26. `/` = space | `WELCOME` ⇄ `23 5 12 3 15 13 5` |

### Cipher Details

#### Caesar Cipher
- Shifts each letter by a fixed offset (default: 3, as per the show)
- Preserves case (uppercase/lowercase)
- Non-alphabetic characters are preserved unchanged
- Wraps around alphabet (e.g., Z+1 = A)

#### Atbash Cipher
- **Symmetric**: encoding and decoding use the same operation
- Alphabet mapping: A↔Z, B↔Y, C↔X, etc.
- Preserves case and non-alphabetic characters
- Useful for simple obfuscation

#### A1Z26 Cipher
- **Encoding**: Each letter converts to its position (A=1, B=2, ... Z=26)
- **Decoding**: Numbers convert back to letters
- Separator: Space, comma, or hyphen between numbers
- Special: `/` represents a space in the plaintext
- Tolerant parsing: accepts multiple separators and ignores invalid characters

---

## 📁 Project Structure

```
AppGravityFalls/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/gravityfalls/codificador/
        │   ├── MainActivity.kt              # Activity + setContent() entry point
        │   ├── ciphers/Ciphers.kt           # Pure cipher logic (Caesar, Atbash, A1Z26)
        │   ├── history/HistoryStore.kt      # Conversion history (SharedPreferences + JSON)
        │   └── ui/
        │       ├── CipherScreen.kt          # Main UI + history display
        │       └── theme/Theme.kt           # Dark CRT + Light Journal color palettes
        └── res/
            ├── values/strings.xml
            └── values/themes.xml
```

### Key Modules

- **MainActivity.kt**: Entry point, initializes Compose UI
- **Ciphers.kt**: Stateless functions for all three cipher operations
- **HistoryStore.kt**: Persistence layer using SharedPreferences + JSON serialization
- **CipherScreen.kt**: Main Composable with input field, cipher selection, and result display
- **Theme.kt**: Color definitions for light/dark modes

---

## 🚀 Installation & Setup

### Via Android Studio

1. Open **Android Studio**
2. Click *File* → *Open* → Select the `AppGravityFalls` folder
3. Wait for Gradle sync to complete (uses AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.10.00)
4. Run on an emulator or physical device (minSdk 26)

### Via Command Line

With Android SDK installed:

```powershell
cd AppGravityFalls
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.gravityfalls.codificador/.MainActivity
```

### Requirements

- **Android SDK**: API 26 (Android 8.0) or higher
- **Kotlin**: 2.0.21+
- **Compose**: 2024.10.00 BOM

---

## ✨ Features

### Core Functionality
- **Encode / Decode Toggle**: Switch between modes with a red-styled button (inspired by the website)
- **Cipher Selection**: Cards with `[●]` style (terminal-like appearance)
- **Auto-Conversion**: Updates result as you type, with an optional manual button
- **Multi-Action Buttons**:
  - **EXAMPLE**: Loads `WELCOME TO GRAVITY FALLS` and its encoded variants
  - **CLEAR**: Clears all input fields
  - **SWAP** (⇄): Moves the result to input, inverts the mode
  - **COPY RESULT**: Copies to clipboard

### Conversion History
- **Automatic Saving**: Every encode/decode action is logged (max 50 entries, newest first)
- **Persistence**: History survives app restart (stored in SharedPreferences)
- **Reuse**: Tap a history item to restore the cipher, mode, and input
- **Delete Single**: Trash icon on each history card
- **Clear All**: Button with confirmation dialog to wipe history
- **Display**: Shows input → output for quick reference

### Input Tolerance
- **Caesar & Atbash**:
  - Preserves case (uppercase/lowercase)
  - Ignores punctuation, numbers, and special characters
  - Only transforms alphabetic characters
- **A1Z26**:
  - Accepts numbers separated by spaces, commas, or hyphens
  - Tolerates extra whitespace
  - `/` represents a space in decoded text
  - Silently skips invalid characters

---

## ✅ Quick Tests

### Caesar Cipher
- **Decode**: `ZHOFRPH WR JUDYLWB IDOOV` → `WELCOME TO GRAVITY FALLS`
- **Encode**: `WELCOME TO GRAVITY FALLS` → `ZHOFRPH WR JUDYLWB IDOOV`
- **Mixed case**: `Welcome` → `Zhofrph`

### Atbash Cipher
- **Encode**: `WELCOME` → `DVOXLNV`
- **Decode**: `DVOXLNV` → `WELCOME` (symmetric)
- **With punctuation**: `Hello, World!` → `Svool, Dliow!`

### A1Z26 Cipher
- **Encode**: `WELCOME` → `23 5 12 3 15 13 5`
- **Decode**: `23 5 12 3 15 13 5` → `WELCOME`
- **Sentence**: `23 5 12 3 15 13 5 / 20 15 / 7 18 1 22 9 20 25 / 6 1 12 12 19` → `WELCOME TO GRAVITY FALLS`
- **Alternative separators**: `23-5-12-3-15-13-5` or `23,5,12,3,15,13,5` (both work)

---

## 📝 Troubleshooting

### App doesn't switch themes
- Ensure your device theme setting is active (not forcing light/dark in app settings)
- Check that `isSystemInDarkTheme()` is being called in the Compose recomposition

### History not persisting
- Verify app has Write permissions in device settings
- Check that SharedPreferences is not being cleared by system

### A1Z26 decode fails
- Ensure numbers are separated by space, comma, or hyphen
- Check that numbers are in range 1-26
- Use `/` for spaces in the plaintext (not a space character)

---

## 📦 Dependencies

- **Kotlin**: 2.0.21
- **Jetpack Compose**: 2024.10.00
- **Android Gradle Plugin**: 8.5.2
- **MinSDK**: 26
- **CompileSDK**: 35

---

## 📖 References

- [Gravity Falls Wiki](https://gravityfalls.fandom.com/)
- [thisisnotawebsitedotcom.com](https://thisisnotawebsitedotcom.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Cryptography](https://developer.android.com/training/articles/keystore)

---

> ⚠️ **DO NOT TRUST THE TRIANGLE.** Reality is an illusion, the universe is a hologram, buy gold, bye!
