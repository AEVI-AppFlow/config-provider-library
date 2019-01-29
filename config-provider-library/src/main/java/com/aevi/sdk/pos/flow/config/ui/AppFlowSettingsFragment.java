package com.aevi.sdk.pos.flow.config.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aevi.sdk.flow.model.config.AppFlowSettings;
import com.aevi.sdk.pos.flow.config.*;
import com.aevi.ui.library.view.settings.SettingControl;
import com.aevi.ui.library.view.settings.SettingSwitch;
import com.aevi.ui.library.view.settings.SettingTextInput;

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
    SettingTextInput dateFormat;

    @BindView(R2.id.time_format)
    SettingTextInput timeFormat;

    @BindView(R2.id.primary_language)
    SettingTextInput primaryLanguage;

    @BindView(R2.id.primary_currency)
    SettingTextInput primaryCurrency;

    @BindView(R2.id.use_websocket)
    SettingSwitch useWebsocket;

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
    public void onResume() {
        super.onResume();
        setupGeneral();
        setupExperimental();
        if (readOnlyMode) {
            setReadOnlyMode();
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

    private void setupTextInput(SettingTextInput textInput, String initialValue, ValueConsumer valueChangedConsumer) {
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
