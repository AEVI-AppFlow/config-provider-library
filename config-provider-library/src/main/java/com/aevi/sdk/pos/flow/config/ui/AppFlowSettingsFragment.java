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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.aevi.sdk.flow.model.config.AppFlowSettings;
import com.aevi.sdk.pos.flow.config.*;
import com.aevi.sdk.pos.flow.config.ui.view.ConfigSettingTextInput;
import com.aevi.sdk.pos.flow.config.ui.view.SettingControl;
import com.aevi.sdk.pos.flow.config.ui.view.SettingSwitch;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static com.aevi.android.rxmessenger.MessageConstants.*;

public class AppFlowSettingsFragment extends BaseFragment {

    @BindView(R2.id.date_format)
    ConfigSettingTextInput dateFormat;

    @BindView(R2.id.time_format)
    ConfigSettingTextInput timeFormat;

    @BindView(R2.id.primary_language)
    ConfigSettingTextInput primaryLanguage;

    @BindView(R2.id.primary_currency)
    ConfigSettingTextInput primaryCurrency;

    @BindView(R2.id.use_websocket)
    SettingSwitch useWebsocket;

    @BindView(R2.id.read_only_note)
    TextView readOnlyNote;

    @Inject
    Context appContext;

    @Inject
    SettingsProvider settingsProvider;

    private AppFlowSettings appFlowSettings;
    private boolean hasChanges;
    private boolean readOnlyMode;

    public AppFlowSettingsFragment() {
    }

    public AppFlowSettingsFragment(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigComponentProvider.getFpsConfigComponent().inject(this);
        setRetainInstance(true);
        appFlowSettings = settingsProvider.getAppFlowSettings();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_appflow_settings;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupGeneral();
        setupExperimental();
        if (readOnlyMode) {
            setReadOnlyMode();
            readOnlyNote.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hasChanges) {
            DefaultConfigProvider.notifyConfigUpdated(appContext);
        }
    }

    private void setReadOnlyMode() {
        setReadOnly(dateFormat, timeFormat, primaryLanguage, primaryCurrency, useWebsocket);
    }

    private void setReadOnly(SettingControl... controls) {
        for (SettingControl control : controls) {
            control.setEnabled(false);
        }
    }

    private void setupGeneral() {
        setupTextInput(dateFormat, appFlowSettings.getDateFormat(), this::validateAndSetDateFormat);
        setupTextInput(timeFormat, appFlowSettings.getTimeFormat(), this::validateAndSetTimeFormat);
        setupTextInput(primaryLanguage, appFlowSettings.getPrimaryLanguage(), this::validateAndSetLanguage);
        setupTextInput(primaryCurrency, appFlowSettings.getPrimaryCurrency(), this::validateAndSetCurrency);
    }

    private boolean validateAndSetDateFormat(String dateFormatValue) {
        try {
            new SimpleDateFormat(dateFormatValue, Locale.getDefault());
            appFlowSettings.setDateFormat(dateFormatValue);
            dateFormat.clearInvalidState();
            return true;
        } catch (Throwable throwable) {
            dateFormat.notifyInvalidValue(getString(R.string.invalid_date_format));
        }
        return false;
    }

    private boolean validateAndSetTimeFormat(String timeFormatValue) {
        try {
            new SimpleDateFormat(timeFormatValue, Locale.getDefault());
            appFlowSettings.setTimeFormat(timeFormatValue);
            timeFormat.clearInvalidState();
            return true;
        } catch (Throwable throwable) {
            timeFormat.notifyInvalidValue(getString(R.string.invalid_time_format));
            return false;
        }
    }

    private boolean validateAndSetLanguage(String language) {
        if (Arrays.asList(Locale.getISOLanguages()).contains(language)) {
            appFlowSettings.setPrimaryLanguage(language);
            primaryLanguage.clearInvalidState();
            return true;
        } else {
            primaryLanguage.notifyInvalidValue(getString(R.string.invalid_lang_code));
            return false;
        }
    }

    private boolean validateAndSetCurrency(String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            if (currency == null) {
                throw new IllegalArgumentException();
            }
            if (Build.VERSION.SDK_INT >= 24 && currency.getNumericCode() == 0) {
                throw new IllegalArgumentException();
            }
            primaryCurrency.clearInvalidState();
            appFlowSettings.setPrimaryCurrency(currencyCode);
            return true;
        } catch (Throwable throwable) {
            primaryCurrency.notifyInvalidValue(getString(R.string.invalid_currency_code));
            return false;
        }
    }

    private void setupTextInput(ConfigSettingTextInput textInput, String initialValue, ValueConsumer valueChangedConsumer) {
        textInput.setValue(initialValue);
        textInput.subscribeToValueChanges().observeOn(AndroidSchedulers.mainThread()).subscribe(value -> {
            boolean accepted = valueChangedConsumer.onValue(value);
            if (accepted) {
                settingsProvider.saveAppFlowSettings(appFlowSettings);
                hasChanges = true;
            }
        });
    }

    private void setupExperimental() {
        setupSwitch(useWebsocket, appFlowSettings.getCommsChannel().equals(CHANNEL_WEBSOCKET),
                    useWebsocket -> appFlowSettings.setCommsChannel(useWebsocket ? CHANNEL_WEBSOCKET : CHANNEL_MESSENGER));
    }

    private void setupSwitch(SettingSwitch settingSwitch, boolean initialValue, Consumer<Boolean> valueChangeConsumer) {
        settingSwitch.setChecked(initialValue);
        settingSwitch.subscribeToValueChanges().subscribe(value -> {
            valueChangeConsumer.accept(value);
            settingsProvider.saveAppFlowSettings(appFlowSettings);
            hasChanges = true;
        });
    }

    interface ValueConsumer {
        boolean onValue(String value);
    }

}
