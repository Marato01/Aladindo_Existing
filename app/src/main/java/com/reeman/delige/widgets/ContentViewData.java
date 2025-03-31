package com.reeman.delige.widgets;


import com.reeman.delige.utils.SizeUtils;

public class ContentViewData {
    private String text;
    private int marginL,marginT,marginR,marginB,arrowLength;
    public static final int M_LEFT = 1,M_TOP = 2,M_RIGHT = 3,M_BOTTOM = 4;

    private boolean alwaysCenter;
    public ContentViewData(String text){
        this.text = text;
        this.alwaysCenter = true;
        this.arrowLength = SizeUtils.dp2px(30);
    }
    public ContentViewData(String text, int ml, int mt, int mr, int mb){
        this(text);
        this.marginL = SizeUtils.dp2px(ml);
        this.marginT = SizeUtils.dp2px(mt);
        this.marginR = SizeUtils.dp2px(mr);
        this.marginB = SizeUtils.dp2px(mb);
    }
    public ContentViewData(String text, int direction, int margin){
        this(text);
        switch (direction){
            case M_LEFT:
                this.marginL = SizeUtils.dp2px(margin);
                break;
            case M_TOP:
                this.marginT = SizeUtils.dp2px(margin);
                break;
            case M_RIGHT:
                this.marginR = SizeUtils.dp2px(margin);
                break;
            case M_BOTTOM:
                this.marginB = SizeUtils.dp2px(margin);
                break;
        }
    }
    public ContentViewData(String text, int arrowLength, boolean alwaysCenter){
        this(text);
        this.arrowLength = SizeUtils.dp2px(arrowLength);
        this.alwaysCenter = alwaysCenter;
    }
    public ContentViewData(String text, int direction, int margin, int arrowLength, boolean alwaysCenter){
        this(text,direction,margin);
        this.arrowLength = SizeUtils.dp2px(arrowLength);
        this.alwaysCenter = alwaysCenter;
    }

    public ContentViewData(String text, int ml, int mt, int mr, int mb, int arrowLength, boolean alwaysCenter){
        this(text,ml,mt,mr,mb);
        this.arrowLength = SizeUtils.dp2px(arrowLength);
        this.alwaysCenter = alwaysCenter;
    }

    public boolean isAlwaysCenter() {
        return alwaysCenter;
    }

    public void setAlwaysCenter(boolean alwaysCenter) {
        this.alwaysCenter = alwaysCenter;
    }
    public int getArrowLength() {
        return arrowLength;
    }

    public void setArrowLength(int arrowLength) {
        this.arrowLength = arrowLength;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMarginL() {
        return marginL;
    }

    public void setMarginL(int marginL) {
        this.marginL = marginL;
    }

    public int getMarginT() {
        return marginT;
    }

    public void setMarginT(int marginT) {
        this.marginT = marginT;
    }

    public int getMarginR() {
        return marginR;
    }

    public void setMarginR(int marginR) {
        this.marginR = marginR;
    }

    public int getMarginB() {
        return marginB;
    }

    public void setMarginB(int marginB) {
        this.marginB = marginB;
    }
}
