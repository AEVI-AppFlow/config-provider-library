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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aevi.sdk.flow.model.config.FpsSettings;
import com.aevi.sdk.pos.flow.config.*;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppDatabase;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;
import com.aevi.ui.library.view.settings.SettingControl;
import com.aevi.ui.library.view.settings.SettingIntegerInput;
import com.aevi.ui.library.view.settings.SettingSwitch;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.functions.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FpsSettingsFragment extends BaseFragment {

    private static final int TIMEOUT_MAX = 3600;
    private static final int ROW_LIMIT_MAX = 10000;

    @BindView(R2.id.split_timeout)
    SettingIntegerInput splitTimeout;

    @BindView(R2.id.flow_response_timeout)
    SettingIntegerInput flowResponseTimeout;

    @BindView(R2.id.payment_response_timeout)
    SettingIntegerInput paymentResponseTimeout;

    @BindView(R2.id.select_timeout)
    SettingIntegerInput selectTimeout;

    @BindView(R2.id.status_update_timeout)
    SettingIntegerInput statusUpdateTimeout;

    @BindView(R2.id.database_row_limit)
    SettingIntegerInput databaseRowLimit;

    @BindView(R2.id.abort_on_flow_error)
    SettingSwitch abortOnFlow;

    @BindView(R2.id.abort_on_payment_error)
    SettingSwitch abortOnPayment;

    @BindView(R2.id.allow_status_bar_access)
    SettingSwitch allowStatusBar;

    @BindView(R2.id.enable_legacy_pa_support)
    SettingSwitch enableLegacySupport;

    @BindView(R2.id.multi_device_support)
    SettingSwitch multiDeviceSupport;

    @BindView(R2.id.currency_conversion_support)
    SettingSwitch currencyConversionSupport;

    @BindView(R2.id.filter_flow_services_by_type)
    SettingSwitch filterFlowServices;

    @BindView(R2.id.always_call_preflow)
    SettingSwitch alwaysCallPreFlow;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    Context appContext;

    @Inject
    ProviderAppDatabase appDatabase;

    @Inject
    ProviderAppScanner appScanner;

    private FpsSettings fpsSettings;
    private boolean hasChanges;
    private boolean readOnlyMode;

    public FpsSettingsFragment() {
    }

    public FpsSettingsFragment(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigComponentProvider.getFpsConfigComponent().inject(this);
        setRetainInstance(true);
        fpsSettings = settingsProvider.getFpsSettings();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_fps_settings;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notifyChangeIfNecessary();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupNumericInput();
        setupSwitches();
        if (readOnlyMode) {
            setReadOnly(splitTimeout, paymentResponseTimeout, flowResponseTimeout, selectTimeout, abortOnFlow, abortOnPayment,
                        allowStatusBar, enableLegacySupport, statusUpdateTimeout, multiDeviceSupport, currencyConversionSupport,
                        filterFlowServices, alwaysCallPreFlow, databaseRowLimit);
        }
    }

    private void setReadOnly(SettingControl... controls) {
        for (SettingControl control : controls) {
            control.setEnabled(false);
        }
    }

    private void notifyChangeIfNecessary() {
        if (hasChanges) {
            DefaultConfigProvider.notifyConfigUpdated(appContext);
        }
    }

    private void setupSwitches() {
        setupSwitch(abortOnPayment, fpsSettings.shouldAbortOnPaymentAppError(), checked -> fpsSettings.setAbortOnPaymentError(checked));
        setupSwitch(abortOnFlow, fpsSettings.shouldAbortOnFlowAppError(), checked -> fpsSettings.setAbortOnFlowError(checked));
        setupSwitch(allowStatusBar, fpsSettings.isAccessViaStatusBarAllowed(), checked -> fpsSettings.setAllowAccessViaStatusBar(checked));
        setupSwitch(enableLegacySupport, fpsSettings.legacyPaymentAppsEnabled(), this::enableLegacy);
        setupSwitch(multiDeviceSupport, fpsSettings.isMultiDeviceEnabled(), checked -> fpsSettings.setMultiDeviceEnabled(checked));
        setupSwitch(currencyConversionSupport, fpsSettings.isCurrencyChangeAllowed(), checked -> fpsSettings.setCurrencyChangeAllowed(checked));
        setupSwitch(filterFlowServices, fpsSettings.shouldFilterServicesByFlowType(), checked -> fpsSettings.setFilterServicesByFlowType(checked));
        setupSwitch(alwaysCallPreFlow, fpsSettings.shouldAlwaysCallPreFlow(), checked -> fpsSettings.setAlwaysCallPreFlow(checked));

        // Features currently not available
        multiDeviceSupport.setEnabled(false);
        currencyConversionSupport.setEnabled(false);
    }

    private void setupSwitch(SettingSwitch settingSwitch, boolean initialValue, Consumer<Boolean> valueChangeConsumer) {
        settingSwitch.setChecked(initialValue);
        settingSwitch.subscribeToValueChanges().subscribe(value -> {
            valueChangeConsumer.accept(value);
            settingsProvider.saveFpsSettings(fpsSettings);
            hasChanges = true;
        });
    }

    private void enableLegacy(boolean enable) {
        fpsSettings.setLegacyPaymentAppsEnabled(enable);
        settingsProvider.saveFpsSettings(fpsSettings);
        if (!enable) {
            appDatabase.clearApps();
            appScanner.reScanForPaymentAndFlowApps();
        }
    }

    protected void setupNumericInput() {
        setupIntegerInput(splitTimeout, fpsSettings.getSplitResponseTimeoutSeconds(), TIMEOUT_MAX,
                          integer -> fpsSettings.setSplitResponseTimeoutSeconds(integer));
        setupIntegerInput(flowResponseTimeout, fpsSettings.getFlowResponseTimeoutSeconds(), TIMEOUT_MAX,
                          integer -> fpsSettings.setFlowResponseTimeoutSeconds(integer));
        setupIntegerInput(paymentResponseTimeout, fpsSettings.getPaymentResponseTimeoutSeconds(), TIMEOUT_MAX,
                          integer -> fpsSettings.setPaymentResponseTimeoutSeconds(integer));
        setupIntegerInput(selectTimeout, fpsSettings.getUserSelectionTimeoutSeconds(), TIMEOUT_MAX,
                          integer -> fpsSettings.setUserSelectionTimeoutSeconds(integer));
        setupIntegerInput(statusUpdateTimeout, fpsSettings.getStatusUpdateTimeoutSeconds(), TIMEOUT_MAX,
                          integer -> fpsSettings.setStatusUpdateTimeoutSeconds(integer));
        setupIntegerInput(databaseRowLimit, fpsSettings.getDatabaseRowLimit(), ROW_LIMIT_MAX,
                          integer -> fpsSettings.setDatabaseRowLimit(integer));
    }

    private void setupIntegerInput(SettingIntegerInput integerInput, int value, int maxValue, Consumer<Integer> valueChangedConsumer) {
        integerInput.setMinMax(0, maxValue);
        integerInput.setInitialValue(value);
        integerInput.subscribeToValueChanges().subscribe(value1 -> {
            valueChangedConsumer.accept(value1);
            settingsProvider.saveFpsSettings(fpsSettings);
            hasChanges = true;
        });
    }

}
