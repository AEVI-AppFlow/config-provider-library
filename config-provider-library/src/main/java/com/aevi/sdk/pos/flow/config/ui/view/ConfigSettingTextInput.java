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
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aevi.sdk.pos.flow.config.R;
import com.aevi.ui.library.view.settings.SettingTextInput;

public class ConfigSettingTextInput extends SettingTextInput {

    public ConfigSettingTextInput(Context context) {
        super(context);
    }

    public ConfigSettingTextInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConfigSettingTextInput(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ConfigSettingTextInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getLayout() {
        return R.layout.config_settings_edit_text;
    }
}
