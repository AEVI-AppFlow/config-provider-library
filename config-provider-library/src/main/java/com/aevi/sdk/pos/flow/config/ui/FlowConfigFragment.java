package com.aevi.sdk.pos.flow.config.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aevi.sdk.app.scanning.model.AppInfoModel;
import com.aevi.sdk.flow.model.config.FlowApp;
import com.aevi.sdk.flow.model.config.FlowConfig;
import com.aevi.sdk.flow.model.config.FlowStage;
import com.aevi.sdk.pos.flow.config.ConfigComponentProvider;
import com.aevi.sdk.pos.flow.config.DefaultConfigProvider;
import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.R2;
import com.aevi.sdk.pos.flow.config.SettingsProvider;
import com.aevi.sdk.pos.flow.config.flowapps.AppProvider;
import com.aevi.sdk.pos.flow.config.flowapps.FlowProvider;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderFlowConfigStore;
import com.aevi.sdk.pos.flow.config.model.FlowAppInStage;
import com.aevi.sdk.pos.flow.config.ui.item.AbstractItem;
import com.aevi.sdk.pos.flow.config.ui.item.FlowAppItem;
import com.aevi.sdk.pos.flow.config.ui.item.FlowList;
import com.aevi.sdk.pos.flow.config.ui.item.FlowStageHeader;
import com.aevi.sdk.pos.flow.config.ui.view.DropDownSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;

import static com.aevi.sdk.pos.flow.config.SettingsProvider.*;

@SuppressWarnings("ConstantConditions")
public class FlowConfigFragment extends BaseFragment implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemMoveListener,
        FlowAppInStage.FlowAppStateChangedListener {

    private static final int MAX_HEADERS_FOR_AUTO_EXPAND = 2;
    private static final String TAG = FlowConfigFragment.class.getSimpleName();

    @BindView(R2.id.items)
    RecyclerView items;

    @BindView(R2.id.flow_spinner)
    DropDownSpinner flowSpinner;

    @BindView(R2.id.user_notification)
    TextView userNotification;

    @BindView(R2.id.enable_manual_configuration)
    Button enableManualConfiguration;

    @BindView(R2.id.view_read_only)
    Button viewReadOnly;

    @Inject
    AppProvider appProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ProviderFlowConfigStore flowConfigStore;

    @Inject
    FlowProvider flowProvider;

    private boolean isReadOnly;

    private ActionModeHelper actionModeHelper;
    private FlexibleAdapter<FlowStageHeader> adapter;
    private Map<String, FlowList> flowMap = new HashMap<>();
    private String currentFlowName;
    private boolean hasChanges;

    private int dragFromPosition = -1;
    private int dragToPosition = -1;

    public FlowConfigFragment(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public FlowConfigFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigComponentProvider.getFpsConfigComponent().inject(this);
        setRetainInstance(true);
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_flow_config;
    }

    @Override
    public void onStart() {
        super.onStart();
        setup();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hasChanges) {
            DefaultConfigProvider.Companion.notifyConfigUpdated(getContext());
        }
        adapter = null;
        items.setAdapter(null);
    }

    private void setup() {
        if (!isReadOnly && settingsProvider.shouldAutoGenerateConfigs()) {
            showUserNotification(R.string.auto_is_on);
            showHideAutoGenerateInfoViews(true);
            flowSpinner.setVisibility(View.GONE);
            items.setVisibility(View.GONE);
        } else {
            if (isReadOnly) {
                showUserNotification(R.string.is_read_only);
            } else {
                userNotification.setVisibility(View.GONE);
            }
            observe(appProvider.subscribeToAppUpdates()).subscribe(this::onFlowAppsChanged);
            observe(settingsProvider.subscribeToUpdates()).subscribe(this::onSettingsUpdated);
        }
    }

    private synchronized void onSettingsUpdated(String settingKey) {
        if (settingKey.equals(KEY_SHOW_FLOWS_NO_APPS) || settingKey.equals(KEY_SHOW_STAGES_NO_APPS)) {
            clearAndSetup(appProvider.getAll(), true);
        } else if (settingKey.equals(KEY_AUTO_GENERATE_CONFIGS)) {
            isReadOnly = false;
            disposeAll();
            adapter = null;
            setup();
        }
    }

    private void showUserNotification(int messageId) {
        userNotification.setText(messageId);
        userNotification.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.enable_manual_configuration)
    public void onEnableManualConfiguration() {
        settingsProvider.updateAutoGenerateConfig(false);
        showHideAutoGenerateInfoViews(false);
        setup();
    }

    @OnClick(R2.id.view_read_only)
    public void onViewReadOnly() {
        isReadOnly = true;
        showHideAutoGenerateInfoViews(false);
        setup();
    }

    private void showHideAutoGenerateInfoViews(boolean show) {
        enableManualConfiguration.setVisibility(show ? View.VISIBLE : View.GONE);
        viewReadOnly.setVisibility(show ? View.VISIBLE : View.GONE);
        userNotification.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnItemSelected(R2.id.flow_spinner)
    public void onFlowSelected(Spinner spinner, int position) {
        String flowName = (String) spinner.getItemAtPosition(position);
        adapter.updateDataSet(flowMap.get(flowName).getFlowStageHeaders());
        currentFlowName = flowName;
        Log.d(TAG, "Selected flow: " + currentFlowName);
    }

    private void onFlowAppsChanged(List<AppInfoModel> appInfoModels) {
        if (adapter == null) {
            setupList();
            scanForFlowApps(appInfoModels);
            setupFlowSpinner();
        } else {
            clearAndSetup(appInfoModels, false);
        }
    }

    private void clearAndSetup(List<AppInfoModel> appInfoModels, boolean recreateFlowSpinner) {
        flowMap.clear();
        scanForFlowApps(appInfoModels);
        if (adapter == null) {
            setupList();
        }
        if (recreateFlowSpinner) {
            adapter.notifyDataSetChanged();
            setupFlowSpinner();
        } else {
            adapter.updateDataSet(flowMap.get(currentFlowName).getFlowStageHeaders());
        }
    }

    private void scanForFlowApps(List<AppInfoModel> appInfoModels) {
        if (isAdded()) {
            updateFlowApps(appInfoModels);
        }
    }

    private void updateFlowApps(List<AppInfoModel> installedApps) {
        String[] allFlowConfigs = flowProvider.getAllFlowConfigs();
        for (String flowConfigJson : allFlowConfigs) {
            FlowConfig flowConfig = FlowConfig.fromJson(flowConfigJson);
            FlowList flowList = new FlowList();

            for (FlowStage stage : flowConfig.getStages(true)) {
                FlowStageHeader flowStageHeader = new FlowStageHeader(stage);
                List<FlowAppInStage> appsToDisplay = new ArrayList<>();
                List<FlowApp> appsDefinedInFlow = stage.getFlowApps();

                // Add apps from the flow
                for (FlowApp flowApp : appsDefinedInFlow) {
                    FlowAppInStage flowAppInStage = FlowAppInStage.fromFlowApp(flowApp, this, stage.getName());
                    appsToDisplay.add(flowAppInStage);
                    flowList.addFlowAppData(flowAppInStage);
                }
                // Cross-match installed apps against the flow ones
                matchWithInstalledApps(installedApps, flowConfig, stage, appsToDisplay, flowList);
                addAppsToStage(flowList, flowStageHeader, appsToDisplay);
            }

            // TODO maybe.maybe not
            if (flowList.getFlowStageHeaders().size() <= MAX_HEADERS_FOR_AUTO_EXPAND) {
                for (FlowStageHeader header : flowList.getFlowStageHeaders()) {
                    header.setExpanded(true);
                }
            }

            if (flowList.getFlowStageHeaders().size() > 0 || settingsProvider.shouldShowFlowsWithNoApps()) {
                flowMap.put(flowConfig.getName(), flowList);
            }
        }
    }

    private void matchWithInstalledApps(List<AppInfoModel> installedApps, FlowConfig flowConfig, FlowStage stage, List<FlowAppInStage> appsToDisplay,
                                        FlowList flowList) {
        for (AppInfoModel installedApp : installedApps) {
            // If the installed app supports the flow type (or all flow types) and the stage
            if (qualifiesForStage(installedApp, flowConfig.getType(), stage.getName())) {
                FlowAppInStage flowAppInStage = getMatchingData(appsToDisplay, installedApp);
                if (flowAppInStage == null && !isReadOnly) {
                    flowAppInStage = new FlowAppInStage(installedApp.getPaymentFlowServiceInfo().getId(), this);
                    flowAppInStage.setFlowStage(stage.getName());
                    appsToDisplay.add(flowAppInStage);
                    flowList.addFlowAppData(flowAppInStage);
                }
                if (flowAppInStage != null) {
                    flowAppInStage.setAppInfoModel(installedApp);
                }
            }
        }
    }

    private boolean qualifiesForStage(AppInfoModel installedApp, String flowType, String stageName) {
        return (installedApp.getPaymentFlowServiceInfo().getSupportedFlowTypes().isEmpty() ||
                installedApp.getPaymentFlowServiceInfo().supportsFlowType(flowType)) &&
                installedApp.supportsStage(stageName);
    }

    private void addAppsToStage(FlowList flowList, FlowStageHeader flowStageHeader, List<FlowAppInStage> appsToDisplay) {
        for (FlowAppInStage flowAppInStage : appsToDisplay) {
            // Apps that are not installed are only shown for read-only views (to avoid confusion when configuring)
            if (isReadOnly || flowAppInStage.isInstalled()) {
                flowStageHeader.addSubItem(new FlowAppItem(flowAppInStage, isReadOnly));
            }
        }
        if (flowStageHeader.hasSubItems() || settingsProvider.shouldShowStagesWithNoApps()) {
            flowList.addStageHeader(flowStageHeader);
        }
    }

    private FlowAppInStage getMatchingData(List<FlowAppInStage> dataList, AppInfoModel installedAppModel) {
        for (FlowAppInStage flowAppInStage : dataList) {
            if (flowAppInStage.getId().equals(installedAppModel.getPaymentFlowServiceInfo().getId())) {
                return flowAppInStage;
            }
        }
        return null;
    }

    private void setupFlowSpinner() {
        flowSpinner.setVisibility(View.VISIBLE);
        Set<String> flowNames = flowMap.keySet();
        String[] flowNameArray = flowNames.toArray(new String[flowNames.size()]);
        ArrayAdapter requestTypeAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout, flowNameArray);
        requestTypeAdapter.setDropDownViewResource(R.layout.dropdown_item);
        flowSpinner.setAdapter(requestTypeAdapter);
        flowSpinner.setSpinnerEventsListener(new DropDownSpinner.OnSpinnerEventsListener() {
            public void onSpinnerOpened() {
                flowSpinner.setSelected(true);
            }

            public void onSpinnerClosed() {
                flowSpinner.setSelected(false);
            }
        });
        // Try to find a sale flow and use as default
        for (int i = 0; i < flowNameArray.length; i++) {
            if (flowNameArray[i].toLowerCase().contains("sale")) {
                flowSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setupList() {
        items.setVisibility(View.VISIBLE);
        setupRecyclerView(items);
        List<FlowStageHeader> headers = new ArrayList<>();
        adapter = new FlexibleAdapter<>(headers, this);
        adapter.setAutoCollapseOnExpand(false)
                .setAutoScrollOnExpand(true)
                .setAnimateToLimit(Integer.MAX_VALUE)
                .setAnimationOnForwardScrolling(true)
                .setAnimationOnReverseScrolling(true);
        items.setAdapter(adapter);
        adapter.setStickyHeaders(false);
        adapter.setLongPressDragEnabled(!isReadOnly);
        items.setHasFixedSize(true);
        items.setItemAnimator(new DefaultItemAnimator());
        items.addItemDecoration(new FlexibleItemDecoration(getActivity())
                .withDivider(R.drawable.divider)
                .withDrawOver(true));
        actionModeHelper = new ActionModeHelper(adapter, 0);
    }

    @Override
    public boolean onItemClick(View view, int position) {
        actionModeHelper.onClick(position);
        return true;
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean shouldMoveItem(int fromPosition, int toPosition) {
        if (toPosition == 0) {
            return false;
        }
        AbstractItem from = adapter.getItem(fromPosition);
        AbstractItem to = adapter.getItem(toPosition > fromPosition ? toPosition : toPosition - 1);
        return from.getFlowStageName().equals(to.getFlowStageName());
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // For some stupid reason, the fromPosPosition updates for each intermediate position, so we just keep the first one...
        this.dragFromPosition = dragFromPosition == -1 ? fromPosition : dragFromPosition;
        this.dragToPosition = toPosition;
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // Drag & drop done...
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            int headerPos = Math.min(dragFromPosition, dragToPosition) - 1;
            FlowStageHeader draggedItemHeader = null;
            while (headerPos >= 0) {
                AbstractItem item = adapter.getItem(headerPos);
                if (item instanceof FlowStageHeader) {
                    draggedItemHeader = (FlowStageHeader) item;
                    break;
                }
                --headerPos;
            }
            if (draggedItemHeader != null) {
                int moveOffset = dragToPosition - dragFromPosition;
                draggedItemHeader.moveItem(dragFromPosition - headerPos - 1, moveOffset);
                updateAppsOrderInFlow(draggedItemHeader);
            }

            dragFromPosition = -1;
            dragToPosition = -1;
        }

    }

    private void updateAppsOrderInFlow(FlowStageHeader flowStageHeader) {
        List<FlowApp> flowApps = new ArrayList<>();
        for (FlowAppItem subItem : flowStageHeader.getSubItems()) {
            if (subItem.getFlowAppInStage().isInFlow()) {
                flowApps.add(subItem.getFlowAppInStage().getInFlowData());
            }
        }
        flowConfigStore.updateFlowAppOrder(currentFlowName, flowStageHeader.getFlowStageName(), flowApps);
        hasChanges = true;
    }

    private void toggleAllStages(FlowAppInStage flowAppInStage, boolean inFlow) {
        List<FlowAppInStage> flowAppInStageList = flowMap.get(currentFlowName).getFlowAppDataList(flowAppInStage.getId());
        if (inFlow) {
            flowConfigStore.addFlowAppToAllFlowStages(currentFlowName, flowAppInStageList);
        } else {
            flowConfigStore.removeFlowAppFromAllStages(currentFlowName, flowAppInStage.getId());
        }
        for (FlowAppInStage appData : flowAppInStageList) {
            appData.updateInFlowState(inFlow, false);
        }
        hasChanges = true;
    }

    private void toggleAppInStage(FlowAppInStage flowAppInStage, boolean add) {
        flowConfigStore.toggleAppInStage(currentFlowName, flowAppInStage, add);
        hasChanges = true;
    }

    @Override
    public void onFlowAppToggled(FlowAppInStage flowAppInStage, boolean inFlow) {
        toggleAppInStage(flowAppInStage, inFlow);
    }

    @Override
    public void onToggleAll(FlowAppInStage flowAppInStage, boolean inFlow) {
        toggleAllStages(flowAppInStage, inFlow);
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getNumStagesForAppInFlow(FlowAppInStage flowAppInStage) {
        return flowMap.get(currentFlowName).getFlowAppDataList(flowAppInStage.getId()).size();
    }
}
