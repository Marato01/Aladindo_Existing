package com.reeman.delige.widgets;

import android.view.View;

import com.reeman.delige.utils.SizeUtils;


public class OnViewData {
    private View view;
    private ExplainView explainView;
    private int paddingL,paddingT,paddingR,paddingB;

    //单位dp
    public OnViewData(View view, int l, int t, int r, int b,ExplainView eView){
        this.view = view;
        this.paddingL = SizeUtils.dp2px(l);
        this.paddingT = SizeUtils.dp2px(t);
        this.paddingR = SizeUtils.dp2px(r);
        this.paddingB = SizeUtils.dp2px(b);
        this.explainView = eView;
    }

    public ExplainView getExplainView() {
        return explainView;
    }

    public void setExplainView(ExplainView explainView) {
        this.explainView = explainView;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getPaddingL() {
        return paddingL;
    }

    public void setPaddingL(int paddingL) {
        this.paddingL = paddingL;
    }

    public int getPaddingT() {
        return paddingT;
    }

    public void setPaddingT(int paddingT) {
        this.paddingT = paddingT;
    }

    public int getPaddingR() {
        return paddingR;
    }

    public void setPaddingR(int paddingR) {
        this.paddingR = paddingR;
    }

    public int getPaddingB() {
        return paddingB;
    }

    public void setPaddingB(int paddingB) {
        this.paddingB = paddingB;
    }
}
