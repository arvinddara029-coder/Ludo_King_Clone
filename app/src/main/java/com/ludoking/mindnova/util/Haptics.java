package com.ludoking.mindnova.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * MindNova: chhota vibration helper (dice / kill / win feedback).
 * Har call safe hai — device me vibrator na ho to silently ignore.
 */
public final class Haptics {

    private Haptics() {}

    public static void tick(Context ctx) {
        buzz(ctx, 18);
    }

    public static void kill(Context ctx) {
        buzz(ctx, 70);
    }

    public static void win(Context ctx) {
        try {
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            long[] pattern = new long[]{0, 60, 60, 60, 60, 120};
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                //noinspection deprecation
                v.vibrate(pattern, -1);
            }
        } catch (Throwable ignored) {}
    }

    public static void buzz(Context ctx, int ms) {
        try {
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(ms, 120));
            } else {
                //noinspection deprecation
                v.vibrate(ms);
            }
        } catch (Throwable ignored) {}
    }
}
