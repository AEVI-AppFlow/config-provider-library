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
package com.aevi.sdk.pos.flow.config.dagger;

import android.app.Application;
import android.content.Context;

import com.aevi.sdk.pos.flow.config.BaseConfigProviderApplication;
import com.aevi.sdk.pos.flow.config.DefaultConfigProvider;
import com.aevi.sdk.pos.flow.config.SettingsProvider;
import com.aevi.sdk.pos.flow.config.StartUpReceiver;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppDatabase;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;
import com.aevi.sdk.pos.flow.config.ui.BaseConfigurationActivity;
import com.aevi.sdk.pos.flow.config.ui.FlowConfigurationFragment;
import com.aevi.sdk.pos.flow.config.ui.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {
        FpsConfigModule.class,
})
@Singleton
public interface FpsConfigComponent {

    Application provideApplication();

    Context provideApplicationContext();

    ProviderAppScanner provideAppEntityScanningHelper();

    ProviderAppDatabase provideAppDatabase();

    SettingsProvider provideSettingsProvider();

    void inject(FlowConfigurationFragment flowConfigurationFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(BaseConfigProviderApplication fpsConfig);

    void inject(DefaultConfigProvider defaultConfigProvider);

    void inject(BaseConfigurationActivity configurationActivity);

    void inject(StartUpReceiver startUpReceiver);
}
