package com.aevi.sdk.pos.flow.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aevi.sdk.flow.model.config.AppFlowSettings;
import com.aevi.sdk.flow.model.config.FpsSettings;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.subjects.PublishSubject;

import static com.aevi.sdk.flow.constants.config.FlowConfigKeys.*;

@Singleton
public class SettingsProvider {

    private final SharedPreferences preferences;

    private static final String KEY_AUTO_GENERATE_CONFIGS = "autoGenerateConfigs";
    private static final String KEY_OVERWRITE_CONFIGS_ON_REINSTALL = "overwriteConfigsOnReinstall";
    private static final String KEY_CONFIGS_FOR_FPS_VERSION = "configsForFpsVersion";

    private PublishSubject<SettingsProvider> settingsChangeSubject = PublishSubject.create();

    @Inject
    public SettingsProvider(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean shouldAutoGenerateConfigs() {
        return preferences.getBoolean(KEY_AUTO_GENERATE_CONFIGS, false);
    }

    public void updateAutoGenerateConfig(boolean autoUpdate) {
        preferences.edit().putBoolean(KEY_AUTO_GENERATE_CONFIGS, autoUpdate).apply();
    }

    public boolean shouldOverwriteConfigsOnReinstall() {
        return preferences.getBoolean(KEY_OVERWRITE_CONFIGS_ON_REINSTALL, false);
    }

    public void setOverwriteConfigsOnReinstall(boolean overwrite) {
        preferences.edit().putBoolean(KEY_OVERWRITE_CONFIGS_ON_REINSTALL, overwrite).apply();
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

    private void notifyChange() {
        settingsChangeSubject.onNext(this);
    }

}