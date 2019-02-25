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

import com.aevi.sdk.app.scanning.model.AppInfoModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Application provider interface to allow re-use of flow config UI.
 */
public interface AppProvider {

    /**
     * Subscribe to app updates.
     *
     * @return Stream of apps
     */
    Observable<List<AppInfoModel>> subscribeToAppUpdates();

    /**
     * Get all current apps.
     *
     * @return List of all current apps
     */
    List<AppInfoModel> getAll();
}
