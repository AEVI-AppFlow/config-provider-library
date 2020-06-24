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
package com.aevi.sdk.pos.flow.config.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;

import com.aevi.sdk.pos.flow.config.ConfigComponentProvider;
import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.R2;
import com.aevi.sdk.pos.flow.config.SettingsProvider;
import com.aevi.sdk.pos.flow.config.ui.view.ConfigSettingSwitch;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;

/**
 * Provides filtering options for flow configs.
 */
public class FlowConfigFilterActivity extends AppCompatActivity {

    @BindView(R2.id.auto_manage_apps)
    ConfigSettingSwitch autoManageApps;

    @BindView(R2.id.show_flows_no_apps)
    ConfigSettingSwitch showFlowsNoApps;

    @BindView(R2.id.show_stages_no_apps)
    ConfigSettingSwitch showStagesNoApps;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigComponentProvider.getFpsConfigComponent().inject(this);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_flow_config_filter);
        ButterKnife.bind(this);

        setupSwitch(autoManageApps, settingsProvider.shouldAutoGenerateConfigs(), checked -> settingsProvider.updateAutoGenerateConfig(checked));
        setupSwitch(showFlowsNoApps, settingsProvider.shouldShowFlowsWithNoApps(), checked -> settingsProvider.setShouldShowFlowsNoApps(checked));
        setupSwitch(showStagesNoApps, settingsProvider.shouldShowStagesWithNoApps(), checked -> settingsProvider.setShouldShowStagesNoApps(checked));
    }

    private void setupSwitch(ConfigSettingSwitch settingSwitch, boolean initialValue, Consumer<Boolean> valueChangeConsumer) {
        settingSwitch.setChecked(initialValue);
        settingSwitch.subscribeToValueChanges().subscribe(valueChangeConsumer::accept);
    }

}
