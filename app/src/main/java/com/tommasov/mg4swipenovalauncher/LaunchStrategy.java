package com.tommasov.mg4swipenovalauncher;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * The ways of bringing the target app forward, so that one drive can compare them.
 *
 * <p>The problem this exists to settle: after returning to the home launcher, re-opening the
 * target with a swipe makes you wait — a second, three, five — while returning to it with the
 * simulated back is instant every time, however fast it is done. Two routes to the same app,
 * one immediate and one not, which means the app is alive and warm and the cost is in the
 * route rather than in the app.
 *
 * <p>The route changed once already. Before June 2026 the intent carried
 * {@code REORDER_TO_FRONT}, the flag whose whole job is "bring forward what is already there",
 * and it was replaced by {@code NEW_TASK | SINGLE_TOP} on the suspicion that the implicit
 * {@code RESET_TASK_IF_NEEDED} was the delay. The loader arrived in the same commit and hid
 * the symptom, so the substitution was never judged.
 *
 * <p>And it can only be judged in the car. The head unit's ROM is the only place the delay
 * exists; on an emulator every route is instantaneous, which is why a year of trying produced
 * nothing. That makes one-hypothesis-per-trip the most expensive way possible to work, so all
 * four live here at once and the choice is a button.
 */
public enum LaunchStrategy {

    /** What the app does today. */
    SINGLE_TOP("SINGLE_TOP",
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP),

    /** What it did before June 2026, and the closest thing to what the back button does. */
    REORDER("REORDER_TO_FRONT",
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),

    /** The untouched default of getLaunchIntentForPackage(), i.e. tapping the icon. */
    RESET("RESET_IF_NEEDED",
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),

    /** Nothing but the flag a service is obliged to pass, as a control. */
    PLAIN("NEW_TASK only", Intent.FLAG_ACTIVITY_NEW_TASK),

    /**
     * Empties the task and starts it again, instead of bringing the existing one forward.
     *
     * <p>Added after the emulator finally reproduced the delay and named it: leaving the app
     * with BACK and re-opening it takes 155 ms, leaving it with HOME and re-opening it takes
     * 2300 ms, from the same launcher, to the same app, with the same flags. BACK finishes the
     * activity so there is no task to move; HOME keeps it, and moving a task that was
     * backgrounded in the last ten seconds is what costs the two seconds.
     *
     * <p>So this makes every launch look like the fast one. The price is the app's state:
     * a browser reloads its page, a launcher does not care. Worth measuring before choosing.
     */
    CLEAR("CLEAR_TASK",
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    public final String label;
    public final int flags;

    LaunchStrategy(@NonNull String label, int flags) {
        this.label = label;
        this.flags = flags;
    }

    @NonNull
    public LaunchStrategy next() {
        LaunchStrategy[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    @NonNull
    public static LaunchStrategy byName(String name) {
        if (name != null) {
            for (LaunchStrategy strategy : values()) {
                if (strategy.name().equals(name)) {
                    return strategy;
                }
            }
        }
        return SINGLE_TOP;
    }
}
