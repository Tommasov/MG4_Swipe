package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends a measurement to the author's probe, because on this head unit there is nowhere else
 * for it to go.
 *
 * <p>Lifted from MG4 Simple Launcher's class of the same name, and it exists for the same
 * reason: there is no text field on board to paste into, nothing accepts a share — the
 * firmware ships the Bluetooth stack with {@code profile_supported_opp} false, which disables
 * the one activity handling {@code ACTION_SEND} — and there is no adb in a car. A report that
 * cannot be uploaded can only be photographed off the screen.
 *
 * <p><b>This class belongs to the diagnostic branch and must not reach master.</b> MG4 Swipe
 * has never asked for {@code INTERNET}, and that is worth something to the people who install
 * it: an app that draws two invisible strips and cannot reach the network is easy to trust.
 * The permission is here so that a launch experiment run in a moving car can be sent back in
 * one press instead of transcribed from a photograph, and for no other reason.
 *
 * <p>The receiving end accepts a report and does nothing else: post the text with the write
 * key and the app it came from, and the answer is one line, {@code OK <app>/<name>} or
 * {@code ERR <reason>}. Nothing can read a report back over HTTP, by anybody, which is what
 * makes it safe to carry the write key inside an APK.
 */
public final class ProbeReport {

    private static final int TIMEOUT_MS = 20_000;

    /** The probe refuses an empty or wrong key with a 403 and no explanation. */
    private static final int HTTP_FORBIDDEN = 403;

    public interface Callback {
        /** Stored, under the name the probe gave it. */
        void onSent(@NonNull String reportName);

        /** Not stored. The reason is meant to be shown: it is the only clue there is. */
        void onFailed(@NonNull String reason);
    }

    private static final ExecutorService SENDER = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ProbeReport() {
    }

    /**
     * Whether this build can send at all. The key lives in the git-ignored
     * {@code apikeys.properties}, so a fresh clone builds without one and the button is simply
     * not offered rather than offered and broken.
     */
    public static boolean isConfigured() {
        return !BuildConfig.PROBE_KEY.isEmpty() && !BuildConfig.PROBE_URL.isEmpty();
    }

    /** Uploads off the main thread; the callback always lands back on it. */
    public static void send(@NonNull Context context, @NonNull String note,
                            @NonNull String text, @NonNull Callback callback) {
        SENDER.execute(() -> {
            try {
                String body = "k=" + encode(BuildConfig.PROBE_KEY)
                        + "&app=" + encode(BuildConfig.PROBE_APP)
                        + "&note=" + encode(headed(note))
                        + "&text=" + encode(text);
                String answer = post(BuildConfig.PROBE_URL, body);
                if (answer.startsWith("OK")) {
                    String name = answer.substring(2).trim();
                    MAIN.post(() -> callback.onSent(name));
                } else {
                    String reason = answer.startsWith("ERR") ? answer.substring(3).trim() : answer;
                    MAIN.post(() -> callback.onFailed(reason));
                }
            } catch (Exception e) {
                MAIN.post(() -> callback.onFailed(describe(e)));
            }
        });
    }

    /** One line so a report can be told apart in the list without opening it. */
    @NonNull
    private static String headed(@NonNull String typed) {
        String head = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") — "
                + Build.MANUFACTURER + " " + Build.MODEL;
        String trimmed = typed.trim();
        return trimmed.isEmpty() ? head : head + " — " + trimmed;
    }

    @NonNull
    private static String post(@NonNull String url, @NonNull String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream()
                    : connection.getInputStream();
            String answer = read(stream);
            if (status == HTTP_FORBIDDEN) {
                return "ERR the probe refused the key";
            }
            return answer.isEmpty() ? "ERR empty answer (HTTP " + status + ")" : answer;
        } finally {
            connection.disconnect();
        }
    }

    @NonNull
    private static String read(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = stream.read(chunk)) > 0) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toString("UTF-8").trim();
    }

    @NonNull
    private static String encode(@NonNull String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    @NonNull
    private static String describe(@NonNull Exception e) {
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
