package com.ludoking.mindnova.remote;

import java.util.HashMap;
import java.util.Map;

/**
 * MindNova: Firebase Realtime Database se aane wali saari remote values ka holder.
 * Ye pure-Java class hai (koi Firebase dependency nahi) taaki game offline bhi
 * default values ke saath bilkul normal chale.
 *
 * Database path: sessions/{CODE}/control
 */
public class RemoteConfig {

    public static final String[] COLORS = {"red", "green", "blue", "yellow"};

    // ---- global dice control ----
    public volatile int nextDice = 0;          // 0=off, 1..6 = agla dice fix (one-shot)
    public volatile String diceQueue = "";     // "6,3,1" = aanewale dice ki line (one-shot, FIFO)

    // ---- per color ----
    public final Map<String, Integer> forceDice = new HashMap<>();  // 0=off (one-shot)
    public final Map<String, String> luck = new HashMap<>();        // low / normal / high
    public final Map<String, Boolean> forceSix = new HashMap<>();   // har baar 6
    public final Map<String, Boolean> blockSix = new HashMap<>();   // kabhi 6 nahi
    public final Map<String, Boolean> protect = new HashMap<>();    // kill protection
    public final Map<String, Boolean> autoPlay = new HashMap<>();   // bot ki tarah auto khelo

    // ---- per goti luck: index 0..3, value 0=cursed,1=low,2=normal,3=blessed ----
    public final Map<String, int[]> pieceLuck = new HashMap<>();

    // ---- rules ----
    public volatile boolean needSixToOpen = true;   // goti kholne ke liye 6 chahiye?
    public volatile boolean tripleSixRule = true;   // 2 baar 6 ke baad teesra 1-5?
    public volatile boolean safeAll = false;        // sab khaane safe (koi kill nahi)

    // ---- game feel ----
    public volatile double gameSpeed = 1.0;         // 0.5 .. 2.5 animation speed
    public volatile int turnTimeoutSec = 15;        // 0 = timer off
    public volatile boolean lockDice = false;       // dice freeze (pause jaisa)

    public RemoteConfig() {
        reset();
    }

    public void reset() {
        nextDice = 0;
        diceQueue = "";
        for (String c : COLORS) {
            forceDice.put(c, 0);
            luck.put(c, "normal");
            forceSix.put(c, false);
            blockSix.put(c, false);
            protect.put(c, false);
            autoPlay.put(c, false);
            pieceLuck.put(c, new int[]{2, 2, 2, 2});
        }
        needSixToOpen = true;
        tripleSixRule = true;
        safeAll = false;
        gameSpeed = 1.0;
        turnTimeoutSec = 15;
        lockDice = false;
    }

    // ---------- read helpers (hamesha safe defaults) ----------

    public String getColorLuck(String color) {
        try {
            String v = luck.get(color);
            if ("low".equals(v) || "high".equals(v)) return v;
        } catch (Throwable ignored) {}
        return "normal";
    }

    /** 0=cursed, 1=low, 2=normal, 3=blessed */
    public int getPieceLuck(String color, int pieceIndex) {
        try {
            int[] arr = pieceLuck.get(color);
            if (arr != null && pieceIndex >= 0 && pieceIndex < arr.length) {
                int v = arr[pieceIndex];
                if (v >= 0 && v <= 3) return v;
            }
        } catch (Throwable ignored) {}
        return 2;
    }

    public boolean isAuto(String color) {
        try {
            return Boolean.TRUE.equals(autoPlay.get(color));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isProtected(String color) {
        try {
            return Boolean.TRUE.equals(protect.get(color));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isForceSix(String color) {
        try {
            return Boolean.TRUE.equals(forceSix.get(color));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isBlockSix(String color) {
        try {
            return Boolean.TRUE.equals(blockSix.get(color));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Animation duration ko isse divide karo. Clamp 0.5..2.5 */
    public double speedScale() {
        double s = gameSpeed;
        if (s < 0.5) s = 0.5;
        if (s > 2.5) s = 2.5;
        return s;
    }

    public long scaled(long baseMs) {
        return Math.max(30L, (long) (baseMs / speedScale()));
    }

    // ---------- one-shot queue ----------

    /** Queue ka pehla number nikaalo (1..6) ya 0. Queue andar hi update ho jaati hai. */
    public synchronized int popQueue() {
        try {
            if (diceQueue == null) return 0;
            String[] parts = diceQueue.trim().split("[,\\s]+");
            for (int i = 0; i < parts.length; i++) {
                String p = parts[i].trim();
                if (p.isEmpty()) continue;
                int v = Integer.parseInt(p);
                // baaki queue wapas jod do
                StringBuilder rest = new StringBuilder();
                for (int j = i + 1; j < parts.length; j++) {
                    if (parts[j].trim().isEmpty()) continue;
                    if (rest.length() > 0) rest.append(",");
                    rest.append(parts[j].trim());
                }
                diceQueue = rest.toString();
                if (v >= 1 && v <= 6) return v;
                return 0;
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    @Override
    public String toString() {
        return "RemoteConfig{nextDice=" + nextDice + ",queue=" + diceQueue
                + ",speed=" + gameSpeed + ",lock=" + lockDice + "}";
    }
}
