# PromptPay — iOS Real Device Installation Guide

## Prerequisites

- macOS 13 or later
- Xcode 15 or later (download from the [Mac App Store](https://apps.apple.com/app/xcode/id497799835))
- An Apple ID (free or paid developer account)
- iPhone running iOS 16.0 or later
- A USB cable (Lightning or USB-C depending on your iPhone model)

---

## Step 1: Open the Project in Xcode

1. Open **Finder** and navigate to `letmakemoney/ios/`
2. Double-click `PromptPay.xcodeproj` to open it in Xcode
3. Wait for Xcode to finish indexing (progress bar in the top toolbar)

> **Note:** If Xcode shows package resolution errors, go to **File → Packages → Resolve Package Versions**

---

## Step 2: Set Your Development Team (Apple ID)

1. In the **Project Navigator** (left sidebar), click on `PromptPay` (the blue Xcode project icon at the top)
2. Click on the `PromptPay` **target** (under TARGETS)
3. Click the **Signing & Capabilities** tab
4. Under **Team**, click the dropdown and sign in with your Apple ID if needed, then select your personal team
5. Xcode will automatically generate a provisioning profile

> **Free Apple ID users:** You can still install on your own device, but the app will expire after **7 days** and you'll need to reinstall. A paid Apple Developer account (\$99/year) gives you a 1-year certificate.

---

## Step 3: Connect Your iPhone

1. Plug your iPhone into your Mac using a USB cable
2. Unlock your iPhone
3. If prompted on your iPhone: tap **Trust** and enter your passcode
4. In Xcode's toolbar (top), click the device selector (next to the play/stop buttons)
5. Your iPhone should appear in the list — select it

---

## Step 4: Trust the Developer Profile on iPhone (first time only)

If you're using a **free Apple ID**, iOS will block the app by default:

1. On your iPhone, go to **Settings → General → VPN & Device Management**
2. Tap your Apple ID email under "Developer App"
3. Tap **Trust "[your email]"**
4. Tap **Trust** again on the confirmation prompt
5. The app will now launch normally

---

## Step 5: Build and Run

1. In Xcode, press **⌘ + R** (or click the ▶ Play button in the toolbar)
2. Xcode will compile the app and install it on your iPhone
3. The app will launch automatically on your device

> **Tip:** First build may take 2–5 minutes. Subsequent builds are faster.

---

## Troubleshooting

### ❌ "No signing certificate" / "Failed to create provisioning profile"
- Make sure you selected a **Team** in Signing & Capabilities
- Go to **Xcode → Settings → Accounts** and add your Apple ID
- Try **Product → Clean Build Folder** (⇧⌘K), then build again

### ❌ "Could not launch — untrusted developer"
- Follow Step 4 above to trust the developer profile on your iPhone

### ❌ "Device is locked"
- Unlock your iPhone before building

### ❌ "Provisioning profile expired" (free accounts)
- Free Apple ID provisioning profiles expire every **7 days**
- Simply rebuild and reinstall from Xcode (⌘R) to renew

### ❌ Xcode shows build errors about missing files
- Go to **Product → Clean Build Folder** (⇧⌘K)
- Then **File → Packages → Resolve Package Versions**
- Then build again (⌘R)

### ❌ App crashes immediately on launch
- Check that you are running iOS 16.0 or later
- Check Xcode's debug console (⇧⌘C) for error messages

### ❌ "bundle identifier is already in use"
- Change the Bundle Identifier in Signing & Capabilities to something unique,
  e.g. `com.yourname.promptpay`

---

## App Features Notes

- **SMS Parsing** requires SMS messages from supported mobile money services (MTN MoMo, Airtel Money, M-Pesa, etc.)
- **Face ID / Touch ID** will be requested on first unlock attempt
- **Supabase Sync** requires internet — the app works fully offline and syncs when connected
- **Font**: The app uses the Urbanist font family. If `.ttf` files are absent, SwiftUI gracefully falls back to the system font

---

## Project Structure

```
ios/
├── PromptPay.xcodeproj/      ← Open this in Xcode
├── PromptPay/
│   ├── App/                  ← Entry point (PromptPayApp.swift)
│   ├── Models/               ← Data models
│   ├── ViewModels/           ← Business logic (MainViewModel)
│   ├── Views/                ← SwiftUI screens
│   ├── Services/             ← SMS parsing, auth, sync, storage
│   └── Info.plist            ← App permissions & metadata
├── project.yml               ← XcodeGen project definition
└── INSTALL.md                ← This file
```

---

## Support

For issues, open a GitHub issue at [github.com/daniel7nrk/letmakemoney](https://github.com/daniel7nrk/letmakemoney).
