package com.aevi.sdk.pos.flow.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aevi.sdk.flow.model.config.AppFlowSettings;
import com.aevi.sdk.flow.model.config.FpsSettings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.aevi.sdk.flow.constants.config.FlowConfigKeys.*;

@Singleton
public class SettingsProvider {

    private final SharedPreferences preferences;

    public static final String KEY_AUTO_GENERATE_CONFIGS = "autoGenerateConfigs";
    public static final String KEY_OVERWRITE_CONFIGS_ON_REINSTALL = "overwriteConfigsOnReinstall";
    public static final String KEY_CONFIGS_FOR_FPS_VERSION = "configsForFpsVersion";
    public static final String KEY_AUTO_APP_IGNORE_LIST = "autoAppsIgnoreList";
    public static final String KEY_SHOW_FLOWS_NO_APPS = "showFlowsWithNoApps";
    public static final String KEY_SHOW_STAGES_NO_APPS = "showStagesWithNoApps";

    private PublishSubject<String> settingsChangeSubject = PublishSubject.create();

    @Inject
    public SettingsProvider(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean shouldAutoGenerateConfigs() {
        return preferences.getBoolean(KEY_AUTO_GENERATE_CONFIGS, false);
    }

    public void setInitialAutoGenerateConfigsValue(boolean initialValue) {
        if (!preferences.contains(KEY_AUTO_GENERATE_CONFIGS)) {
            updateAutoGenerateConfig(initialValue);
        }
    }

    public void updateAutoGenerateConfig(boolean autoUpdate) {
        preferences.edit().putBoolean(KEY_AUTO_GENERATE_CONFIGS, autoUpdate).apply();
        notifyChange(KEY_AUTO_GENERATE_CONFIGS);
    }

    public void setAppsToIgnoreForAutoGeneration(String... packageNames) {
        Set<String> values = new HashSet<>(Arrays.asList(packageNames));
        preferences.edit().putStringSet(KEY_AUTO_APP_IGNORE_LIST, values).apply();
        notifyChange(KEY_AUTO_APP_IGNORE_LIST);
    }

    public Set<String> getAppsToIgnoreForAutoGeneration() {
        return preferences.getStringSet(KEY_AUTO_APP_IGNORE_LIST, new HashSet<>());
    }

    public boolean shouldOverwriteConfigsOnReinstall() {
        return preferences.getBoolean(KEY_OVERWRITE_CONFIGS_ON_REINSTALL, false);
    }

    public void setOverwriteConfigsOnReinstall(boolean overwrite) {
        preferences.edit().putBoolean(KEY_OVERWRITE_CONFIGS_ON_REINSTALL, overwrite).apply();
        notifyChange(KEY_OVERWRITE_CONFIGS_ON_REINSTALL);
    }

    public boolean shouldShowFlowsWithNoApps() {
        return preferences.getBoolean(KEY_SHOW_FLOWS_NO_APPS, false);
    }

    public void setShouldShowFlowsNoApps(boolean set) {
        preferences.edit().putBoolean(KEY_SHOW_FLOWS_NO_APPS, set).apply();
        notifyChange(KEY_SHOW_FLOWS_NO_APPS);
    }

    public boolean shouldShowStagesWithNoApps() {
        return preferences.getBoolean(KEY_SHOW_STAGES_NO_APPS, false);
    }

    public void setShouldShowStagesNoApps(boolean set) {
        preferences.edit().putBoolean(KEY_SHOW_STAGES_NO_APPS, set).apply();
        notifyChange(KEY_SHOW_STAGES_NO_APPS);
    }

    private boolean hasFpsSettings() {
        return preferences.contains(FPS_CONFIG_KEY_SETTINGS);
    }

    public FpsSettings getFpsSettings() {
        return hasFpsSettings() ? deserialiseFpsSettings() : new FpsSettings();
    }

    public void saveFpsSettings(FpsSettings fpsSettings) {
        setString(FPS_CONFIG_KEY_SETTINGS, fpsSettings.toJson());
    }

    private FpsSettings deserialiseFpsSettings() {
        String val = getString(FPS_CONFIG_KEY_SETTINGS, "");
        if (val.isEmpty()) {
            return new FpsSettings();
        }
        return FpsSettings.fromJson(val);
    }

    public AppFlowSettings getAppFlowSettings() {
        return hasAppFlowSettings() ? deserialiseAppFlowSettings() : new AppFlowSettings();
    }

    private boolean hasAppFlowSettings() {
        return preferences.contains(APPFLOW_CONFIG_KEY_SETTINGS);
    }

    public void saveAppFlowSettings(AppFlowSettings appFlowSettings) {
        setString(APPFLOW_CONFIG_KEY_SETTINGS, appFlowSettings.toJson());
    }

    private AppFlowSettings deserialiseAppFlowSettings() {
        String val = getString(APPFLOW_CONFIG_KEY_SETTINGS, "");
        if (val.isEmpty()) {
            return new AppFlowSettings();
        }
        return AppFlowSettings.fromJson(val);
    }

    public String getFpsVersionUsedForStoredConfigs() {
        return getString(KEY_CONFIGS_FOR_FPS_VERSION, null);
    }

    public void setFpsVersionUsedForStoredConfigs(String version) {
        setString(KEY_CONFIGS_FOR_FPS_VERSION, version);
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    private void notifyChange(String key) {
        settingsChangeSubject.onNext(key);
    }

    public Observable<String> subscribeToUpdates() {
        return settingsChangeSubject;
    }

}