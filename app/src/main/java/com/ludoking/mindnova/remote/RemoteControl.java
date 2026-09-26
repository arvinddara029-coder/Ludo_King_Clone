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
 *  1 nextDice (agla dice fix)            2 diceQueue (dice ki line)
 *  3 forceDice per color                 4 luck per color (low/normal/high)
 *  5 pieceLuck per goti (4x4 grid)       6 forceSix per color
 *  7 blockSix per color                  8 extra turn button
 *  9 skip turn button                   10 kill protection per color
 * 11 auto-play (bot) per color          12 safe-all toggle
 * 13 need-6-to-open toggle              14 triple-six rule toggle
 * 15 game speed slider                  16 turn timer seconds
 * 17 force winner                       18 lock dice (pause)
 * 19 send message (toast)               20 reset / end match
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
    private boolean attached = false;
    private String lastMsg = "";
    private final Random rng = new Random();

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
                        applySnapshot(snap);
                    } catch (Throwable ignored) {}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            controlRef.addValueEventListener(listener);
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
        attached = false;
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

    private void applySnapshot(DataSnapshot s) {
        if (s == null || !s.exists()) {
            return;
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

        boolean sa = boolVal(s, "safeAll", false);
        if (sa != config.safeAll) { config.safeAll = sa; changed = true; }

        boolean ld = boolVal(s, "lockDice", false);
        if (ld != config.lockDice) { config.lockDice = ld; changed = true; }

        double sp = dblVal(s, "gameSpeed", 1.0);
        if (sp != config.gameSpeed) { config.gameSpeed = sp; changed = true; }

        int to = intVal(s, "turnTimeout", 15);
        if (to != config.turnTimeoutSec) { config.turnTimeoutSec = to; changed = true; }

        if (changed && handler != null) {
            try { handler.onRemoteConfigChanged(); } catch (Throwable ignored) {}
        }

        // ---- one-shot commands (turant consume) ----
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
        // 1) global next dice (one-shot)
        if (config.nextDice >= 1 && config.nextDice <= 6) {
            int v = config.nextDice;
            config.nextDice = 0;
            writeBack("nextDice", 0);
            lastWasForced = true;
            return v;
        }
        // 2) queue (FIFO)
        int q = config.popQueue();
        if (q >= 1 && q <= 6) {
            writeBack("diceQueue", config.diceQueue);
            lastWasForced = true;
            return q;
        }
        // 3) per-color force (one-shot)
        try {
            Integer f = config.forceDice.get(color);
            if (f != null && f >= 1 && f <= 6) {
                config.forceDice.put(color, 0);
                writeBack("forceDice_" + color, 0);
                lastWasForced = true;
                return f;
            }
        } catch (Throwable ignored) {}
        // 4) force six (sticky jab tak off na ho)
        if (config.isForceSix(color)) {
            lastWasForced = true;
            return 6;
        }
        // 5) luck weighted
        lastWasForced = false;
        int v = weightedRoll(config.getColorLuck(color));
        if (config.isBlockSix(color) && v == 6) {
            v = 1 + rng.nextInt(5);
        }
        return v;
    }

    private int weightedRoll(String luck) {
        double r = rng.nextDouble();
        double[] w;
        if ("high".equals(luck)) {
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
