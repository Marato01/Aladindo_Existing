package com.reeman.delige.widgets;

import android.app.Activity;
import android.view.View;

public class GuideView {
    public static int RECTANGLE = 1,CIRCLE = 2,OVAL = 3;
    private static Activity context;
    private static View bgView;
    public GuideView(){

    }
    public static ViewBuilder with(Activity activity){
        if(activity == null){
            return null;
        }
        context = activity;
        bgView = null;
        return ViewBuilder.getInstance(activity);
    }
    public static ViewBuilder with(Activity activity,View bgLayout){
        if(activity == null){
            return null;
        }
        context = activity;
        bgView = bgLayout;
        return ViewBuilder.getInstance(activity,bgView);
    }
    public static ExplainView buildExplainView(LayoutIdData iddata, ContentViewData viewData, int width, int height){
        return new ExplainView(iddata,viewData,width,height);
    }
    public static OnViewData buildOnViewData(int id, int l, int t, int r, int b,ExplainView explainView){
        return new OnViewData(bgView==null?context.findViewById(id):bgView.findViewById(id),l,t,r,b,explainView);
    }
    public static OnViewData buildOnViewData(View view, int l, int t, int r, int b,ExplainView explainView){
        return new OnViewData(view,l,t,r,b,explainView);
    }
}
