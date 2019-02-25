package com.aevi.sdk.pos.flow.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aevi.sdk.pos.flow.config.flowapps.FlowAppChangeReceiver;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;

import javax.inject.Inject;

public class StartUpReceiver extends BroadcastReceiver {

    @Inject
    FlowAppChangeReceiver flowAppChangeReceiver;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ProviderFlowConfigStore flowConfigStore;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConfigComponentProvider.getFpsConfigComponent().inject(this);
        flowAppChangeReceiver.registerForBroadcasts();
        // If we are re-installed, check if we should overwrite configs
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED") &&
                settingsProvider.shouldOverwriteConfigsOnReinstall()) {
            Log.d(StartUpReceiver.class.getSimpleName(), "Resetting all flows due to re-installation");
            flowConfigStore.resetFlowConfigs();
        }
    }
}
