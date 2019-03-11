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
package com.aevi.sdk.pos.flow.config.model;

import com.aevi.sdk.app.scanning.model.AppInfoModel;
import com.aevi.sdk.flow.model.config.FlowApp;

/**
 * Representation of a flow app in a particular stage.
 */
public class FlowAppInStage {

    private final String id;
    private final FlowAppStateChangedListener listener;
    private String flowStage;
    private FlowApp inFlowData;
    private boolean inFlow;
    private AppInfoModel appInfoModel;

    public FlowAppInStage(String id, FlowAppStateChangedListener listener) {
        this.id = id;
        this.listener = listener;
    }

    public String getId() {
        return id;
    }

    public AppInfoModel getAppInfoModel() {
        return appInfoModel;
    }

    public void setAppInfoModel(AppInfoModel appInfoModel) {
        this.appInfoModel = appInfoModel;
    }

    public boolean isInFlow() {
        return inFlow;
    }

    public boolean isInstalled() {
        return appInfoModel != null;
    }

    public FlowApp getInFlowData() {
        return inFlowData;
    }

    public String getFlowStage() {
        return flowStage;
    }

    public void setFlowStage(String flowStage) {
        this.flowStage = flowStage;
    }

    public void setInFlowData(FlowApp inFlowData) {
        boolean hasChanged = this.inFlowData != null && !this.inFlowData.equals(inFlowData);
        this.inFlowData = inFlowData;
        this.inFlow = true;
        if (hasChanged) {
            listener.onFlowAppToggled(this, inFlow);
        }
    }

    public void updateInFlowState(boolean inFlow, boolean notifyListener) {
        this.inFlow = inFlow;
        if (notifyListener) {
            listener.onFlowAppToggled(this, inFlow);
        }
    }

    public void notifyToggleAll(boolean inFlow) {
        listener.onToggleAll(this, inFlow);
    }

    public static FlowAppInStage fromFlowApp(FlowApp flowApp, FlowAppStateChangedListener listener, String stage) {
        FlowAppInStage flowAppInStage = new FlowAppInStage(flowApp.getId(), listener);
        flowAppInStage.setInFlowData(flowApp);
        flowAppInStage.setFlowStage(stage);
        return flowAppInStage;
    }

    public interface FlowAppStateChangedListener {
        void onFlowAppToggled(FlowAppInStage flowAppInStage, boolean inFlow);

        void onToggleAll(FlowAppInStage flowAppInStage, boolean inFlow);
    }
}
