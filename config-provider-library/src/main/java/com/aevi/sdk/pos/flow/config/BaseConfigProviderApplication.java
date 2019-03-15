package com.aevi.sdk.pos.flow.config;

import android.app.Application;
import android.content.Context;

import com.aevi.sdk.pos.flow.config.dagger.DaggerFpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigModule;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppDatabase;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;

import javax.inject.Inject;

public abstract class BaseConfigProviderApplication extends Application {

    private ProviderFlowConfigStore flowConfigStore;

    @Inject
    ProviderAppScanner appEntityScanningHelper;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        setupFlowConfigs();
        setupDagger();
        onComponentsReady();
        scanForApps();
    }

    // Can be overridden by subclasses to configure settings, etc before scanning starts
    protected void onComponentsReady() {

    }

    public abstract int[] getFlowConfigs();

    public void setupFlowConfigs() {
        flowConfigStore = new ProviderFlowConfigStore(this);
    }

    private void scanForApps() {
        appEntityScanningHelper.reScanForPaymentAndFlowApps();
    }

    protected void setupDagger() {
        FpsConfigComponent fpsComponent = DaggerFpsConfigComponent.builder()
                .fpsConfigModule(new FpsConfigModule(this, flowConfigStore, new ProviderAppDatabase(this)))
                .build();
        fpsComponent.inject(this);
        fpsComponent.inject(flowConfigStore);
        flowConfigStore.init(getFlowConfigs());
        ConfigComponentProvider.setFpsComponent(fpsComponent);
        ConfigComponentProvider.setConfigProviderApplication(this);
    }

}
