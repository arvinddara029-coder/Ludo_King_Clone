package com.vinaykpro.ludoking;

import android.content.Context;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Firebase-backed controls for local/debug testing only.
 * Release builds always use the normal random dice path.
 */
public final class FirebaseTestController {
    private static volatile boolean listening = false;
    private static volatile boolean testEnabled = false;
    private static volatile boolean sixGuard = true;
    private static volatile int nextDice = 0;
    private static volatile String nextDiceId = "";
    private static volatile String consumedCommandId = "";
    private static volatile int lastRoll1 = -1;
    private static volatile int lastRoll2 = -1;
    private static volatile String jamColor = "";
    private static final Map<String, Integer> luckByColor = new HashMap<>();
    private static ListenerRegistration controlRegistration;

    private FirebaseTestController() {}

    public static int resolveDice(Context context) {
        int result = ThreadLocalRandom.current().nextInt(1, 7);

        // Controls are intentionally unavailable in release builds.
        if (!BuildConfig.DEBUG) {
            return result;
        }

        ensureListening(context.getApplicationContext());

        if (testEnabled) {
            String currentColor = MainActivity.currentPlayerColor == null ? "" : MainActivity.currentPlayerColor;

            if (nextDice >= 1 && nextDice <= 6 && !nextDiceId.equals(consumedCommandId)) {
                result = nextDice;
                consumedCommandId = nextDiceId;
            } else if (currentColor.equals(jamColor)) {
                // Test-mode "jam": force a low roll so normal move opportunities are reduced.
                result = 1;
            } else {
                Integer luck = luckByColor.get(currentColor);
                if (luck != null) {
                    result = rollWithLuck(luck);
                }
            }

            if (sixGuard && lastRoll1 == 6 && lastRoll2 == 6 && result == 6) {
                result = ThreadLocalRandom.current().nextInt(1, 6);
            }
        }

        lastRoll1 = lastRoll2;
        lastRoll2 = result;
        publishStatus(result);
        return result;
    }

    private static int rollWithLuck(int luckPercent) {
        int luck = Math.max(0, Math.min(100, luckPercent));
        // In test mode, luck% is the probability of a boosted 6; otherwise choose 1-5 normally.
        int roll = ThreadLocalRandom.current().nextInt(0, 100);
        if (roll < luck) {
            return 6;
        }
        return ThreadLocalRandom.current().nextInt(1, 6);
    }

    private static synchronized void ensureListening(Context context) {
        if (listening || !BuildConfig.DEBUG) {
            return;
        }
        listening = true;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            attachListener();
        } else {
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    attachListener();
                }
            });
        }
    }

    private static void attachListener() {
        if (controlRegistration != null) {
            controlRegistration.remove();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        controlRegistration = db.collection("ludo_test_control")
                .document("current")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }
                    applySnapshot(snapshot);
                });
    }

    private static void applySnapshot(DocumentSnapshot snapshot) {
        Boolean enabled = snapshot.getBoolean("enabled");
        Boolean guard = snapshot.getBoolean("sixGuard");
        testEnabled = enabled != null && enabled;
        sixGuard = guard == null || guard;

        Long commandedDice = snapshot.getLong("nextDice");
        nextDice = commandedDice == null ? 0 : commandedDice.intValue();
        String commandId = snapshot.getString("nextDiceId");
        nextDiceId = commandId == null ? "" : commandId;

        jamColor = snapshot.getString("jamColor");
        if (jamColor == null) {
            jamColor = "";
        }

        luckByColor.clear();
        luckByColor.put("red", valueOrDefault(snapshot.getLong("luckRed"), 0));
        luckByColor.put("green", valueOrDefault(snapshot.getLong("luckGreen"), 0));
        luckByColor.put("blue", valueOrDefault(snapshot.getLong("luckBlue"), 0));
        luckByColor.put("yellow", valueOrDefault(snapshot.getLong("luckYellow"), 0));
    }

    private static int valueOrDefault(Long value, int fallback) {
        return value == null ? fallback : Math.max(0, Math.min(100, value.intValue()));
    }

    private static void publishStatus(int dice) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            FirebaseFirestore.getInstance()
                    .collection("ludo_test_status")
                    .document("current")
                    .set(new HashMap<String, Object>() {{
                        put("playerColor", MainActivity.currentPlayerColor);
                        put("dice", dice);
                        put("testEnabled", testEnabled);
                        put("sixGuard", sixGuard);
                        put("timestamp", System.currentTimeMillis());
                    }});
        } catch (Exception ignored) {
        }
    }
}
