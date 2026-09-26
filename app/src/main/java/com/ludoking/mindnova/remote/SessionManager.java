package com.ludoking.mindnova.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * MindNova: har device ka secret 6-character session code + Firebase heartbeat.
 *
 * - Code ek baar banta hai aur SharedPreferences me save rehta hai.
 * - Settings > Version par 5 baar tap karne se yehi code copyable dialog me dikhta hai.
 * - admin/index.html me ye code daal kar game live control hota hai.
 * - Firebase na ho / net na ho to sab kuch silently offline chalता hai.
 */
public final class SessionManager {

    private static final String PREFS = "MindNovaSession";
    private static final String KEY_CODE = "session_code";
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // confusing chars hataye

    private static String cachedCode = null;
    private static Boolean firebaseReady = null;

    private SessionManager() {}

    /** Device ka session code lao (pehli baar banao + save karo). */
    public static synchronized String getSessionCode(Context ctx) {
        if (cachedCode != null && cachedCode.length() == 6) return cachedCode;
        try {
            SharedPreferences sp = ctx.getApplicationContext()
                    .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String code = sp.getString(KEY_CODE, "");
            if (code == null || code.length() != 6) {
                code = generateCode();
                sp.edit().putString(KEY_CODE, code).apply();
            }
            cachedCode = code;
            return code;
        } catch (Throwable t) {
            if (cachedCode == null) cachedCode = generateCode();
            return cachedCode;
        }
    }

    /** Naya code chahiye to (kabhi zaroorat pade to). */
    public static synchronized String regenerateCode(Context ctx) {
        try {
            String code = generateCode();
            ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_CODE, code).apply();
            cachedCode = code;
            return code;
        } catch (Throwable t) {
            cachedCode = generateCode();
            return cachedCode;
        }
    }

    private static String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(r.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    public static String getDeviceLabel() {
        try {
            String m = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
            String model = Build.MODEL == null ? "Android" : Build.MODEL.trim();
            String label = (m + " " + model).trim();
            if (label.length() > 40) label = label.substring(0, 40);
            return label.isEmpty() ? "Android" : label;
        } catch (Throwable t) {
            return "Android";
        }
    }

    public static String getAppVersion(Context ctx) {
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionName == null ? "?" : pi.versionName;
        } catch (Throwable t) {
            return "?";
        }
    }

    /** Firebase sach me ready hai? (google-services.json + net). Result cache hota hai. */
    public static synchronized boolean isFirebaseReady(Context ctx) {
        if (firebaseReady != null) return firebaseReady;
        boolean ready = false;
        try {
            if (FirebaseApp.getApps(ctx.getApplicationContext()).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(ctx.getApplicationContext());
                } catch (Throwable ignored) {}
            }
            if (!FirebaseApp.getApps(ctx.getApplicationContext()).isEmpty()) {
                // instance ban sake to ready
                FirebaseDatabase.getInstance();
                ready = true;
            }
        } catch (Throwable ignored) {
            ready = false;
        }
        firebaseReady = ready;
        return ready;
    }

    /** Dobara check karna ho (jaise net wapas aaya) to cache reset. */
    public static synchronized void resetFirebaseCache() {
        firebaseReady = null;
    }

    private static DatabaseReference metaRef(Context ctx) {
        String code = getSessionCode(ctx);
        return FirebaseDatabase.getInstance().getReference("sessions").child(code).child("meta");
    }

    /**
     * Heartbeat: session online dikhao + current screen bhejo.
     * screen = "home" / "match" / "over" etc.
     */
    public static void heartbeat(final Context ctx, final String screen) {
        try {
            if (!isFirebaseReady(ctx)) return;
            final DatabaseReference ref = metaRef(ctx);
            Map<String, Object> meta = new HashMap<>();
            meta.put("code", getSessionCode(ctx));
            meta.put("device", getDeviceLabel());
            meta.put("app", getAppVersion(ctx));
            meta.put("pkg", ctx.getPackageName());
            meta.put("screen", screen == null ? "home" : screen);
            meta.put("status", "online");
            meta.put("lastSeen", ServerValue.TIMESTAMP);
            ref.updateChildren(meta);
            try {
                Map<String, Object> off = new HashMap<>();
                off.put("status", "offline");
                off.put("lastSeen", ServerValue.TIMESTAMP);
                ref.onDisconnect().updateChildren(off);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    /** Match ke andar ho ya nahi + mode (classic/teamup/quick/computer). */
    public static void setMatchInfo(final Context ctx, final boolean inMatch, final String mode) {
        try {
            if (!isFirebaseReady(ctx)) return;
            Map<String, Object> m = new HashMap<>();
            m.put("inMatch", inMatch);
            m.put("mode", mode == null ? "" : mode);
            m.put("lastSeen", ServerValue.TIMESTAMP);
            metaRef(ctx).updateChildren(m);
        } catch (Throwable ignored) {}
    }

    /** App band ho to offline mark (best effort). */
    public static void goOffline(final Context ctx) {
        try {
            if (!isFirebaseReady(ctx)) return;
            Map<String, Object> m = new HashMap<>();
            m.put("status", "offline");
            m.put("lastSeen", ServerValue.TIMESTAMP);
            metaRef(ctx).updateChildren(m);
        } catch (Throwable ignored) {}
    }
}
