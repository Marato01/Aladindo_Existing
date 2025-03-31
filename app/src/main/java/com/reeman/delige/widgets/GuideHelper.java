package com.reeman.delige.widgets;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.reeman.delige.utils.VoiceHelper;

import java.util.List;

public class GuideHelper {
    private static int currentGuide = 0;

    public static void startGuide(Activity activity, List<GuideItem> list, OnEventListener listener) {
        createGuideItem(activity, currentGuide, list.get(currentGuide), new OnSkipListener() {
            @Override
            public void onSkip() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (++currentGuide < list.size()) {
                        createGuideItem(activity, currentGuide, list.get(currentGuide), this, listener);
                    } else {
                        currentGuide = 0;
                        listener.onComplete();
                    }
                }, 300);
            }
        }, listener);
    }

    public static void createGuideItem(Activity activity, int current, GuideItem guideItem, OnSkipListener listener, OnEventListener eventListener) {
        if (eventListener != null)
            eventListener.onGuideItemStart(current);
        VoiceHelper.play(guideItem.audioFile);
        GuideView.with(activity)
                .setShadowSize(10)
                .setShapeType(GuideView.RECTANGLE)
                .setSkipButton(guideItem.skipText, guideItem.skipGravity)
                .setOnViews(new OnViewData[]{GuideView.buildOnViewData(guideItem.view, 5, 10, 5, 10, GuideView.buildExplainView(guideItem.alignment, new ContentViewData(guideItem.desc), guideItem.width, guideItem.height))})
                .setDismissCallback(new ViewBuilder.DismissCallback() {
                    @Override
                    public void skip() {
                        if (listener != null) {
                            listener.onSkip();
                        }
                    }

                    @Override
                    public void dismiss(int oldPosition, int newPosition) {
                    }
                })
                .show();
    }

    public interface OnSkipListener {
        void onSkip();
    }

    public interface OnEventListener {
        void onComplete();

        void onGuideItemStart(int currentGuide);
    }
}
