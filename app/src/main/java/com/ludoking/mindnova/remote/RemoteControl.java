package com.ludoking.mindnova.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

/**
 * MindNova: Firebase Realtime Database listener — poora game admin/index.html se control.
 *
 * Path: sessions/{CODE}/control
 *
 * Features (panel se):
 *  1 nextDice (agla dice fix)            2 diceQueue + per-color queueFor_* (number line)
 *  3 forceDice per color                 4 luck (low/normal/high) + luckPct (%) per color
 *  5 pieceLuck per goti (4x4 grid)       6 forceSix per color
 *  7 blockSix per color                  8 extra turn button
 *  9 skip turn button                   10 kill protection per color
 * 11 auto-play (bot) per color          12 safe-all toggle
 * 13 need-6-to-open toggle              14 triple-six rule + limit
 * 15 game speed slider                  16 auto-play delay (timer UI nahi)
 * 17 force winner                       18 lock dice (pause)
 * 19 send message (toast)               20 reset / end match
 * 21 NUMBER ENGINE per color: mode auto/manual/assist/sure, target goti, "last goti stuck"
 * 22 extra-turn rules (6 par / kill par), dice min-max, blocked numbers, speed timings
 * 23 UNIVERSAL (global/control) — developer ki settings sabhi games par apne aap lage
 *
 * Firebase/net na ho to ye class nिष्क्रिय rehti hai aur game 100% normal chalta hai.
 * UI me kuch extra NAHI dikhta — sab silent.
 */
public class RemoteControl {

    public interface Handler {
        void onExtraTurn();
        void onSkipTurn();
        void onResetMatch();
        void onEndMatch();
        void onForceWin(String color);
        void onRemoteMessage(String msg);
        void onRemoteConfigChanged();
    }

    private final Handler handler;
    private final Context appCtx;
    public final RemoteConfig config = new RemoteConfig();
    /** Aakhiri roll force kiya hua tha? (forced dice par triple-six rule nahi lagega) */
    public volatile boolean lastWasForced = false;

    private DatabaseReference controlRef;
    private ValueEventListener listener;
    private DatabaseReference globalRef;
    private ValueEventListener globalListener;
    private boolean attached = false;
    private String lastMsg = "";
    private final Random rng = new Random();
    /** Developer ki universal settings — session se PEHLE lagti hain, session unhe override karta hai. */
    private volatile DataSnapshot lastGlobal = null;
    private volatile DataSnapshot lastSession = null;

    public RemoteControl(Context ctx, Handler handler) {
        this.appCtx = ctx.getApplicationContext();
        this.handler = handler;
        attach();
    }

    public boolean isAttached() {
        return attached;
    }

    // ---------------- listener ----------------

    private void attach() {
        try {
            if (!SessionManager.isFirebaseReady(appCtx)) return;
            String code = SessionManager.getSessionCode(appCtx);
            controlRef = FirebaseDatabase.getInstance()
                    .getReference("sessions").child(code).child("control");
            // pehle se bache one-shot commands saaf karo taaki purana command dobara na chale
            clearOneShots();
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    try {
                        lastSession = snap;
                        reapply();
                    } catch (Throwable ignored) {}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            controlRef.addValueEventListener(listener);

            // ---- developer ki UNIVERSAL settings (global/control) ----
            try {
                globalRef = FirebaseDatabase.getInstance().getReference("global").child("control");
                globalListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        try {
                            lastGlobal = snap;
                            reapply();
                        } catch (Throwable ignored) {}
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                };
                globalRef.addValueEventListener(globalListener);
            } catch (Throwable ignored) {}

            attached = true;
        } catch (Throwable ignored) {
            attached = false;
            controlRef = null;
        }
    }

    public void detach() {
        try {
            if (controlRef != null && listener != null) {
                controlRef.removeEventListener(listener);
            }
        } catch (Throwable ignored) {}
        try {
            if (globalRef != null && globalListener != null) {
                globalRef.removeEventListener(globalListener);
            }
        } catch (Throwable ignored) {}
        attached = false;
    }

    /**
     * Universal settings pehle, uske baad session ki settings — is tarah session
     * hamesha universal ko override karta hai (per-game tweak).
     */
    private void reapply() {
        boolean changed = false;
        try {
            config.reset();
            DataSnapshot g = lastGlobal;
            if (g != null && g.exists()) changed |= applySnapshot(g, false);
            DataSnapshot ses = lastSession;
            if (ses != null && ses.exists()) changed |= applySnapshot(ses, true);
        } catch (Throwable ignored) {}
        if (changed && handler != null) {
            try { handler.onRemoteConfigChanged(); } catch (Throwable ignored) {}
        }
    }

    private void clearOneShots() {
        try {
            controlRef.child("cmd_extraTurn").setValue(false);
            controlRef.child("cmd_skipTurn").setValue(false);
            controlRef.child("cmd_reset").setValue(false);
            controlRef.child("cmd_end").setValue(false);
            controlRef.child("forceWin").setValue("");
            controlRef.child("msg").setValue("");
        } catch (Throwable ignored) {}
    }

    // ---------------- parse ----------------

    private boolean applySnapshot(DataSnapshot s, boolean allowCommands) {
        if (s == null || !s.exists()) {
            return false;
        }
        boolean changed = false;

        int nd = intVal(s, "nextDice", config.nextDice);
        if (nd != config.nextDice) { config.nextDice = clampDice(nd); changed = true; }

        String q = strVal(s, "diceQueue", config.diceQueue);
        if (q != null && !q.equals(config.diceQueue)) { config.diceQueue = q; changed = true; }

        for (String c : RemoteConfig.COLORS) {
            int fd = intVal(s, "forceDice_" + c, config.forceDice.get(c));
            if (fd != config.forceDice.get(c)) { config.forceDice.put(c, clampDice(fd)); changed = true; }

            String lk = strVal(s, "luck_" + c, "normal");
            if (!lk.equals(config.luck.get(c))) { config.luck.put(c, lk); changed = true; }

            int lp = intVal(s, "luckPct_" + c, -1);
            if (lp != config.luckPct.get(c)) { config.luckPct.put(c, lp); changed = true; }

            int md = intVal(s, "mode_" + c, config.mode.get(c));
            if (md != config.mode.get(c)) { config.mode.put(c, md); changed = true; }

            boolean bh = boolVal(s, "blockHome_" + c, false);
            if (bh != config.blockHome.get(c)) { config.blockHome.put(c, bh); changed = true; }

            int tg = intVal(s, "targetPiece_" + c, config.targetPiece.get(c));
            if (tg != config.targetPiece.get(c)) { config.targetPiece.put(c, tg); changed = true; }

            String qf = strVal(s, "queueFor_" + c, "");
            if (!qf.equals(config.queueFor.get(c))) { config.queueFor.put(c, qf); changed = true; }

            boolean fs = boolVal(s, "forceSix_" + c, false);
            if (fs != config.forceSix.get(c)) { config.forceSix.put(c, fs); changed = true; }

            boolean bs = boolVal(s, "blockSix_" + c, false);
            if (bs != config.blockSix.get(c)) { config.blockSix.put(c, bs); changed = true; }

            boolean pr = boolVal(s, "protect_" + c, false);
            if (pr != config.protect.get(c)) { config.protect.put(c, pr); changed = true; }

            boolean au = boolVal(s, "auto_" + c, false);
            if (au != config.autoPlay.get(c)) { config.autoPlay.put(c, au); changed = true; }

            int[] pl = parsePieceLuck(strVal(s, "pieceLuck_" + c, ""));
            if (pl != null) { config.pieceLuck.put(c, pl); changed = true; }
        }

        boolean n6 = boolVal(s, "needSixToOpen", true);
        if (n6 != config.needSixToOpen) { config.needSixToOpen = n6; changed = true; }

        boolean t6 = boolVal(s, "tripleSixRule", true);
        if (t6 != config.tripleSixRule) { config.tripleSixRule = t6; changed = true; }

        int t6l = intVal(s, "tripleSixLimit", config.tripleSixLimit);
        if (t6l != config.tripleSixLimit) { config.tripleSixLimit = t6l; changed = true; }

        boolean sa = boolVal(s, "safeAll", false);
        if (sa != config.safeAll) { config.safeAll = sa; changed = true; }

        boolean e6 = boolVal(s, "extraTurnOnSix", true);
        if (e6 != config.extraTurnOnSix) { config.extraTurnOnSix = e6; changed = true; }

        boolean ek = boolVal(s, "extraTurnOnKill", true);
        if (ek != config.extraTurnOnKill) { config.extraTurnOnKill = ek; changed = true; }

        int pp = intVal(s, "pantaPieces", config.pantaPieces);
        if (pp != config.pantaPieces) { config.pantaPieces = pp; changed = true; }

        int mind = intVal(s, "minDice", config.minDice);
        if (mind != config.minDice) { config.minDice = mind; changed = true; }

        int maxd = intVal(s, "maxDice", config.maxDice);
        if (maxd != config.maxDice) { config.maxDice = maxd; changed = true; }

        String bn = strVal(s, "blockNumbers", "");
        if (!bn.equals(config.blockNumbers)) { config.blockNumbers = bn; changed = true; }

        boolean ld = boolVal(s, "lockDice", false);
        if (ld != config.lockDice) { config.lockDice = ld; changed = true; }

        double sp = dblVal(s, "gameSpeed", 1.0);
        if (sp != config.gameSpeed) { config.gameSpeed = sp; changed = true; }

        int to = intVal(s, "turnTimeout", 0);
        if (to != config.turnTimeoutSec) { config.turnTimeoutSec = to; changed = true; }

        long v;
        v = longVal(s, "stepMs", config.stepMs);
        if (v != config.stepMs) { config.stepMs = v; changed = true; }
        v = longVal(s, "stepGapMs", config.stepGapMs);
        if (v != config.stepGapMs) { config.stepGapMs = v; changed = true; }
        v = longVal(s, "openMs", config.openMs);
        if (v != config.openMs) { config.openMs = v; changed = true; }
        v = longVal(s, "homeMs", config.homeMs);
        if (v != config.homeMs) { config.homeMs = v; changed = true; }
        v = longVal(s, "diceSpinMs", config.diceSpinMs);
        if (v != config.diceSpinMs) { config.diceSpinMs = v; changed = true; }
        v = longVal(s, "diceFrameMs", config.diceFrameMs);
        if (v != config.diceFrameMs) { config.diceFrameMs = v; changed = true; }
        v = longVal(s, "turnGapMs", config.turnGapMs);
        if (v != config.turnGapMs) { config.turnGapMs = v; changed = true; }
        v = longVal(s, "killGapMs", config.killGapMs);
        if (v != config.killGapMs) { config.killGapMs = v; changed = true; }

        // ---- one-shot commands sirf session node se (universal me commands nahi) ----
        if (!allowCommands) return changed;

        if (boolVal(s, "cmd_extraTurn", false)) {
            writeBack("cmd_extraTurn", false);
            if (handler != null) try { handler.onExtraTurn(); } catch (Throwable ignored) {}
        }
        if (boolVal(s, "cmd_skipTurn", false)) {
            writeBack("cmd_skipTurn", false);
            if (handler != null) try { handler.onSkipTurn(); } catch (Throwable ignored) {}
        }
        if (boolVal(s, "cmd_reset", false)) {
            writeBack("cmd_reset", false);
            if (handler != null) try { handler.onResetMatch(); } catch (Throwable ignored) {}
        }
        if (boolVal(s, "cmd_end", false)) {
            writeBack("cmd_end", false);
            if (handler != null) try { handler.onEndMatch(); } catch (Throwable ignored) {}
        }
        String fw = strVal(s, "forceWin", "");
        if (fw != null && !fw.isEmpty() && isColor(fw)) {
            writeBack("forceWin", "");
            if (handler != null) try { handler.onForceWin(fw); } catch (Throwable ignored) {}
        }
        String msg = strVal(s, "msg", "");
        if (msg != null && !msg.isEmpty() && !msg.equals(lastMsg)) {
            lastMsg = msg;
            writeBack("msg", "");
            if (handler != null) try { handler.onRemoteMessage(msg); } catch (Throwable ignored) {}
        }
        return changed;
    }

    private void writeBack(String key, Object value) {
        try {
            if (controlRef != null) controlRef.child(key).setValue(value);
        } catch (Throwable ignored) {}
    }

    // ---------------- dice engine ----------------

    /**
     * Is color ke liye dice nikalo: forced > queue > forceSix > luck-weighted.
     * Triple-six rule MainActivity me lagta hai (turn context wahi hai).
     */
    public int rollDice(String color) {
        Integer manual = takeManualDice(color);
        if (manual != null) {
            lastWasForced = true;
            return manual;
        }
        // force six (sticky jab tak off na ho)
        if (config.isForceSix(color)) {
            lastWasForced = true;
            return 6;
        }
        // luck weighted (percent wala slider pehle, warna low/normal/high)
        lastWasForced = false;
        int v = weightedRoll(config.getColorLuck(color), config.getLuckPercent(color));
        if (config.isBlockSix(color) && v == 6) {
            v = 1 + rng.nextInt(5);
        }
        return v;
    }

    /**
     * Developer ke MANUAL number (panel se). Order:
     * nextDice > diceQueue (sabke liye) > queueFor_{color} > forceDice_{color}.
     * Kuch set nahi hai to null (matlab normal/luck/engine chalega).
     */
    public synchronized Integer takeManualDice(String color) {
        try {
            if (config.nextDice >= 1 && config.nextDice <= 6) {
                int v = config.nextDice;
                config.nextDice = 0;
                writeBack("nextDice", 0);
                return v;
            }
            int q = config.popQueue();
            if (q >= 1 && q <= 6) {
                writeBack("diceQueue", config.diceQueue);
                return q;
            }
            if (color != null) {
                int qf = config.popQueueFor(color);
                if (qf >= 1 && qf <= 6) {
                    writeBack("queueFor_" + color, config.queueFor.get(color));
                    return qf;
                }
            }
            if (color != null) {
                Integer f = config.forceDice.get(color);
                if (f != null && f >= 1 && f <= 6) {
                    config.forceDice.put(color, 0);
                    writeBack("forceDice_" + color, 0);
                    return f;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** Panel ke live hint ke liye: agla manual number kya hoga (consume kiye bina). */
    public synchronized String peekManual(String color) {
        try {
            if (config.nextDice >= 1 && config.nextDice <= 6) return String.valueOf(config.nextDice) + "*";
            int q = firstOf(config.diceQueue);
            if (q >= 1 && q <= 6) return String.valueOf(q);
            if (color != null) {
                int qf = firstOf(config.queueFor.get(color));
                if (qf >= 1 && qf <= 6) return String.valueOf(qf);
                Integer f = config.forceDice.get(color);
                if (f != null && f >= 1 && f <= 6) return String.valueOf(f) + "*";
            }
        } catch (Throwable ignored) {}
        return "";
    }

    private static int firstOf(String queue) {
        try {
            if (queue == null) return 0;
            String[] parts = queue.trim().split("[,\\s]+");
            for (String p : parts) {
                if (p.trim().isEmpty()) continue;
                return Integer.parseInt(p.trim());
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    private int weightedRoll(String luck, int percent) {
        double r = rng.nextDouble();
        double[] w;
        if (percent >= 0) {
            // 0 = sabse ghatiya (1 zyada), 50 = bilkul barabar, 100 = sabse lucky (6 zyada)
            double p = percent;
            if (p < 0) p = 0;
            if (p > 100) p = 100;
            double tilt = (p - 50.0) / 50.0;             // -1 .. +1
            w = new double[6];
            double sum = 0;
            for (int i = 0; i < 6; i++) {
                double base = 1.0;
                double pull = (i + 1 - 3.5) / 2.5;        // -1 (1) .. +1 (6)
                double val = base + tilt * pull * 2.2;    // tilt ke hisaab se jhukao
                if (val < 0.05) val = 0.05;
                w[i] = val;
                sum += val;
            }
            for (int i = 0; i < 6; i++) w[i] = w[i] / sum;
        } else if ("high".equals(luck)) {
            w = new double[]{0.05, 0.07, 0.12, 0.20, 0.25, 0.31}; // 4-6 zyada
        } else if ("low".equals(luck)) {
            w = new double[]{0.31, 0.25, 0.20, 0.12, 0.07, 0.05}; // 1-3 zyada
        } else {
            return 1 + rng.nextInt(6);
        }
        double c = 0;
        for (int i = 0; i < 6; i++) {
            c += w[i];
            if (r < c) return i + 1;
        }
        return 6;
    }

    // ---------------- value readers ----------------

    private static int clampDice(int v) {
        if (v < 0) return 0;
        if (v > 6) return 6;
        return v;
    }

    private static boolean isColor(String s) {
        return "red".equals(s) || "green".equals(s) || "blue".equals(s) || "yellow".equals(s);
    }

    private static int[] parsePieceLuck(String csv) {
        try {
            if (csv == null || csv.trim().isEmpty()) return null;
            String[] parts = csv.trim().split("[,\\s]+");
            if (parts.length < 4) return null;
            int[] out = new int[4];
            for (int i = 0; i < 4; i++) {
                int v = Integer.parseInt(parts[i].trim());
                if (v < 0) v = 0;
                if (v > 3) v = 3;
                out[i] = v;
            }
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    private static int intVal(DataSnapshot s, String key, int def) {
        try {
            Object o = s.child(key).getValue();
            if (o == null) return def;
            if (o instanceof Number) return ((Number) o).intValue();
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Throwable t) {
            return def;
        }
    }

    private static double dblVal(DataSnapshot s, String key, double def) {
        try {
            Object o = s.child(key).getValue();
            if (o == null) return def;
            if (o instanceof Number) return ((Number) o).doubleValue();
            return Double.parseDouble(String.valueOf(o).trim());
        } catch (Throwable t) {
            return def;
        }
    }

    private static boolean boolVal(DataSnapshot s, String key, boolean def) {
        try {
            Object o = s.child(key).getValue();
            if (o == null) return def;
            if (o instanceof Boolean) return (Boolean) o;
            if (o instanceof Number) return ((Number) o).intValue() != 0;
            String str = String.valueOf(o).trim().toLowerCase();
            if ("true".equals(str) || "1".equals(str) || "yes".equals(str)) return true;
            if ("false".equals(str) || "0".equals(str) || "no".equals(str)) return false;
            return def;
        } catch (Throwable t) {
            return def;
        }
    }

    private static long longVal(DataSnapshot s, String key, long def) {
        try {
            Object o = s.child(key).getValue();
            if (o == null) return def;
            if (o instanceof Number) return ((Number) o).longValue();
            return Long.parseLong(String.valueOf(o).trim());
        } catch (Throwable t) {
            return def;
        }
    }

    private static String strVal(DataSnapshot s, String key, String def) {
        try {
            Object o = s.child(key).getValue();
            if (o == null) return def;
            return String.valueOf(o);
        } catch (Throwable t) {
            return def;
        }
    }
}
