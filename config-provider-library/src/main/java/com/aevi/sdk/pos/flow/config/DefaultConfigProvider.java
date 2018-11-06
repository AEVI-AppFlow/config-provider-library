/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aevi.sdk.pos.flow.config;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.aevi.sdk.config.provider.BaseConfigProvider;
import com.aevi.sdk.flow.model.config.ConfigStyles;
import com.aevi.sdk.pos.flow.config.flowapps.FlowAppChangeReceiver;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;

import javax.inject.Inject;

import static com.aevi.sdk.flow.constants.config.ConfigStyleKeys.*;
import static com.aevi.sdk.flow.constants.config.FlowConfigKeys.*;


public class DefaultConfigProvider extends BaseConfigProvider {

    private static final String TAG = DefaultConfigProvider.class.getSimpleName();
    private static final String APPFLOW_COMMS_CHANNEL_KEY = "appFlowCommsChannel";
    private static final String LAUNCHER_CONFIG_KEY_OPERATOR_WHITELIST = "whitelist_OPERATOR";

    private ProviderFlowConfigStore flowConfigStore;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    FlowAppChangeReceiver flowAppChangeReceiver;

    @Override
    public boolean onCreate() {
        BaseConfigProviderApplication.getFpsConfigComponent().inject(this);
        flowConfigStore = BaseConfigProviderApplication.getFlowConfigStore();
        flowAppChangeReceiver.registerForBroadcasts();
        return super.onCreate();
    }

    @Override
    public String[] getConfigKeys() {
        return new String[]{
                FPS_CONFIG_KEY_FLOW_CONFIGS,
                FPS_CONFIG_KEY_STYLES,
                FPS_CONFIG_KEY_SETTINGS,
                LAUNCHER_CONFIG_KEY_OPERATOR_WHITELIST,
                APPFLOW_COMMS_CHANNEL_KEY
        };
    }

    @Override
    public String getConfigValue(String key) {
        switch (key) {
            case FPS_CONFIG_KEY_STYLES:
                return getStyleConfig();
            case FPS_CONFIG_KEY_SETTINGS:
                return getFpsSettings();
            case APPFLOW_COMMS_CHANNEL_KEY:
                return settingsProvider.getCommsChannel();
        }
        return "";
    }

    @Override
    public int getIntConfigValue(String s) {
        return 0;
    }

    @Override
    public String[] getConfigArrayValue(String key) {
        switch (key) {
            case FPS_CONFIG_KEY_FLOW_CONFIGS:
                return flowConfigStore.getAllFlowConfigs();
            case LAUNCHER_CONFIG_KEY_OPERATOR_WHITELIST:
                return getWhitelist();
        }
        return new String[0];
    }

    @NonNull
    @Override
    protected String[] getAllowedCallingPackageNames() {
        return new String[]{};
    }

    @NonNull
    @Override
    protected String getVendorName() {
        return "AEVI";
    }

    private String getFpsSettings() {
        return settingsProvider.getFpsSettings().toJson();
    }

    private String getStyleConfig() {
        ConfigStyles configStyles = new ConfigStyles();
        Resources resources = getContext().getResources();
        configStyles.setColor(COLOR_PRIMARY, resources.getColor(R.color.colorPrimary));
        configStyles.setColor(COLOR_PRIMARY_DARK, resources.getColor(R.color.colorPrimaryDark));
        configStyles.setColor(COLOR_ACCENT, resources.getColor(R.color.colorAccent));
        configStyles.setColor(COLOR_ALERT, resources.getColor(R.color.colorAlert));
        configStyles.setColor(COLOR_MAIN_TEXT, resources.getColor(R.color.colorMainText));
        configStyles.setColor(COLOR_TITLE_TEXT, resources.getColor(R.color.colorTitleText));

        configStyles.setStyle(DIALOG_STYLE, DIALOG_STYLE_FULLSCREEN);

        return configStyles.toJson();
    }

    private String[] getWhitelist() {
        return getContext().getResources().getStringArray(R.array.whitelist_default);
    }
}
