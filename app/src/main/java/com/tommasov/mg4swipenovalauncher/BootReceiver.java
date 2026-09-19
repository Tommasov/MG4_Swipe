package com.tommasov.mg4swipenovalauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }
        BootLog log = new BootLog(context);

        // Arrives before the credential storage is unlocked, and only because this receiver is
        // declared directBootAware. Timed but not acted on: SwipeService is not directBootAware
        // and could not be started here, and its preferences would not be readable yet.
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            log.recordLockedBoot();
            return;
        }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        log.recordBootCompleted();
        startSwipeService(context);
    }

    private void startSwipeService(Context context) {
        // Checked here rather than inside the service, and that is the point of moving it.
        // startForegroundService() is a contract: the service must call startForeground()
        // within five seconds or the system kills it with RemoteServiceException. The service
        // accepted that contract and then, finding it had no overlay permission, stopped
        // itself without ever calling startForeground — so a boot with the permission missing
        // produced a crash rather than a quiet no-op. Not entering the contract at all is
        // simpler than getting out of it correctly.
        if (!Settings.canDrawOverlays(context)) {
            return;
        }
        context.startForegroundService(new Intent(context, SwipeService.class));
    }
}
