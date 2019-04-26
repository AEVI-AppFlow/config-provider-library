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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import com.aevi.android.rxmessenger.activity.NoSuchInstanceException;
import com.aevi.android.rxmessenger.activity.ObservableActivityHelper;
import com.aevi.sdk.flow.model.config.FlowApp;
import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.R2;
import com.aevi.sdk.pos.flow.config.ui.view.ConfigSettingSwitch;
import com.aevi.sdk.pos.flow.config.ui.view.ConfigSettingTextInput;
import com.aevi.util.json.JsonConverter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

/**
 * Provides details of a flow app.
 */
public class FlowAppDetailsActivity extends AppCompatActivity {

    public static final String KEY_FLOW_APP = "flowApp";
    public static final String KEY_FLOW_APP_NAME = "flowAppName";
    public static final String KEY_READ_ONLY = "readOnly";

    @BindView(R2.id.mandatory)
    ConfigSettingSwitch mandatory;

    @BindView(R2.id.conditional_on)
    ConfigSettingTextInput conditionalOn;

    @BindView(R2.id.title)
    TextView title;

    private FlowApp inputFlowApp;
    private boolean mandatoryValue;
    private String conditionalOnValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_flow_app_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        inputFlowApp = JsonConverter.deserialize(intent.getStringExtra(KEY_FLOW_APP), FlowApp.class);
        String flowAppName = intent.getStringExtra(KEY_FLOW_APP_NAME);
        boolean readOnly = intent.getBooleanExtra(KEY_READ_ONLY, false);
        title.setText(flowAppName);
        mandatoryValue = inputFlowApp.isMandatory();
        conditionalOnValue = inputFlowApp.getConditionalOnValue();

        setupSwitch(mandatory, mandatoryValue, checked -> mandatoryValue = checked);
        setupTextInput(conditionalOn, conditionalOnValue, value -> conditionalOnValue = value);

        if (readOnly) {
            mandatory.setEnabled(false);
            conditionalOn.setEnabled(false);
        }
    }

    private void setupSwitch(ConfigSettingSwitch settingSwitch, boolean initialValue, Consumer<Boolean> valueChangeConsumer) {
        settingSwitch.setChecked(initialValue);
        settingSwitch.subscribeToValueChanges().subscribe(valueChangeConsumer::accept);
    }

    private void setupTextInput(ConfigSettingTextInput textInput, String initialValue, Consumer<String> valueChangedConsumer) {
        textInput.setValue(initialValue);
        textInput.subscribeToValueChanges().subscribe(valueChangedConsumer::accept);
    }

    @OnClick(R2.id.finish_button)
    public void onClick() {
        try {
            ObservableActivityHelper<FlowApp> helper = ObservableActivityHelper.getInstance(getIntent());
            helper.sendMessageToClient(new FlowApp(inputFlowApp.getId(), mandatoryValue, conditionalOnValue));
            helper.completeStream();
        } catch (NoSuchInstanceException e) {
            // Ignore
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // No-op
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // No-op
    }
}