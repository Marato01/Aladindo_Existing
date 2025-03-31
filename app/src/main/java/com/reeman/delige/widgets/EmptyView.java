package com.reeman.delige.widgets;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.reeman.delige.R;

public class EmptyView extends LinearLayout {

    private TextView tvErrorPrompt;
    private Button btnRefresh;

    public EmptyView(Context context, OnRefreshBtnClickListener listener) {
        super(context);
        this.listener = listener;
        init(context);
    }

    private void init(Context context) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_no_data, null);
        btnRefresh = root.findViewById(R.id.btn_refresh);
        tvErrorPrompt = root.findViewById(R.id.tv_error_prompt);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRefreshBtnClick();
                }
            }
        });
        addView(root);
        MarginLayoutParams layoutParams = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = 40;
        SwipeRefreshLayout.LayoutParams swipeRefreshLayoutParams = new SwipeRefreshLayout.LayoutParams(layoutParams);
        /*RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.topMargin = 40;
        layoutParams.addRule(RelativeLayout.BELOW, R.id.table_group);
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.btn_start);*/
        setLayoutParams(swipeRefreshLayoutParams);
    }

    public void updatePrompt(String prompt) {
        tvErrorPrompt.setText(prompt);
    }

    private OnRefreshBtnClickListener listener;

    public interface OnRefreshBtnClickListener {
        void onRefreshBtnClick();
    }
}
