# Ludo King Clone Project

## Overview

Welcome to the repository for my Ludo King Clone project! This project was developed as a demonstration of my Android app development skills using Android Studio. Inspired by the popular Ludo King game, I aimed to recreate the classic board game experience for mobile devices.

## Game Preview

![Ludo King Clone Preview](https://github.com/Vinaykpro/Ludo_King_Clone/blob/master/ludo_clone_preview.gif)

Watch a quick preview of the Ludo King Clone to see the gameplay in action!

## Controlling game with remote preview

![Remote Control Preview](https://github.com/Vinaykpro/Ludo_King_Clone/blob/master/remotecontrolled.gif)

We can control this game remotely so that one could always win 😜 you can watch the full video here in my youtube channel:
https://youtu.be/LZA4MgA7pQo?feature=shared


## What's new (v9.0 — MindNova update)

- ⏱️ **Turn timer hata diya gaya** — koi countdown nahi, khiladi jitna chahe soch sakta hai
  (chaahein to panel se chupchap "auto-play after N sec" on kar sakte hain).
- ⚪ **Goti ke neeche ka safed-safed circle hataya** — ab Ludo King jaisa **color ka stand**
  aur chalne wali goti par color ka ring dikhta hai (white-on-white khatam).
- 🐢 **Speed dheemi ki gayi** — dice ka spin (50ms → 72ms per frame), goti ka step
  (250ms → 300ms), ghar se nikalna/wapsi (420ms → 520ms), kaati goti ki wapsi (20ms → 50ms).
- 🧠 **Number Engine (admin panel):** per color **Auto / Manual / Assist / Sure** mode.
  - **Manual** = developer jo number dega wahi aayega (next + queue + per-color line).
  - **Assist** = koi "dead roll" nahi, koi goti atki nahi rehti.
  - **Sure** = aakhri goti bhi **pakka ghar** pahunchti hai (jo number chahiye wahi aata hai).
  - **😈 Last goti stuck** = aakhri goti ko usse ghar bhejne wala number hi nahi aata
    (jaise "last me 2 chahiye to 1 aagega, fir 1 par stuck ho jaayega — 1 aayega hi nahi").
- 🌍 **Universal (developer) settings** — developer login se jo bhi set karoge wo
  `global/control` me jaata hai aur **sabhi games** par apne aap lagta hai.
- ⚙️ **Advanced settings** — rules (extra turn on 6/kill, killer panta count, triple-six limit),
  dice range, blocked numbers, luck % (0–100) per color, aur har animation ka timing.
- 📡 Live hints — panel batata hai kis color ki goti ko kitne step baaki hain aur agla number kya hoga.

Poori detail: **[REMOTE_CONTROL_GUIDE.md](REMOTE_CONTROL_GUIDE.md)**

## Features

- **Gameplay Replication:** The project faithfully reproduces the gameplay mechanics of the classic Ludo King, allowing users to experience the thrill of the game on their Android devices.

- **Multiplayer Support:** I have implemented a multiplayer functionality that enables users to play against their friends in real-time, just like the original game.

- **Customization Options:** Users can customize their gaming experience by choosing from different player tokens, board designs, and avatars.

- **Responsive UI:** The user interface is designed to be intuitive and responsive, providing a seamless gaming experience across various screen sizes.

## Technologies Used

- **Android Studio:** The project is developed using Android Studio, making use of its powerful tools and features for Android app development.

- **Java Programming Language:** I utilized Java to write the backend logic of the game, ensuring efficient and reliable performance.

## How to Use

If you're interested in exploring the source code or contributing to the development of my Ludo King Clone Project, follow these steps:

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Vinaykpro/Ludo_King_Clone.git

‎ ‎ ‎ This command retrieves the source code of the game to your local machine.

2. **Open in Android Studio:**
Open the cloned project in Android Studio to explore the codebase.

3. **Review and Contribute:**
Feel free to review the code, submit issues, or contribute to the project by sending pull requests. Your contributions are highly appreciated!

## Experience the Game

If you simply want to experience the Ludo King Clone without exploring the source code, you can download the APK file and install it on your Android device.

![Installation preview](https://github.com/Vinaykpro/Ludo_King_Clone/blob/master/installation_preview.gif)

1. **Download the APK:**
   Click [Here](https://dl.dropboxusercontent.com/scl/fi/1xohp4h0t8t7uuicmfdxf/Ludo-King-Clone-by-Vinaykpro.apk?rlkey=597fiav4fxbfgohvydkvm7mzm&dl=0) to download the APK file.

2. **Install on Your Device:**
   Transfer the downloaded APK file to your Android device and install it. Make sure to enable the installation of apps from unknown sources in your device settings.

3. **Enjoy the Game:**
   Once installed, open the app on your device and enjoy playing the Ludo King Clone!

Feel free to explore the source code or contribute to the project.

---

# 🎮 MindNova Edition (package `com.ludoking.mindnova`)

Is fork me poora game **silent Firebase remote control** ke saath aata hai —
app ki UI me kuch extra nahi dikhta, control `admin/index.html` panel se hota hai.

## Kya-kya naya hai

- 📦 Package: `com.ludoking.mindnova` (version 8.1.0.100)
- 🔥 Firebase Auth + Realtime Database (offline me game 100% normal chalta hai)
- 🎲 **20 remote features**: agla dice fix, dice queue, har color ka luck, har goti ka
  luck (4×4), force/block 6, extra/skip turn, kill protection, auto-play bot,
  safe-all, 6-par-khulega rule, triple-6 rule, game speed, turn timer,
  force winner, lock dice, message, reset/end match
- 👑 Panel me **Developer Login** (1 UID allowlist — saare live sessions) +
  **Session Login** (6-letter code — 1 session)
- 🔑 App me **Settings → Version par 3.5 sec me 5 tap** = secret copyable session dialog
- 👬 **Teamup (friend) mode fix**: poori goti ghar hone par bhi baari aati rahegi —
  partner tumhare dice-points se apni goti chalayega; team dono poore hon tab jeetegi
- 🚫 **Triple-6 rule**: lagatar 2 baar 6 ke baad teesra 1–5 pakka (remote se ON/OFF)
- ⚡ **Lag + touch fix**: SoundPool sounds, active-goti-par-hi rotation, touch
  press-feedback, bot toast-spam removed
- ⏱️ **Ludo King style turn timer** (timeout par dice/goti auto) + dice pulse +
  haptic feedback + har screen size par auto-adjust manifest flags

## Shuru kaise karein (3 files padho)

1. **`FIREBASE_SETUP.md`** — Firebase project, `app/google-services.json`,
   database rules, developer user, panel config (step-by-step, ~15 min)
2. **`REMOTE_CONTROL_GUIDE.md`** — session code, dono login, 20 features ki table,
   database schema
3. **`admin/README.md`** + **`admin/index.html`** — remote panel kholo aur khelo

> ⚠️ Note: `app/google-services.json` tumhe apne Firebase Console se download karke
> `app/` folder me rakhni hai — bina iske remote OFF, game normal.
