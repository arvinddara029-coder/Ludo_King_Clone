package com.ludoking.mindnova.remote;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * MindNova: game ki live halat admin panel ko bhejo (silent telemetry).
 * Path: sessions/{CODE}/state
 * Firebase/net na ho to kuch nahi hota — game normal chalta hai.
 */
public class GameStateReporter {

    private final Context appCtx;
    private DatabaseReference stateRef;
    private long lastWriteMs = 0;
    private static final long MIN_GAP_MS = 350;

    public GameStateReporter(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        try {
            if (SessionManager.isFirebaseReady(appCtx)) {
                String code = SessionManager.getSessionCode(appCtx);
                stateRef = FirebaseDatabase.getInstance()
                        .getReference("sessions").child(code).child("state");
            }
        } catch (Throwable ignored) {
            stateRef = null;
        }
    }

    public boolean isActive() {
        return stateRef != null;
    }

    private boolean throttle() {
        long now = System.currentTimeMillis();
        if (now - lastWriteMs < MIN_GAP_MS) return true;
        lastWriteMs = now;
        return false;
    }

    private void write(Map<String, Object> map, boolean force) {
        try {
            if (stateRef == null) return;
            if (!force && throttle()) return;
            map.put("ts", ServerValue.TIMESTAMP);
            stateRef.updateChildren(map);
        } catch (Throwable ignored) {}
    }

    public void reportTurn(String color, String name, int turnNo, int playersLeft) {
        Map<String, Object> m = new HashMap<>();
        m.put("screen", "match");
        m.put("turnColor", color == null ? "" : color);
        m.put("turnName", name == null ? "" : name);
        m.put("turnNo", turnNo);
        m.put("playersLeft", playersLeft);
        write(m, true);
        SessionManager.heartbeat(appCtx, "match");
    }

    public void reportDice(String color, int value) {
        Map<String, Object> m = new HashMap<>();
        m.put("lastDiceColor", color == null ? "" : color);
        m.put("lastDice", value);
        write(m, false);
    }

    /** Jaise "R:3a0h G:2a1h B:4a0h Y:1a0h" (a=alive bahar, h=home/pakka). */
    public void reportPieces(String summary) {
        Map<String, Object> m = new HashMap<>();
        m.put("pieces", summary == null ? "" : summary);
        write(m, false);
    }

    public void reportEvent(String text) {
        Map<String, Object> m = new HashMap<>();
        m.put("lastEvent", text == null ? "" : text);
        write(m, true);
    }

    public void reportMatchEnd(String winners) {
        Map<String, Object> m = new HashMap<>();
        m.put("screen", "over");
        m.put("winners", winners == null ? "" : winners);
        write(m, true);
        SessionManager.heartbeat(appCtx, "over");
    }

    public void reportMessage(String text) {
        Map<String, Object> m = new HashMap<>();
        m.put("lastMsg", text == null ? "" : text);
        write(m, true);
    }
}
