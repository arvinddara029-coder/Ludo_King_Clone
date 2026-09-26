# 🎮 Remote Control Guide (MindNova)

**App ki UI me kuch extra NAHI dikhta.** Poora game `admin/index.html` panel se
Firebase Realtime Database ke through silent control hota hai.

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

### 🎮 Session Login
- App se copy kiya **6-letter code** paste karo → **Connect Session**.
- Koi password nahi chahiye — code hi password hai.
- Sirf usi 1 session ka control khulega.

---

## 3. 20 Remote Features (sab live, silent)

| # | Feature | Kya hota hai | Database key (`control/`) |
|---|---|---|---|
| 1 | **Agla Dice Fix** | Agla dice 1–6 me se pakka wahi aayega (1 baar) | `nextDice` |
| 2 | **Dice Queue** | Aanewale dice ki line, jaise `6,6,3,1` (FIFO) | `diceQueue` |
| 3 | **Color Force Dice** | Laal/Hara/Neela/Peela ka agla dice fix (1 baar) | `forceDice_red` … |
| 4 | **Color Luck** | Low = 1-3 zyada, High = 4-6 zyada (dice ka wazan) | `luck_red` … |
| 5 | **Goti Luck (4×4)** | Har goti alag: Cursed / Low / Normal / Blessed — bot+autoplay isi hisaab se goti chunte hain | `pieceLuck_red` … (`"0,1,2,3"`) |
| 6 | **Force 6** | Us color ka har dice 6 aayega (jab tak ON) | `forceSix_red` … |
| 7 | **Block 6** | Us color ka kabhi 6 nahi aayega | `blockSix_red` … |
| 8 | **Extra Turn** | Chal rahi baari me 1 extra dice turant | `cmd_extraTurn` |
| 9 | **Skip Turn** | Chal rahi baari turant agli baari par | `cmd_skipTurn` |
| 10 | **Kill Protection** | Wo color kabhi marega nahi (khaana unsafe ho tab bhi) | `protect_red` … |
| 11 | **Auto-play (bot)** | Wo color bot ki tarah khud khelega — dice + goti dono | `auto_red` … |
| 12 | **Safe All** | Poore board par koi kill nahi (sab safe) | `safeAll` |
| 13 | **6-par-khulega rule** | OFF = kisi bhi dice par goti ghar se bahar | `needSixToOpen` |
| 14 | **Triple-6 rule** | ON = lagatar 2 baar 6 ke baad teesra 1–5 pakka | `tripleSixRule` |
| 15 | **Game Speed** | 0.5x–2.5x — goti move + circle + dice sab tez/dheemi | `gameSpeed` |
| 16 | **Turn Timer** | Har baari ke second (0 = band) — time out par dice/goti auto | `turnTimeout` |
| 17 | **Force Winner** | Kisi bhi color ko turant jeeta do | `forceWin` |
| 18 | **Lock Dice** | Dice freeze — koi chaal nahi chalegi (pause jaisa) | `lockDice` |
| 19 | **Message** | Game me toast message dikhao | `msg` |
| 20 | **Reset / End Match** | Match dobara shuru ya khatm karke home | `cmd_reset`, `cmd_end` |

**One-shot vs Sticky:**
- One-shot (1 baar chalke auto-clear): nextDice, queue item, forceDice_color, extra/skip/reset/end, forceWin, msg.
- Sticky (jab tak OFF na karo): luck, goti luck, forceSix, blockSix, protect, auto, safeAll, rules, speed, timer, lock.

### 📡 Live status (panel me auto-update)
Baari kaunsi, aakhiri dice, gotiyon ka hisaab (`R:3a1h` = 3 bahar, 1 pakki),
aakhiri event (kill / no-move / home), winners — sab `state` se live aata hai.

---

## 4. Database Schema (Realtime Database)

```
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
      lastEvent, lastMsg, winners, ts
    control/                   ← panel → app (upar wali table)
      ...20 features ki keys...
```

---

## 5. Zaroori notes

- **Offline = normal game.** Net na ho ya Firebase na jude to remote apne aap
  nishkriya — game bilkul normal chalta hai, koi error/popup nahi.
- **Forced dice** (nextDice/queue/forceDice/forceSix) par triple-6 rule lagu nahi hota —
  developer ka hukum sabse upar. Normal/luck dice par rule lagta hai.
- **Goti luck** bot + auto-play + timeout auto-move me kaam karta hai
  (khud khelne wale insaan ki ungli to hum pakad nahi sakte 😄 —
  uske liye auto-play ON kar do).
- **Teamup (friend) mode:** jis friend ki 4 goti ghar pahunch jaayein, uski baari
  phir bhi aati rahegi — uske dice-points se uska **partner apni goti chalayega**.
  Team tab jeetegi jab **dono** partners poore ho jaayein.
- Panel se **Reset** dabane par saare controls default (normal game) ho jaate hain.
