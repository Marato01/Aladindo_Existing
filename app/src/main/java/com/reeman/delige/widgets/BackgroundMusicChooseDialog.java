package com.reeman.delige.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.delige.R;
import com.reeman.delige.adapter.BackgroundMusicItemAdapter;
import com.reeman.delige.models.BackgroundMusicItem;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.event.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackgroundMusicChooseDialog extends BaseDialog {

    private final RecyclerView rvBackgroundMusicList;
    public static final int BACKGROUND_MUSIC_TYPE_BIRTHDAY = 0;
    public static final int BACKGROUND_MUSIC_TYPE_CRUISE = 1;
    public static final int BACKGROUND_MUSIC_TYPE_DELIVERY = 2;
    public static final int BACKGROUND_MUSIC_TYPE_MULTI_DELIVERY = 3;
    private final int type;

    public BackgroundMusicChooseDialog(@NonNull Context context, int type, String dir, String current, List<String> onlineMusics, OnBackgroundMusicSelectedListener listener) {
        super(context);
        this.type = type;
        setCanceledOnTouchOutside(false);
        setOnDismissListener(dialog -> {
            if (VoiceHelper.isPlaying()) {
                VoiceHelper.pause();
            }
        });
        View root = LayoutInflater.from(context).inflate(R.layout.layout_background_music_choose_dialog, null);
        List<BackgroundMusicItem> list = new ArrayList<>();

        if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
            list.add(new BackgroundMusicItem("happy_birthday_1.wav", true, null, null));
        } else if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
            list.add(new BackgroundMusicItem("cruise_music_1.mp3", true, null, null));
        }

        File file = new File(dir);
        if (!file.exists()) file.mkdirs();
        File[] files = file.listFiles();
        if (files != null) {
            for (File localFile : files) {
                list.add(new BackgroundMusicItem(localFile.getName(), false, null, localFile.getAbsolutePath()));
            }
        }

        BackgroundMusicItem backgroundMusicItem;
        for (String onlineMusic : onlineMusics) {
            boolean hasDownload = false;
            for (BackgroundMusicItem musicItem : list) {
                if (musicItem.fileName.equals(onlineMusic)) {
                    hasDownload = true;
                    break;
                }
            }
            if (hasDownload) continue;
            if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
                backgroundMusicItem = new BackgroundMusicItem(onlineMusic, false, API.downBirthdayMusicAPI(Event.getIpEvent().ipAddress), null);
            } else if(type == BACKGROUND_MUSIC_TYPE_CRUISE){
                backgroundMusicItem = new BackgroundMusicItem(onlineMusic, false, API.downCruiseMusicAPI(Event.getIpEvent().ipAddress), null);
            } else{
                backgroundMusicItem = new BackgroundMusicItem(onlineMusic, false, API.downDeliveryMusicAPI(Event.getIpEvent().ipAddress), null);
            }
            list.add(backgroundMusicItem);
        }


        //list.add(new BackgroundMusicItem(null, false, null, null));
        int currentSelectIndex = -1;
        if (current != null) {
            for (int i = 0; i < list.size(); i++) {
                if (current.equals(list.get(i).fileName)) {
                    currentSelectIndex = i;
                    break;
                }
            }
        }

        rvBackgroundMusicList = root.findViewById(R.id.rv_background_music_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvBackgroundMusicList.setLayoutManager(layoutManager);
        BackgroundMusicItemAdapter adapter = new BackgroundMusicItemAdapter(list, currentSelectIndex, type);
        rvBackgroundMusicList.setAdapter(adapter);
        rvBackgroundMusicList.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));

        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BackgroundMusicItem> fileList = adapter.getFileList();
                if (fileList != null) {
                    for (BackgroundMusicItem musicItem : fileList) {
                        if (musicItem.isDownloading) {
                            ToastUtils.showShortToast(getContext().getString(R.string.text_downloading_do_not_exit));
                            return;
                        }
                    }
                }

                BackgroundMusicItem currentSelectMusic = adapter.getCurrentSelectMusic();

                if (currentSelectMusic == null) {
                    dismiss();
                    listener.onBackgroundMusicSelected(type, null);
                    return;
                }

                //网络文件需要下载
                if (currentSelectMusic.localPath == null && !currentSelectMusic.isAssetsFile && currentSelectMusic.netPath != null) {
                    ToastUtils.showShortToast(context.getString(R.string.text_please_download_this_music_first));
                    return;
                }

                if (VoiceHelper.isPlaying()) VoiceHelper.pause();
                dismiss();
                if (listener != null) {
                    listener.onBackgroundMusicSelected(type, currentSelectMusic);
                }
            }
        });

        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }


    public interface OnBackgroundMusicSelectedListener {
        void onBackgroundMusicSelected(int type, BackgroundMusicItem file);
    }

}
