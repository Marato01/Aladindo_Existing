package com.reeman.delige.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.delige.R;
import com.reeman.delige.adapter.BroadcastItemAdapter;
import com.reeman.delige.base.BaseFragment;
import com.reeman.delige.base.BaseSetting;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.ModeSettingFragmentContract;
import com.reeman.delige.models.BackgroundMusicItem;
import com.reeman.delige.presenter.impl.ModeSettingFragmentPresenter;
import com.reeman.delige.settings.BirthdayModeSetting;
import com.reeman.delige.settings.CruiseModeSetting;
import com.reeman.delige.settings.DeliveryMealSetting;
import com.reeman.delige.settings.MultiDeliverySetting;
import com.reeman.delige.settings.RecycleModeSetting;
import com.reeman.delige.settings.creator.SettingCreator;
import com.reeman.delige.utils.ClickRestrict;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.BackgroundMusicChooseDialog;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.EditorCallback;
import com.reeman.delige.widgets.EditorHolder;
import com.reeman.delige.widgets.ExpandableLayout;
import com.reeman.delige.widgets.FloatEditorActivity;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_BIRTHDAY_PICK_MEAL_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_CRUISE_LOOP_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_DELIVERY_ARRIVAL_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_RECYCLE_LOOP_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST;
import static com.reeman.delige.adapter.BroadcastItemAdapter.TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_BIRTHDAY;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_CRUISE;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_DELIVERY;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_MULTI_DELIVERY;

public class ModeSettingFragment extends BaseFragment implements ExpandableLayout.OnExpandListener, View.OnClickListener, OnSeekChangeListener, BackgroundMusicChooseDialog.OnBackgroundMusicSelectedListener, ModeSettingFragmentContract.View, BroadcastItemAdapter.onItemDeleteListener {

    private ExpandableLayout elCruiseMode;
    private ExpandableLayout elRecycleMode;
    private ExpandableLayout elBirthdayMode;
    private ExpandableLayout elDeliveryMealMode;
    private ExpandableLayout elMultiDeliveryMode;
    private ImageButton ibIncreaseDeliveryModeSpeed;
    private ImageButton ibDecreaseDeliveryModeSpeed;
    private IndicatorSeekBar isbAdjustDeliveryModeSpeed;
    private ImageButton ibIncreaseDeliveryModePauseTime;
    private ImageButton ibDecreaseDeliveryModePauseTime;
    private IndicatorSeekBar isbAdjustDeliveryModePauseTime;
    private IndicatorSeekBar isbAdjustCruiseModeInterval;
    private ImageButton ibIncreaseCruiseModeInterval;
    private ImageButton ibDecreaseCruiseModeInterval;
    private Button btnChooseCruiseBackgroundMusic;
    private TextView tvCruiseBackgroundMusic;
    private ImageButton ibIncreaseRecycleModeSpeed;
    private ImageButton ibDecreaseRecycleModeSpeed;
    private IndicatorSeekBar isbAdjustRecycleModeSpeed;
    private ImageButton ibIncreaseRecycleModeBroadcastInterval;
    private ImageButton ibDecreaseRecycleModeBroadcastInterval;
    private IndicatorSeekBar isbAdjustRecycleModeBroadcastInterval;
    private ImageButton ibDecreaseBirthdayModeSpeed;
    private ImageButton ibIncreaseBirthdayModeSpeed;
    private IndicatorSeekBar isbAdjustBirthdayModeSpeed;
    private ImageButton ibIncreaseCruiseModeSpeed;
    private ImageButton ibDecreaseCruiseModeSpeed;
    private IndicatorSeekBar isbAdjustCruiseModeSpeed;
    private ImageButton ibIncreaseBirthdayModeBroadcastPauseTime;
    private ImageButton ibDecreaseBirthdayModeBroadcastPauseTime;
    private IndicatorSeekBar isbAdjustBirthdayModeBroadcastPauseTime;
    private Button btnChooseBirthdayModeBackgroundMusic;
    private TextView tvBirthdayModeBackgroundMusic;

    private DeliveryMealSetting deliveryMealSetting;
    private CruiseModeSetting cruiseModeSetting;
    private BirthdayModeSetting birthdayModeSetting;
    private RecycleModeSetting recycleModeSetting;
    private MultiDeliverySetting multiDeliverySetting;

    private IndicatorSeekBar isbAdjustRecycleModePauseTime;
    private ImageButton ibDecreaseRecycleModePauseTime;
    private ImageButton ibIncreaseRecycleModePauseTime;
    private ModeSettingFragmentPresenter presenter;
    private BroadcastItemAdapter cruiseLoopAdapter;
    private BroadcastItemAdapter recycleLoopAdapter;
    private SwitchButton swCruiseModeEnableLoopBroadcast;
    private SwitchButton swRecycleModeEnableLoopBroadcast;
    private BroadcastItemAdapter placeRecyclableAdapter;
    private BroadcastItemAdapter recycleCompleteAdapter;
    private BroadcastItemAdapter birthdayPickMealAdapter;
    private BroadcastItemAdapter birthdayPickMealCompleteAdapter;
    private BroadcastItemAdapter deliveryModeDeliveryArrivalPromptAdapter;
    private SwitchButton swEnableMultiDeliveryModeBackgroundMusic;
    private SwitchButton swRecycleModeEnableRecycleCompletePrompt;
    private SwitchButton swRecycleModeEnablePlaceRecyclablePrompt;
    private SwitchButton swBirthdayModeEnableBirthdayPickMealPrompt;
    private SwitchButton swBirthdayModeEnableBirthdayCompletePrompt;
    private List<ExpandableLayout> list;
    private SwitchButton swEnableBirthdayBackgroundMusic;
    private SwitchButton swEnableCruiseBackgroundMusic;
    private TextView tvDeliveryModeBackgroundMusic;
    private Button btnChooseDeliveryModeBackgroundMusic;
    private SwitchButton swEnableDeliveryModeBackgroundMusic;
    private ImageButton ibIncreaseMultiDeliveryModeSpeed;
    private ImageButton ibDecreaseMultiDeliveryModeSpeed;
    private IndicatorSeekBar isbAdjustMultiDeliveryModeSpeed;
    private ImageButton ibIncreaseMultiDeliveryModePauseTime;
    private ImageButton ibDecreaseMultiDeliveryModePauseTime;
    private IndicatorSeekBar isbAdjustMultiDeliveryModePauseTime;
    private TextView tvMultiDeliveryModeBackgroundMusic;
    private Button btnChooseMultiDeliveryModeBackgroundMusic;
    private RadioGroup rgBackgroundMusicPlayTimeControl;
    private SwitchButton swEnableCustomPromptForDeliveryModeDeliveryArrival;
    private BroadcastItemAdapter multiDeliveryModeDeliveryArrivalPromptAdapter;
    private SwitchButton swEnableCustomPromptForMultiDeliveryModeDeliveryArrival;
    private Gson gson;

    private boolean isFloatEditorActivityShow;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingCreator<BaseSetting> typeAdapter = new SettingCreator<>();
        gson = new GsonBuilder()
                .registerTypeAdapter(BirthdayModeSetting.class, typeAdapter)
                .registerTypeAdapter(CruiseModeSetting.class, typeAdapter)
                .registerTypeAdapter(DeliveryMealSetting.class, typeAdapter)
                .registerTypeAdapter(MultiDeliverySetting.class, typeAdapter)
                .registerTypeAdapter(RecycleModeSetting.class, typeAdapter)
                .create();
        presenter = new ModeSettingFragmentPresenter(this);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        elDeliveryMealMode = root.findViewById(R.id.el_delivery_mode);
        elCruiseMode = root.findViewById(R.id.el_cruise_mode);
        elRecycleMode = root.findViewById(R.id.el_recycle_mode);
        elBirthdayMode = root.findViewById(R.id.el_birthday_mode);
        elMultiDeliveryMode = root.findViewById(R.id.el_multi_delivery_mode);

        elDeliveryMealMode.setOnExpandListener(this);
        elCruiseMode.setOnExpandListener(this);
        elRecycleMode.setOnExpandListener(this);
        elBirthdayMode.setOnExpandListener(this);
        elMultiDeliveryMode.setOnExpandListener(this);

        list = Arrays.asList(elDeliveryMealMode, elCruiseMode, elRecycleMode, elBirthdayMode, elMultiDeliveryMode);
    }

    @Override
    public void onExpand(ExpandableLayout expandableLayout, boolean isExpand) {
        ImageButton ibExpandIndicator = expandableLayout.getHeaderLayout().findViewById(R.id.ib_expand_indicator);
        ibExpandIndicator.animate().rotation(isExpand ? 90 : 0).setDuration(200).start();

        for (ExpandableLayout layout : list) {
            if (layout != expandableLayout && layout.isOpened()) {
                layout.hide();
            }
        }

        //懒加载，展开时再findViewById
        initExpandLayout(expandableLayout);
    }

    private void initExpandLayout(ExpandableLayout expandableLayout) {
        if (expandableLayout == elDeliveryMealMode) {
            initDeliveryModeView(expandableLayout);
        } else if (expandableLayout == elCruiseMode) {
            initCruiseView(expandableLayout);
        } else if (expandableLayout == elRecycleMode) {
            initRecycleModeView(expandableLayout);
        } else if (expandableLayout == elBirthdayMode) {
            initBirthdayModeView(expandableLayout);
        } else if (expandableLayout == elMultiDeliveryMode) {
            initMultiDeliveryModeView(expandableLayout);
        }
    }

    private void initMultiDeliveryModeView(ExpandableLayout root) {
        if (multiDeliverySetting == null) {
            String multiDeliveryModeConfig = SpManager.getInstance().getString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, null);
            if (TextUtils.isEmpty(multiDeliveryModeConfig)) {
                multiDeliverySetting = MultiDeliverySetting.getDefault();
            } else {
                multiDeliverySetting = gson.fromJson(multiDeliveryModeConfig, MultiDeliverySetting.class);
            }
        }
        if (root.getTag() == null) {
            ibIncreaseMultiDeliveryModeSpeed = root.findViewById(R.id.ib_increase_multi_delivery_mode_speed);
            ibIncreaseMultiDeliveryModeSpeed.setOnClickListener(this);
            ibDecreaseMultiDeliveryModeSpeed = root.findViewById(R.id.ib_decrease_multi_delivery_mode_speed);
            ibDecreaseMultiDeliveryModeSpeed.setOnClickListener(this);
            isbAdjustMultiDeliveryModeSpeed = root.findViewById(R.id.isb_adjust_multi_delivery_mode_speed);
            isbAdjustMultiDeliveryModeSpeed.setOnSeekChangeListener(this);

            ibIncreaseMultiDeliveryModePauseTime = root.findViewById(R.id.ib_increase_multi_delivery_mode_pause_time);
            ibIncreaseMultiDeliveryModePauseTime.setOnClickListener(this);
            ibDecreaseMultiDeliveryModePauseTime = root.findViewById(R.id.ib_decrease_multi_delivery_mode_pause_time);
            ibDecreaseMultiDeliveryModePauseTime.setOnClickListener(this);
            isbAdjustMultiDeliveryModePauseTime = root.findViewById(R.id.isb_adjust_multi_delivery_mode_pause_time);
            isbAdjustMultiDeliveryModePauseTime.setOnSeekChangeListener(this);


            swEnableCustomPromptForMultiDeliveryModeDeliveryArrival = root.findViewById(R.id.sw_enable_custom_prompt_for_multi_delivery_mode_delivery_arrival);
            swEnableCustomPromptForMultiDeliveryModeDeliveryArrival.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    multiDeliverySetting.enableDeliveryArrivalPrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                }
            });
            ImageButton ibAddCustomPromptForMultiDeliveryModeDeliveryArrival = root.findViewById(R.id.ib_add_custom_prompt_for_multi_delivery_mode_delivery_arrival);
            ibAddCustomPromptForMultiDeliveryModeDeliveryArrival.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (multiDeliverySetting.deliveryArrivalPrompts != null && multiDeliverySetting.deliveryArrivalPrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_MULTI_DELIVERY_MODE_ASSETS_PREFIX, content, Constants.KEY_MULTI_DELIVERY_MODE_DELIVERY_ARRIVAL_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    multiDeliverySetting.deliveryArrivalPrompts.add(presenter.getLastTryListenText());
                                    multiDeliverySetting.deliveryArrivalPromptAudioList.add(presenter.getLastGeneratedFile());
                                    multiDeliveryModeDeliveryArrivalPromptAdapter.setTextContentList(multiDeliverySetting.deliveryArrivalPrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
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
                }
            });
            RecyclerView rvCustomPromptForMultiDeliveryModeDeliveryArrival = root.findViewById(R.id.rv_custom_prompt_for_multi_delivery_mode_delivery_arrival);
            multiDeliveryModeDeliveryArrivalPromptAdapter = new BroadcastItemAdapter(TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST);
            multiDeliveryModeDeliveryArrivalPromptAdapter.setCheckedList(multiDeliverySetting.targetPromptForDeliveryArrival);
            multiDeliveryModeDeliveryArrivalPromptAdapter.setListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvCustomPromptForMultiDeliveryModeDeliveryArrival.setLayoutManager(layoutManager);
            rvCustomPromptForMultiDeliveryModeDeliveryArrival.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvCustomPromptForMultiDeliveryModeDeliveryArrival.setAdapter(multiDeliveryModeDeliveryArrivalPromptAdapter);


            tvMultiDeliveryModeBackgroundMusic = root.findViewById(R.id.tv_multi_delivery_mode_background_music);
            btnChooseMultiDeliveryModeBackgroundMusic = root.findViewById(R.id.btn_choose_multi_delivery_mode_background_music);
            btnChooseMultiDeliveryModeBackgroundMusic.setOnClickListener(this);
            swEnableMultiDeliveryModeBackgroundMusic = root.findViewById(R.id.sw_multi_delivery_mode_enable_background_music);
            swEnableMultiDeliveryModeBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    multiDeliverySetting.enableBackgroundMusic = isChecked;
                    if (isChecked) {
                        btnChooseMultiDeliveryModeBackgroundMusic.setVisibility(View.VISIBLE);
                        tvMultiDeliveryModeBackgroundMusic.setText(multiDeliverySetting.backgroundMusicFileName == null ? getString(R.string.text_do_not_play_background_music) : multiDeliverySetting.backgroundMusicFileName);
                    } else {
                        btnChooseMultiDeliveryModeBackgroundMusic.setVisibility(View.GONE);
                        tvMultiDeliveryModeBackgroundMusic.setText(R.string.text_do_not_play_background_music);
                    }
                    SpManager.getInstance().edit().putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                }
            });

            root.setTag("initialized");
        }

        swEnableMultiDeliveryModeBackgroundMusic.setChecked(multiDeliverySetting.enableBackgroundMusic);
        btnChooseMultiDeliveryModeBackgroundMusic.setVisibility(multiDeliverySetting.enableBackgroundMusic ? View.VISIBLE : View.GONE);
        tvMultiDeliveryModeBackgroundMusic.setText((!multiDeliverySetting.enableBackgroundMusic || multiDeliverySetting.backgroundMusicFileName == null) ? getString(R.string.text_do_not_play_background_music) : multiDeliverySetting.backgroundMusicFileName);
        isbAdjustMultiDeliveryModeSpeed.setProgress(multiDeliverySetting.runningSpeed);
        isbAdjustMultiDeliveryModePauseTime.setProgress(multiDeliverySetting.pauseTime);

        swEnableCustomPromptForMultiDeliveryModeDeliveryArrival.setChecked(multiDeliverySetting.enableDeliveryArrivalPrompt);
        if (multiDeliverySetting.deliveryArrivalPrompts != null && !multiDeliverySetting.deliveryArrivalPrompts.isEmpty()) {
            multiDeliveryModeDeliveryArrivalPromptAdapter.setTextContentList(multiDeliverySetting.deliveryArrivalPrompts);
        }
    }

    //送餐模式
    private void initDeliveryModeView(ExpandableLayout root) {
        if (deliveryMealSetting == null) {
            String deliveryModeConfig = SpManager.getInstance().getString(Constants.KEY_DELIVERY_MODE_CONFIG, null);
            if (TextUtils.isEmpty(deliveryModeConfig)) {
                deliveryMealSetting = DeliveryMealSetting.getDefault();
            } else {
                deliveryMealSetting = gson.fromJson(deliveryModeConfig, DeliveryMealSetting.class);
            }
        }
        if (root.getTag() == null) {
            ibIncreaseDeliveryModeSpeed = root.findViewById(R.id.ib_increase_delivery_mode_speed);
            ibIncreaseDeliveryModeSpeed.setOnClickListener(this);
            ibDecreaseDeliveryModeSpeed = root.findViewById(R.id.ib_decrease_delivery_mode_speed);
            ibDecreaseDeliveryModeSpeed.setOnClickListener(this);
            isbAdjustDeliveryModeSpeed = root.findViewById(R.id.isb_adjust_delivery_mode_speed);
            isbAdjustDeliveryModeSpeed.setOnSeekChangeListener(this);

            ibIncreaseDeliveryModePauseTime = root.findViewById(R.id.ib_increase_delivery_mode_pause_time);
            ibIncreaseDeliveryModePauseTime.setOnClickListener(this);
            ibDecreaseDeliveryModePauseTime = root.findViewById(R.id.ib_decrease_delivery_mode_pause_time);
            ibDecreaseDeliveryModePauseTime.setOnClickListener(this);
            isbAdjustDeliveryModePauseTime = root.findViewById(R.id.isb_adjust_delivery_mode_pause_time);
            isbAdjustDeliveryModePauseTime.setOnSeekChangeListener(this);

            swEnableCustomPromptForDeliveryModeDeliveryArrival = root.findViewById(R.id.sw_enable_custom_prompt_for_delivery_mode_delivery_arrival);
            swEnableCustomPromptForDeliveryModeDeliveryArrival.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    deliveryMealSetting.enableDeliveryArrivalPrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                }
            });
            ImageButton ibAddCustomPromptForDeliveryModeDeliveryArrival = root.findViewById(R.id.ib_add_custom_prompt_for_delivery_mode_delivery_arrival);
            ibAddCustomPromptForDeliveryModeDeliveryArrival.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deliveryMealSetting.deliveryArrivalPrompts != null && deliveryMealSetting.deliveryArrivalPrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_DELIVERY_MODE_ASSETS_PREFIX, content, Constants.KEY_DELIVERY_MODE_DELIVERY_ARRIVAL_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    deliveryMealSetting.deliveryArrivalPrompts.add(presenter.getLastTryListenText());
                                    deliveryMealSetting.deliveryArrivalPromptAudioList.add(presenter.getLastGeneratedFile());
                                    deliveryModeDeliveryArrivalPromptAdapter.setTextContentList(deliveryMealSetting.deliveryArrivalPrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
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
                }
            });
            RecyclerView rvCustomPromptForDeliveryModeDeliveryArrival = root.findViewById(R.id.rv_custom_prompt_for_delivery_mode_delivery_arrival);
            deliveryModeDeliveryArrivalPromptAdapter = new BroadcastItemAdapter(TYPE_DELIVERY_ARRIVAL_BROADCAST);
            deliveryModeDeliveryArrivalPromptAdapter.setCheckedList(deliveryMealSetting.targetPromptForDeliveryArrival);
            deliveryModeDeliveryArrivalPromptAdapter.setListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvCustomPromptForDeliveryModeDeliveryArrival.setLayoutManager(layoutManager);
            rvCustomPromptForDeliveryModeDeliveryArrival.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvCustomPromptForDeliveryModeDeliveryArrival.setAdapter(deliveryModeDeliveryArrivalPromptAdapter);


            tvDeliveryModeBackgroundMusic = root.findViewById(R.id.tv_delivery_mode_background_music);
            btnChooseDeliveryModeBackgroundMusic = root.findViewById(R.id.btn_choose_delivery_mode_background_music);
            btnChooseDeliveryModeBackgroundMusic.setOnClickListener(this);
            swEnableDeliveryModeBackgroundMusic = root.findViewById(R.id.sw_delivery_mode_enable_background_music);
            swEnableDeliveryModeBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    deliveryMealSetting.enableBackgroundMusic = isChecked;
                    if (isChecked) {
                        btnChooseDeliveryModeBackgroundMusic.setVisibility(View.VISIBLE);
                        tvDeliveryModeBackgroundMusic.setText(deliveryMealSetting.backgroundMusicFileName == null ? getString(R.string.text_do_not_play_background_music) : deliveryMealSetting.backgroundMusicFileName);
                    } else {
                        btnChooseDeliveryModeBackgroundMusic.setVisibility(View.GONE);
                        tvDeliveryModeBackgroundMusic.setText(R.string.text_do_not_play_background_music);
                    }
                    SpManager.getInstance().edit().putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                }
            });

            root.setTag("initialized");
        }

        swEnableDeliveryModeBackgroundMusic.setChecked(deliveryMealSetting.enableBackgroundMusic);
        btnChooseDeliveryModeBackgroundMusic.setVisibility(deliveryMealSetting.enableBackgroundMusic ? View.VISIBLE : View.GONE);
        tvDeliveryModeBackgroundMusic.setText((!deliveryMealSetting.enableBackgroundMusic || deliveryMealSetting.backgroundMusicFileName == null) ? getString(R.string.text_do_not_play_background_music) : deliveryMealSetting.backgroundMusicFileName);

        isbAdjustDeliveryModeSpeed.setProgress(deliveryMealSetting.runningSpeed);
        isbAdjustDeliveryModePauseTime.setProgress(deliveryMealSetting.pauseTime);

        swEnableCustomPromptForDeliveryModeDeliveryArrival.setChecked(deliveryMealSetting.enableDeliveryArrivalPrompt);
        if (deliveryMealSetting.deliveryArrivalPrompts != null && !deliveryMealSetting.deliveryArrivalPrompts.isEmpty()) {
            deliveryModeDeliveryArrivalPromptAdapter.setTextContentList(deliveryMealSetting.deliveryArrivalPrompts);
        }
    }


    //巡航模式
    private void initCruiseView(ExpandableLayout root) {
        if (cruiseModeSetting == null) {
            String cruiseModeConfig = SpManager.getInstance().getString(Constants.KEY_CRUISE_MODE_CONFIG, null);
            if (TextUtils.isEmpty(cruiseModeConfig)) {
                cruiseModeSetting = CruiseModeSetting.getDefault();
            } else {
                cruiseModeSetting = gson.fromJson(cruiseModeConfig, CruiseModeSetting.class);
            }
        }

        if (root.getTag() == null) {
            ibIncreaseCruiseModeSpeed = root.findViewById(R.id.ib_increase_cruise_mode_speed);
            ibIncreaseCruiseModeSpeed.setOnClickListener(this);
            ibDecreaseCruiseModeSpeed = root.findViewById(R.id.ib_decrease_cruise_mode_speed);
            ibDecreaseCruiseModeSpeed.setOnClickListener(this);
            isbAdjustCruiseModeSpeed = root.findViewById(R.id.isb_adjust_cruise_mode_speed);
            isbAdjustCruiseModeSpeed.setOnSeekChangeListener(this);

            ibIncreaseCruiseModeInterval = root.findViewById(R.id.ib_increase_cruise_broadcast_interval);
            ibIncreaseCruiseModeInterval.setOnClickListener(this);
            ibDecreaseCruiseModeInterval = root.findViewById(R.id.ib_decrease_cruise_broadcast_interval);
            ibDecreaseCruiseModeInterval.setOnClickListener(this);
            isbAdjustCruiseModeInterval = root.findViewById(R.id.isb_adjust_cruise_mode_broadcast_interval);
            isbAdjustCruiseModeInterval.setOnSeekChangeListener(this);

            tvCruiseBackgroundMusic = root.findViewById(R.id.tv_cruise_background_music);
            btnChooseCruiseBackgroundMusic = root.findViewById(R.id.btn_choose_cruise_background_music);
            btnChooseCruiseBackgroundMusic.setOnClickListener(this);
            swEnableCruiseBackgroundMusic = root.findViewById(R.id.sw_cruise_mode_enable_background_music);
            swEnableCruiseBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cruiseModeSetting.enableBackgroundMusic = isChecked;
                    if (isChecked) {
                        btnChooseCruiseBackgroundMusic.setVisibility(View.VISIBLE);
                        tvCruiseBackgroundMusic.setText(cruiseModeSetting.backgroundMusicFileName == null ? getString(R.string.text_do_not_play_background_music) : cruiseModeSetting.backgroundMusicFileName);
                    } else {
                        btnChooseCruiseBackgroundMusic.setVisibility(View.GONE);
                        tvCruiseBackgroundMusic.setText(R.string.text_do_not_play_background_music);
                    }
                    SpManager.getInstance().edit().putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                }
            });

            swCruiseModeEnableLoopBroadcast = root.findViewById(R.id.sw_cruise_mode_enable_loop_broadcast);
            swCruiseModeEnableLoopBroadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cruiseModeSetting.enableLoopBroadcast = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                }
            });

            ImageButton ibAddBroadcastItem = root.findViewById(R.id.ib_cruise_mode_add_loop_broadcast_item);
            ibAddBroadcastItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cruiseModeSetting.loopBroadcastPromptAudioList != null && cruiseModeSetting.loopBroadcastPromptAudioList.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_CRUISE_MODE_ASSETS_PREFIX, content, Constants.KEY_CRUISE_MODE_LOOP_BROADCAST, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    cruiseModeSetting.loopBroadcastPromptList.add(presenter.getLastTryListenText());
                                    cruiseModeSetting.loopBroadcastPromptAudioList.add(presenter.getLastGeneratedFile());
                                    cruiseLoopAdapter.setTextContentList(cruiseModeSetting.loopBroadcastPromptList);
                                    SpManager.getInstance().edit().putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
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
                }
            });

            RecyclerView rvCruiseLoopBroadcast = root.findViewById(R.id.rv_cruise_loop_broadcast);
            cruiseLoopAdapter = new BroadcastItemAdapter(TYPE_CRUISE_LOOP_BROADCAST);
            cruiseLoopAdapter.setCheckedList(cruiseModeSetting.targetLoopBroadcastPromptList);
            cruiseLoopAdapter.setListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvCruiseLoopBroadcast.setLayoutManager(layoutManager);
            rvCruiseLoopBroadcast.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvCruiseLoopBroadcast.setAdapter(cruiseLoopAdapter);

            root.setTag("initialized");
        }

        isbAdjustCruiseModeSpeed.setProgress(cruiseModeSetting.runningSpeed);
        isbAdjustCruiseModeInterval.setProgress(cruiseModeSetting.broadcastInterval);

        swEnableCruiseBackgroundMusic.setChecked(cruiseModeSetting.enableBackgroundMusic);
        btnChooseCruiseBackgroundMusic.setVisibility(cruiseModeSetting.enableBackgroundMusic ? View.VISIBLE : View.GONE);
        tvCruiseBackgroundMusic.setText((!cruiseModeSetting.enableBackgroundMusic || cruiseModeSetting.backgroundMusicFileName == null) ? getString(R.string.text_do_not_play_background_music) : cruiseModeSetting.backgroundMusicFileName);

        swCruiseModeEnableLoopBroadcast.setChecked(cruiseModeSetting.enableLoopBroadcast);
        if (cruiseModeSetting.loopBroadcastPromptList != null && !cruiseModeSetting.loopBroadcastPromptList.isEmpty()) {
            cruiseLoopAdapter.setTextContentList(cruiseModeSetting.loopBroadcastPromptList);
        }
    }

    //回收模式
    private void initRecycleModeView(ExpandableLayout root) {
        if (recycleModeSetting == null) {
            String recycleModeConfig = SpManager.getInstance().getString(Constants.KEY_RECYCLE_MODE_CONFIG, null);
            if (TextUtils.isEmpty(recycleModeConfig)) {
                recycleModeSetting = RecycleModeSetting.getDefault();
            } else {
                recycleModeSetting = gson.fromJson(recycleModeConfig, RecycleModeSetting.class);
            }
        }
        if (root.getTag() == null) {
            ibIncreaseRecycleModeSpeed = root.findViewById(R.id.ib_increase_recycle_mode_speed);
            ibIncreaseRecycleModeSpeed.setOnClickListener(this);
            ibDecreaseRecycleModeSpeed = root.findViewById(R.id.ib_decrease_recycle_mode_speed);
            ibDecreaseRecycleModeSpeed.setOnClickListener(this);
            isbAdjustRecycleModeSpeed = root.findViewById(R.id.isb_adjust_recycle_mode_speed);
            isbAdjustRecycleModeSpeed.setOnSeekChangeListener(this);

            ibIncreaseRecycleModePauseTime = root.findViewById(R.id.ib_increase_recycle_mode_pause_time);
            ibIncreaseRecycleModePauseTime.setOnClickListener(this);
            ibDecreaseRecycleModePauseTime = root.findViewById(R.id.ib_decrease_recycle_mode_pause_time);
            ibDecreaseRecycleModePauseTime.setOnClickListener(this);
            isbAdjustRecycleModePauseTime = root.findViewById(R.id.isb_adjust_recycle_mode_pause_time);
            isbAdjustRecycleModePauseTime.setOnSeekChangeListener(this);


            ibIncreaseRecycleModeBroadcastInterval = root.findViewById(R.id.ib_increase_recycle_mode_broadcast_interval);
            ibIncreaseRecycleModeBroadcastInterval.setOnClickListener(this);
            ibDecreaseRecycleModeBroadcastInterval = root.findViewById(R.id.ib_decrease_recycle_mode_broadcast_interval);
            ibDecreaseRecycleModeBroadcastInterval.setOnClickListener(this);
            isbAdjustRecycleModeBroadcastInterval = root.findViewById(R.id.isb_adjust_recycle_mode_broadcast_interval);
            isbAdjustRecycleModeBroadcastInterval.setOnSeekChangeListener(this);

            swRecycleModeEnableLoopBroadcast = root.findViewById(R.id.sw_recycle_mode_enable_loop_broadcast);
            swRecycleModeEnableLoopBroadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    recycleModeSetting.enableLoopBroadcast = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                }
            });
            ImageButton ibAddBroadcastItem = root.findViewById(R.id.ib_recycle_mode_add_loop_broadcast_item);
            ibAddBroadcastItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recycleModeSetting.loopBroadcastPromptAudioList != null && recycleModeSetting.loopBroadcastPromptAudioList.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_RECYCLE_MODE_ASSETS_PREFIX, content, Constants.KEY_RECYCLE_MODE_LOOP_BROADCAST, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    recycleModeSetting.loopBroadcastPrompts.add(presenter.getLastTryListenText());
                                    recycleModeSetting.loopBroadcastPromptAudioList.add(presenter.getLastGeneratedFile());
                                    recycleLoopAdapter.setTextContentList(recycleModeSetting.loopBroadcastPrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
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
                }
            });

            RecyclerView rvRecycleLoopBroadcast = root.findViewById(R.id.rv_recycle_loop_broadcast);
            recycleLoopAdapter = new BroadcastItemAdapter(TYPE_RECYCLE_LOOP_BROADCAST);
            recycleLoopAdapter.setCheckedList(recycleModeSetting.targetLoopBroadcastPrompts);
            recycleLoopAdapter.setListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvRecycleLoopBroadcast.setLayoutManager(layoutManager);
            rvRecycleLoopBroadcast.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvRecycleLoopBroadcast.setAdapter(recycleLoopAdapter);


            //放置回收物
            swRecycleModeEnablePlaceRecyclablePrompt = root.findViewById(R.id.sw_recycle_mode_enable_place_recyclables_prompt);
            swRecycleModeEnablePlaceRecyclablePrompt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    recycleModeSetting.enablePlaceRecyclablesPrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                }
            });
            ImageButton ibAddPlaceRecyclablePrompts = root.findViewById(R.id.ib_recycle_mode_add_place_recyclables_prompt);
            ibAddPlaceRecyclablePrompts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recycleModeSetting.placeRecyclablePrompts != null && recycleModeSetting.placeRecyclablePrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_RECYCLE_MODE_ASSETS_PREFIX, content, Constants.KEY_RECYCLE_MODE_PLACE_RECYCLABLES_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    recycleModeSetting.placeRecyclablePrompts.add(presenter.getLastTryListenText());
                                    recycleModeSetting.placeRecyclablePromptAudioList.add(presenter.getLastGeneratedFile());
                                    placeRecyclableAdapter.setTextContentList(recycleModeSetting.placeRecyclablePrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
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
                }
            });

            RecyclerView rvPlaceRecyclable = root.findViewById(R.id.rv_place_recyclables);
            placeRecyclableAdapter = new BroadcastItemAdapter(TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST);
            placeRecyclableAdapter.setCheckedList(recycleModeSetting.targetPlaceRecyclablePrompt);
            placeRecyclableAdapter.setListener(this);
            LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireContext());
            rvPlaceRecyclable.setLayoutManager(layoutManager1);
            rvPlaceRecyclable.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvPlaceRecyclable.setAdapter(placeRecyclableAdapter);

            //回收完毕
            swRecycleModeEnableRecycleCompletePrompt = root.findViewById(R.id.sw_recycle_mode_enable_recycle_complete_prompt);
            swRecycleModeEnableRecycleCompletePrompt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    recycleModeSetting.enableRecycleCompletePrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                }
            });
            ImageButton ibRecycleModeAddRecycleCompletePrompt = root.findViewById(R.id.ib_recycle_mode_add_recycle_complete_prompt);
            ibRecycleModeAddRecycleCompletePrompt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recycleModeSetting.recycleCompletePrompts != null && recycleModeSetting.recycleCompletePrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_RECYCLE_MODE_ASSETS_PREFIX, content, Constants.KEY_RECYCLE_MODE_RECYCLE_COMPLETE_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    recycleModeSetting.recycleCompletePrompts.add(presenter.getLastTryListenText());
                                    recycleModeSetting.recycleCompletePromptAudioList.add(presenter.getLastGeneratedFile());
                                    recycleCompleteAdapter.setTextContentList(recycleModeSetting.recycleCompletePrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
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
                }
            });

            RecyclerView rvRecycleComplete = root.findViewById(R.id.rv_recycle_complete);
            recycleCompleteAdapter = new BroadcastItemAdapter(TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST);
            recycleCompleteAdapter.setCheckedList(recycleModeSetting.targetRecycleCompletePrompts);
            recycleCompleteAdapter.setListener(this);
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireContext());
            rvRecycleComplete.setLayoutManager(layoutManager2);
            rvRecycleComplete.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvRecycleComplete.setAdapter(recycleCompleteAdapter);
            root.setTag("initialized");
        }

        isbAdjustRecycleModeSpeed.setProgress(recycleModeSetting.runningSpeed);
        isbAdjustRecycleModePauseTime.setProgress(recycleModeSetting.pauseTime);
        isbAdjustRecycleModeBroadcastInterval.setProgress(recycleModeSetting.broadcastInterval);
        swRecycleModeEnableLoopBroadcast.setChecked(recycleModeSetting.enableLoopBroadcast);
        swRecycleModeEnablePlaceRecyclablePrompt.setChecked(recycleModeSetting.enablePlaceRecyclablesPrompt);
        swRecycleModeEnableRecycleCompletePrompt.setChecked(recycleModeSetting.enableRecycleCompletePrompt);

        if (recycleModeSetting.loopBroadcastPrompts != null && !recycleModeSetting.loopBroadcastPrompts.isEmpty()) {
            recycleLoopAdapter.setTextContentList(recycleModeSetting.loopBroadcastPrompts);
        }

        if (recycleModeSetting.placeRecyclablePrompts != null && !recycleModeSetting.placeRecyclablePrompts.isEmpty()) {
            placeRecyclableAdapter.setTextContentList(recycleModeSetting.placeRecyclablePrompts);
        }

        if (recycleModeSetting.recycleCompletePrompts != null && !recycleModeSetting.recycleCompletePrompts.isEmpty()) {
            recycleCompleteAdapter.setTextContentList(recycleModeSetting.recycleCompletePrompts);
        }
    }

    //生日模式
    private void initBirthdayModeView(ExpandableLayout root) {
        if (birthdayModeSetting == null) {
            String birthdayModeConfig = SpManager.getInstance().getString(Constants.KEY_BIRTHDAY_MODE_CONFIG, null);
            if (TextUtils.isEmpty(birthdayModeConfig)) {
                birthdayModeSetting = BirthdayModeSetting.getDefault();
            } else {
                birthdayModeSetting = gson.fromJson(birthdayModeConfig, BirthdayModeSetting.class);
            }
        }

        if (root.getTag() == null) {
            ibIncreaseBirthdayModeSpeed = root.findViewById(R.id.ib_increase_birthday_mode_speed);
            ibIncreaseBirthdayModeSpeed.setOnClickListener(this);
            ibDecreaseBirthdayModeSpeed = root.findViewById(R.id.ib_decrease_birthday_mode_speed);
            ibDecreaseBirthdayModeSpeed.setOnClickListener(this);
            isbAdjustBirthdayModeSpeed = root.findViewById(R.id.isb_adjust_birthday_mode_speed);
            isbAdjustBirthdayModeSpeed.setOnSeekChangeListener(this);

            ibIncreaseBirthdayModeBroadcastPauseTime = root.findViewById(R.id.ib_increase_birthday_mode_pause_time);
            ibIncreaseBirthdayModeBroadcastPauseTime.setOnClickListener(this);
            ibDecreaseBirthdayModeBroadcastPauseTime = root.findViewById(R.id.ib_decrease_birthday_mode_pause_time);
            ibDecreaseBirthdayModeBroadcastPauseTime.setOnClickListener(this);
            isbAdjustBirthdayModeBroadcastPauseTime = root.findViewById(R.id.isb_adjust_birthday_mode_pause_time);
            isbAdjustBirthdayModeBroadcastPauseTime.setOnSeekChangeListener(this);

            swEnableBirthdayBackgroundMusic = root.findViewById(R.id.sw_birthday_mode_enable_background_music);
            swEnableBirthdayBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    birthdayModeSetting.enableBackgroundMusic = isChecked;
                    if (isChecked) {
                        tvBirthdayModeBackgroundMusic.setText(birthdayModeSetting.backgroundMusicFileName == null ? getString(R.string.text_do_not_play_background_music) : birthdayModeSetting.backgroundMusicFileName);
                        btnChooseBirthdayModeBackgroundMusic.setVisibility(View.VISIBLE);
                    } else {
                        btnChooseBirthdayModeBackgroundMusic.setVisibility(View.GONE);
                        tvBirthdayModeBackgroundMusic.setText(R.string.text_do_not_play_background_music);
                    }
                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                }
            });
            btnChooseBirthdayModeBackgroundMusic = root.findViewById(R.id.btn_choose_birthday_mode_background_music);
            btnChooseBirthdayModeBackgroundMusic.setOnClickListener(this);
            tvBirthdayModeBackgroundMusic = root.findViewById(R.id.tv_birthday_mode_background_music);
            rgBackgroundMusicPlayTimeControl = root.findViewById(R.id.rg_background_music_play_time_control);
            rgBackgroundMusicPlayTimeControl.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    birthdayModeSetting.backgroundMusicPlayTime = (checkedId == R.id.rb_play_at_the_target_point) ? 0 : 1;
                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                }
            });

            swBirthdayModeEnableBirthdayPickMealPrompt = root.findViewById(R.id.sw_enable_birthday_pick_meal_prompt);
            swBirthdayModeEnableBirthdayPickMealPrompt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    birthdayModeSetting.enablePickMealPrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                }
            });
            ImageButton ibAddPickMealItem = root.findViewById(R.id.ib_birthday_mode_add_pick_meal_prompt);
            ibAddPickMealItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (birthdayModeSetting.pickMealPrompt != null && birthdayModeSetting.pickMealPrompt.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_BIRTHDAY_MODE_ASSETS_PREFIX, content, Constants.KEY_BIRTHDAY_MODE_PICK_MEAL_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    birthdayModeSetting.pickMealPrompt.add(presenter.getLastTryListenText());
                                    birthdayModeSetting.pickMealPromptAudioList.add(presenter.getLastGeneratedFile());
                                    birthdayPickMealAdapter.setTextContentList(birthdayModeSetting.pickMealPrompt);
                                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
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
                }
            });

            RecyclerView rvBirthdayModePickMeal = root.findViewById(R.id.rv_birthday_mode_pick_meal);
            birthdayPickMealAdapter = new BroadcastItemAdapter(TYPE_BIRTHDAY_PICK_MEAL_BROADCAST);
            birthdayPickMealAdapter.setCheckedList(birthdayModeSetting.targetPickMealPrompt);
            birthdayPickMealAdapter.setListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvBirthdayModePickMeal.setLayoutManager(layoutManager);
            rvBirthdayModePickMeal.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvBirthdayModePickMeal.setAdapter(birthdayPickMealAdapter);


            swBirthdayModeEnableBirthdayCompletePrompt = root.findViewById(R.id.sw_birthday_mode_enable_birthday_complete_prompt);
            swBirthdayModeEnableBirthdayCompletePrompt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    birthdayModeSetting.enablePickMealCompletePrompt = isChecked;
                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                }
            });
            ImageButton ibAddBroadcastItem = root.findViewById(R.id.ib_birthday_mode_add_birthday_complete_prompt);
            ibAddBroadcastItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (birthdayModeSetting.pickMealCompletePrompts != null && birthdayModeSetting.pickMealCompletePrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                        ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                        return;
                    }
                    if (isFloatEditorActivityShow || ClickRestrict.restrictFrequency(500))return;
                    FloatEditorActivity.openEditor(requireContext(),
                            new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                            new EditorCallback() {

                                @Override
                                public void onTryListen(Activity activity, String content, View cancel, View submit) {
                                    presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_BIRTHDAY_MODE_ASSETS_PREFIX, content, Constants.KEY_BIRTHDAY_MODE_PICK_MEAL_COMPLETE_PROMPT, cancel, submit);
                                }

                                @Override
                                public void onConfirm(Activity activity, String content, View cancel, View submit) {
                                    birthdayModeSetting.pickMealCompletePrompts.add(presenter.getLastTryListenText());
                                    birthdayModeSetting.pickMealCompletePromptAudioList.add(presenter.getLastGeneratedFile());
                                    birthdayPickMealCompleteAdapter.setTextContentList(birthdayModeSetting.pickMealCompletePrompts);
                                    SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
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
                }
            });

            RecyclerView rvBirthdayComplete = root.findViewById(R.id.rv_birthday_complete);
            birthdayPickMealCompleteAdapter = new BroadcastItemAdapter(TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST);
            birthdayPickMealCompleteAdapter.setCheckedList(birthdayModeSetting.targetPickMealCompletePrompt);
            birthdayPickMealCompleteAdapter.setListener(this);
            LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireContext());
            rvBirthdayComplete.setLayoutManager(layoutManager1);
            rvBirthdayComplete.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
            rvBirthdayComplete.setAdapter(birthdayPickMealCompleteAdapter);
            root.setTag("initialized");
        }

        rgBackgroundMusicPlayTimeControl.check(birthdayModeSetting.backgroundMusicPlayTime == 0 ? R.id.rb_play_at_the_target_point : R.id.rb_play_all_the_way);

        isbAdjustBirthdayModeSpeed.setProgress(birthdayModeSetting.runningSpeed);
        isbAdjustBirthdayModeBroadcastPauseTime.setProgress(birthdayModeSetting.pauseTime);

        swEnableBirthdayBackgroundMusic.setChecked(birthdayModeSetting.enableBackgroundMusic);
        tvBirthdayModeBackgroundMusic.setText((!birthdayModeSetting.enableBackgroundMusic || birthdayModeSetting.backgroundMusicFileName == null) ? getString(R.string.text_do_not_play_background_music) : birthdayModeSetting.backgroundMusicFileName);

        swBirthdayModeEnableBirthdayPickMealPrompt.setChecked(birthdayModeSetting.enablePickMealPrompt);
        swBirthdayModeEnableBirthdayCompletePrompt.setChecked(birthdayModeSetting.enablePickMealCompletePrompt);
        if (birthdayModeSetting.pickMealPrompt != null && !birthdayModeSetting.pickMealPrompt.isEmpty()) {
            birthdayPickMealAdapter.setTextContentList(birthdayModeSetting.pickMealPrompt);
        }

        if (birthdayModeSetting.pickMealCompletePrompts != null && !birthdayModeSetting.pickMealCompletePrompts.isEmpty()) {
            birthdayPickMealCompleteAdapter.setTextContentList(birthdayModeSetting.pickMealCompletePrompts);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        switch (id) {
            case R.id.ib_increase_multi_delivery_mode_speed:
                multiDeliverySetting.runningSpeed = onFloatValueChange(v, true);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.ib_increase_delivery_mode_speed:
                deliveryMealSetting.runningSpeed = onFloatValueChange(v, true);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.ib_decrease_multi_delivery_mode_speed:
                multiDeliverySetting.runningSpeed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.ib_decrease_delivery_mode_speed:
                deliveryMealSetting.runningSpeed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.ib_increase_cruise_mode_speed:
                cruiseModeSetting.runningSpeed = onFloatValueChange(v, true);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.ib_decrease_cruise_mode_speed:
                cruiseModeSetting.runningSpeed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.ib_increase_recycle_mode_speed:
                recycleModeSetting.runningSpeed = onFloatValueChange(v, true);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.ib_decrease_recycle_mode_speed:
                recycleModeSetting.runningSpeed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.ib_increase_birthday_mode_speed:
                birthdayModeSetting.runningSpeed = onFloatValueChange(v, true);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.ib_decrease_birthday_mode_speed:
                birthdayModeSetting.runningSpeed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.ib_increase_multi_delivery_mode_pause_time:
                multiDeliverySetting.pauseTime = onIntValueChange(v, true);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.ib_decrease_multi_delivery_mode_pause_time:
                multiDeliverySetting.pauseTime = onIntValueChange(v, false);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.ib_increase_delivery_mode_pause_time:
                deliveryMealSetting.pauseTime = onIntValueChange(v, true);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.ib_decrease_delivery_mode_pause_time:
                deliveryMealSetting.pauseTime = onIntValueChange(v, false);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.ib_increase_recycle_mode_pause_time:
                recycleModeSetting.pauseTime = onIntValueChange(v, true);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.ib_decrease_recycle_mode_pause_time:
                recycleModeSetting.pauseTime = onIntValueChange(v, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.ib_increase_birthday_mode_pause_time:
                birthdayModeSetting.pauseTime = onIntValueChange(v, true);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.ib_decrease_birthday_mode_pause_time:
                birthdayModeSetting.pauseTime = onIntValueChange(v, false);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.ib_increase_cruise_broadcast_interval:
                cruiseModeSetting.broadcastInterval = onIntValueChange(v, true);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.ib_decrease_cruise_broadcast_interval:
                cruiseModeSetting.broadcastInterval = onIntValueChange(v, false);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.ib_increase_recycle_mode_broadcast_interval:
                recycleModeSetting.broadcastInterval = onIntValueChange(v, true);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.ib_decrease_recycle_mode_broadcast_interval:
                recycleModeSetting.broadcastInterval = onIntValueChange(v, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.btn_choose_cruise_background_music:
                presenter.loadBackgroundMusic(requireContext(), BACKGROUND_MUSIC_TYPE_CRUISE);
                break;
            case R.id.btn_choose_birthday_mode_background_music:
                presenter.loadBackgroundMusic(requireContext(), BACKGROUND_MUSIC_TYPE_BIRTHDAY);
                break;
            case R.id.btn_choose_delivery_mode_background_music:
                presenter.loadBackgroundMusic(requireContext(), BACKGROUND_MUSIC_TYPE_DELIVERY);
                break;
            case R.id.btn_choose_multi_delivery_mode_background_music:
                presenter.loadBackgroundMusic(requireContext(), BACKGROUND_MUSIC_TYPE_MULTI_DELIVERY);
                break;
        }
    }

    @Override
    public void onMusicListLoaded(int type, @NonNull List<String> music) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> showBackgroundMusicSelectDialog(type, music), 1000);
    }

    private void showBackgroundMusicSelectDialog(int type, @NotNull List<String> music) {
        if (EasyDialog.isShow()) {
            EasyDialog.getInstance().dismiss();
        }
        File file;
        if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
            file = new File(requireContext().getFilesDir() + Constants.KEY_CRUISE_MODE_BACKGROUND_MUSIC_PATH);
            new BackgroundMusicChooseDialog(requireContext(), BACKGROUND_MUSIC_TYPE_CRUISE, file.getAbsolutePath(), cruiseModeSetting.backgroundMusicFileName, music, this).show();
        } else if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
            file = new File(requireContext().getFilesDir() + Constants.KEY_BIRTHDAY_MODE_BACKGROUND_MUSIC_PATH);
            new BackgroundMusicChooseDialog(requireContext(), BACKGROUND_MUSIC_TYPE_BIRTHDAY, file.getAbsolutePath(), birthdayModeSetting.backgroundMusicFileName, music, this).show();
        } else if (type == BACKGROUND_MUSIC_TYPE_DELIVERY) {
            file = new File(requireContext().getFilesDir() + Constants.KEY_DELIVERY_MODE_BACKGROUND_MUSIC_PATH);
            new BackgroundMusicChooseDialog(requireContext(), BACKGROUND_MUSIC_TYPE_DELIVERY, file.getAbsolutePath(), deliveryMealSetting.backgroundMusicFileName, music, this).show();
        } else {
            file = new File(requireContext().getFilesDir() + Constants.KEY_MULTI_DELIVERY_MODE_BACKGROUND_MUSIC_PATH);
            new BackgroundMusicChooseDialog(requireContext(), BACKGROUND_MUSIC_TYPE_MULTI_DELIVERY, file.getAbsolutePath(), multiDeliverySetting.backgroundMusicFileName, music, this).show();
        }
    }


    @Override
    public void onMusicListFailed(int type, Throwable e) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showBackgroundMusicSelectDialog(type, new ArrayList<>());
        }, 1000);
    }


    private float onFloatValueChange(View v, boolean isAdd) {
        if (v instanceof IndicatorSeekBar) return ((IndicatorSeekBar) v).getProgressFloat();
        ViewGroup parent = (ViewGroup) v.getParent();
        IndicatorSeekBar seekBar = (IndicatorSeekBar) parent.getChildAt(1);
        float progress = seekBar.getProgressFloat();
        progress = (float) (isAdd ? progress + 0.1 : progress - 0.1);
        seekBar.setProgress(progress);
        return seekBar.getProgressFloat();
    }

    private int onIntValueChange(View v, boolean isAdd) {
        if (v instanceof IndicatorSeekBar) return ((IndicatorSeekBar) v).getProgress();
        ViewGroup parent = (ViewGroup) v.getParent();
        IndicatorSeekBar seekBar = (IndicatorSeekBar) parent.getChildAt(1);
        float progress = seekBar.getProgressFloat();
        progress = isAdd ? progress + 1 : progress - 1;
        seekBar.setProgress(progress);
        return seekBar.getProgress();
    }


    @Override
    public void onSeeking(SeekParams seekParams) {

    }

    @Override
    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
        int id = seekBar.getId();
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        switch (id) {
            case R.id.isb_adjust_delivery_mode_speed:
                deliveryMealSetting.runningSpeed = onFloatValueChange(seekBar, false);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.isb_adjust_multi_delivery_mode_speed:
                multiDeliverySetting.runningSpeed = onFloatValueChange(seekBar, false);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.isb_adjust_cruise_mode_speed:
                cruiseModeSetting.runningSpeed = onFloatValueChange(seekBar, false);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.isb_adjust_recycle_mode_speed:
                recycleModeSetting.runningSpeed = onFloatValueChange(seekBar, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.isb_adjust_birthday_mode_speed:
                birthdayModeSetting.runningSpeed = onFloatValueChange(seekBar, false);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.isb_adjust_delivery_mode_pause_time:
                deliveryMealSetting.pauseTime = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
                break;
            case R.id.isb_adjust_multi_delivery_mode_pause_time:
                multiDeliverySetting.pauseTime = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
                break;
            case R.id.isb_adjust_recycle_mode_pause_time:
                recycleModeSetting.pauseTime = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
            case R.id.isb_adjust_birthday_mode_pause_time:
                birthdayModeSetting.pauseTime = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
                break;
            case R.id.isb_adjust_cruise_mode_broadcast_interval:
                cruiseModeSetting.broadcastInterval = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
                break;
            case R.id.isb_adjust_recycle_mode_broadcast_interval:
                recycleModeSetting.broadcastInterval = onIntValueChange(seekBar, false);
                edit.putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
                break;
        }
    }


    @Override
    public void onBackgroundMusicSelected(int type, BackgroundMusicItem file) {
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
            tvBirthdayModeBackgroundMusic.setText(file == null ? getString(R.string.text_do_not_play_background_music) : (file.fileName == null ? getString(R.string.text_do_not_play_background_music) : file.fileName));
            birthdayModeSetting.backgroundMusicPath = file == null ? null : file.localPath;
            birthdayModeSetting.backgroundMusicFileName = file == null ? null : file.fileName;
            edit.putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
        } else if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
            tvCruiseBackgroundMusic.setText(file == null ? getString(R.string.text_do_not_play_background_music) : (file.fileName == null ? getString(R.string.text_do_not_play_background_music) : file.fileName));
            cruiseModeSetting.backgroundMusicPath = file == null ? null : file.localPath;
            cruiseModeSetting.backgroundMusicFileName = file == null ? null : file.fileName;
            edit.putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
        } else if (type == BACKGROUND_MUSIC_TYPE_DELIVERY) {
            tvDeliveryModeBackgroundMusic.setText(file == null ? getString(R.string.text_do_not_play_background_music) : (file.fileName == null ? getString(R.string.text_do_not_play_background_music) : file.fileName));
            deliveryMealSetting.backgroundMusicPath = file == null ? null : file.localPath;
            deliveryMealSetting.backgroundMusicFileName = file == null ? null : file.fileName;
            edit.putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
        } else if (type == BACKGROUND_MUSIC_TYPE_MULTI_DELIVERY) {
            tvMultiDeliveryModeBackgroundMusic.setText(file == null ? getString(R.string.text_do_not_play_background_music) : (file.fileName == null ? getString(R.string.text_do_not_play_background_music) : file.fileName));
            multiDeliverySetting.backgroundMusicPath = file == null ? null : file.localPath;
            multiDeliverySetting.backgroundMusicFileName = file == null ? null : file.fileName;
            edit.putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
        }
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
        ToastUtils.showShortToast(message);
    }

    @Override
    public void onDeleteBroadcastItem(int type, int position, String text) {
        if (type == TYPE_CRUISE_LOOP_BROADCAST) {
            cruiseModeSetting.loopBroadcastPromptList.remove(position);
            String s = cruiseModeSetting.loopBroadcastPromptAudioList.get(position);
            new File(s).delete();
            cruiseModeSetting.loopBroadcastPromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
            cruiseLoopAdapter.setTextContentList(cruiseModeSetting.loopBroadcastPromptList);
        } else if (type == TYPE_RECYCLE_LOOP_BROADCAST) {
            recycleModeSetting.loopBroadcastPrompts.remove(position);
            String s = recycleModeSetting.loopBroadcastPromptAudioList.get(position);
            new File(s).delete();
            recycleModeSetting.loopBroadcastPromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
            recycleLoopAdapter.setTextContentList(recycleModeSetting.loopBroadcastPrompts);
        } else if (type == TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST) {
            recycleModeSetting.placeRecyclablePrompts.remove(position);
            String s = recycleModeSetting.placeRecyclablePromptAudioList.get(position);
            new File(s).delete();
            recycleModeSetting.placeRecyclablePromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
            placeRecyclableAdapter.setTextContentList(recycleModeSetting.placeRecyclablePrompts);
        } else if (type == TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST) {
            recycleModeSetting.recycleCompletePrompts.remove(position);
            String s = recycleModeSetting.recycleCompletePromptAudioList.get(position);
            new File(s).delete();
            recycleModeSetting.recycleCompletePromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
            recycleCompleteAdapter.setTextContentList(recycleModeSetting.recycleCompletePrompts);
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_BROADCAST) {
            birthdayModeSetting.pickMealPrompt.remove(position);
            String s = birthdayModeSetting.pickMealPromptAudioList.get(position);
            new File(s).delete();
            birthdayModeSetting.pickMealPromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
            birthdayPickMealAdapter.setTextContentList(birthdayModeSetting.pickMealPrompt);
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST) {
            birthdayModeSetting.pickMealCompletePrompts.remove(position);
            String s = birthdayModeSetting.pickMealCompletePromptAudioList.get(position);
            new File(s).delete();
            birthdayModeSetting.pickMealCompletePromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
            birthdayPickMealCompleteAdapter.setTextContentList(birthdayModeSetting.pickMealCompletePrompts);
        } else if (type == TYPE_DELIVERY_ARRIVAL_BROADCAST) {
            deliveryMealSetting.deliveryArrivalPrompts.remove(position);
            String s = deliveryMealSetting.deliveryArrivalPromptAudioList.get(position);
            new File(s).delete();
            deliveryMealSetting.deliveryArrivalPromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
            deliveryModeDeliveryArrivalPromptAdapter.setTextContentList(deliveryMealSetting.deliveryArrivalPrompts);
        } else if (type == TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST) {
            multiDeliverySetting.deliveryArrivalPrompts.remove(position);
            String s = multiDeliverySetting.deliveryArrivalPromptAudioList.get(position);
            new File(s).delete();
            multiDeliverySetting.deliveryArrivalPromptAudioList.remove(position);
            SpManager.getInstance().edit().putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
            multiDeliveryModeDeliveryArrivalPromptAdapter.setTextContentList(multiDeliverySetting.deliveryArrivalPrompts);
        }
    }

    @Override
    public void onAudition(int type, int position, View view) {
        if (VoiceHelper.isPlaying()) {
            VoiceHelper.pause();
            ((ImageButton) view).setImageResource(R.drawable.icon_audition);
            return;
        }
        ((ImageButton) view).setImageResource(R.drawable.icon_audition_inactive);
        String file = null;
        if (type == TYPE_CRUISE_LOOP_BROADCAST) {
            file = cruiseModeSetting.loopBroadcastPromptAudioList.get(position);
        } else if (type == TYPE_RECYCLE_LOOP_BROADCAST) {
            file = recycleModeSetting.loopBroadcastPromptAudioList.get(position);
        } else if (type == TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST) {
            file = recycleModeSetting.placeRecyclablePromptAudioList.get(position);
        } else if (type == TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST) {
            file = recycleModeSetting.recycleCompletePromptAudioList.get(position);
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_BROADCAST) {
            file = birthdayModeSetting.pickMealPromptAudioList.get(position);
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST) {
            file = birthdayModeSetting.pickMealCompletePromptAudioList.get(position);
        } else if (type == TYPE_DELIVERY_ARRIVAL_BROADCAST) {
            file = deliveryMealSetting.deliveryArrivalPromptAudioList.get(position);
        } else if (type == TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST) {
            file = multiDeliverySetting.deliveryArrivalPromptAudioList.get(position);
        }
        VoiceHelper.playFile(file, () -> ((ImageButton) view).setImageResource(R.drawable.icon_audition));
    }

    @Override
    public void onCheckChange(int type, List<Integer> list) {
        if (type == TYPE_CRUISE_LOOP_BROADCAST) {
            cruiseModeSetting.targetLoopBroadcastPromptList = list;
            SpManager.getInstance().edit().putString(Constants.KEY_CRUISE_MODE_CONFIG, gson.toJson(cruiseModeSetting)).apply();
        } else if (type == TYPE_RECYCLE_LOOP_BROADCAST) {
            recycleModeSetting.targetLoopBroadcastPrompts = list;
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
        } else if (type == TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST) {
            recycleModeSetting.targetPlaceRecyclablePrompt = list;
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
        } else if (type == TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST) {
            recycleModeSetting.targetRecycleCompletePrompts = list;
            SpManager.getInstance().edit().putString(Constants.KEY_RECYCLE_MODE_CONFIG, gson.toJson(recycleModeSetting)).apply();
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_BROADCAST) {
            birthdayModeSetting.targetPickMealPrompt = list;
            SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
        } else if (type == TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST) {
            birthdayModeSetting.targetPickMealCompletePrompt = list;
            SpManager.getInstance().edit().putString(Constants.KEY_BIRTHDAY_MODE_CONFIG, gson.toJson(birthdayModeSetting)).apply();
        } else if (type == TYPE_DELIVERY_ARRIVAL_BROADCAST) {
            deliveryMealSetting.targetPromptForDeliveryArrival = list;
            SpManager.getInstance().edit().putString(Constants.KEY_DELIVERY_MODE_CONFIG, gson.toJson(deliveryMealSetting)).apply();
        } else if (type == TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST) {
            multiDeliverySetting.targetPromptForDeliveryArrival = list;
            SpManager.getInstance().edit().putString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, gson.toJson(multiDeliverySetting)).apply();
        }
    }
}
