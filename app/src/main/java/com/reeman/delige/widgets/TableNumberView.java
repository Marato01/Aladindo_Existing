package com.reeman.delige.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.reeman.delige.R;

public class TableNumberView extends RelativeLayout {

    private TextView content;
    private ImageView badgeView;

    public TableNumberView(Context context) {
        super(context);
        content = new TextView(context);
        content.setSingleLine();
        content.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 10;
        layoutParams.rightMargin = 10;
        layoutParams.addRule(CENTER_IN_PARENT);
        content.setLayoutParams(layoutParams);
        addView(content);

        badgeView = new ImageView(getContext());
        badgeView.setVisibility(INVISIBLE);
        badgeView.setPadding(5, 5, 5, 5);
        badgeView.setImageResource(R.drawable.ic_check_24);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.addRule(ALIGN_PARENT_TOP);
        badgeView.setLayoutParams(lp);
        addView(badgeView);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
    }

    public void setTextSize(float textSize) {
        content.setTextSize(textSize);
    }

    public String getText() {
        return content.getText().toString();
    }

    public void setTextColor(@ColorInt int color) {
        content.setTextColor(color);
    }

    public void setText(String text) {
        content.setText(text);
    }

    public void select(boolean selected) {
        badgeView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
    }
}
