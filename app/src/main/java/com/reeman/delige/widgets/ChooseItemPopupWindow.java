package com.reeman.delige.widgets;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.reeman.delige.R;

import java.util.List;

public class ChooseItemPopupWindow extends ListPopupWindow {

    public ChooseItemPopupWindow(Context context, View anchor, List<String> items) {
        super(context);
        setAnchorView(anchor);
        setWidth(240);
        setHeight(160);
        setDropDownGravity(Gravity.CENTER);
        View promptView = LayoutInflater.from(context).inflate(R.layout.layout_pop_up_window_prompt, null);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_common_dialog));
        setAnimationStyle(R.style.popupWindowAlphaAnimation);
        setPromptView(promptView);
        setAdapter(new ArrayAdapter<>(context, R.layout.layout_popup_item, R.id.tv_spinner_item, items));
        setOnItemClickListener((parent, view, position, id) -> {
            if (onItemChosenListener != null) {
                onItemChosenListener.onChosen(ChooseItemPopupWindow.this, position);
            }
        });
    }

    private OnItemChosenListener onItemChosenListener;

    public void setOnItemChosenListener(OnItemChosenListener onItemChosenListener) {
        this.onItemChosenListener = onItemChosenListener;
    }

    public interface OnItemChosenListener {
        void onChosen(ListPopupWindow window, int position);
    }

}
