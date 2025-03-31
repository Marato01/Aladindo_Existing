package com.reeman.delige.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.reeman.delige.R;

import java.util.Arrays;
import java.util.List;

public class PointNameInputDialog extends BaseDialog {

    private EditText etPointName;

    public PointNameInputDialog(@NonNull Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_point_name_input_dialog, null);
        GridLayout gridLayout = root.findViewById(R.id.gl_number);
        ImageButton tvBack = root.findViewById(R.id.tv_back);
        etPointName = root.findViewById(R.id.et_point_name);
        tvBack.setOnClickListener(v -> {
            String s = etPointName.getText().toString();
            if (s.length() >= 1) {
                etPointName.setText(s.substring(0, s.length() - 1));
            }
            etPointName.setSelection(etPointName.getText().length());
        });
        List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", getContext().getString(R.string.text_delete), getContext().getString(R.string.text_confirm));
        for (int i = 0; i < list.size(); i++) {
            TextView textView = new TextView(context);
            textView.setText(list.get(i));
            textView.setBackgroundColor(Color.parseColor("#787def"));
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(26);
            if (i == 10) {
                textView.setOnClickListener(v -> etPointName.setText(""));
            } else if (i == 11) {
                textView.setOnClickListener(v -> {
                    if (onClickListener != null) {
                        onClickListener.onClick(this, etPointName.getText().toString());
                    }
                });
            } else {
                textView.setOnClickListener(v -> {
                    String s = etPointName.getText().toString();
                    String text = s + textView.getText().toString();
                    etPointName.setText(text);
                    etPointName.setSelection(text.length());
                });
            }
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(160, 80);
            marginLayoutParams.leftMargin = 10;
            marginLayoutParams.rightMargin = 10;
            marginLayoutParams.topMargin = 10;
            marginLayoutParams.bottomMargin = 10;
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(marginLayoutParams);
            textView.setLayoutParams(layoutParams);
            gridLayout.addView(textView);
        }
        setContentView(root);
    }

    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(Dialog dialog, String name);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etPointName.setText("");
    }
}
