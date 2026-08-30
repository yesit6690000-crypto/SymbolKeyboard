# Symbol Keyboard (Android)

A real Android custom keyboard (IME) that types symbols in place of letters,
plus a live "decode strip" above the keys that shows the plain English of
what you've typed near the cursor.

## What's included
- `SymbolCipher.kt` — the letter↔symbol map (your "code language"). Edit this
  file to invent your own cipher; everything else reads from it automatically.
- `SymbolIME.kt` — the actual keyboard: builds the key rows, commits symbols
  into whatever app has focus, and refreshes the decode strip on every key.
- `SetupActivity.kt` — a launcher screen with two buttons: enable the keyboard
  in system settings, then switch to it.

## How to build it
1. Install **Android Studio** (free, from developer.android.com).
2. Unzip this project, then in Android Studio: **File → Open** → select the
   `SymbolKeyboard` folder.
3. Let Gradle sync (first run downloads dependencies — needs internet).
4. Plug in a device (USB debugging on) or start an emulator.
5. Click **Run ▶**. This installs the app.

## How to enable and use it on the phone
1. Open the "Symbol Keyboard" app you just installed.
2. Tap **Step 1: Enable keyboard** → toggle "Symbol Keyboard" on in the list
   → confirm the security dialog.
3. Tap **Step 2: Switch keyboard**, or long-press the space bar in any app's
   text field, and pick "Symbol Keyboard".
4. Open any text field. The keys show a symbol (big) with the real letter
   (small) underneath. Tapping a key types the symbol. The strip above the
   keys shows the decoded plain-English text.

## Customizing the cipher
Open `SymbolCipher.kt` and change the right-hand side of `letterToSymbol`.
Nothing else needs to change — the keyboard UI and the decoder both derive
from that one map. Symbols can be more than one character if you want
(e.g. multi-character glyphs); `decode()` already checks 2-character chunks
before falling back to 1.

## Notes / next steps if you want to go further
- Add a Shift/Caps key and a numbers layer (currently letters-only).
- Persist a user-editable cipher (let people design their own code inside
  the app, save it with SharedPreferences, and load it in `SymbolCipher`).
- Add haptic feedback (`android.os.VibrationEffect`) per key press.
- Style the keys with a proper `Drawable` background instead of default
  `Button` styling if you want a nicer look.
- Publishing to Google Play requires a signed release build and a developer
  account — the debug build above is only for your own device/testing.
