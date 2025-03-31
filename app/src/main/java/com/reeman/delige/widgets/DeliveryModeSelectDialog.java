package com.reeman.delige.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.TextView;

import com.reeman.delige.R;

public class DeliveryModeSelectDialog extends BaseDialog {

    private final OnDeliveryModeSelectListener listener;

    public DeliveryModeSelectDialog(Context context, int currentDeliveryMode, OnDeliveryModeSelectListener listener) {
        super(context);
        this.listener = listener;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        ViewGroup root = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_delivery_mode_select_dialog, null);
        GridLayout glDeliveryModes = root.findViewById(R.id.gl_delivery_modes);
        View modeView;
        int[] selectedIds = new int[]{R.drawable.icon_delivery_food_mode_selected, R.drawable.icon_cruise_mode_selected, R.drawable.icon_recycling_mode_selected, R.drawable.icon_birthday_mode_selected,R.drawable.icon_multi_delivery_mode_selected, R.drawable.icon_recycling_mode_selected, };
        int[] normalIds = new int[]{R.drawable.icon_delivery_food_mode_normal, R.drawable.icon_cruise_mode_normal, R.drawable.icon_recycling_mode_normal, R.drawable.icon_birthday_mode_normal,R.drawable.icon_multi_delivery_mode_normal, R.drawable.icon_recycling_mode_normal, };
        for (int i = 0; i < glDeliveryModes.getChildCount(); i++) {
            modeView = glDeliveryModes.getChildAt(i);
            int mode = Integer.parseInt((String) modeView.getTag());
            modeView.setOnClickListener(v -> {
                dismiss();
                if (listener != null)
                    listener.onDeliveryModeSelect(Integer.parseInt((String) v.getTag()));
            });
            TextView textView = ((TextView) modeView);
            if (mode == currentDeliveryMode) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, selectedIds[i], 0, 0);
                textView.setTextColor(Color.parseColor("#ff33b5e5"));
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, normalIds[i], 0, 0);
            }
        }
        setContentView(root);
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.drawable.bg_delivery_mode_dialog);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = 500;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    public interface OnDeliveryModeSelectListener {
        void onDeliveryModeSelect(int mode);
    }
}
