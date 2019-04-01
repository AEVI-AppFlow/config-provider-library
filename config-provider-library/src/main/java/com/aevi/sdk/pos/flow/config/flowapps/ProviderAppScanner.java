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
package com.aevi.sdk.pos.flow.config.flowapps;

import android.content.Context;
import android.util.Log;

import com.aevi.payment.legacy.app.scanning.LegacyPaymentAppScanner;
import com.aevi.sdk.app.audit.LogcatAudit;
import com.aevi.sdk.app.scanning.PaymentFlowServiceScanner;
import com.aevi.sdk.app.scanning.model.AppInfoModel;
import com.aevi.sdk.pos.flow.config.DefaultConfigProvider;
import com.aevi.sdk.pos.flow.config.SettingsProvider;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

@Singleton
public class ProviderAppScanner {

    private static final String TAG = ProviderAppScanner.class.getSimpleName();
    private static final int SERVICE_INFO_TIMEOUT_SECONDS = 3;

    private final ProviderAppDatabase appDatabase;
    private final ProviderFlowConfigStore flowConfigStore;
    private final Context appContext;
    private final SettingsProvider settingsProvider;
    private final PaymentFlowServiceScanner flowServiceScanner;
    private final LegacyPaymentAppScanner legacyPaymentAppScanner;

    @Inject
    public ProviderAppScanner(ProviderAppDatabase appDatabase, PaymentFlowServiceScanner flowServiceScanner,
                              LegacyPaymentAppScanner legacyPaymentAppScanner, ProviderFlowConfigStore providerFlowConfigStore,
                              Context appContext, SettingsProvider settingsProvider) {
        this.appDatabase = appDatabase;
        this.flowServiceScanner = flowServiceScanner;
        this.legacyPaymentAppScanner = legacyPaymentAppScanner;
        this.flowConfigStore = providerFlowConfigStore;
        this.appContext = appContext;
        this.settingsProvider = settingsProvider;
    }

    public void reScanForPaymentAndFlowApps() {
        LogcatAudit logcatAudit = new LogcatAudit();
        if (settingsProvider.getFpsSettings().legacyPaymentAppsEnabled()) {
            Observable.merge(flowServiceScanner.scan(logcatAudit, SERVICE_INFO_TIMEOUT_SECONDS), legacyPaymentAppScanner.scan(logcatAudit)).toList()
                    .subscribe(this::handleApps);
        } else {
            flowServiceScanner.scan(logcatAudit, SERVICE_INFO_TIMEOUT_SECONDS).toList().subscribe(this::handleApps);
        }
    }

    private void handleApps(List<AppInfoModel> newApps) {
        appDatabase.clearApps();
        for (AppInfoModel appInfoModel : newApps) {
            appDatabase.save(appInfoModel, false);
        }
        if (settingsProvider.shouldAutoGenerateConfigs()) {
            Log.d(TAG, "Auto-add apps is set - updating flow config with apps");
            flowConfigStore.addAllToFlowConfigs(newApps, settingsProvider.getAppsToIgnoreForAutoGeneration());
            DefaultConfigProvider.notifyConfigUpdated(appContext);
        }
        appDatabase.notifySubscribers();
    }
}
