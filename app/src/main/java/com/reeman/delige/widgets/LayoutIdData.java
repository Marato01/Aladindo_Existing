package com.reeman.delige.widgets;


import com.reeman.delige.R;

public enum LayoutIdData {
    LEFT(R.layout.layout_explain_view_left),
    TOP(R.layout.layout_explain_view_top),
    RIGHT(R.layout.layout_explain_view_right),
    BOTTOM(R.layout.layout_explain_view_bottom);

    private int layoutId;
    private LayoutIdData(int id){
        this.layoutId = id;
    }

    public int getLayoutId(){
        return layoutId;
    }
}
