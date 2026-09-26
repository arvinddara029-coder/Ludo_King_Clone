package com.ludoking.mindnova.remote;

import java.util.HashMap;
import java.util.Map;

/**
 * MindNova: Firebase Realtime Database se aane wali saari remote values ka holder.
 * Ye pure-Java class hai (koi Firebase dependency nahi) taaki game offline bhi
 * default values ke saath bilkul normal chale.
 *
 * Do jagah se values aati hain:
 *   global/control              = developer ki UNIVERSAL settings (sabhi games par),
 *   sessions/{CODE}/control     = us khaas session ki settings (universal ko override karti hai)
 */
public class RemoteConfig {

    public static final String[] COLORS = {"red", "green", "blue", "yellow"};

    // ---- global dice control ----
    public volatile int nextDice = 0;          // 0=off, 1..6 = agla dice fix (one-shot)
    public volatile String diceQueue = "";     // "6,3,1" = aanewale dice ki line (one-shot, FIFO)

    // ---- per color ----
    public final Map<String, Integer> forceDice = new HashMap<>();  // 0=off (one-shot)
    public final Map<String, String> luck = new HashMap<>();        // low / normal / high
    public final Map<String, Integer> luckPct = new HashMap<>();    // 0..100 (nahi set = -1)
    public final Map<String, Boolean> forceSix = new HashMap<>();   // har baar 6
    public final Map<String, Boolean> blockSix = new HashMap<>();   // kabhi 6 nahi
    public final Map<String, Boolean> protect = new HashMap<>();    // kill protection
    public final Map<String, Boolean> autoPlay = new HashMap<>();   // bot ki tarah auto khelo
    public final Map<String, String> queueFor = new HashMap<>();    // us color ke liye number line
    public final Map<String, Integer> mode = new HashMap<>();       // 0 auto,1 manual,2 assist,3 sure
    public final Map<String, Boolean> blockHome = new HashMap<>();  // aakhri goti ghar na jaaye
    public final Map<String, Integer> targetPiece = new HashMap<>();// 0 = sab, 1..4 = us goti ko

    // ---- per goti luck: index 0..3, value 0=cursed,1=low,2=normal,3=blessed ----
    public final Map<String, int[]> pieceLuck = new HashMap<>();

    // ---- rules ----
    public volatile boolean needSixToOpen = true;   // goti kholne ke liye 6 chahiye?
    public volatile boolean tripleSixRule = true;   // 2 baar 6 ke baad teesra 1-5?
    public volatile int tripleSixLimit = 2;         // kitne 6 ke baad rokna hai
    public volatile boolean safeAll = false;        // sab khaane safe (koi kill nahi)
    public volatile boolean extraTurnOnSix = true;  // 6 par extra baari
    public volatile boolean extraTurnOnKill = true; // goti kaatne par extra baari
    public volatile int pantaPieces = 0;            // 0 = game default, warna 1..4 goti ghar

    // ---- dice range ----
    public volatile int minDice = 1;
    public volatile int maxDice = 6;
    public volatile String blockNumbers = "";       // "6,5" = ye number kabhi nahi

    // ---- game feel ----
    public volatile double gameSpeed = 1.0;         // 0.5 .. 2.5 animation speed
    public volatile int turnTimeoutSec = 0;         // 0 = timer/auto-play band (DEFAULT)
    public volatile boolean lockDice = false;       // dice freeze (pause jaisa)

    // ---- animation timings (0 = app ka default) ----
    public volatile long stepMs = 0;
    public volatile long stepGapMs = 0;
    public volatile long openMs = 0;
    public volatile long homeMs = 0;
    public volatile long diceSpinMs = 0;
    public volatile long diceFrameMs = 0;
    public volatile long turnGapMs = 0;
    public volatile long killGapMs = 0;

    public RemoteConfig() {
        reset();
    }

    public void reset() {
        nextDice = 0;
        diceQueue = "";
        for (String c : COLORS) {
            forceDice.put(c, 0);
            luck.put(c, "normal");
            luckPct.put(c, -1);
            forceSix.put(c, false);
            blockSix.put(c, false);
            protect.put(c, false);
            autoPlay.put(c, false);
            queueFor.put(c, "");
            mode.put(c, 0);
            blockHome.put(c, false);
            targetPiece.put(c, 0);
            pieceLuck.put(c, new int[]{2, 2, 2, 2});
        }
        needSixToOpen = true;
        tripleSixRule = true;
        tripleSixLimit = 2;
        safeAll = false;
        extraTurnOnSix = true;
        extraTurnOnKill = true;
        pantaPieces = 0;
        minDice = 1;
        maxDice = 6;
        blockNumbers = "";
        gameSpeed = 1.0;
        turnTimeoutSec = 0;   // timer hata diya gaya hai — default band
        lockDice = false;
        stepMs = 0;
        stepGapMs = 0;
        openMs = 0;
        homeMs = 0;
        diceSpinMs = 0;
        diceFrameMs = 0;
        turnGapMs = 0;
        killGapMs = 0;
    }

    // ---------- read helpers (hamesha safe defaults) ----------

    public String getColorLuck(String color) {
        try {
            String v = luck.get(color);
            if ("low".equals(v) || "high".equals(v) || "normal".equals(v)) return v;
        } catch (Throwable ignored) {}
        return "normal";
    }

    /** -1 = set nahi kiya (low/normal/high use hoga) */
    public int getLuckPercent(String color) {
        try {
            Integer v = luckPct.get(color);
            if (v == null) return -1;
            return v;
        } catch (Throwable ignored) {}
        return -1;
    }

    /** 0 = auto, 1 = manual, 2 = assist, 3 = sure */
    public int modeFor(String color) {
        try {
            Integer v = mode.get(color);
            if (v == null) return 0;
            if (v < 0) return 0;
            if (v > 3) return 3;
            return v;
        } catch (Throwable ignored) {}
        return 0;
    }

    public boolean isHomeBlocked(String color) {
        try {
            return Boolean.TRUE.equals(blockHome.get(color));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** 0 = sab gotiyan, 1..4 = sirf wo goti */
    public int targetFor(String color) {
        try {
            Integer v = targetPiece.get(color);
            if (v == null) return 0;
            if (v < 0) return 0;
            if (v > 4) return 4;
            return v;
        } catch (Throwable ignored) {}
        return 0;
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

    // ---------- dice range / blocked numbers ----------

    public boolean isNumberBlocked(int n) {
        try {
            if (blockNumbers == null || blockNumbers.trim().isEmpty()) return false;
            String[] parts = blockNumbers.trim().split("[,\\s]+");
            for (String p : parts) {
                if (p.trim().isEmpty()) continue;
                try {
                    if (Integer.parseInt(p.trim()) == n) return true;
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return false;
    }

    /** Min/max + "kabhi ye number mat do" — value ko theek karke wapas do. */
    public int sanitizeDice(int v) {
        int lo = minDice, hi = maxDice;
        if (lo < 1) lo = 1;
        if (hi > 6) hi = 6;
        if (hi < lo) hi = lo;
        if (v < lo) v = lo;
        if (v > hi) v = hi;
        if (isNumberBlocked(v)) {
            for (int d = lo; d <= hi; d++) {
                if (!isNumberBlocked(d)) return d;
            }
        }
        return v;
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
        if (diceQueue == null || diceQueue.trim().isEmpty()) return 0;
        int v = popFrom(diceQueue);
        diceQueue = dropFirst(diceQueue);
        return (v >= 1 && v <= 6) ? v : 0;
    }

    /** Us color ke liye number line ka pehla number (us color ke liye hi). */
    public synchronized int popQueueFor(String color) {
        try {
            String q = queueFor.get(color);
            if (q == null || q.trim().isEmpty()) return 0;
            int v = popFrom(q);
            queueFor.put(color, dropFirst(q));
            return (v >= 1 && v <= 6) ? v : 0;
        } catch (Throwable ignored) {}
        return 0;
    }

    public synchronized void clearQueues() {
        queueFor.clear();
    }

    private static int popFrom(String queue) {
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

    private static String dropFirst(String queue) {
        try {
            if (queue == null) return "";
            String[] parts = queue.trim().split("[,\\s]+");
            StringBuilder rest = new StringBuilder();
            boolean skipped = false;
            for (String p : parts) {
                if (p.trim().isEmpty()) continue;
                if (!skipped) { skipped = true; continue; }
                if (rest.length() > 0) rest.append(",");
                rest.append(p.trim());
            }
            return rest.toString();
        } catch (Throwable ignored) {}
        return "";
    }

    @Override
    public String toString() {
        return "RemoteConfig{nextDice=" + nextDice + ",queue=" + diceQueue
                + ",speed=" + gameSpeed + ",lock=" + lockDice + "}";
    }
}
