package com.reeman.delige.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.reeman.delige.R;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.models.BackgroundMusicItem;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.service.RobotService;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.EasyDialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.github.abdularis.buttonprogress.DownloadButtonProgress.STATE_IDLE;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_BIRTHDAY;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_CRUISE;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_DELIVERY;

public class BackgroundMusicItemAdapter extends RecyclerView.Adapter<BackgroundMusicItemAdapter.ViewHolder> {
    private final List<BackgroundMusicItem> fileList;
    private final int type;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int currentSelectIndex = -1;

    private int currentPlayingIndex = -1;

    static class ProgressRunnable implements Runnable {

        public long progress;

        public ProgressRunnable(long progress) {
            this.progress = progress;
        }

        @Override
        public void run() {

        }
    }

    public List<BackgroundMusicItem> getFileList() {
        return fileList;
    }

    public BackgroundMusicItem getCurrentSelectMusic() {
        return currentSelectIndex == -1 ? null : fileList.get(currentSelectIndex);
    }

    public BackgroundMusicItemAdapter(List<BackgroundMusicItem> files, int currentSelectIndex, int type) {
        this.currentSelectIndex = currentSelectIndex;
        this.fileList = files;
        this.type = type;
    }

    @NonNull
    @Override
    public BackgroundMusicItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_music_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull BackgroundMusicItemAdapter.ViewHolder holder, int pos) {
        BackgroundMusicItem file = fileList.get(pos);
        Context context = holder.root.getContext();
        holder.tvFileName.setText(file.fileName == null ? context.getString(R.string.text_do_not_play_background_music) : file.fileName);
        holder.tvTryListen.setOnClickListener(v -> {
            if (VoiceHelper.isPlaying()) {
                VoiceHelper.pause();
                if (currentPlayingIndex == pos) {
                    holder.tvTryListen.setText(context.getString(R.string.text_try_listen));
                    currentPlayingIndex = -1;
                } else {
                    notifyItemChanged(currentPlayingIndex);
                    currentPlayingIndex = pos;
                    holder.tvTryListen.setText(context.getString(R.string.text_pause));
                    if (file.isAssetsFile) {
                        try {
                            VoiceHelper.playAssetsFile(context.getAssets().openFd("zh/" + file.fileName), () -> holder.tvTryListen.setText(context.getString(R.string.text_try_listen)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (file.localPath != null) {
                        VoiceHelper.playFile(file.localPath, () -> holder.tvTryListen.setText(context.getString(R.string.text_try_listen)));
                    }
                }
                return;
            }
            try {
                if (file.isAssetsFile) {
                    VoiceHelper.playAssetsFile(context.getAssets().openFd("zh/" + file.fileName), () -> holder.tvTryListen.setText(context.getString(R.string.text_try_listen)));
                    holder.tvTryListen.setText(context.getString(R.string.text_pause));
                } else if (file.localPath != null) {
                    VoiceHelper.playFile(file.localPath, () -> holder.tvTryListen.setText(context.getString(R.string.text_try_listen)));
                    holder.tvTryListen.setText(context.getString(R.string.text_pause));
                }
                if (currentPlayingIndex == -1) {
                    currentPlayingIndex = pos;
                } else if (currentPlayingIndex != pos) {
                    notifyItemChanged(currentPlayingIndex);
                    currentPlayingIndex = pos;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        holder.progress.addOnClickListener(new DownloadButtonProgress.OnClickListener() {
            @Override
            public void onIdleButtonClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                BackgroundMusicItem file = fileList.get(adapterPosition);
                XLog.w("xuedong", "adapterPosition: " + adapterPosition + " " + file.toString());
                if (file.isDownloading || holder.progress.getCurrState() != STATE_IDLE) return;
                int concurrentDownloadCount = 0;
                for (BackgroundMusicItem backgroundMusicItem : fileList) {
                    if (backgroundMusicItem.isDownloading) concurrentDownloadCount++;
                }
                if (concurrentDownloadCount >= 1) {
                    ToastUtils.showShortToast(context.getString(R.string.text_overhead_max_concurrent_download_count));
                    return;
                }

                File targetFile;
                if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
                    targetFile = new File(context.getFilesDir() + Constants.KEY_BIRTHDAY_MODE_BACKGROUND_MUSIC_PATH);
                } else if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
                    targetFile = new File(context.getFilesDir() + Constants.KEY_CRUISE_MODE_BACKGROUND_MUSIC_PATH);
                } else {
                    targetFile = new File(context.getFilesDir() + Constants.KEY_DELIVERY_MODE_BACKGROUND_MUSIC_PATH);
                }

                File[] files = targetFile.listFiles();
                if (concurrentDownloadCount + (files == null ? 0 : files.length) + 1 >= 10) {
                    ToastUtils.showShortToast(context.getString(R.string.text_overhead_max_download_count));
                    return;
                }

                holder.progress.setDeterminate();
                holder.progress.setCurrentProgress(0);
                file.isDownloading = true;
                Map<String, String> params = new HashMap<>();
                params.put("name", file.fileName);
                RobotService robotService = ServiceFactory.getRobotService();
                XLog.w("开始下载");
                robotService.download(file.netPath, params)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@io.reactivex.rxjava3.annotations.NonNull ResponseBody responseBody) {
                                File targetFile;
                                if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
                                    targetFile = new File(context.getFilesDir() + Constants.KEY_BIRTHDAY_MODE_BACKGROUND_MUSIC_PATH + "/" + file.fileName);
                                } else if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
                                    targetFile = new File(context.getFilesDir() + Constants.KEY_CRUISE_MODE_BACKGROUND_MUSIC_PATH + "/" + file.fileName);
                                } else if (type == BACKGROUND_MUSIC_TYPE_DELIVERY) {
                                    targetFile = new File(context.getFilesDir() + Constants.KEY_DELIVERY_MODE_BACKGROUND_MUSIC_PATH + "/" + file.fileName);
                                } else {
                                    targetFile = new File(context.getFilesDir() + Constants.KEY_MULTI_DELIVERY_MODE_BACKGROUND_MUSIC_PATH + "/" + file.fileName);
                                }

                                if (!targetFile.exists()) {
                                    if (!targetFile.getParentFile().exists())
                                        targetFile.getParentFile().mkdir();
                                    try {
                                        targetFile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                OutputStream os = null;
                                long currentLength = 0;
                                InputStream inputStream = null;
                                try {
                                    long totalLength = responseBody.contentLength();
                                    inputStream = responseBody.byteStream();
                                    os = new BufferedOutputStream(new FileOutputStream(targetFile));
                                    byte[] data = new byte[1024];
                                    int len;
                                    while ((len = inputStream.read(data, 0, 1024)) != -1) {
                                        os.write(data, 0, len);
                                        currentLength += len;

                                        Log.w("xuedong", "" + file.isDownloading);
                                        if (!file.isDownloading) {
                                            XLog.w("取消下载");
                                            throw new IllegalStateException();
                                        }

                                        int progress = (int) (currentLength * 1.0f / totalLength * 100);
                                        if (holder.progress.getCurrentProgress() != progress) {
                                            handler.post(new ProgressRunnable(progress) {
                                                @Override
                                                public void run() {
                                                    holder.progress.setCurrentProgress((int) this.progress);
                                                }
                                            });
                                        }
                                    }
                                    file.isDownloading = false;
                                    handler.post(() -> {
                                        holder.progress.setIdle();
                                        holder.progress.setVisibility(View.GONE);
                                        holder.tvTryListen.setVisibility(View.VISIBLE);
                                        file.localPath = targetFile.getAbsolutePath();
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.w("xuedong", "" + e.getMessage());
                                    file.isDownloading = false;
                                    targetFile.delete();
                                    if (e instanceof IllegalStateException) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.progress.setIdle();
                                            }
                                        });
                                    } else {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showShortToast(context.getString(R.string.text_download_failed));
                                                holder.progress.setIdle();
                                            }
                                        });
                                    }
                                } finally {
                                    try {
                                        if (inputStream != null)
                                            inputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        if (os != null) {
                                            os.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                e.printStackTrace();
                                handler.post(() -> {
                                    ToastUtils.showShortToast(context.getString(R.string.text_download_failed));
                                    holder.progress.setIdle();
                                });
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }

            @Override
            public void onCancelButtonClick(View view) {
                Log.w("xuedong", "onCancelButtonClick");
                file.isDownloading = false;
            }

            @Override
            public void onFinishButtonClick(View view) {
                Log.w("xuedong", "onFinishButtonClick");
            }
        });
        holder.root.setOnClickListener((v) -> {
            int position = holder.getAdapterPosition();
            if (fileList.get(position).isDownloading) return;
            if (currentSelectIndex == -1) {
                currentSelectIndex = position;
                notifyItemChanged(currentSelectIndex);
            } else if (position == currentSelectIndex) {
                currentSelectIndex = -1;
                notifyItemChanged(position);
            } else {
                notifyItemChanged(position);
                notifyItemChanged(currentSelectIndex);
                currentSelectIndex = position;
            }
        });
        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                BackgroundMusicItem file = fileList.get(position);
                if (!file.isAssetsFile && file.localPath != null) {
                    if (VoiceHelper.isPlaying()) VoiceHelper.pause();
                    EasyDialog.getInstance(context).confirm(context.getString(R.string.text_confirm_delete_this_music), new EasyDialog.OnViewClickListener() {
                        @Override
                        public void onViewClick(Dialog dialog, int id) {
                            dialog.dismiss();
                            if (id == R.id.btn_confirm) {
                                new File(file.localPath).delete();
                                file.localPath = null;
                                if (currentPlayingIndex == pos) currentPlayingIndex = -1;
                                holder.progress.setIdle();
                                holder.progress.setVisibility(View.VISIBLE);
                                holder.tvTryListen.setVisibility(View.GONE);
                            } else {
                                holder.tvTryListen.setText(context.getString(R.string.text_try_listen));
                            }
                        }
                    });
                }
                return true;
            }
        });

        holder.cbSelectStatus.setChecked(pos == currentSelectIndex);

        if (currentPlayingIndex == pos) {
            holder.tvTryListen.setText(context.getString(R.string.text_pause));
        } else {
            holder.tvTryListen.setText(context.getString(R.string.text_try_listen));
        }

        if (file.isAssetsFile) {
            //默认文件
            holder.tvTryListen.setVisibility(View.VISIBLE);
            holder.progress.setVisibility(View.GONE);
        } else if (file.localPath != null) {
            //下载后的文件
            holder.tvTryListen.setVisibility(View.VISIBLE);
            holder.progress.setVisibility(View.GONE);
        } else if (file.netPath != null) {
            //网络文件
            if (file.isDownloading) {

            } else {
                holder.progress.setIdle();
            }
            holder.progress.setVisibility(View.VISIBLE);
            holder.tvTryListen.setVisibility(View.GONE);
        } else {
            holder.tvTryListen.setVisibility(View.GONE);
            holder.progress.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return fileList == null ? 0 : fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvFileName;
        private final View root;
        private final TextView tvTryListen;
        private final CheckBox cbSelectStatus;
        private final DownloadButtonProgress progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            cbSelectStatus = itemView.findViewById(R.id.cb_select_status);
            tvFileName = itemView.findViewById(R.id.tv_music_item);
            tvTryListen = itemView.findViewById(R.id.tv_try_listen);
            progress = itemView.findViewById(R.id.progress);
        }
    }
}
