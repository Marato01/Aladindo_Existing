package com.reeman.delige.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.delige.R;
import com.reeman.delige.SplashActivity;
import com.reeman.delige.activities.CallingConfigActivity;
import com.reeman.delige.activities.MultiMachineConfigActivity;
import com.reeman.delige.adapter.BroadcastItemAdapter;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.base.BaseFragment;
import com.reeman.delige.calling.CallingHelper;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.BasicSettingContract;
import com.reeman.delige.dispatch.DispatchManager;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.presenter.impl.BasicSettingPresenter;
import com.reeman.delige.request.model.MapVO;
import com.reeman.delige.settings.ObstacleSetting;
import com.reeman.delige.settings.creator.SettingCreator;
import com.reeman.delige.utils.ClickRestrict;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.ChooseItemPopupWindow;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.EditorCallback;
import com.reeman.delige.widgets.EditorHolder;
import com.reeman.delige.widgets.FloatEditorActivity;
import com.reeman.delige.widgets.MapChooseDialog;
import com.reeman.delige.event.Event;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_OBSTACLE_PROMPT;
import static com.reeman.delige.base.BaseApplication.activityStack;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.ros;
import static com.reeman.delige.base.BaseApplication.shouldRefreshPoints;


public class BasicSettingFragment extends BaseFragment implements View.OnClickListener, BasicSettingContract.View, BroadcastItemAdapter.onItemDeleteListener, EasyDialog.OnViewClickListener, MapChooseDialog.OnMapListItemSelectedListener {

    private BasicSettingPresenter presenter;
    private IndicatorSeekBar isbPower;
    private IndicatorSeekBar isbBrightness;
    private IndicatorSeekBar isbVolume;
    private ObstacleSetting obstacleModeSetting;
    private BroadcastItemAdapter obstacleAdapter;
    private double[] lastRelocateCoordinate;
    private final Handler handler = new Handler();
    private int currentCheckId;
    private RadioGroup rgSettingPasswordControl;
    private RadioGroup.OnCheckedChangeListener listener;
    private GifDrawable gifDrawable;
    private Gson gson;
    private TextView tvAnimation;
    private GifImageView gifAnimationPreview;
    private List<String> items;

    //导航模式设置
    private RadioGroup rgSettingNavigationModelControl;
    private Mode lastMode;

    private boolean isFloatEditorActivityShow;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_basic_setting;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new GsonBuilder()
                .registerTypeAdapter(ObstacleSetting.class, new SettingCreator<>())
                .create();
        presenter = new BasicSettingPresenter(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMediaVolume();

        initBrightness();

        initPower();

        initObstacleSetting();

        initSettingPasswordControl();

        initSettingNavigationModelControl();

        initDeliveryDisplayControl();

        initAnimationStyle();

        initColumnControl();

        initLayersControl();

        initDataSynchronizationControl();

        initRelocate();

        initSwitchMap();

        initCallingConfig();

        initMultiMachineConfig();
    }

    @Override
    public void onPause() {
        super.onPause();
        shouldRefreshPoints = lastMode != navigationMode;
    }

    private void initMultiMachineConfig() {
        Button btnMultiMachineConfig = root.findViewById(R.id.btn_multi_machine_config);
        btnMultiMachineConfig.setOnClickListener(this);
    }

    private void initAnimationStyle() {
        tvAnimation = root.findViewById(R.id.tv_animation);
        tvAnimation.setOnClickListener(this);
        gifAnimationPreview = root.findViewById(R.id.gif_animation_preview);
        items = Arrays.asList(getResources().getStringArray(R.array.animation_names));
        int deliveryAnimation = SpManager.getInstance().getInt(Constants.KEY_DELIVERY_ANIMATION, Constants.DEFAULT_DELIVERY_ANIMATION);
        tvAnimation.setText(items.get(deliveryAnimation));
        showAnimationPreview(deliveryAnimation, gifAnimationPreview);
    }

    private void showAnimationChooseWindow() {
        ChooseItemPopupWindow chooseItemPopupWindow = new ChooseItemPopupWindow(getContext(), tvAnimation, items);
        chooseItemPopupWindow.setOnItemChosenListener((window, position) -> {
            SpManager.getInstance().edit().putInt(Constants.KEY_DELIVERY_ANIMATION, position).apply();
            showAnimationPreview(position, gifAnimationPreview);
            tvAnimation.setText(items.get(position));
            window.dismiss();
        });
        chooseItemPopupWindow.show();
    }

    private void initCallingConfig() {
        Button btnCallingConfig = root.findViewById(R.id.btn_calling_config);
        btnCallingConfig.setOnClickListener(this);
    }

    private void initSettingNavigationModelControl() {
        lastMode = navigationMode;
        rgSettingNavigationModelControl = root.findViewById(R.id.rg_setting_navigation_model_control);
        String settingNavigationModelControl = SpManager.getInstance().getString(Constants.KEY_NAVIGATION_MODE, Mode.AUTO_ROUTE.name());
        rgSettingNavigationModelControl.check(settingNavigationModelControl.equals(Mode.AUTO_ROUTE.name()) ? R.id.rb_auto_model : R.id.rb_fix_model);
        rgSettingNavigationModelControl.setOnCheckedChangeListener((radioGroup, i) -> {
            navigationMode = (i == R.id.rb_auto_model ? Mode.AUTO_ROUTE : Mode.FIX_ROUTE);
            SpManager.getInstance().edit().putString(Constants.KEY_NAVIGATION_MODE, i == R.id.rb_auto_model ? Mode.AUTO_ROUTE.name() : Mode.FIX_ROUTE.name()).apply();

        });
    }

    private void initSettingPasswordControl() {
        rgSettingPasswordControl = root.findViewById(R.id.rg_setting_password_control);
        int settingPasswordControl = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
        rgSettingPasswordControl.check(settingPasswordControl == 0 ? R.id.rb_close_password : R.id.rb_open_password);
        listener = (group, checkedId) -> {
            currentCheckId = checkedId;
            EasyDialog.newCustomInstance(requireContext(), R.layout.layout_input_setting_password).showInputPasswordDialog(getString(R.string.text_please_verify_password), BasicSettingFragment.this);
        };
        rgSettingPasswordControl.setOnCheckedChangeListener(listener);
    }

    @Override
    public void onViewClick(Dialog dialog, int id) {
        if (id == R.id.btn_confirm) {
            EditText editText = (EditText) EasyDialog.getInstance().getView(R.id.et_password);
            String str = editText.getText().toString();
            if (TextUtils.equals(str, Constants.KEY_SETTING_PASSWORD)) {
                dialog.dismiss();
                SpManager.getInstance().edit().putInt(Constants.KEY_SETTING_PASSWORD_CONTROL, currentCheckId == R.id.rb_close_password ? 0 : 1).apply();
            } else {
                ToastUtils.showShortToast(getString(R.string.text_password_error));
            }
        } else {
            dialog.dismiss();
            int settingPasswordControl = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
            rgSettingPasswordControl.setOnCheckedChangeListener(null);
            rgSettingPasswordControl.check(settingPasswordControl == 0 ? R.id.rb_close_password : R.id.rb_open_password);
            rgSettingPasswordControl.setOnCheckedChangeListener(listener);
        }
    }

    private void initDataSynchronizationControl() {
        RadioGroup rgDataSyncControl = root.findViewById(R.id.rg_data_sync_control);
        int dataSync = SpManager.getInstance().getInt(Constants.KEY_DATA_SYNC_TYPE, 1);
        rgDataSyncControl.check(dataSync == 0 ? R.id.rb_domestic : R.id.rb_foreign);
        rgDataSyncControl.setOnCheckedChangeListener((group, checkedId) -> SpManager.getInstance().edit().putInt(Constants.KEY_DATA_SYNC_TYPE, checkedId == R.id.rb_domestic ? 0 : 1).apply());
    }

    private void initSwitchMap() {
        Button btnSwitchMap = root.findViewById(R.id.btn_switch_map);
        btnSwitchMap.setOnClickListener(this);
    }


    private void initObstacleSetting() {
        if (obstacleModeSetting == null) {
            String obstacleSetting = SpManager.getInstance().getString(Constants.KEY_OBSTACLE_CONFIG, null);
            if (TextUtils.isEmpty(obstacleSetting)) {
                obstacleModeSetting = ObstacleSetting.getDefault();
            } else {
                obstacleModeSetting = gson.fromJson(obstacleSetting, ObstacleSetting.class);
            }
        }

        SwitchButton swEnableObstaclePrompt = root.findViewById(R.id.sw_enable_obstacle_prompt);
        ImageButton ibAddBroadcastItem = root.findViewById(R.id.ib_add_encounter_obstacle_prompt);
        ibAddBroadcastItem.setOnClickListener(v -> {
            if (obstacleModeSetting.obstaclePrompts != null && obstacleModeSetting.obstaclePrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                return;
            }
            if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
            FloatEditorActivity.openEditor(requireContext(),
                    new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                    new EditorCallback() {

                        @Override
                        public void onTryListen(Activity activity, String content, View cancel, View submit) {
                            presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_OBSTACLE_ASSETS_PREFIX, content, "", cancel, submit);
                        }

                        @Override
                        public void onConfirm(Activity activity, String content, View cancel, View submit) {
                            obstacleModeSetting.obstaclePrompts.add(presenter.getLastTryListenText());
                            obstacleModeSetting.obstaclePromptAudioList.add(presenter.getLastGeneratedFile());
                            obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
                            SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
                            activity.finish();
                        }

                        @Override
                        public void onAttached(ViewGroup rootView) {
                            isFloatEditorActivityShow = true;
                        }

                        @Override
                        public void onFinish() {
                            isFloatEditorActivityShow = false;
                        }
                    });
        });
        swEnableObstaclePrompt.setChecked(obstacleModeSetting.enableObstaclePrompt);
        swEnableObstaclePrompt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            obstacleModeSetting.enableObstaclePrompt = isChecked;
            SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
        });

        RecyclerView rvObstaclePrompt = root.findViewById(R.id.rv_obstacle_prompt);
        obstacleAdapter = new BroadcastItemAdapter(TYPE_OBSTACLE_PROMPT);
        obstacleAdapter.setCheckedList(obstacleModeSetting.targetObstaclePrompts);
        obstacleAdapter.setListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvObstaclePrompt.setLayoutManager(layoutManager);
        rvObstaclePrompt.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
        rvObstaclePrompt.setAdapter(obstacleAdapter);

        if (obstacleModeSetting.obstaclePrompts != null && !obstacleModeSetting.obstaclePrompts.isEmpty()) {
            obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
        }
    }

    private void initMediaVolume() {
        ImageButton ibDecreaseVolume = root.findViewById(R.id.ib_decrease_volume);
        ImageButton ibIncreaseVolume = root.findViewById(R.id.ib_increase_volume);
        ibDecreaseVolume.setOnClickListener(this);
        ibIncreaseVolume.setOnClickListener(this);
        isbVolume = root.findViewById(R.id.isb_volume);
        int mediaVolume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        isbVolume.setProgress(mediaVolume);
        isbVolume.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                saveMediaVolume(progress);
            }
        });
    }

    private void saveMediaVolume(int progress) {
        SpManager.getInstance().edit().putInt(Constants.KEY_MEDIA_VOLUME, progress).apply();
    }

    private void initBrightness() {
        ImageButton ibDecreaseBrightness = root.findViewById(R.id.ib_decrease_brightness);
        ImageButton ibIncreaseBrightness = root.findViewById(R.id.ib_increase_brightness);
        ibDecreaseBrightness.setOnClickListener(this);
        ibIncreaseBrightness.setOnClickListener(this);
        isbBrightness = root.findViewById(R.id.isb_brightness);
        int screenBrightness = SpManager.getInstance().getInt(Constants.KEY_SCREEN_BRIGHTNESS, Constants.DEFAULT_SCREEN_BRIGHTNESS);
        isbBrightness.setProgress(screenBrightness);
        isbBrightness.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                saveBrightness(progress);
            }
        });
    }

    private void saveBrightness(int progress) {
        SpManager.getInstance().edit().putInt(Constants.KEY_SCREEN_BRIGHTNESS, progress).apply();
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(requireContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (progress / 100.0 * 255));
        requireActivity().getContentResolver().notifyChange(uri, null);
    }

    private void initPower() {
        ImageButton ibDecreasePower = root.findViewById(R.id.ib_decrease_power);
        ImageButton ibIncreasePower = root.findViewById(R.id.ib_increase_power);
        isbPower = root.findViewById(R.id.isb_power);
        ibDecreasePower.setOnClickListener(this);
        ibIncreasePower.setOnClickListener(this);
        int lowPower = SpManager.getInstance().getInt(Constants.KEY_LOW_POWER, Constants.DEFAULT_LOW_POWER);
        isbPower.setProgress(lowPower);
        isbPower.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress <= 10) progress = 10;
                if (progress >= 80) progress = 80;
                seekBar.setProgress(progress);
                savePower(progress);
            }
        });
    }

    private void savePower(int progress) {
        SpManager.getInstance().edit().putInt(Constants.KEY_LOW_POWER, progress).apply();
    }

    private void initDeliveryDisplayControl() {
        RadioGroup displayControl = root.findViewById(R.id.rg_display_content_during_delivery);
        int displayContent = SpManager.getInstance().getInt(Constants.KEY_DISPLAY_DURING_DELIVERY, Constants.DEFAULT_DISPLAY_DURING_DELIVERY);
        displayControl.check(displayContent == 0 ? R.id.rb_display_expression : R.id.rb_display_target_table);
        displayControl.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_display_expression) {
                SpManager.getInstance().edit().putInt(Constants.KEY_DISPLAY_DURING_DELIVERY, 0).apply();
            } else {
                SpManager.getInstance().edit().putInt(Constants.KEY_DISPLAY_DURING_DELIVERY, 1).apply();
            }
        });
    }

    private void showAnimationPreview(int position, GifImageView gifAnimationPreview) {
        try {
            if (gifDrawable != null) gifDrawable.stop();
            int identifier = getResources().getIdentifier("delivery_animation_" + position, "drawable", getContext().getPackageName());
            gifDrawable = new GifDrawable(getResources(), identifier);
            gifAnimationPreview.setImageDrawable(gifDrawable);
            gifDrawable.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initColumnControl() {
        RadioGroup columnControl = root.findViewById(R.id.rg_column_control);
        int pointColumn = SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN);
        columnControl.check(pointColumn == 3 ? R.id.rb_three_column : R.id.rb_four_column);
        columnControl.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_three_column) {
                SpManager.getInstance().edit().putInt(Constants.KEY_POINT_COLUMN, 3).apply();
            } else {
                SpManager.getInstance().edit().putInt(Constants.KEY_POINT_COLUMN, 4).apply();
            }
        });
    }

    private void initLayersControl() {
        RadioGroup tableLayerControl = root.findViewById(R.id.rg_table_layers_control);
        int tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
        tableLayerControl.check(tableLayer == 3 ? R.id.rb_three_layers : R.id.rb_four_layers);
        tableLayerControl.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_three_layers) {
                SpManager.getInstance().edit().putInt(Constants.KEY_TABLE_LAYER, 3).apply();
            } else {
                SpManager.getInstance().edit().putInt(Constants.KEY_TABLE_LAYER, 4).apply();
            }
        });
    }


    private void initRelocate() {
        Button btnRelocate = root.findViewById(R.id.btn_relocate);
        btnRelocate.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ib_decrease_brightness:
            case R.id.ib_increase_brightness:
                onAdjustBrightnessBtnClick(id);
                break;
            case R.id.ib_increase_volume:
            case R.id.ib_decrease_volume:
                onAdjustVolumeBtnClick(id);
                break;
            case R.id.ib_increase_power:
            case R.id.ib_decrease_power:
                onAdjustPowerBtnClick(id);
                break;
            case R.id.btn_relocate:
                onRelocateBtnClick();
                break;
            case R.id.btn_switch_map:
                onSwitchMap();
                break;
            case R.id.btn_calling_config:
                onCallingConfigBtnClick();
                break;
            case R.id.btn_multi_machine_config:
                ros.positionAutoUploadControl(false);
                EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_entering));
                handler.postDelayed(() -> {
                    if (DispatchManager.isStarted()) {
                        DispatchManager.stop();
                    }
                    handler.postDelayed(() -> {
                        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                        BaseActivity.startup(requireContext(), MultiMachineConfigActivity.class);
                    }, 1000);
                }, 2000);
                break;
            case R.id.tv_animation:
                showAnimationChooseWindow();
                break;
        }
    }

    private void onCallingConfigBtnClick() {
        CallingHelper.getInstance().stop();
        BaseActivity.startup(getContext(), CallingConfigActivity.class);
    }

    private void onSwitchMap() {
        presenter.onSwitchMap(requireContext());
    }

    @Override
    public void onMapListLoaded(List<MapVO> list) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        MapChooseDialog mapChooseDialog = new MapChooseDialog(requireContext(), list, this);
        mapChooseDialog.show();
    }

    @Override
    public void onMapListLoadedFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_load_map_failed));
    }

    @Override
    public void onMapListItemSelected(MapChooseDialog mapChooseDialog, String map) {
        XLog.w("map ::" + map + " :: " + Event.getMapEvent().map);
        if (map.equals(Event.getMapEvent().map)) {
            ToastUtils.showShortToast(getString(R.string.text_do_not_apply_map_repeatedly));
            return;
        }
        mapChooseDialog.dismiss();
        EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_changing_map));
        ros.applyMap(map);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMapApply(Event.OnApplyMapEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (event.success) {
            ToastUtils.showShortToast(getString(R.string.text_switch_map_success));
            handler.postDelayed(() -> EasyDialog.getInstance(requireContext()).warn(getString(R.string.text_restart_for_configuration_change), (dialog, id) -> {
                if (id == R.id.btn_confirm) {
                    dialog.dismiss();
                    ros.setRelocating(false);
                    Intent intent = new Intent(requireContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    activityStack.get(activityStack.size()-1).finish();
                }
            }), 500);
        } else {
            ToastUtils.showShortToast(getString(R.string.text_switch_map_failure));
        }

    }

    @Override
    public void onNoMapSelected() {
        ToastUtils.showShortToast(getString(R.string.text_please_choose_map));
    }

    private void onAdjustPowerBtnClick(int id) {
        int progress = isbPower.getProgress();
        if (id == R.id.ib_decrease_power) {
            if (progress <= 10) return;
            progress--;
        } else {
            if (progress >= 80) return;
            progress++;
        }
        isbPower.setProgress(progress);
        savePower(progress);
    }

    private void onAdjustVolumeBtnClick(int id) {
        int progress = isbVolume.getProgress();
        if (id == R.id.ib_decrease_volume) {
            if (progress <= 0) return;
            progress--;
        } else {
            if (progress >= 15) return;
            progress++;
        }
        isbVolume.setProgress(progress);
        saveMediaVolume(progress);
    }

    private void onAdjustBrightnessBtnClick(int id) {
        int progress = isbBrightness.getProgress();
        if (id == R.id.ib_decrease_brightness) {
            if (progress <= 0) return;
            progress -= 1;
        } else {
            if (progress >= 100) return;
            progress += 1;
        }
        isbBrightness.setProgress(progress);
        saveBrightness(progress);
    }

    public void onRelocateBtnClick() {
        lastRelocateCoordinate = null;
        if (DestHelper.getInstance().getChargePointCoordinate() == null) {
            handler.postDelayed(() -> EasyDialog.getInstance(requireContext()).warnError(getString(R.string.voice_not_found_target_point)), 1000);
            return;
        }
        EasyDialog.getInstance(requireContext()).confirm(getString(R.string.text_relocate_prompt), (dialog, id) -> {
            if (id == R.id.btn_confirm) {
                if (!ros.isCharging()) {
                    ToastUtils.showShortToast(getString(R.string.voice_please_dock_charging_pile));
                    return;
                }
                dialog.dismiss();
                lastRelocateCoordinate = DestHelper.getInstance().getChargePointCoordinate();
                presenter.relocate(requireContext());
            } else {
                lastRelocateCoordinate = null;
                dialog.dismiss();
            }
        });
    }

    public double[] getLastRelocateCoordinate() {
        return lastRelocateCoordinate;
    }

    public void setLastRelocateCoordinate(double[] lastRelocateCoordinate) {
        this.lastRelocateCoordinate = lastRelocateCoordinate;
    }

    @Override
    public void showRelocatingView() {
        EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_relocating));
    }

    @Override
    public void onDeleteBroadcastItem(int type, int position, String text) {
        obstacleModeSetting.obstaclePrompts.remove(position);
        String s = obstacleModeSetting.obstaclePromptAudioList.get(position);
        new File(s).delete();
        obstacleModeSetting.obstaclePromptAudioList.remove(position);
        SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
        obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
    }

    @Override
    public void onAudition(int type, int position, View v) {
        if (VoiceHelper.isPlaying()) {
            VoiceHelper.pause();
            ((ImageButton) v).setImageResource(R.drawable.icon_audition);
            return;
        }
        ((ImageButton) v).setImageResource(R.drawable.icon_audition_inactive);
        String file = obstacleModeSetting.obstaclePromptAudioList.get(position);
        VoiceHelper.playFile(file, () -> ((ImageButton) v).setImageResource(R.drawable.icon_audition));
    }

    @Override
    public void onCheckChange(int type, List<Integer> list) {
        obstacleModeSetting.targetObstaclePrompts = list;
        SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
    }

    @Override
    public void onSynthesizeStart(View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(false);
        btnTryListen.setBackgroundResource(R.drawable.bg_common_button_inactive);
        btnSave.setEnabled(false);
        btnSave.setBackgroundResource(R.drawable.bg_common_button_inactive);
    }

    @Override
    public void onSynthesizeEnd(View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(true);
        btnTryListen.setBackgroundResource(R.drawable.selector_common_button);
        btnSave.setEnabled(true);
        btnSave.setBackgroundResource(R.drawable.selector_common_button);
    }

    @Override
    public void onSynthesizeError(String message, View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(true);
        btnTryListen.setBackgroundResource(R.drawable.selector_common_button);
        btnSave.setEnabled(false);
        btnSave.setBackgroundResource(R.drawable.bg_common_button_inactive);
    }
}