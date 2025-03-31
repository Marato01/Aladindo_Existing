package com.reeman.delige.widgets;

import android.view.View;

import com.reeman.delige.utils.SizeUtils;


public class ExplainView {
    private int id,l,t,r,b;

    private View view;
    private int width,height;

    private LayoutIdData idData;

    private ContentViewData contentViewData;
    private int viewGravity;

    public ExplainView(int id,int height,int paddingTop){
        this.id = id;
        this.height = SizeUtils.dp2px(height);
        this.t = SizeUtils.dp2px(paddingTop);
    }

    public ExplainView(View view,int height,int paddingTop){
        this.view = view;
        this.height = SizeUtils.dp2px(height);
        this.t = SizeUtils.dp2px(paddingTop);
    }

    public ExplainView(int id,int height,int margin,int gravity){
        this(id,height,margin);
        this.viewGravity = gravity;
    }

    public ExplainView(View view,int height,int margin,int gravity){
        this(view,height,margin);
        this.viewGravity = gravity;
    }

    public ExplainView(LayoutIdData idData,ContentViewData contentData,int width, int height){
        this.idData = idData;
        this.contentViewData = contentData;
        this.width = SizeUtils.dp2px(width);
        this.height = SizeUtils.dp2px(height);
    }

    public int getViewGravity() {
        return viewGravity;
    }

    public void setViewGravity(int viewGravity) {
        this.viewGravity = viewGravity;
    }

    public LayoutIdData getIdData() {
        return idData;
    }

    public ContentViewData getContentViewData() {
        return contentViewData;
    }

    public void setContentViewData(ContentViewData contentViewData) {
        this.contentViewData = contentViewData;
    }

    public void setIdData(LayoutIdData idData) {
        this.idData = idData;
    }
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public View getView() {
        return view;
    }

    public void setView(View explainView) {
        this.view = explainView;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
