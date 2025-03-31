package com.reeman.delige.widgets;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;

public class ViewBuilder {
    Activity context;
    View[] onViews;
    OnViewData[] onViewDatas[];
    ExplainView[] explainViews;
    ExplainView[] otherViews;
    int shadowSize = 0,shapeType = 1;
    DismissCallback callback;
    String skipButtonText;
    int skipButtonGravity = Gravity.LEFT;
    private static ViewBuilder instance;
    private ViewContainer viewContainer;
    View bgView;
    public ViewBuilder(Activity activity){
        this.context = activity;
    }

    public ViewBuilder(Activity activity, View view){
        this(activity);
        bgView = view;
    }

    public static ViewBuilder getInstance(Activity activity){
        if(instance == null){
            instance = new ViewBuilder(activity);
        }
        return instance;
    }

    public static ViewBuilder getInstance(Activity activity,View view){
        if(instance == null){
            instance = new ViewBuilder(activity,view);
        }
        return instance;
    }

    public static ViewBuilder build(Activity activity){
        instance = new ViewBuilder(activity);
        return instance;
    }

    public static void clearInstance(){
        instance = null;
    }

    public boolean isShowing(){
        return viewContainer != null;
    }

    public void show(){
        viewContainer = new ViewContainer(instance);
        viewContainer.showGuideView();
    }
    public void hide(){
        clearInstance();
        if (viewContainer != null) {
            viewContainer.hideGuideView();
            viewContainer = null;
        }
    }
    public void showAgain(){
        viewContainer.showAgain();
    }

    public void refreshView(int index){
        if(viewContainer == null){
            return;
        }
        viewContainer.refreshView(index);
    }

    public ViewBuilder setOnView(View... view){
        this.onViews = view;
        return this;
    }

    public ViewBuilder setOnViews(OnViewData[]... views){
        this.onViewDatas = views;
        return this;
    }

    public ViewBuilder setOtherViews(ExplainView... otherViews){
        this.otherViews = otherViews;
        return this;
    }

    public ViewBuilder setSkipButton(String text){
        this.skipButtonText = text;
        return this;
    }

    public ViewBuilder setSkipButton(String text,int gravity){
        this.skipButtonText = text;
        this.skipButtonGravity = gravity;
        return this;
    }

    public ViewBuilder setShadowSize(int size){
        this.shadowSize = size;
        return this;
    }

    public ViewBuilder setShapeType(int type){
        this.shapeType = type;
        return this;
    }

    public ViewBuilder setDismissCallback(DismissCallback callback){
        this.callback = callback;
        return this;
    }

    public ViewBuilder setExplainViews(ExplainView... explainViews){
        this.explainViews = explainViews;
        return this;
    }

    public interface DismissCallback{
        void skip();
        void dismiss(int oldPosition,int newPosition);
    }

}
