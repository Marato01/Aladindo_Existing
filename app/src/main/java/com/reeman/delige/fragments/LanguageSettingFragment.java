package com.reeman.delige.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.reeman.delige.R;
import com.reeman.delige.activities.LanguageSelectActivity;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.base.BaseFragment;

public class LanguageSettingFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_language_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout llSwitchLanguage = root.findViewById(R.id.ll_switch_language);
        llSwitchLanguage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_switch_language) {
            BaseActivity.startup(requireContext(), LanguageSelectActivity.class);
        }
    }
}
