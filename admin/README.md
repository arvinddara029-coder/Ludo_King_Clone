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

## Naya kya hai (v9.0)

- **🧠 Number Engine** — har color ke liye mode: Auto / Manual / Assist / Sure.
  - Manual = "agla number kya aayega" aap decide karo (Next dice, Dice queue, per-color line).
  - Sure = aakhri goti **pakka ghar** (jo number chahiye wahi).
  - Assist = koi dead roll nahi (koi goti atki nahi).
- **😈 Aakhri goti stuck** — us color ki teesri goti tak sab kaat do, par aakhri goti ko ghar
  bhejne wala number hi nahi aayega (`blockHome_<color>`).
- **🎨 Luck %** — per color slider (0 = sabse ghatiya, 50 = barabar, 100 = sabse lucky).
- **🌍 Universal Settings** (developer login) — `global/control` me save hote hain aur
  **bina session code, sabhi games** par live lag jaate hain. "Universal → sabhi live sessions"
  button abhi chal rahe games me turant bhi bhej deta hai.
- **⚙️ Advanced** — game speed, auto-play delay (0 = band), aur har animation ka timing
  (goti ka step, dice spin, ghar wapsi, kill gap).
- **📡 Live hints** — panel dikhata hai kis color ki goti ko kitne step baaki hain
  (`state/needs`) aur agla number kya hoga (`state/nextHint`).

Poori feature list: `../REMOTE_CONTROL_GUIDE.md` (25+ features + advanced timings + schema).

> ⚠️ Database rules me `global/control` ka read/write khula hona chahiye
> (`database-rules.json` me already hai) — warna universal settings kaam nahi karengi.
