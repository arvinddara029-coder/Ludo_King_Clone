# 🔥 Firebase Setup Guide (MindNova Remote Control)

App ka package: **`com.ludoking.mindnova`**
Remote panel: **`admin/index.html`**

Ye guide poora setup ~15 minute me karwa degi. Har step number-wise follow karo.

---

## Step 0 — Kya-kya chahiye

- Google account
- Android Studio (app build karne ke liye)
- Internet (app + panel dono me)

---

## Step 1 — Firebase project banao

1. [Firebase Console](https://console.firebase.google.com/) kholo → **Add project**.
2. Project name kuch bhi do, jaise `ludo-mindnova` → Continue → Google Analytics ON/OFF (koi farak nahi) → **Create project**.

---

## Step 2 — Android app jodo + `google-services.json`

1. Project khulne par **Android icon (🤖)** dabao — "Add an Android app".
2. **Android package name:** bilkul ye likho (chhota-bada same):
   ```
   com.ludoking.mindnova
   ```
3. App nickname: `Ludo King` (optional). **SHA-1:** khaali chhod sakte ho (zaroori nahi).
4. **Register app** → **`google-services.json` Download** karo.
5. Ye file is repo me yahan rakho (exact naam, exact jagah):
   ```
   app/google-services.json
   ```
6. Console me **Next → Next → Continue to console** dabao.

> ⚠️ Bina `google-services.json` ke app **banegi aur chalegi**, lekin remote control
> OFF rahega (game 100% normal chalega). File rakhte hi rebuild par remote ON.

---

## Step 3 — Realtime Database banao

1. Firebase Console → left menu → **Build → Realtime Database** → **Create Database**.
2. Location: `asia-southeast1` (India ke paas, fast) ya default US → **Enable**.
3. Database banne ke baad **Rules** tab kholo → ye rules paste karo
   (yehi rules repo me `database-rules.json` me bhi hain):

   ```json
   {
     "rules": {
       "sessions": {
         ".indexOn": ["meta/lastSeen"],
         "$code": {
           ".read": true,
           ".write": true
         }
       }
     }
   }
   ```

4. **Publish** dabao.

> 🔐 Note: ye rules session-code wale control ke liye khule rakhe hain (code jiske
> paas, control uske paas). Code 6-letter secret hai — kisi se share mat karo.

---

## Step 4 — Developer login (Authentication, sirf 1 user)

1. Console → **Build → Authentication** → **Get started** → **Sign-in method** tab.
2. **Email/Password** → **Enable** → Save.
3. **Users** tab → **Add user** → apna developer email + strong password → Add user.
   - ⚠️ **Bas ye 1 user banao.** Panel sirf isi 1 UID ko developer manega.
4. User row par click karo → **User UID** copy kar lo (Step 5 me lagega).

---

## Step 5 — Web app config + `admin/index.html` bharo

1. Console → ⚙️ **Project Settings** (left-bottom gear) → **Your apps** → **Web (`</>`)** icon.
2. Nickname: `mindnova-panel` → Register (hosting ki zaroorat nahi).
3. Jo `firebaseConfig` code mile, uski values copy karo. `databaseURL` me dhyaan do —
   ye Realtime Database wala URL hona chahiye, jaise:
   ```
   https://ludo-mindnova-default-rtdb.firebaseio.com
   ```
   (Agar config me `databaseURL` na ho to database ka URL khud likh do —
   Realtime Database page ke top par dikhta hai.)
4. Repo me **`admin/index.html`** kholo → sabse neeche `<script>` me ye block bharo:

   ```js
   const FIREBASE_CONFIG = {
     apiKey: "...",
     authDomain: "...",
     databaseURL: "...",
     projectId: "...",
     storageBucket: "...",
     messagingSenderId: "...",
     appId: "..."
   };
   const DEVELOPER_UID = "Step-4-wala-UID";
   ```

5. File save karo. Panel taiyaar!

---

## Step 6 — App build karo aur test karo

1. Android Studio me project kholo → **Sync** → **Run** (APK banao ya direct install).
2. App me **Settings (⚙️) → Version wali line par 3.5 sec me 5 baar tap** →
   secret box me **6-letter SESSION CODE** + **CONNECTED** dikhna chahiye.
   - `Code copy` dabao.
3. Computer/browser me `admin/index.html` kholo (double-click hi kaafi hai, net ON rakho).
4. **Session Login** → code paste → **Connect Session** → live game + saare controls dikhenge.
5. **Developer Login** → Step-4 wala email+password → saare live sessions ki list.

---

## 🛠️ Musibat? (Troubleshooting)

| Problem | Ilaaj |
|---|---|
| App me `Firebase: OFFLINE` | Net check karo; `app/google-services.json` sahi jagah + sahi package hai? Rebuild karo. |
| Panel me "config baaki hai" | `admin/index.html` me `FIREBASE_CONFIG` paste nahi hua. |
| Developer login: "ye account developer nahi hai" | `DEVELOPER_UID` galat hai — Authentication → user → UID dobara copy karo. |
| Session code "nahi mila" | App khuli + net ON hona chahiye; code capital me likho; database Rules publish hue? |
| Panel me data nahi badal raha | Database Rules me `.read/.write: true` + Publish; ad-blocker/VPN off karke dekho. |
| Build fail: `google-services.json is missing` | File `app/` folder me rakho (naam exact), phir Sync + Rebuild. |

---

## 📁 Files ka hisaab

| File | Kaam |
|---|---|
| `app/google-services.json` | Firebase Android config (tumhari wali file yahan) |
| `admin/index.html` | Remote control panel (developer + session login) |
| `database-rules.json` | Realtime Database rules (copy-paste ready) |
| `REMOTE_CONTROL_GUIDE.md` | 20 features + database schema + session instructions |
| `app/src/.../remote/` | App ke andar silent Firebase code (UI me kuch nahi dikhta) |
