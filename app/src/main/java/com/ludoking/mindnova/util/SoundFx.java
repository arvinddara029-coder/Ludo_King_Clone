package com.ludoking.mindnova.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.ludoking.mindnova.R;

import java.util.HashMap;
import java.util.Map;

/**
 * MindNova: halki-phulki short sound effects (SoundPool).
 * Pehle har step/kill par MediaPlayer.create() hota tha — wahi sabse bada lag tha.
 * Ye class sounds ko ek baar load karke reuse karti hai.
 */
public final class SoundFx {

    public static final String STEP = "step";
    public static final String SAFE = "safe";
    public static final String DEATH = "death";
    public static final String PANTA = "panta";
    public static final String CLICK = "click";
    public static final String DICE = "dice";

    private static SoundFx instance;

    private final Context appCtx;
    private SoundPool pool;
    private final Map<String, Integer> ids = new HashMap<>();
    private volatile boolean enabled = true;

    private SoundFx(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
    }

    public static synchronized SoundFx get(Context ctx) {
        if (instance == null) {
            instance = new SoundFx(ctx);
        }
        return instance;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private synchronized void ensureLoaded() {
        if (pool != null) return;
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            pool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(attrs)
                    .build();
            ids.put(STEP, pool.load(appCtx, R.raw.step, 1));
            ids.put(SAFE, pool.load(appCtx, R.raw.safe, 1));
            ids.put(DEATH, pool.load(appCtx, R.raw.death, 1));
            ids.put(PANTA, pool.load(appCtx, R.raw.panta, 1));
            ids.put(CLICK, pool.load(appCtx, R.raw.click, 1));
            ids.put(DICE, pool.load(appCtx, R.raw.diceroll, 1));
        } catch (Throwable ignored) {
            pool = null;
        }
    }

    /** Aag-laga-do-style fire-and-forget play. Kabhi crash nahi karega. */
    public void play(String key) {
        play(key, 1.0f);
    }

    public void play(String key, float rate) {
        try {
            if (!enabled) return;
            ensureLoaded();
            if (pool == null) return;
            Integer id = ids.get(key);
            if (id == null || id == 0) return;
            float r = rate <= 0 ? 1.0f : Math.min(2.0f, rate);
            pool.play(id, 1.0f, 1.0f, 1, 0, r);
        } catch (Throwable ignored) {}
    }

    public void playStep() {
        // har kadam par halka alag pitch — Ludo King jaisa feel
        float rate = 0.94f + (float) (Math.random() * 0.12f);
        play(STEP, rate);
    }

    public synchronized void release() {
        try {
            if (pool != null) {
                pool.release();
            }
        } catch (Throwable ignored) {}
        pool = null;
        ids.clear();
    }
}
