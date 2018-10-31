package com.aevi.sdk.pos.flow.config;

import android.app.Application;
import android.content.Context;

import com.aevi.sdk.pos.flow.config.dagger.DaggerFpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigModule;
import com.aevi.sdk.pos.flow.config.flowapps.AppEntityScanningHelper;
import com.aevi.sdk.pos.flow.config.flowapps.FlowConfigStore;

import javax.inject.Inject;

public abstract class BaseConfigProviderApplication extends Application {

    protected static FpsConfigComponent fpsComponent;
    protected static FlowConfigStore flowConfigStore;

    @Inject
    AppEntityScanningHelper appEntityScanningHelper;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        setupFlowConfigs();
        setupDagger();
        scanForApps();
    }

    private void setupFlowConfigs() {
        flowConfigStore = new FlowConfigStore(this, getFlowConfigs());
    }

    protected abstract int[] getFlowConfigs();

    public static FlowConfigStore getFlowConfigStore() {
        return flowConfigStore;
    }

    public static SettingsProvider getSettingsProvider() {
        return fpsComponent.provideSettingsProvider();
    }

    private void scanForApps() {
        appEntityScanningHelper.reScanForPaymentAndFlowApps();
    }

    protected void setupDagger() {
        fpsComponent = DaggerFpsConfigComponent.builder()
                .fpsConfigModule(new FpsConfigModule(this))
                .build();

        fpsComponent.inject(this);
    }

    public static FpsConfigComponent getFpsConfigComponent() {
        return fpsComponent;
    }
}
