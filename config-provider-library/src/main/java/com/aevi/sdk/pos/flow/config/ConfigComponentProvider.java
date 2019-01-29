package com.aevi.sdk.pos.flow.config;

import com.aevi.sdk.pos.flow.config.dagger.FpsConfigComponent;

public class ConfigComponentProvider {

    private static FpsConfigComponent fpsComponent;

    public static void setFpsComponent(FpsConfigComponent component) {
        fpsComponent = component;
    }

    public static FpsConfigComponent getFpsConfigComponent() {
        return fpsComponent;
    }

    public static SettingsProvider getSettingsProvider() {
        return fpsComponent.provideSettingsProvider();
    }
}
