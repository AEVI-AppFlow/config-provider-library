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
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.aevi.sdk.app.scanning.model.AppInfoModel;

public final class IconHelper {

    public static Drawable getIcon(Context context, AppInfoModel item) {
        if (item == null) {
            return null;
        }
        Drawable appIcon = getAppIcon(context, item);
        if (appIcon == null) {
            appIcon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);
        }
        return appIcon;
    }

    private static Drawable getAppIcon(Context context, AppInfoModel item) {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(item.getPaymentFlowServiceInfo().getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            // carry on
        }
        return icon;
    }
}
