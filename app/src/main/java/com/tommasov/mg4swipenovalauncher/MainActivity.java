package com.tommasov.mg4swipenovalauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private PackageManager packageManager;
    private AppListAdapter adapter;
    private List<ApplicationInfo> allApps;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView explanationText = findViewById(R.id.explanation_text);
        explanationText.setText(R.string.explanation_text);

        Button sendButton = findViewById(R.id.button_send_report);
        // Offered only when this build has a key: a fresh clone shows nothing rather than a
        // button that fails.
        sendButton.setVisibility(ProbeReport.isConfigured() ? android.view.View.VISIBLE
                : android.view.View.GONE);
        sendButton.setOnClickListener(v -> askThenSend(sendButton));

        Button strategyButton = findViewById(R.id.button_launch_strategy);
        strategyButton.setText(getString(R.string.launch_strategy,
                preferencesManager.getLaunchStrategy().label));
        strategyButton.setOnClickListener(v -> {
            LaunchStrategy next = preferencesManager.getLaunchStrategy().next();
            preferencesManager.setLaunchStrategy(next);
            strategyButton.setText(getString(R.string.launch_strategy, next.label));
            // Restarted so the service picks the new strategy up without a reboot: it reads
            // the preference per launch, but the restart also makes the change visible.
            startSwipeService();
        });

        TextView launchLogText = findViewById(R.id.launch_log_text);
        String launchLog = new Journal(this, "launch", 24).describe();
        launchLogText.setText(launchLog);
        launchLogText.setVisibility(launchLog.isEmpty() ? android.view.View.GONE
                : android.view.View.VISIBLE);

        TextView bootText = findViewById(R.id.boot_timing_text);
        String bootTiming = new BootLog(this).describe(this);
        bootText.setText(bootTiming);
        bootText.setVisibility(bootTiming.isEmpty() ? android.view.View.GONE
                : android.view.View.VISIBLE);

        TextView versionText = findViewById(R.id.version_text);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.version_label, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("");
        }

        String currentPackageName = getPackageName();
        packageManager = getPackageManager();
        List<ApplicationInfo> userApps = new ArrayList<>();

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (!appInfo.packageName.equals(currentPackageName)) {
                userApps.add(appInfo);
            }
        }

        Collections.sort(userApps, (app1, app2) -> {
            String label1 = app1.loadLabel(packageManager).toString();
            String label2 = app2.loadLabel(packageManager).toString();
            return label1.compareToIgnoreCase(label2);
        });

        String selectedPackage = preferencesManager.getSelectedPackage();
        if (selectedPackage == null) {
            for (ApplicationInfo appInfo : userApps) {
                if (appInfo.packageName.equals("com.teslacoilsw.launcher")) {
                    selectedPackage = appInfo.packageName;
                    preferencesManager.saveSelectedPackage(selectedPackage);
                    break;
                }
            }
        }

        ListView listView = findViewById(R.id.app_list);
        adapter = new AppListAdapter(this, userApps, selectedPackage);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo selectedApp = userApps.get(position);
            preferencesManager.saveSelectedPackage(selectedApp.packageName);
            adapter.setSelectedPackage(selectedApp.packageName);
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, getString(R.string.selected_app, selectedApp.packageName), Toast.LENGTH_SHORT).show();
        });

        Button toggleSystemAppsButton = findViewById(R.id.toggle_system_apps_button);
        toggleSystemAppsButton.setOnClickListener(v -> {
            adapter.toggleSystemAppsVisibility();
            toggleSystemAppsButton.setText(adapter.isSystemAppsVisible() ? getString(R.string.hide_system_apps) : getString(R.string.show_system_apps));
        });

        Switch switchBackButton = findViewById(R.id.switch_back_button);

        // The switch label reads "Hide the back button", so checked == hidden.
        switchBackButton.setChecked(!preferencesManager.isBackButtonVisible());

        switchBackButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setBackButtonVisible(!isChecked);
            stopSwipeService();
            startSwipeService();
        });

        Switch switchSwapAreas = findViewById(R.id.switch_swap_areas);
        switchSwapAreas.setChecked(preferencesManager.isSwipeAreasSwapped());
        updateHelpLabels(preferencesManager.isSwipeAreasSwapped());

        switchSwapAreas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setSwipeAreasSwapped(isChecked);
            updateHelpLabels(isChecked);
            stopSwipeService();
            startSwipeService();
        });

        Switch switchShowLoader = findViewById(R.id.switch_show_loader);
        switchShowLoader.setChecked(preferencesManager.isShowLoader());
        // The loader preference is read on every swipe in SwipeService, so no service
        // restart is needed for the change to take effect.
        switchShowLoader.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferencesManager.setShowLoader(isChecked));

        startSwipeService();
    }

    private void updateHelpLabels(boolean swapped) {
        TextView leftHelp = findViewById(R.id.textView);
        TextView rightHelp = findViewById(R.id.textView2);
        leftHelp.setText(swapped
                ? R.string.help_swipe_up_here_to_open_selected_app
                : R.string.help_swipe_up_here_to_go_back_this_simulate_the_physical_back_button);
        rightHelp.setText(swapped
                ? R.string.help_swipe_up_here_to_go_back_this_simulate_the_physical_back_button
                : R.string.help_swipe_up_here_to_open_selected_app);
        leftHelp.setBackgroundResource(swapped ? R.color.help_open_area : R.color.help_back_area);
        rightHelp.setBackgroundResource(swapped ? R.color.help_back_area : R.color.help_open_area);
    }

    private void stopSwipeService() {
        Intent intent = new Intent(this, SwipeService.class);
        stopService(intent);
    }

    /**
     * Asks for one sentence, then sends the measurements.
     *
     * <p>Both halves matter. The report leaves the car for somebody else's server, which is
     * not a thing to do on a stray tap without saying so. And the numbers on their own are
     * half a report: only the sentence says what was being tried when they were taken — which
     * app was the target, how quickly the swipes followed one another, whether the engine was
     * running.
     */
    private void askThenSend(Button sendButton) {
        final EditText note = new EditText(this);
        note.setHint(R.string.report_note_hint);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.report_send)
                .setMessage(R.string.report_explain)
                .setView(note)
                .setPositiveButton(R.string.report_send, (d, w) -> {
                    sendButton.setEnabled(false);
                    ProbeReport.send(this, note.getText().toString(), collectReport(),
                            new ProbeReport.Callback() {
                                @Override
                                public void onSent(@NonNull String reportName) {
                                    sendButton.setEnabled(true);
                                    Toast.makeText(MainActivity.this,
                                            getString(R.string.report_sent, reportName),
                                            Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailed(@NonNull String reason) {
                                    sendButton.setEnabled(true);
                                    Toast.makeText(MainActivity.this,
                                            getString(R.string.report_failed, reason),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Everything worth reading afterwards, in the order it is worth reading it. */
    @NonNull
    private String collectReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" (").append(Build.DEVICE).append("), android ")
                .append(Build.VERSION.RELEASE).append(" / API ").append(Build.VERSION.SDK_INT)
                .append('\n');
        sb.append("target: ").append(preferencesManager.getSelectedPackage()).append('\n');
        sb.append("strategy: ").append(preferencesManager.getLaunchStrategy().label)
                .append("   loader: ").append(preferencesManager.isShowLoader() ? "on" : "off")
                .append('\n');
        sb.append('\n').append("BOOT\n  ")
                .append(new BootLog(this).describe(this)).append('\n');
        sb.append('\n').append("LAUNCHES\n");
        String launches = new Journal(this, "launch", 24).describe();
        sb.append(launches.isEmpty() ? "  nothing recorded yet" : launches).append('\n');
        return sb.toString();
    }

    private void startSwipeService() {
        Intent intent = new Intent(this, SwipeService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
