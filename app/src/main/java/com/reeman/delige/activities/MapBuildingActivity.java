package com.reeman.delige.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.MapBuildingContract;
import com.reeman.delige.presenter.impl.MapBuildingPresenter;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.MapWebViewChromeClient;
import com.reeman.delige.widgets.MapWebViewClient;
import com.reeman.delige.widgets.PointNameInputDialog;
import com.reeman.delige.widgets.WebViewHolder;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.reeman.delige.base.BaseApplication.ros;


public class MapBuildingActivity extends BaseActivity implements View.OnClickListener,
        MapWebViewClient.OnMapWebViewEventListener, MapBuildingContract.View, PointNameInputDialog.OnClickListener {

    private WebView mWebView;
    private MapBuildingPresenter mapBuildingPresenter;
    private PointNameInputDialog pointNameInputDialog;
    private String currentMarkPointName;
    private boolean receiveError = false;
    private String loadUrl;
    private int versionCode = 0;
    private boolean drawingPath = false;

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }

    @Override
    public void onSoftKeyboardClosed() {
        ScreenUtils.setImmersive(this);
    }

    private int hostIpQueryCount = 0;

    private final Runnable hostIpRunnable = new Runnable() {
        @Override
        public void run() {
            if (++hostIpQueryCount >= 3) {
                hostIpQueryCount = 0;
                EasyDialog.getInstance(MapBuildingActivity.this).warn(getString(R.string.text_communicate_failed_with_ros), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                return;
            }
            ros.getHostIP();
            mHandler.postDelayed(this, 1000);
        }
    };

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            hideLoading();
            EasyDialog.getInstance(MapBuildingActivity.this).warn(getString(R.string.text_page_load_failed), new EasyDialog.OnViewClickListener() {
                @Override
                public void onViewClick(Dialog dialog, int id) {
                    if (id == R.id.btn_confirm) {
                        dialog.dismiss();
                        finish();
                    }
                }
            });
        }
    };
    private FrameLayout webLayout;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            ScreenUtils.setImmersive(this);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_map_building;
    }

    protected void initData() {
        mapBuildingPresenter = new MapBuildingPresenter(this);
        ros.positionAutoUploadControl(false);
    }

    @Override
    protected void initCustomView() {
        setOnClickListeners(
                R.id.tv_restart_construct_map,
                R.id.tv_mark_delivery_point,
                R.id.tv_mark_charging_pile,
                R.id.tv_mark_product_point,
                R.id.tv_mark_recycling_point,
                R.id.tv_save_map,
                R.id.tv_start_path,
                R.id.tv_exit,
                R.id.tv_switch_network,
                R.id.tv_back_to_main_page);
        mWebView = WebViewHolder.getView(this);
        mWebView.setWebChromeClient(new MapWebViewChromeClient());
        mWebView.setWebViewClient(new MapWebViewClient(this));
        webLayout = $(R.id.web_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        WebViewHolder.onResume();
        ros.getHostName();
        hostIpQueryCount = 0;
        mHandler.postDelayed(hostIpRunnable, 300);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (VoiceHelper.isPlaying()) {
            VoiceHelper.pause();
        }
        WebViewHolder.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.stopLoading();
            webLayout.removeView(mWebView);
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostIpLoaded(Event.OnIpEvent event) {
        mHandler.removeCallbacks(hostIpRunnable);
        ((TextView) $(R.id.tv_host_ip)).setText(getString(R.string.text_current_host_ip, event.ipAddress));
        if ("127.0.0.1".equals(event.ipAddress)) {
            EasyDialog.getInstance(this).warn(getString(R.string.text_point_loaded_failed), new EasyDialog.OnViewClickListener() {
                @Override
                public void onViewClick(Dialog dialog, int id) {
                    dialog.dismiss();
                    finish();
                }
            });
            return;
        }
        ros.setRequestModeMyself(true);
        ros.modelRequest();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostNameLoaded(Event.OnHostnameEvent event) {
        ((TextView) $(R.id.tv_hostname)).setText(getString(R.string.text_current_hostname, event.hostname));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNavModeLoaded(Event.OnNavModeEvent event) {
        if (!ros.isRequestModeMyself()) return;
        ros.setRequestModeMyself(false);
        mHandler.postDelayed(timeoutRunnable, 3 * 60_000);
        loadUrl = "http://" + Event.getIpEvent().ipAddress + "/pad";
        mWebView.loadUrl(loadUrl);

        boolean hasGuide = SpManager.getInstance().getBoolean(Constants.KEY_IS_MAP_BUILDING_GUIDE, false);
        if (!hasGuide) {
            SharedPreferences.Editor edit = SpManager.getInstance().edit();
            edit.putBoolean(Constants.KEY_IS_MAP_BUILDING_GUIDE, true);
            edit.apply();
            if (event.mode != 2)
                mapBuildingPresenter.changeToConstructMap();
            showLoading(getString(R.string.voice_entering_map_building_mode));
            VoiceHelper.play("voice_entering_map_building_mode");
            return;
        }


        mapBuildingPresenter.setCurrentMode(event.mode);
        if (event.mode == 2 || event.mode == 3) {
            showLoading(getString(R.string.voice_entering_map_building_mode));
            VoiceHelper.play("voice_entering_map_building_mode");
        } else {
            showLoading(getString(R.string.voice_entering_nav_mode));
            VoiceHelper.play("voice_entering_nav_mode");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            //重新建图
            case R.id.tv_restart_construct_map:
                VoiceHelper.play("voice_click_to_rebuild_map");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_click_to_rebuild_map), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            VoiceHelper.play("voice_entering_map_building_mode");
                            mapBuildingPresenter.changeToConstructMap();
                            showLoading(getString(R.string.text_switch_to_constructing_map_mode));
                            mWebView.loadUrl(loadUrl);
                        }
                    }
                });
                break;
            case R.id.tv_mark_charging_pile:
                //标注充电桩
                mapBuildingPresenter.setPointType(1);
                VoiceHelper.play("voice_make_sure_machine_in_front_of_charging_pile");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_make_sure_machine_in_front_of_charging_pile), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            mapBuildingPresenter.getCurrentPosition();
                        }
                    }
                });
                break;
            case R.id.tv_mark_delivery_point:
                //标注配送点
                mapBuildingPresenter.setPointType(2);
                VoiceHelper.play("voice_confirm_mark_delivery_point_at_this_location");
                if (pointNameInputDialog == null) {
                    pointNameInputDialog = new PointNameInputDialog(this);
                    pointNameInputDialog.setOnClickListener(this);
                }
                pointNameInputDialog.show();
                break;
            case R.id.tv_mark_product_point:
                //标注出品点
                mapBuildingPresenter.setPointType(3);
                VoiceHelper.play("voice_introduce_product_point");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_introduce_product_point), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            mapBuildingPresenter.getCurrentPosition();
                        }
                    }
                });
                break;
            case R.id.tv_mark_recycling_point:
                //回收点
                mapBuildingPresenter.setPointType(4);
                VoiceHelper.play("voice_introduce_recycling_point");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_introduce_recycling_point), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            mapBuildingPresenter.getCurrentPosition();
                        }
                    }
                });
                break;
            case R.id.tv_exit_construct_map:
                //退出建图
                VoiceHelper.play("voice_confirm_exit_construct_map");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_confirm_exit_construct_map), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            VoiceHelper.play("voice_exiting_map_building_mode");
                            mapBuildingPresenter.setBackFromMapScanning(true);
                            mapBuildingPresenter.exitWithoutSaving();
                            showLoading(getString(R.string.voice_exiting_map_building_mode));
                            mWebView.loadUrl(loadUrl);
                        }
                    }
                });
                break;
            case R.id.tv_save_map:
                //保存地图
                VoiceHelper.play("voice_confirm_save_map");
                EasyDialog.getInstance(this).confirm(getString(R.string.voice_confirm_save_map), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            VoiceHelper.play("voice_saving_map_and_exiting_map_building_mode");
                            mapBuildingPresenter.setBackFromMapScanning(true);
                            mapBuildingPresenter.saveMap();
                            showLoading(getString(R.string.voice_saving_map_and_exiting_map_building_mode));
                            mWebView.loadUrl(loadUrl);
                        }
                    }
                });

                break;
            case R.id.tv_back_to_main_page:
                VoiceHelper.play("voice_confirm_back_to_main_page");
                EasyDialog.getInstance(MapBuildingActivity.this).confirm(getString(R.string.voice_confirm_back_to_main_page), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            VoiceHelper.play("voice_exiting_map_building_mode");
                            mapBuildingPresenter.setBackFromMapScanning(true);
                            mapBuildingPresenter.changeToNavMode();
                            showLoading(getString(R.string.voice_exiting_map_building_mode));
                            mWebView.loadUrl(loadUrl);
                        }
                    }
                });
                break;
            case R.id.tv_start_path:
                drawingPath = true;
                VoiceHelper.play("voice_confirm_make_a_route");
                EasyDialog.getInstance(MapBuildingActivity.this).confirm(getString(R.string.text_save_path), getString(R.string.text_cancel), getString(R.string.voice_confirm_make_a_route), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        drawingPath = false;
                        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
                        if (id == R.id.btn_confirm) {
                            mapBuildingPresenter.savePath(MapBuildingActivity.this);
                        } else {
                            mapBuildingPresenter.abandonPath();
                        }
                    }
                });
                mapBuildingPresenter.startDrawPath(this);
                break;
            case R.id.tv_exit:
                VoiceHelper.play("voice_confirm_exit_map_deploy");
                EasyDialog.getInstance(MapBuildingActivity.this).confirm(getString(R.string.voice_confirm_exit_map_deploy), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            EasyDialog.getLoadingInstance(MapBuildingActivity.this).loading(getString(R.string.text_exiting_map_deploy));
                            mapBuildingPresenter.reloadPoints(MapBuildingActivity.this);
                        }
                    }
                });
                break;
            case R.id.tv_switch_network:
                BaseActivity.startup(this, WiFiConnectActivity.class);
                break;
        }
    }

    @Override
    public void onPathSaveFailed(String message) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_path_save_failed, message));
    }

    @Override
    public void onPathSaveSuccess() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_path_save_success));
    }

    @Override
    public void onReloadPointFailed() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        finish();
    }


    @Override
    public void onReloadPointSuccess() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        finish();
    }


    /**
     * 目标点位标注结果
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetFlagPoint(Event.OnSetFlagPointEvent event) {
        if (event.result == 0) {
            VoiceHelper.play("voice_target_point_mark_success");
            ToastUtils.showShortToast(getString(R.string.voice_target_point_mark_success));
        } else {
            if (event.result == -3) {
                EasyDialog.getInstance(this).confirm(getString(R.string.text_point_exist), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        if (id == R.id.btn_confirm) {
                            mapBuildingPresenter.deletePoint();
                        }
                        dialog.dismiss();
                    }
                });
            } else {
                VoiceHelper.play("voice_target_point_mark_failed");
                ToastUtils.showShortToast(getString(R.string.voice_target_point_mark_failed));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void delPoint(Event.OnDelFlagPointEvent event) {
        mapBuildingPresenter.getCurrentPosition();
    }

    @Override
    protected void onCustomInitPose(String currentPosition) {
        hideLoading();
    }

    @Override
    public void onClick(Dialog dialog, String name) {
        this.currentMarkPointName = name;
        if (!TextUtils.isEmpty(name)) {
            dialog.dismiss();
            mapBuildingPresenter.getCurrentPosition();
        } else {
            ToastUtils.showShortToast(getString(R.string.text_please_input_point_name));
        }
    }

    private void showLoading(String prompt) {
        EasyDialog.getCancelableLoadingInstance(this).loadingCancelable(prompt, new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                dialog.dismiss();
                mHandler.removeCallbacks(timeoutRunnable);
                mHandler.removeCallbacks(hostIpRunnable);
                finish();
            }
        });
    }

    public void hideLoading() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
    }

    @Override
    public void onPageStart(WebView view, String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        XLog.w("onPageFinished");
        mHandler.removeCallbacks(timeoutRunnable);

        FrameLayout.MarginLayoutParams layoutParams = (FrameLayout.MarginLayoutParams) mWebView.getLayoutParams();
        layoutParams.leftMargin = -330;
        mWebView.setLayoutParams(layoutParams);
        webLayout.removeAllViews();
        webLayout.addView(mWebView, 0);

        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        String language;
        switch (languageType) {
            case 1:
                language = "zh";
                break;
            case 2:
                language = "ja";
                break;
            case 3:
                language = "ko";
                break;
            case 8:
                language = "fr";
                break;
            default:
                language = "en";
                break;
        }

        mWebView.loadUrl("javascript:setLanguage('" + language + "')");

        $(R.id.ll_operation_bar).setVisibility(View.VISIBLE);

        if (mapBuildingPresenter.getCurrentMode() == 2) {
            onEnterMapScanningMode();
        } else if (mapBuildingPresenter.getCurrentMode() == 1) {
            onEnterNavigationMode();
        }
    }

    private void onEnterNavigationMode() {
        if (!mapBuildingPresenter.isBackFromMapScanning()) {
            hideLoading();
        }
        $(R.id.tv_hostname).setVisibility(View.VISIBLE);
        $(R.id.tv_host_ip).setVisibility(View.VISIBLE);
        $(R.id.tv_restart_construct_map).setVisibility(View.VISIBLE);
        $(R.id.tv_exit).setVisibility(View.VISIBLE);
        $(R.id.tv_save_map).setVisibility(View.GONE);
        $(R.id.tv_back_to_main_page).setVisibility(View.GONE);
        $(R.id.tv_mark_charging_pile).setVisibility(View.VISIBLE);
        $(R.id.tv_mark_delivery_point).setVisibility(View.VISIBLE);
        $(R.id.tv_mark_product_point).setVisibility(View.VISIBLE);
        $(R.id.tv_mark_recycling_point).setVisibility(View.VISIBLE);
        $(R.id.tv_start_path).setVisibility(View.VISIBLE);
        $(R.id.tv_switch_network).setVisibility(View.VISIBLE);
        VoiceHelper.play("voice_start_mark_point_when_finish_scanning_map");
    }

    private void onEnterMapScanningMode() {
        hideLoading();
        $(R.id.tv_hostname).setVisibility(View.GONE);
        $(R.id.tv_host_ip).setVisibility(View.GONE);
        $(R.id.tv_exit).setVisibility(View.GONE);
        $(R.id.tv_restart_construct_map).setVisibility(View.VISIBLE);
        $(R.id.tv_save_map).setVisibility(View.VISIBLE);
        $(R.id.tv_back_to_main_page).setVisibility(View.VISIBLE);
        $(R.id.tv_mark_charging_pile).setVisibility(View.GONE);
        $(R.id.tv_mark_delivery_point).setVisibility(View.GONE);
        $(R.id.tv_mark_product_point).setVisibility(View.GONE);
        $(R.id.tv_mark_recycling_point).setVisibility(View.GONE);
        $(R.id.tv_start_path).setVisibility(View.GONE);
        $(R.id.tv_switch_network).setVisibility(View.VISIBLE);
        VoiceHelper.play("voice_enter_map_building_mode_success");
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        XLog.w("onReceivedError");
        if (!receiveError) {
            receiveError = true;
            mHandler.removeCallbacks(timeoutRunnable);
            mHandler.removeCallbacks(hostIpRunnable);
            hideLoading();
            mHandler.postDelayed(timeoutRunnable, 1000);
        }
    }

    @Override
    protected void onCustomPositionObtained(double[] position) {
        if (drawingPath) {
            mapBuildingPresenter.onPositionLoaded(position);
        } else {
            mapBuildingPresenter.markPoint(this, position, currentMarkPointName);
        }
    }

    @Override
    protected void onWaypointUpdate(Event.OnWaypointUpdateEvent event) {

    }
}