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

import com.aevi.sdk.pos.flow.config.*;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppDatabase;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;
import com.aevi.ui.library.views.EditTimeout;
import com.aevi.ui.library.views.SettingSwitch;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.functions.Consumer;

import static com.aevi.sdk.pos.flow.config.model.Channels.*;

public class SettingsFragment extends BaseFragment {

    private static final int TIMEOUT_MIN = 0;
    private static final int TIMEOUT_MAX = 3600;

    @BindView(R2.id.split_timeout)
    EditTimeout splitTimeout;

    @BindView(R2.id.flow_response_timeout)
    EditTimeout flowResponseTimeout;

    @BindView(R2.id.payment_response_timeout)
    EditTimeout paymentResponseTimeout;

    @BindView(R2.id.select_timeout)
    EditTimeout selectTimeout;

    @BindView(R2.id.abort_on_flow_error)
    SettingSwitch abortOnFlow;

    @BindView(R2.id.abort_on_payment_error)
    SettingSwitch abortOnPayment;

    @BindView(R2.id.allow_status_bar_access)
    SettingSwitch allowStatusBar;

    @BindView(R2.id.use_websocket)
    SettingSwitch useWebsocket;

    @BindView(R2.id.enable_legacy_pa_support)
    SettingSwitch enableLegacySupport;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    Context appContext;

    @Inject
    ProviderAppDatabase appDatabase;

    @Inject
    ProviderAppScanner appScanner;

    private boolean settingsChanged = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseConfigProviderApplication.getFpsConfigComponent().inject(this);
        setRetainInstance(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (settingsChanged) {
            DefaultConfigProvider.notifyConfigUpdated(appContext);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupTimeouts();
        setupFlags();
    }

    private void setupChangeDetection(EditTimeout... timeouts) {
        for (EditTimeout timeout : timeouts) {
            timeout.subscribeToValueChanges().subscribe(integer -> settingsChanged = true);
        }
    }

    private void setupChangeDetection(SettingSwitch... settingSwitches) {
        for (SettingSwitch settingSwitch : settingSwitches) {
            settingSwitch.subscribeToValueChanges().subscribe(aBoolean -> settingsChanged = true);
        }
    }

    private void setupFlags() {
        abortOnPayment.subscribeToValueChanges().subscribe(value -> settingsProvider.abortOnPaymentError(value));
        abortOnFlow.subscribeToValueChanges().subscribe(value -> settingsProvider.abortOnFlowError(value));
        allowStatusBar.subscribeToValueChanges().subscribe(value -> settingsProvider.allowStatusBarAccess(value));
        useWebsocket.subscribeToValueChanges().subscribe(value -> {
            if (value) {
                settingsProvider.setCommsChannel(WEBSOCKET);
            } else {
                settingsProvider.setCommsChannel(MESSENGER);
            }
        });
        enableLegacySupport.subscribeToValueChanges().subscribe(enabled -> {
            settingsProvider.setLegacyPaymentAppsEnabled(enabled);
            if (!enabled) {
                appDatabase.clearApps();
                appScanner.reScanForPaymentAndFlowApps();
            }
        });
        setupChangeDetection(abortOnPayment, abortOnFlow, allowStatusBar, useWebsocket, enableLegacySupport);
        abortOnPayment.setChecked(settingsProvider.shouldAbortOnPaymentAppError());
        abortOnFlow.setChecked(settingsProvider.shouldAbortOnFlowAppError());
        allowStatusBar.setChecked(settingsProvider.allowAccessViaStatusBar());
        useWebsocket.setChecked(settingsProvider.getCommsChannel().equals(WEBSOCKET));
        enableLegacySupport.setChecked(settingsProvider.legacyPaymentAppsEnabled());
    }

    protected void setupTimeouts() {
        setupEditTimeout(splitTimeout, settingsProvider.getSplitResponseTimeoutSeconds(),
                         integer -> settingsProvider.setSplitResponseTimeoutSeconds(integer));
        setupEditTimeout(flowResponseTimeout, settingsProvider.getFlowResponseTimeoutSeconds(),
                         integer -> settingsProvider.setFlowResponseTimeoutSeconds(integer));
        setupEditTimeout(paymentResponseTimeout, settingsProvider.getPaymentResponseTimeoutSeconds(),
                         integer -> settingsProvider.setPaymentResponseTimeoutSeconds(integer));
        setupEditTimeout(selectTimeout, settingsProvider.getAppOrDeviceSelectionTimeoutSeconds(),
                         integer -> settingsProvider.setAppOrDeviceSelectionTimeoutSeconds(integer));
        setupChangeDetection(splitTimeout, flowResponseTimeout, paymentResponseTimeout, selectTimeout);
    }

    private void setupEditTimeout(EditTimeout editTimeout, int value, TimeoutChange timeoutChange) {
        editTimeout.setMinMax(TIMEOUT_MIN, TIMEOUT_MAX);
        editTimeout.setInitialValue(value);
        observe(editTimeout.subscribeToValueChanges()).subscribe(timeoutChange);
    }

    interface TimeoutChange extends Consumer<Integer> {
        void accept(Integer integer);
    }
}
