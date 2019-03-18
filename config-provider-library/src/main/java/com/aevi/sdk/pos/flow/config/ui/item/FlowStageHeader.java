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

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.aevi.sdk.flow.model.config.FlowStage;
import com.aevi.sdk.pos.flow.config.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Header representing a flow stage.
 */
public class FlowStageHeader extends AbstractItem<FlowStageHeader.StageViewHolder>
        implements IExpandable<FlowStageHeader.StageViewHolder, FlowAppItem>, IHeader<FlowStageHeader.StageViewHolder> {

    private boolean expanded = false;
    private List<FlowAppItem> flowAppItems;
    private StageViewHolder viewHolder;
    private FlowStage flowStage;

    public FlowStageHeader(FlowStage flowStage) {
        super(flowStage.getName());
        setDraggable(false);
        setHidden(false);
        setSelectable(false);
        this.flowStage = flowStage;
    }

    public FlowStage getFlowStage() {
        return flowStage;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpandedIfHasChildren() {
        if (hasSubItems()) {
            setExpanded(true);
        }
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (viewHolder != null) {
            if (expanded) {
                animateExpand(false);
            } else {
                animateCollapse();
            }
        }
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<FlowAppItem> getSubItems() {
        return flowAppItems;
    }

    public void moveItem(int fromPos, int offset) {
        FlowAppItem item = flowAppItems.remove(fromPos);
        flowAppItems.add(fromPos + offset, item);
    }

    public final boolean hasSubItems() {
        return flowAppItems != null && flowAppItems.size() > 0;
    }

    public boolean removeSubItem(FlowAppItem item) {
        return item != null && flowAppItems.remove(item);
    }

    public boolean removeSubItem(int position) {
        if (flowAppItems != null && position >= 0 && position < flowAppItems.size()) {
            flowAppItems.remove(position);
            return true;
        }
        return false;
    }

    public void addSubItem(FlowAppItem flowAppItem) {
        if (flowAppItems == null) {
            flowAppItems = new ArrayList<>();
        }
        flowAppItems.add(flowAppItem);
    }

    public void addSubItem(int position, FlowAppItem flowAppItem) {
        if (flowAppItems != null && position >= 0 && position < flowAppItems.size()) {
            flowAppItems.add(position, flowAppItem);
        } else {
            addSubItem(flowAppItem);
        }
    }

    private void animateExpand(boolean immediate) {
        if (hasSubItems()) {
            rotateArrow(360, 180, immediate ? 0 : 300);
        }
    }

    private void animateCollapse() {
        rotateArrow(180, 360, 300);
    }

    private void rotateArrow(int fromDegrees, int toDegrees, int duration) {
        if (viewHolder != null) {
            RotateAnimation rotate =
                    new RotateAnimation(fromDegrees, toDegrees, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(duration);
            rotate.setFillAfter(true);
            viewHolder.arrow.startAnimation(rotate);
            viewHolder.getContentView().setActivated(false);
        }
    }

    @Override
    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.snippet_flow_stage;
    }

    @Override
    public StageViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new StageViewHolder(view, adapter);
    }

    @Override
    public void onViewAttached(FlexibleAdapter<IFlexible> adapter, StageViewHolder holder, int position) {
        super.onViewAttached(adapter, holder, position);
        if (isExpanded()) {
            animateExpand(true);
        }
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, StageViewHolder holder, int position, List payloads) {
        this.viewHolder = holder;
        holder.title.setText(flowStage.getName().replace('_', '-'));
        if (hasSubItems()) {
            holder.getContentView().setEnabled(true);
            holder.getContentView().setAlpha(1.0f);
            setEnabled(true);
            holder.arrow.setVisibility(View.VISIBLE);
            if (isExpanded()) {
                animateExpand(true);
            }
        } else {
            holder.getContentView().setEnabled(false);
            holder.getContentView().setAlpha(0.8f);
            setEnabled(false);
            holder.arrow.setVisibility(View.INVISIBLE);
        }
    }

    static class StageViewHolder extends ExpandableViewHolder {

        TextView title;
        ImageView arrow;

        StageViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, false);
            title = view.findViewById(R.id.stage_title);
            arrow = view.findViewById(R.id.stage_arrow);
            setFullSpan(true);
        }


    }

    @Override
    public String getFlowStageName() {
        return flowStage.getName();
    }

    @Override
    public String toString() {
        return "ExpandableHeaderItem[" + super.toString() + "//SubItems" + flowAppItems + "]";
    }

}