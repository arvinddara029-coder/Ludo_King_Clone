# MindNova Ludo — Admin Panel

Ye folder ka `index.html` hi poora remote control panel hai (single file, koi build nahi).

## Kholna kaise hai

- Is file ko browser me kholo — **double-click** hi kaafi hai (internet ON rakho).
- Chaaho to kisi bhi static hosting (Firebase Hosting / Netlify / GitHub Pages) par daal do.

## Pehli baar setup (5 minute)

1. `../FIREBASE_SETUP.md` follow karke Firebase project + database + developer user banao.
2. `index.html` ke sabse neeche `<script>` me `FIREBASE_CONFIG` + `DEVELOPER_UID` bharo.
3. Panel refresh karo → **Developer Login** ya **Session Login**.

## Session code kahan se milega?

App → **Settings (⚙️)** → **Version** wali line par **3.5 sec me 5 baar tap** →
secret box me 6-letter code → copy → panel ke Session Login me paste.

Poori feature list: `../REMOTE_CONTROL_GUIDE.md` (20 features).
