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
package com.aevi.sdk.pos.flow.config.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;


import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.R2;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

abstract class BaseSettingInput extends LinearLayout implements SettingControl {

    private static final long DELAY = 500;

    @BindView(R2.id.title)
    TextView title;

    @BindView(R2.id.summary)
    TextView summary;

    @BindView(R2.id.value)
    EditText value;

    @BindView(R2.id.value_suffix)
    TextView valueSuffix;

    private Timer timer = new Timer();
    private String originalSummaryText;
    private int originalSummaryColor;
    private boolean valueChanges;

    public BaseSettingInput(Context context) {
        super(context);
        init(context, null);
    }

    public BaseSettingInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseSettingInput(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BaseSettingInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View v = View.inflate(context, getLayout(), this);
        ButterKnife.bind(this, v);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Editable, 0, 0);
            title.setText(a.getString(R.styleable.Editable_editTitle));
            originalSummaryText = a.getString(R.styleable.Editable_editSummary);
            originalSummaryColor = summary.getCurrentTextColor();
            summary.setText(originalSummaryText);
            int maxChars = a.getInt(R.styleable.Editable_editMaxChars, -1);
            if (maxChars != -1) {
                value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxChars)});
            }
            valueSuffix.setText(a.getString(R.styleable.Editable_editSuffix));
            a.recycle();
        }

        value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                notifyChange();
            }
        });

        init(context);
    }

    protected int getLayout() {
        return R.layout.edit_text;
    }

    protected abstract void init(Context context);

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanUp();
    }

    @Override
    public boolean hasValueChanged() {
        return valueChanges;
    }

    @Override
    public void setEnabled(boolean enabled) {
        value.setEnabled(enabled);
    }

    public void notifyInvalidValue(String invalidText) {
        summary.setText(invalidText);
        summary.setTextColor(Color.RED);
        summary.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_error_outline_red, 0, 0, 0);
    }

    public void clearInvalidState() {
        summary.setTextColor(originalSummaryColor);
        summary.setText(originalSummaryText);
        summary.setCompoundDrawables(null, null, null, null);
    }

    public void setValue(String value) {
        this.value.setText(value);
    }

    private void notifyChange() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!value.getText().toString().isEmpty()) {
                    valueChanges = true;
                    onChange(value.getText().toString());
                }
            }
        }, DELAY);
    }

    protected abstract void cleanUp();

    protected abstract void onChange(String newValue);

}
