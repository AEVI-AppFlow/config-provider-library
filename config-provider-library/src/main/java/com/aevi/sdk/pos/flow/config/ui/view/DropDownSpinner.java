/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aevi.sdk.pos.flow.config.ui.view;

import android.content.Context;

import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

public class DropDownSpinner extends AppCompatSpinner {

    private static final String TAG = DropDownSpinner.class.getSimpleName();
    private OnSpinnerEventsListener listener;
    private boolean openInitiated = false;

    public DropDownSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public DropDownSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DropDownSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropDownSpinner(Context context, int mode) {
        super(context, mode);
    }

    public DropDownSpinner(Context context) {
        super(context);
    }

    public interface OnSpinnerEventsListener {

        void onSpinnerOpened();

        void onSpinnerClosed();
    }

    @Override
    public boolean performClick() {

        openInitiated = true;
        if (listener != null) {
            listener.onSpinnerOpened();
        }
        return super.performClick();
    }

    public void setSpinnerEventsListener(OnSpinnerEventsListener onSpinnerEventsListener) {
        listener = onSpinnerEventsListener;
    }

    /**
     * Propagate the closed Spinner event to the listener from outside.
     */
    public void performClosedEvent() {
        openInitiated = false;
        if (listener != null) {
            listener.onSpinnerClosed();
        }
    }

    /**
     * A boolean flag indicating that the Spinner triggered an open event.
     *
     * @return true for opened Spinner
     */
    public boolean hasBeenOpened() {
        return openInitiated;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasBeenOpened() && hasWindowFocus) {
            performClosedEvent();
        }
    }
}
