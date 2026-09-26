# 🎮 Remote Control Guide (MindNova)

**App ki UI me kuch extra NAHI dikhta.** Poora game `admin/index.html` panel se
Firebase Realtime Database ke through silent control hota hai.

> 🆕 **v9.0 me naya:** turn timer hata diya gaya, goti ki speed dheemi, goti ke
> neeche ka safed circle hataya (Ludo King jaisa color stand), aur panel me
> **number engine + universal (developer) settings + advanced settings** aa gaye.

---

## 1. Session Code kaise nikale (App se)

1. App kholo → Home screen par **Settings (⚙️)** dabao.
2. Settings me sabse neeche **Version** wali line par **3.5 second ke andar 5 baar tap** karo.
3. Secret box khulega — **6-letter SESSION CODE** (bade capital letters).
4. **Code copy** dabao (ya text ko select karke copy karo — dono kaam karte hain).
5. Box me **Firebase: CONNECTED / OFFLINE** bhi dikhta hai (net + setup status).

> 🔑 Code jiske paas, game uske haath me. Code kisi se share mat karo.

## 2. Panel me Login (2 tareeke)

**`admin/index.html` browser me kholo (net ON), phir:**

### 👑 Developer Login
- Firebase **Authentication me banaya hua 1 email + password** daalo.
- Sirf wahi account chalega jiska **UID** `admin/index.html` me `DEVELOPER_UID` me set hai.
- Login ke baad **jitne bhi sessions live chal rahe hain, sabki list** dikhegi —
  kisi bhi session par **Control** dabakar uska game chalao.
- **🌍 Universal Settings** card bhi milega — yahan ki koi bhi setting **bina session
  code daale, sabhi games par** apne aap lag jaayegi (app `global/control` padhta hai).

### 🎮 Session Login
- App se copy kiya **6-letter code** paste karo → **Connect Session**.
- Koi password nahi chahiye — code hi password hai.
- Sirf usi 1 session ka control khulega.

---

## 3. Remote Features (sab live, silent)

### 🎲 Dice

| # | Feature | Kya hota hai | Database key (`control/` me) |
|---|---|---|---|
| 1 | **Agla Dice Fix** | Agla dice 1–6 me se pakka wahi (1 baar) | `nextDice` |
| 2 | **Dice Queue** | Aanewale dice ki line, jaise `6,6,3,1` (FIFO) | `diceQueue` |
| 3 | **Per-color number line** | Sirf ek color ke liye number line, jaise Blue ko `2,1,1` | `queueFor_blue` … |
| 4 | **Color Force Dice** | Us color ka agla dice fix (1 baar) | `forceDice_red` … |
| 5 | **Luck %** | 0 = sabse ghatiya (1 zyada) • 50 = barabar • 100 = sabse lucky (6 zyada) | `luckPct_red` … (`-1` = band) |
| 6 | **Color Luck preset** | Low / Normal / High (dice ka wazan) | `luck_red` … |
| 7 | **Force 6** | Us color ka har dice 6 (jab tak ON) | `forceSix_red` … |
| 8 | **Block 6** | Us color ka kabhi 6 nahi | `blockSix_red` … |
| 9 | **Dice range** | Sirf kis range ke number aayein, jaise 2–5 | `minDice`, `maxDice` |
| 10 | **Blocked numbers** | Ye number kabhi kisi ko nahi, jaise `6,5` | `blockNumbers` |

### 🧠 Number Engine (naya — "agla number kya aayega")

Har color ke liye mode chuno:

| Mode | Kya karta hai | Key |
|---|---|---|
| **Auto** | Bilkul normal random (jitna pehle tha) | `mode_red = 0` |
| **Manual** | Sirf developer ke diye number chalte hain (next / queue / forceDice) | `mode_red = 1` |
| **Assist** | Random jaisa lagta hai, par **dead roll kabhi nahi** — jab koi goti nahi chal sakti to wo number aa hi nahi sakta. Ghar pahunchane wala number aa sakta hai. | `mode_red = 2` |
| **Sure 🏁** | **Pakka:** agar koi goti isi dice se ghar pahunch sakti hai to wahi number; warna 6. Aakhri goti bhi **atakti nahi** — ghar chali jaati hai. | `mode_red = 3` |

Extra engine keys:

| Feature | Kya hota hai | Key |
|---|---|---|
| **Target goti** | Engine sirf Goti 1/2/3/4 par dhyan de | `targetPiece_red` (0 = sab) |
| **Aakhri goti stuck 😈** | Us color ki **aakhri** goti ko ghar pahunchane wala number **kabhi nahi** milega — wo ghar ke raste par ruk jaati hai (jaise "1 aayega hi nahi"). Baaki gotiyan normally khelti hain. | `blockHome_red = true` |
| **Pakka ghar 🏁** | Ulta — aakhri goti zaroor ghar pahunchti hai | `blockHome_red = false` + `mode_red = 3` |
| **Live hint** | Panel live batata hai kis color ki goti ko kitne step baaki hain (`state/needs`) aur agla best number kya hoga (`state/nextHint`) — `*` matlab one-shot force | `state/needs`, `state/nextHint` |

> 😈 **Stuck wala scene:** Mode = Sure + "Aakhri goti ghar na jaaye" ON → teesri goti tak
> sab kaat deta hai, par aakhri goti ko ghar bhejne wala number hi nahi aata — bilkul
> jaise user ne kaha: *"last me 2 chahiye to 1 aagega, fir 1 par stuck ho jaayega, 1 aayega hi nahi."*

### 🎨 Color / goti

| # | Feature | Kya hota hai | Key |
|---|---|---|---|
| 11 | **Goti Luck (4×4)** | Har goti alag: Cursed / Low / Normal / Blessed — bot + auto-play isi hisaab se goti chunte hain | `pieceLuck_red` … (`"0,1,2,3"`) |
| 12 | **Kill Protection** | Wo color kabhi marega nahi | `protect_red` … |
| 13 | **Auto-play (bot)** | Wo color bot ki tarah khud khelega — dice + goti dono | `auto_red` … |

### ⚡ Commands

| # | Feature | Key |
|---|---|---|
| 14 | **Extra Turn** (turant ek dice) | `cmd_extraTurn` |
| 15 | **Skip Turn** | `cmd_skipTurn` |
| 16 | **Force Winner** (turant jeeta do) | `forceWin` |
| 17 | **Lock Dice** (pause jaisa) | `lockDice` |
| 18 | **Message** (game me toast) | `msg` |
| 19 | **Reset / End Match** | `cmd_reset`, `cmd_end` |

### 📏 Rules

| # | Feature | Default | Key |
|---|---|---|---|
| 20 | **6-par-khulega rule** | ON | `needSixToOpen` |
| 21 | **Triple-6 rule** | ON (2 baar 6 ke baad teesra 1–5) | `tripleSixRule`, `tripleSixLimit` |
| 22 | **Sab khaane safe** | OFF | `safeAll` |
| 23 | **6 par extra baari** | ON | `extraTurnOnSix` |
| 24 | **Kill par extra baari** | ON | `extraTurnOnKill` |
| 25 | **Jeetne ke liye kitni gotiyan ghar** | 0 = game default (classic 4, quick 1) | `pantaPieces` |

### ⚙️ Advanced — Speed + Timing

Timer UI **hata diya gaya hai** (koi countdown nahi). Sirf "auto-play delay" bacha hai —
0 rakha to game kabhi khud nahi chalega.

| Feature | Default (app) | Key |
|---|---|---|
| **Game speed** (0.5x–2.5x, sab kuch) | 1.0 | `gameSpeed` |
| **Auto-play delay** (second, 0 = band) | **0** | `turnTimeout` |
| **Goti ek ghar** (ms) | 300 (pehle 250) | `stepMs` |
| **Do ghar ke beech gap** (ms) | 300 (pehle 230) | `stepGapMs` |
| **Ghar se bahar nikalna** (ms) | 520 (pehle 420) | `openMs` |
| **Kaati goti ki ghar wapsi** (ms) | 520 (pehle 420, chhoti walk 20ms → 30ms) | `homeMs` |
| **Dice frame** (ms — badi = dheemi) | 72 (pehle 50) | `diceFrameMs` |
| **Dice poora spin** (ms) | 620 (pehle 380) | `diceSpinMs` |
| **Baari badalne ka gap** (ms) | 300 (pehle 230) | `turnGapMs` |
| **Kill ke baad gap** (ms) | 300 | `killGapMs` |

`0` matlab "app ka default chalao". Kyunki ab default hi dheema hai, sirf badalna ho tab bharo.

---

## 4. Universal (developer) settings — sab games par

Developer login karke **🌍 Universal Settings** card me jo bhi badloge wo `global/control`
me jaata hai, aur app use **har game me** padhta hai:

```
global/
  control/            ← universal settings (same keys, isi table wale)
sessions/
  {CODE}/control/     ← us session ki settings (universal ko OVERRIDE karti hai)
```

- **Priority:** pehle `global/control`, uske baad `sessions/{CODE}/control`. Jo session
  me set hai wahi jeetega — isliye universal default rakhne ke liye perfect hai.
- Panel me **📤 Universal → sabhi live sessions par bhej do** dabakar abhi chal rahe
  sessions me turant bhi bhej sakte ho.
- Commands (extra turn / skip / reset / end / forceWin / msg) sirf session me chalte hain —
  universal me nahi (warna har game me chale jaate).

---

## 5. Database Schema (Realtime Database)

```
global/
  control/                     ← developer ki universal settings (v9.0 se)

sessions/
  {CODE}/                      ← 6-letter session code (jaise K7M2PQ)
    meta/
      code, device, app, pkg
      screen                   ← home / match / over
      inMatch, mode            ← classic / teamup / quick / computer
      status                   ← online / offline
      lastSeen                 ← timestamp (heartbeat)
    state/                     ← app → panel (live)
      turnColor, turnName, turnNo, playersLeft
      lastDice, lastDiceColor
      pieces                   ← "R:3a0h G:2a1h ..."
      needs                    ← "R:12,5,-1,-1 G:30,-1,-1,-1"  (ghar tak kitne step baaki)
      nextHint                 ← "R:5 G:6 B:- Y:2"           (agla best / forced number)
      lastEvent, lastMsg, winners, ts
    control/                   ← panel → app (upar wali tables)
```

Database rules (`database-rules.json`) me `global/control` ke liye bhi read/write
khula hai — bina uske universal settings kaam nahi karengi.

---

## 6. UI / game feel me kya badla (v9.0)

- ❌ **Turn timer poori tarah hata diya** — na text, na bar, na countdown. Khiladi jitna
  chahe soch sakta hai (default `turnTimeout = 0`).
- ⚪ **Goti ke neeche ka safed-safed circle hata diya** — ab goti ke neeche us color ka
  **Ludo King jaisa gehra "stand"** dikhta hai (ghar me), aur chalne wali goti par
  safed ki jagah **color ka ring** ghoomta hai. Board par khadi goti ka stand chhup jaata hai.
- 🐢 **Speed dheemi:** dice ka spin (50ms/frame → 72ms/frame), goti ka step (250ms → 300ms),
  ghar se nikalna/wapsi (420ms → 520ms), kaati goti ki walk (20ms → 30ms per ghar).
- 🔋 **Lag fix:** 16 gotiyon ki endless rotation ki jagah ab sirf 2 chakkar (timer na hone
  ki wajah se battery/lag bachta hai).

## 7. Zaroori notes

- **Offline = normal game.** Net na ho ya Firebase na jude to remote apne aap
  nishkriya — game bilkul normal chalta hai, koi error/popup nahi.
- **Forced dice** (nextDice/queue/queueFor/forceDice/forceSix/engine) par triple-6 rule
  lagu nahi hota — developer ka hukum sabse upar. Normal/luck dice par rule lagta hai.
- **Goti luck** bot + auto-play + auto-play-delay wale auto-move me kaam karta hai
  (khud khelne wale insaan ki ungli to hum pakad nahi sakte 😄 — uske liye auto-play ON kar do).
- **Teamup (friend) mode:** jis friend ki 4 goti ghar pahunch jaayein, uski baari
  phir bhi aati rahegi — uske dice-points se uska **partner apni goti chalayega**.
  Team tab jeetegi jab **dono** partners poore ho jaayein.
- Panel se **Reset** dabane par saare controls default (normal game) ho jaate hain.
