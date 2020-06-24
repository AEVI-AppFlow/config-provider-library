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
import android.text.InputType;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class SettingTextInput extends BaseSettingInput {

    private PublishSubject<String> changedSubject = PublishSubject.create();

    public SettingTextInput(Context context) {
        super(context);
    }

    public SettingTextInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingTextInput(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingTextInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context) {
        value.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    public void setInputType(int inputType) {
        value.setInputType(inputType);
    }

    @Override
    protected void cleanUp() {
        changedSubject.onComplete();
    }

    @Override
    protected void onChange(String newValue) {
        changedSubject.onNext(newValue);
    }

    public Observable<String> subscribeToValueChanges() {
        return changedSubject;
    }
}
