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
package com.aevi.sdk.pos.flow.config.ui.item;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.aevi.android.rxmessenger.activity.ObservableActivityHelper;
import com.aevi.sdk.flow.model.config.FlowApp;
import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.model.FlowAppInStage;
import com.aevi.sdk.pos.flow.config.ui.FlowAppDetailsActivity;

import java.util.List;
import java.util.UUID;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

import static com.aevi.sdk.pos.flow.config.ui.view.IconHelper.getIcon;

public class FlowAppItem extends AbstractItem<FlowAppItem.ChildViewHolder> {

    private final FlowAppInStage flowAppInStage;
    private final boolean readOnly;

    public FlowAppItem(FlowAppInStage flowAppInStage, boolean readOnly) {
        super(UUID.randomUUID().toString());
        this.flowAppInStage = flowAppInStage;
        this.readOnly = readOnly;
        if (flowAppInStage.isInstalled()) {
            setTitle(flowAppInStage.getAppInfoModel().getPaymentFlowServiceInfo().getDisplayName());
        } else {
            setTitle(flowAppInStage.getId());
        }
    }

    public FlowAppInStage getFlowAppInStage() {
        return flowAppInStage;
    }

    @Override
    public boolean shouldNotifyChange(IFlexible newItem) {
        FlowAppItem flowAppItem = (FlowAppItem) newItem;
        return !title.equals(flowAppItem.getTitle());
    }

    @Override
    public int getLayoutRes() {
        return R.layout.snippet_flow_app;
    }

    @Override
    public ChildViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ChildViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ChildViewHolder holder, int position, List<Object> payloads) {
        holder.toggle.setOnCheckedChangeListener(null);
        holder.title.setText(title);
        Drawable icon = getIcon(holder.title.getContext(), flowAppInStage.getAppInfoModel());
        if (icon == null) {
            icon = holder.itemView.getContext().getDrawable(R.drawable.ic_not_installed);
        }
        holder.icon.setImageDrawable(icon);
        if (readOnly) {
            holder.toggle.setVisibility(View.GONE);
            holder.title.setAlpha(flowAppInStage.isInstalled() ? 1.0f : 0.5f);
        } else {
            holder.toggle.setChecked(flowAppInStage.isInFlow());
            setItemInFlowState(holder, flowAppInStage.isInFlow(), false);
            holder.toggle.setOnCheckedChangeListener(
                    (compoundButton, checked) -> setItemInFlowState(holder, checked, true));
        }

        setupAppDetails(holder);
    }

    private void setupAppDetails(ChildViewHolder holder) {
        holder.details.setVisibility(flowAppInStage.isInFlow() ? View.VISIBLE : View.INVISIBLE);
        if (flowAppInStage.isInFlow()) {
            holder.details.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), FlowAppDetailsActivity.class);
                intent.putExtra(FlowAppDetailsActivity.KEY_FLOW_APP_NAME, title);
                intent.putExtra(FlowAppDetailsActivity.KEY_FLOW_APP, flowAppInStage.getInFlowData().toJson());
                intent.putExtra(FlowAppDetailsActivity.KEY_READ_ONLY, readOnly);

                ObservableActivityHelper<FlowApp> helper = ObservableActivityHelper.createInstance(view.getContext(), intent);
                helper.startObservableActivity().subscribe(flowAppInStage::setInFlowData, throwable -> Log.e("Meep", "Error", throwable));
            });
        }
    }

    private void setItemInFlowState(ChildViewHolder holder, boolean inFlow, boolean notifyListener) {
        holder.icon.setAlpha(inFlow ? 1.0f : 0.5f);
        holder.title.setAlpha(inFlow ? 1.0f : 0.5f);
        flowAppInStage.updateInFlowState(inFlow, notifyListener);
        setupAppDetails(holder);
        setDraggable(inFlow);
        if (notifyListener && flowAppInStage.getNumStagesInCurrentFlow() > 1) {
            showToggleAppPopup(holder, inFlow);
        }
    }

    private void showToggleAppPopup(ChildViewHolder holder, boolean inFlow) {
        View popupView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.snippet_toggle_all, null);
        int popupHeight = holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.flow_popup_height);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, popupHeight, true);
        popupWindow.setBackgroundDrawable(holder.itemView.getContext().getDrawable(R.drawable.popup_background));
        popupWindow.setElevation(8);

        CheckBox toggleBox = popupView.findViewById(R.id.toggle_check);
        toggleBox.setText(inFlow ? R.string.enable_all_stages : R.string.disable_all_stages);
        toggleBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            flowAppInStage.notifyToggleAll(inFlow);
            new Handler().postDelayed(popupWindow::dismiss, 750);
        });
        RecyclerView recyclerView = (RecyclerView) holder.itemView.getParent();
        if (recyclerView.getBottom() - holder.itemView.getBottom() < (popupHeight * 2)) {
            popupWindow.showAsDropDown(holder.itemView, 20, -(holder.itemView.getHeight() + popupHeight), Gravity.RIGHT);
        } else {
            popupWindow.showAsDropDown(holder.itemView, 20, 0, Gravity.RIGHT);
        }

        new Handler().postDelayed(() -> {
            if (!toggleBox.isChecked()) {
                popupWindow.dismiss();
            }
        }, 2000);
    }

    static final class ChildViewHolder extends FlexibleViewHolder {

        TextView title;
        ImageView icon;
        SwitchCompat toggle;
        ImageButton details;

        public ChildViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.title = view.findViewById(R.id.flow_app_label);
            this.icon = view.findViewById(R.id.flow_app_icon);
            this.toggle = view.findViewById(R.id.flow_app_switch);
            this.details = view.findViewById(R.id.flow_app_details);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            toggle.toggle();
        }

        @Override
        public float getActivationElevation() {
            return ViewUtils.dpToPx(itemView.getContext(), 5f);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f);
        }
    }

    @Override
    public String getFlowStageName() {
        return flowAppInStage.getFlowStage();
    }

    @Override
    public String toString() {
        return "SubItem[" + super.toString() + "]";
    }

}