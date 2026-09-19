package com.tommasov.mg4swipenovalauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1000;
    private static final int REQUEST_CODE_ACCESSIBILITY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
    }

    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else if (!isAccessibilityServiceEnabled(this, AccService.class)) {
            requestAccessibilityPermission();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void requestOverlayPermission() {
        setContentView(R.layout.activity_permission_overlay);
        Button grantOverlayPermissionButton = findViewById(R.id.buttonGrantOverlayPermission);
        grantOverlayPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        });
    }

    private void requestAccessibilityPermission() {
        setContentView(R.layout.activity_permission_accessibility);
        Button grantAccessibilityPermissionButton = findViewById(R.id.buttonGrantAccessibilityPermission);
        grantAccessibilityPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY_PERMISSION);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkPermissions();
    }

    /**
     * Whether our accessibility service is switched on.
     *
     * <p>Compared as components rather than as strings, and that is the fix rather than the
     * tidying. The setting is a colon-separated list of flattened component names, and the
     * same service may legitimately appear in it in two spellings: the long
     * {@code pkg/pkg.AccService} that the system's own Settings screen writes, or the short
     * {@code pkg/.AccService} that is equally valid and that anything setting the value by
     * hand is likely to use. A string comparison sees those as different, so the app went on
     * asking for a permission the system had already granted — with the Accessibility screen
     * showing the service as On, which makes it a maddening thing to be told.
     *
     * <p>{@link ComponentName#unflattenFromString} understands both forms and expands the
     * leading dot, so the comparison is between what the two names mean rather than how they
     * were typed.
     */
    private boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        ComponentName wanted = new ComponentName(context, service);
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) {
            return false;
        }
        for (String enabledService : enabledServices.split(":")) {
            ComponentName enabled = ComponentName.unflattenFromString(enabledService.trim());
            if (wanted.equals(enabled)) {
                return true;
            }
        }
        return false;
    }
}
