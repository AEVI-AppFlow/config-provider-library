package com.aevi.sdk.pos.flow.config;

import android.app.Application;
import android.content.Context;

import com.aevi.sdk.pos.flow.config.dagger.DaggerFpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigComponent;
import com.aevi.sdk.pos.flow.config.dagger.FpsConfigModule;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;

import javax.inject.Inject;

public abstract class BaseConfigProviderApplication extends Application {

    @Inject
    ProviderAppScanner appEntityScanningHelper;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        setupDagger();
        setupFlowConfigs();
        onComponentsReady();
        scanForApps();
    }

    // Can be overridden by subclasses to configure settings, etc before scanning starts
    protected void onComponentsReady() {

    }

    private void setupFlowConfigs() {
        ProviderFlowConfigStore providerFlowConfigStore = ConfigComponentProvider.getProviderFlowConfigStore();
        providerFlowConfigStore.init(getFlowConfigs());
    }

    public abstract int[] getFlowConfigs();

    private void scanForApps() {
        appEntityScanningHelper.reScanForPaymentAndFlowApps();
    }

    protected void setupDagger() {
        FpsConfigComponent fpsComponent = DaggerFpsConfigComponent.builder()
                .fpsConfigModule(new FpsConfigModule(this))
                .build();
        fpsComponent.inject(this);
        ConfigComponentProvider.setFpsComponent(fpsComponent);
        ConfigComponentProvider.setConfigProviderApplication(this);
    }

}
