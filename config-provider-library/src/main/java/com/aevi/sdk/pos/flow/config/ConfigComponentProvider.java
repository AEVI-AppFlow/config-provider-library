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

import com.aevi.sdk.pos.flow.config.dagger.FpsConfigComponent;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;

public class ConfigComponentProvider {

    private static FpsConfigComponent fpsComponent;
    private static BaseConfigProviderApplication configProviderApplication;

    public static void setFpsComponent(FpsConfigComponent component) {
        fpsComponent = component;
    }

    public static FpsConfigComponent getFpsConfigComponent() {
        return fpsComponent;
    }

    public static SettingsProvider getSettingsProvider() {
        return fpsComponent.provideSettingsProvider();
    }

    public static BaseConfigProviderApplication getConfigProviderApplication() {
        return configProviderApplication;
    }

    public static void setConfigProviderApplication(BaseConfigProviderApplication configProviderApplication) {
        ConfigComponentProvider.configProviderApplication = configProviderApplication;
    }

    public static ProviderFlowConfigStore getProviderFlowConfigStore() {
        return fpsComponent.provideProviderFlowConfigStore();
    }
}
