package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A short log that survives the app being closed or killed.
 *
 * <p>Written for the launch experiment, and the reason it has to be written down at all is
 * that the thing being measured happens while nobody can look. The delay appears on the head
 * unit and nowhere else, in a car, between one swipe and the next — so a figure shown on
 * screen and then lost is a figure nobody will ever read. These lines are read back afterwards,
 * parked, or sent as a report.
 *
 * <p>Preferences rather than a file: a few short lines appended tens of times, with no stream
 * to keep open and nothing to flush at a moment when the process may be about to be killed.
 */
public final class Journal {

    private static final String PREFS = "journal";
    private static final String SEPARATOR = "\n";

    private final SharedPreferences prefs;
    private final String key;
    private final int maxLines;
    private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public Journal(@NonNull Context context, @NonNull String name, int maxLines) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.key = name;
        this.maxLines = maxLines;
    }

    /** Records one line, stamped with the time it happened. */
    public void add(@NonNull String line) {
        List<String> all = lines();
        all.add(clock.format(new Date()) + "  " + line);
        while (all.size() > maxLines) {
            all.remove(0);
        }
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                joined.append(SEPARATOR);
            }
            joined.append(all.get(i));
        }
        // Committed rather than applied: what is being recorded is often the moment the screen
        // is handed to another app, and an asynchronous write is one that may not have happened
        // when this process is put to sleep.
        prefs.edit().putString(key, joined.toString()).commit();
    }

    @NonNull
    public List<String> lines() {
        String stored = prefs.getString(key, "");
        List<String> all = new ArrayList<>();
        if (stored.isEmpty()) {
            return all;
        }
        for (String line : stored.split(SEPARATOR)) {
            if (!line.isEmpty()) {
                all.add(line);
            }
        }
        return all;
    }

    public void clear() {
        prefs.edit().remove(key).commit();
    }

    /** The lines as one block, or empty when nothing has been recorded. */
    @NonNull
    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (String line : lines()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
