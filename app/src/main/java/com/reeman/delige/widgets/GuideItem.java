package com.reeman.delige.widgets;

import android.view.Gravity;
import android.view.View;

import com.reeman.delige.widgets.LayoutIdData;

public class GuideItem {
    public  String audioFile;
    public View view;
    public String desc;
    public LayoutIdData alignment;
    public int width;
    public int height;
    public String skipText;
    public int skipGravity = Gravity.END | Gravity.BOTTOM;

    public GuideItem(View view, String audioFile, String desc, String skipText, LayoutIdData alignment, int width, int height) {
        this.view = view;
        this.audioFile =audioFile;
        this.desc = desc;
        this.alignment = alignment;
        this.width = width;
        this.height = height;
        this.skipText = skipText;
    }

    public GuideItem(View view, String audioFile, String desc, String skipText, LayoutIdData alignment, int width, int height, int skipGravity) {
        this.view = view;
        this.audioFile =audioFile;
        this.desc = desc;
        this.alignment = alignment;
        this.width = width;
        this.height = height;
        this.skipText = skipText;
        this.skipGravity = skipGravity;
    }

    @Override
    public String toString() {
        return "GuideItem{" +
                "audioFile='" + audioFile + '\'' +
                ", view=" + view +
                ", desc='" + desc + '\'' +
                ", alignment=" + alignment +
                ", width=" + width +
                ", height=" + height +
                ", skipText='" + skipText + '\'' +
                ", skipGravity=" + skipGravity +
                '}';
    }
}
