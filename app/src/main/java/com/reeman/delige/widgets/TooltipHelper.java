package com.reeman.delige.widgets;

import android.content.Context;
import android.view.View;

import com.reeman.delige.R;

import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;

public class TooltipHelper {

    private static TooltipHelper INSTANCE;

    private Tooltip tooltip;
    private View anchorView;
    private View parentView;

    public static TooltipHelper create(Context context, View anchor, View parent, String tip) {
        if (INSTANCE == null) {
            INSTANCE = new TooltipHelper(context, anchor, parent, tip);
        }
        return INSTANCE;
    }

    public TooltipHelper(Context context, View anchor, View parent, String tip) {
        anchorView = anchor;
        parentView = parent;
        tooltip = new Tooltip.Builder(context)
                .anchor(anchor, 0, 0, true)
                .text(tip)
                .styleId(R.style.ToolTipAltStyle)
                .maxWidth(500)
                .arrow(true)
                .floatingAnimation(Tooltip.Animation.Companion.getSLOW())
                .closePolicy(getDefaultClosePolicy())
                .showDuration(-1)
                .overlay(true)
                .create();
    }


    public void show() {
        anchorView.post(() -> {
            try {
                tooltip.show(parentView, Tooltip.Gravity.TOP, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void update(String text) {
        tooltip.update(text);
    }

    public void dismiss() {
        tooltip.dismiss();
        anchorView = null;
        parentView = null;
        INSTANCE = null;
    }

    private static ClosePolicy getDefaultClosePolicy() {
        return new ClosePolicy.Builder().inside(false).outside(false).consume(false).build();
    }

}
