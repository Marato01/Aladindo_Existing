package com.reeman.delige.activities;

import android.app.Dialog;
import android.view.View;

import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;


import static com.reeman.delige.base.BaseApplication.ros;


public class MissPositionActivity extends BaseActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_miss_position;
    }

    @Override
    protected void initCustomView() {
        setOnClickListeners(R.id.btn_relocate);
        $(R.id.tv_miss_position_prompt).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return true;
            }
        });
    }

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_relocate) {
            EasyDialog.getInstance(this).confirm(getString(R.string.text_please_check_current_location, DestHelper.getInstance().getChargePoint()), new EasyDialog.OnViewClickListener() {
                @Override
                public void onViewClick(Dialog dialog, int id) {
                    dialog.dismiss();
                    if (id == R.id.btn_confirm) {
                        mHandler.postDelayed(() -> {
                            EasyDialog.getLoadingInstance(MissPositionActivity.this).loading(getString(R.string.text_relocating));
                                ros.relocateByPointName(DestHelper.getInstance().getChargePoint());
                        }, 300);
                    }
                }
            });
        }
    }

    @Override
    protected void onCustomInitPose(String currentPosition) {
        ToastUtils.showShortToast(getString(R.string.text_locate_finish));
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        mHandler.postDelayed(() -> ros.setMissPoseUpload(true), 1000);
        finish();
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onPointNotFound(Event.OnPointNotFoundEvent event) {
//        ToastUtils.showShortToast(getString(R.string.voice_not_found_target_point));
//        if (EasyDialog.isShow()) {
//            mHandler.postDelayed(() -> EasyDialog.getInstance().dismiss(), 1000);
//        }
//    }
}