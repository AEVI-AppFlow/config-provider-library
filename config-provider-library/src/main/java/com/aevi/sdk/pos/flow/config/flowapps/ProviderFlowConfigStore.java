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
import android.support.annotation.NonNull;
import android.util.Log;

import com.aevi.sdk.app.scanning.model.AppInfoModel;
import com.aevi.sdk.flow.model.config.FlowApp;
import com.aevi.sdk.flow.model.config.FlowConfig;
import com.aevi.sdk.flow.model.config.FlowConfigBuilder;
import com.aevi.sdk.flow.model.config.FlowStage;
import com.aevi.sdk.pos.flow.config.model.FlowAppInStage;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import okio.BufferedSink;
import okio.Okio;

public class ProviderFlowConfigStore implements FlowProvider {

    private static final String TAG = ProviderFlowConfigStore.class.getSimpleName();

    private final Context context;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String CONFIG_FILE_NAME = "_flow_config.json";

    private Set<String> allFlowTypes;
    private Set<String> allFlowNames;

    private final int[] defaultFlowConfigs;
    private Map<String, FlowConfig> defaultConfigCache;

    public ProviderFlowConfigStore(Context context) {
        this.context = context;
        this.defaultFlowConfigs = new int[0];
        allFlowTypes = new HashSet<>();
        allFlowNames = new HashSet<>();
    }

    public ProviderFlowConfigStore(Context context, int[] defaultFlowConfigs) {
        this.context = context;
        this.defaultFlowConfigs = defaultFlowConfigs;
        allFlowTypes = new HashSet<>();
        allFlowNames = new HashSet<>();
        if (hasStoredConfigs()) {
            Log.d(TAG, "Found stored configs - parsing");
            parseStoredFlowConfigs();
        } else {
            Log.d(TAG, "No stored configs - writing defaults");
            writeDefaultFlowConfigs();
        }
        cacheDefaultConfigs();
    }

    private void writeDefaultFlowConfigs() {
        lock.writeLock().lock();
        try {
            for (int config : defaultFlowConfigs) {
                addFlowConfig(config);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write defaults", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<String> getAllFlowNames() {
        return allFlowNames;
    }

    public Set<String> getAllFlowTypes() {
        return allFlowTypes;
    }

    public void removeFlowAppFromAllStages(String flowName, String id) {
        removeAppWithId(flowName, id);
    }

    public void addAllToFlowConfigs(List<AppInfoModel> appInfoModels, Set<String> packageIgnoreList) {
        lock.writeLock().lock();
        try {
            for (String name : allFlowNames) {
                FlowConfig flowConfig = readFlowConfig(name);
                for (AppInfoModel appInfoModel : appInfoModels) {
                    if (appInfoModel.getPaymentFlowServiceInfo().supportsFlowType(flowConfig.getType()) &&
                            !packageIgnoreList.contains(appInfoModel.getPaymentFlowServiceInfo().getId())) {
                        doUpdateFlowAppInConfig(flowConfig, appInfoModel);
                    }
                }
                saveFlowConfig(flowConfig);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void doUpdateFlowAppInConfig(FlowConfig flowConfig, AppInfoModel appInfoModel) {
        Set<String> stages = appInfoModel.getPaymentFlowServiceInfo().getStages();
        for (String stageName : stages) {
            FlowStage stage = flowConfig.getStage(stageName);
            if (stage != null && !stageHasApp(stage, appInfoModel.getPaymentFlowServiceInfo().getPackageName())) {
                FlowApp flowApp = new FlowApp(appInfoModel.getPaymentFlowServiceInfo().getPackageName());
                List<FlowApp> flowApps = stage.getFlowApps();
                int indexForApp = getFlowAppIndex(flowApps, flowApp.getId());
                if (indexForApp == -1) {
                    flowApps.add(flowApp);
                } else {
                    flowApps.set(indexForApp, flowApp);
                }
            }
        }
    }

    public void addFlowAppToAllFlowStages(String flowName, List<FlowAppInStage> flowAppInStageList) {
        lock.writeLock().lock();
        try {
            FlowConfig flowConfig = readFlowConfig(flowName);
            for (FlowAppInStage flowAppInStage : flowAppInStageList) {
                addOrUpdateApp(flowAppInStage, flowConfig);
            }
            saveFlowConfig(flowConfig);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateFlowAppOrder(String flowName, String stage, List<FlowApp> flowApps) {
        lock.writeLock().lock();
        try {
            FlowConfig flowConfig = readFlowConfig(flowName);
            FlowStage flowStage = flowConfig.getStage(stage);
            flowStage.setFlowApps(flowApps);
            saveFlowConfig(flowConfig);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeAppWithId(String flowName, String flowAppId) {
        lock.writeLock().lock();
        try {
            FlowConfig flowConfig = readFlowConfig(flowName);
            for (String stage : flowConfig.getAllStageNames()) {
                removeAppsWithIdFromList(flowConfig.getAppsForStage(stage), flowAppId);
            }
            saveFlowConfig(flowConfig);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeAppsWithIdFromList(List<FlowApp> flowApps, String appId) {
        Iterator<FlowApp> iterator = flowApps.iterator();
        while (iterator.hasNext()) {
            FlowApp flowApp = iterator.next();
            if (flowApp.getId().equals(appId)) {
                iterator.remove();
            }
        }
    }

    public void toggleAppInStage(String flowName, FlowAppInStage flowApp, boolean add) {
        lock.writeLock().lock();
        try {
            FlowConfig flowConfig = readFlowConfig(flowName);
            if (add) {
                addOrUpdateApp(flowApp, flowConfig);
            } else {
                removeAppsWithIdFromList(flowConfig.getStage(flowApp.getFlowStage()).getFlowApps(), flowApp.getId());
            }
            saveFlowConfig(flowConfig);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean stageHasApp(FlowStage flowStage, String packageName) {
        for (FlowApp flowApp : flowStage.getFlowApps()) {
            if (flowApp.getId().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void addOrUpdateApp(FlowAppInStage flowAppInStage, FlowConfig flowConfig) {
        FlowStage flowStage = flowConfig.getStage(flowAppInStage.getFlowStage());
        List<FlowApp> flowApps = flowStage.getFlowApps();
        int indexForApp = getFlowAppIndex(flowApps, flowAppInStage.getId());
        FlowApp inFlowData = flowAppInStage.getInFlowData();
        if (inFlowData == null) {
            FlowApp fromDefaultFlow = getFlowAppFromDefaultFlow(flowConfig.getName(), flowAppInStage.getFlowStage(), flowAppInStage.getId());
            inFlowData = fromDefaultFlow != null ? fromDefaultFlow : new FlowApp(flowAppInStage.getId());
            flowAppInStage.setInFlowData(inFlowData);
        }

        if (indexForApp == -1) {
            flowApps.add(inFlowData);
        } else {
            flowApps.set(indexForApp, inFlowData);
        }
    }

    private int getFlowAppIndex(List<FlowApp> flowApps, String flowAppId) {
        for (int i = 0; i < flowApps.size(); i++) {
            if (flowApps.get(i).getId().equals(flowAppId)) {
                return i;
            }
        }
        return -1;
    }

    private void saveFlowConfig(FlowConfig flowConfig) {
        saveFlowConfig(flowConfig.getName(), flowConfig.toJson());
    }

    private void saveFlowConfig(String name, String json) {
        lock.writeLock().lock();
        try {
            Log.d(TAG, "SAVE: " + json);
            File file = new File(context.getFilesDir(), getConfigFileName(name));
            Log.d(TAG, "To file: " + file.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedSink buffer = Okio.buffer(Okio.sink(outputStream));
            buffer.writeString(json, Charset.defaultCharset());
            buffer.flush();
            buffer.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to update flow config", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getConfigFileName(String name) {
        return name.toLowerCase().replace(' ', '_') + CONFIG_FILE_NAME;
    }

    @NonNull
    public String readFlowConfigJson(String flowName) {
        if (flowName == null) {
            throw new IllegalArgumentException("Flow Config name cannot be null");
        }
        return doReadFlowConfig(getConfigFileName(flowName));
    }

    private void parseStoredFlowConfigs() {
        lock.readLock().lock();
        try {
            String[] configs = context.getFilesDir().list((file1, name) -> name.endsWith(".json"));
            for (String config : configs) {
                FlowConfig flowConfig = FlowConfig.fromJson(doReadFlowConfig(config));
                if (flowConfig != null) {
                    Log.d(TAG, "Found valid flow: " + flowConfig.getName());
                    allFlowNames.add(flowConfig.getName());
                    allFlowTypes.add(flowConfig.getType());
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private String doReadFlowConfig(String fileName) {
        lock.readLock().lock();
        try {
            File file = new File(context.getFilesDir(), fileName);
            Log.d(TAG, "Reading file: " + file.getAbsolutePath());
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                String json = Okio.buffer(Okio.source(fileInputStream)).readString(Charset.defaultCharset());
                Log.d(TAG, "Content: " + json);
                return json;
            }
        } catch (IOException e) {
            Log.e(TAG, String.format("Failed to read flow config. Creating new default config: %s", e.getMessage()));
        } finally {
            lock.readLock().unlock();
        }
        return "";
    }

    public String[] getAllFlowConfigs() {
        lock.readLock().lock();
        try {
            String[] flowConfigs = new String[allFlowNames.size()];
            int index = 0;
            for (String flowName : allFlowNames) {
                flowConfigs[index++] = readFlowConfigJson(flowName);
            }
            Log.d(TAG, "All flow configs: " + Arrays.toString(flowConfigs));
            return flowConfigs;
        } catch (Exception e) {
            return new String[0];
        } finally {
            lock.readLock().unlock();
        }
    }

    @NonNull
    public FlowConfig readFlowConfig(String flowName) {
        String json = readFlowConfigJson(flowName);
        FlowConfig flowConfig = null;
        if (json != null && !json.isEmpty()) {
            flowConfig = FlowConfig.fromJson(json);
        }

        if (flowConfig == null) {
            Log.w(TAG, "No config found for: " + flowName + ", writing empty config");
            flowConfig = new FlowConfigBuilder().withName(flowName).withType(flowName).build();
            saveFlowConfig(flowConfig);
        }

        return flowConfig;
    }

    private boolean hasStoredConfigs() {
        File file = new File(context.getFilesDir().getAbsolutePath());
        return file.list().length > 0;
    }

    public void resetFlowConfigs() {
        deleteStoredFlowConfigs();
        writeDefaultFlowConfigs();
    }

    /**
     * Replaces any existing flow config with the same flow name as the provided config.
     */
    public void replaceFlowConfig(int flowConfigRes) {
        lock.writeLock().lock();
        try {
            String json = readFile(flowConfigRes);
            FlowConfig flowConfig = FlowConfig.fromJson(json);
            String configFileName = getConfigFileName(flowConfig.getName());
            context.deleteFile(configFileName);
            allFlowNames.add(flowConfig.getName());
            allFlowTypes.add(flowConfig.getType());
            saveFlowConfig(flowConfig);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addFlowConfig(int defaultConfigId) {
        String json = readFile(defaultConfigId);
        FlowConfig flowConfig = FlowConfig.fromJson(json);
        allFlowNames.add(flowConfig.getName());
        allFlowTypes.add(flowConfig.getType());
        saveFlowConfig(flowConfig);
    }

    private void cacheDefaultConfigs() {
        defaultConfigCache = new HashMap<>();
        for (int defaultFlowConfig : defaultFlowConfigs) {
            String json = readFile(defaultFlowConfig);
            FlowConfig flowConfig = FlowConfig.fromJson(json);
            if (flowConfig != null) {
                defaultConfigCache.put(flowConfig.getName(), flowConfig);
            }
        }
    }

    private FlowApp getFlowAppFromDefaultFlow(String flowName, String stage, String flowAppId) {
        FlowConfig flowConfig = defaultConfigCache.get(flowName);
        if (flowConfig != null && flowConfig.hasStage(stage)) {
            List<FlowApp> flowApps = flowConfig.getStage(stage).getFlowApps();
            int flowAppIndex = getFlowAppIndex(flowApps, flowAppId);
            if (flowAppIndex >= 0) {
                return flowApps.get(flowAppIndex);
            }
        }
        return null;
    }

    private String readFile(int resourceFile) {
        if (context != null) {
            try {
                InputStream is = context.getResources().openRawResource(resourceFile);
                return Okio.buffer(Okio.source(is)).readString(Charset.defaultCharset());
            } catch (IOException e) {
                Log.e(TAG, "Failed to read config", e);
            }
        }
        return "";
    }

    private void deleteStoredFlowConfigs() {
        lock.writeLock().lock();
        try {
            for (String fileName : context.fileList()) {
                if (fileName.endsWith(CONFIG_FILE_NAME)) {
                    context.deleteFile(fileName);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
