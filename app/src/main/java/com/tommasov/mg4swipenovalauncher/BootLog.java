package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.annotation.NonNull;

/**
 * When the boot broadcasts actually arrived, measured from power-on.
 *
 * <p>This exists to answer one question with a number instead of a guess. On the MG4 the head
 * unit powers down completely about two minutes after the car is closed, so every journey
 * begins with a cold boot — and for roughly ten seconds after the dashboard is already on
 * screen, the swipe strips are not there and the gesture does nothing. The service is not
 * slow; nobody has told it to start yet. What is not known is how much of that delay is the
 * system taking its time to send {@code BOOT_COMPLETED}, which is a thing an app cannot
 * change, and how much could be recovered by acting on {@code LOCKED_BOOT_COMPLETED}, which
 * arrives earlier.
 *
 * <p>So both are timestamped with {@link SystemClock#elapsedRealtime()}, which counts from
 * power-on, and the two numbers are shown in the app. If they are far apart, moving to the
 * earlier broadcast is worth the work and the figure says how much it buys. If they arrive
 * together — which is plausible on a car, where there is no lock screen and credential storage
 * may be unlocked immediately — then that road is closed and nobody needs to walk down it.
 *
 * <p>Stored in device-protected preferences rather than the ordinary ones. The earlier
 * broadcast arrives before the credential-encrypted storage is available, so writing anywhere
 * else would fail exactly when there is something to record.
 */
public final class BootLog {

    private static final String PREFS = "boot_log";
    private static final String KEY_LOCKED_MS = "locked_boot_ms";
    private static final String KEY_COMPLETED_MS = "boot_completed_ms";

    private final SharedPreferences prefs;

    public BootLog(@NonNull Context context) {
        this.prefs = context.createDeviceProtectedStorageContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Called from the boot receiver, with the clock still counting from power-on. */
    public void recordLockedBoot() {
        prefs.edit()
                .putLong(KEY_LOCKED_MS, SystemClock.elapsedRealtime())
                // A new boot starts a new pair: keeping the previous BOOT_COMPLETED beside a
                // fresh LOCKED_BOOT would read as the two arriving in the wrong order.
                .remove(KEY_COMPLETED_MS)
                .apply();
    }

    public void recordBootCompleted() {
        prefs.edit().putLong(KEY_COMPLETED_MS, SystemClock.elapsedRealtime()).apply();
    }

    /** One line for the settings screen, or empty when nothing has been recorded yet. */
    @NonNull
    public String describe(@NonNull Context context) {
        long locked = prefs.getLong(KEY_LOCKED_MS, -1);
        long completed = prefs.getLong(KEY_COMPLETED_MS, -1);
        if (locked < 0 && completed < 0) {
            return "";
        }
        return context.getString(R.string.boot_timing, seconds(locked), seconds(completed));
    }

    @NonNull
    private static String seconds(long millis) {
        return millis < 0 ? "—" : String.format(java.util.Locale.getDefault(), "%.1f s",
                millis / 1000f);
    }
}
