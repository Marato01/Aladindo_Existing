package com.reeman.delige.widgets;

import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.reeman.delige.R;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SizeUtils;

import java.util.ArrayList;
import java.util.List;

public class ViewContainer {
    private ViewBuilder viewBuilder;
    private ViewGroup vg;
    private DrawView myView;
    private Runnable runnable;
    private int onViewX, onViewY, onViewHeight, onViewWidth, pLeft, pTop, pRight, pBottom;
    private int viewCount;
    private View explainLayout;
    private List<View> viewList;
    private int screenW, screenH;
    private TextView skipButton;
    private int arrowSize, lineStroke;

    public ViewContainer(ViewBuilder builder) {
        this.viewBuilder = builder;
        init();
    }

    private void init() {
        arrowSize = SizeUtils.dp2px(10);
        lineStroke = SizeUtils.dp2px(2);
        screenH = (int) ScreenUtils.getScreenHeight();
        screenW = (int) ScreenUtils.getScreenWidth();
        viewList = new ArrayList<View>();
        myView = new DrawView(viewBuilder.context);
        runnable = new Runnable() {
            @Override
            public void run() {
                vg = (ViewGroup) viewBuilder.context.getWindow().getDecorView();
                if (viewBuilder.bgView != null) {
                    viewBuilder.bgView.setVisibility(View.VISIBLE);
                }
                if (viewBuilder.onViews != null && viewBuilder.onViews.length > 0) {
                    if (viewBuilder.onViews[viewCount] != null) {
                        myView.setShadowSize(viewBuilder.shadowSize);
                        myView.setShapeType(viewBuilder.shapeType);
                        setOnViewInfo(viewCount);
                    }
                }
                vg.addView(myView);
                if (viewBuilder.onViews != null && viewBuilder.onViews.length > 0) {
                    if (viewBuilder.onViews[viewCount] != null) {
                        if (viewBuilder.explainViews != null && viewBuilder.explainViews.length > 0) {
                            addLayout(viewCount);
                        }
                    }
                }
                if (viewBuilder.onViewDatas != null && viewBuilder.onViewDatas.length > 0) {
                    if (viewBuilder.onViewDatas[viewCount] != null) {
                        myView.setShadowSize(viewBuilder.shadowSize);
                        myView.setShapeType(viewBuilder.shapeType);
                        setOnViewInfos(viewBuilder.onViewDatas[viewCount]);
                        addOtherLayout();
                        addSkipButton(viewBuilder.skipButtonText);
                    }
                }
            }
        };
        myView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCount++;
//                if(viewBuilder.onViews != null && viewCount < viewBuilder.onViews.length){
//                    setOnViewInfo(viewCount);
//                    if(viewBuilder.explainViews != null && viewCount < viewBuilder.explainViews.length){
//                        vg.removeView(explainLayout);
//                        addLayout(viewCount);
//                    }else{
//                        vg.removeView(explainLayout);
//                    }
//                }else{
//                    vg.removeView(myView);
//                    vg.removeView(explainLayout);
//                }
                if (viewBuilder.onViewDatas != null && viewCount < viewBuilder.onViewDatas.length) {
                    if (viewBuilder.callback != null) {
                        viewBuilder.callback.dismiss(viewCount - 1, viewCount);
                    }
                    removeExplainViews();
                    if (viewBuilder.onViewDatas[viewCount] != null) {
                        setOnViewInfos(viewBuilder.onViewDatas[viewCount]);
                        addOtherLayout();
                        if (viewCount == viewBuilder.onViewDatas.length - 1 && skipButton != null) {
                            skipButton.setVisibility(View.INVISIBLE);
                            vg.removeView(skipButton);
                        }
                    }
                } else {
//                    vg.removeView(myView);
//                    removeExplainViews();
                }
            }
        });
    }

    private void removeExplainViews() {
        for (int i = 0; i < viewList.size(); i++) {
            vg.removeView(viewList.get(i));
        }
        viewList.clear();
    }

    private void addLayout(int viewCount) {
        if (viewBuilder == null)
            return;
        if (viewBuilder.explainViews[viewCount] != null) {
            explainLayout = LayoutInflater.from(viewBuilder.context).inflate(viewBuilder.explainViews[viewCount].getId(), null);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(onViewX + viewBuilder.explainViews[viewCount].getL(),
                    onViewY + viewBuilder.explainViews[viewCount].getT(),
                    viewBuilder.explainViews[viewCount].getR(),
                    viewBuilder.explainViews[viewCount].getB());
            vg.addView(explainLayout, params);
        }
    }

    private void setOnViewInfo(int viewCount) {
        if (viewBuilder == null)
            return;
        int[] location = new int[2];
        viewBuilder.onViews[viewCount].getLocationOnScreen(location);
        onViewX = location[0];
        onViewY = location[1];
        myView.setOnViewInfo(onViewX, onViewY, viewBuilder.onViews[viewCount].getWidth(), viewBuilder.onViews[viewCount].getHeight());
    }

    private void setOnViewInfos(OnViewData[] views) {
        if (viewBuilder == null)
            return;
        if (views == null || views.length < 1) {
            return;
        }
        myView.setOnViewInfos(views);
        for (int i = 0; i < views.length; i++) {
            int[] location = new int[2];
            views[i].getView().getLocationOnScreen(location);
            onViewX = location[0];
            onViewY = location[1];
            onViewHeight = views[i].getView().getHeight();
            onViewWidth = views[i].getView().getWidth();
            pLeft = views[i].getPaddingL();
            pTop = views[i].getPaddingT();
            pRight = views[i].getPaddingR();
            pBottom = views[i].getPaddingB();
//            myView.setOnViewInfo(onViewX-views[i].getPaddingL(),
//                    onViewY-views[i].getPaddingT(),
//                    views[i].getView().getWidth()+views[i].getPaddingR(),
//                    views[i].getView().getHeight()+views[i].getPaddingB());

            addLayouts(views[i]);
        }
    }

    private void addSkipButton(String text) {
        if (viewBuilder == null)
            return;
        if (TextUtils.isEmpty(text))
            return;
        skipButton = new TextView(viewBuilder.context);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.rightMargin = 20;
        params.bottomMargin = 20;
        LayoutParams layoutParams = new LayoutParams(params);
        layoutParams.gravity = viewBuilder.skipButtonGravity;
        skipButton.setText(text);
        skipButton.setTextSize(26);
        skipButton.setGravity(Gravity.CENTER);
        skipButton.setTextColor(Color.parseColor("#363636"));
        skipButton.setBackground(ResourcesCompat.getDrawable(viewBuilder.context.getResources(), R.drawable.common_bg_rectangle, viewBuilder.context.getTheme()));
        skipButton.setPadding(10, 8, 10, 8);
        /*if (params.gravity == (Gravity.RIGHT|Gravity.BOTTOM)) {
            skipButton.setPadding(0, 0, SizeUtils.dp2px(20), 20);
        } else {
            skipButton.setPadding(SizeUtils.dp2px(20), 20, 0, 0);
        }*/
        //skipButton.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewBuilder.callback != null) {
                    viewBuilder.callback.skip();
                }
                if (viewBuilder != null)
                    viewBuilder.hide();
            }
        });
        vg.addView(skipButton, layoutParams);
    }

    private void clearAllViews() {
        vg.removeView(myView);
        removeExplainViews();
        if (skipButton != null)
            vg.removeView(skipButton);
        if (viewBuilder.bgView != null) {
            viewBuilder.bgView.setVisibility(View.GONE);
            vg.removeView(viewBuilder.bgView);
        }
        viewBuilder = null;
    }

    private void addOtherLayout() {
        if (viewBuilder == null)
            return;
        if (viewBuilder.otherViews != null && viewBuilder.otherViews.length > 0) {
            View view = viewBuilder.otherViews[viewCount].getView() != null ?
                    viewBuilder.otherViews[viewCount].getView() : LayoutInflater.from(viewBuilder.context).inflate(viewBuilder.otherViews[viewCount].getId(), null);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.width = screenW;
            params.height = viewBuilder.otherViews[viewCount].getHeight();
            params.gravity = viewBuilder.otherViews[viewCount].getViewGravity();
            if (params.gravity == Gravity.TOP) {
                params.setMargins(0,
                        viewBuilder.otherViews[viewCount].getT(),
                        0,
                        0);
            } else if (params.gravity == Gravity.BOTTOM) {
                params.setMargins(0,
                        0,
                        0,
                        viewBuilder.otherViews[viewCount].getT());
            } else {
                params.setMargins(0,
                        viewBuilder.otherViews[viewCount].getT(),
                        0,
                        0);
            }

            vg.addView(view, params);
            viewList.add(view);
        }
    }

    private void addLayouts(OnViewData viewData) {
        if (viewBuilder == null)
            return;
        if (viewData.getExplainView() != null) {
            if (viewData.getExplainView().getIdData() != null) {
                View view = LayoutInflater.from(viewBuilder.context).inflate(viewData.getExplainView().getIdData().getLayoutId(), null);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.height = viewData.getExplainView().getHeight();
                params.width = viewData.getExplainView().getWidth();
                TextView tvContent = view.findViewById(R.id.tv_explain);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(viewData.getExplainView().getContentViewData().getMarginL(),
                        viewData.getExplainView().getContentViewData().getMarginT(),
                        viewData.getExplainView().getContentViewData().getMarginR(),
                        viewData.getExplainView().getContentViewData().getMarginB());
                layoutParams.gravity = Gravity.CENTER;
                tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
                tvContent.setText(viewData.getExplainView().getContentViewData().getText());

                View lineView = view.findViewById(R.id.arrow_line);
                LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                switch (viewData.getExplainView().getIdData()) {
                    case LEFT:
                        lineParams.width = viewData.getExplainView().getContentViewData().getArrowLength();
                        lineParams.height = lineStroke;
                        lineView.setLayoutParams(lineParams);
                        layoutParams.width = params.width - arrowSize - lineParams.width - layoutParams.leftMargin - layoutParams.rightMargin;
                        layoutParams.height = params.height / 2;
                        tvContent.setLayoutParams(layoutParams);
                        if (viewData.getExplainView().getContentViewData().isAlwaysCenter()) {
                            params.setMargins(onViewX - pLeft - params.width,
                                    onViewY - pTop + (onViewHeight + pTop + pBottom) / 2 - params.height / 2,
                                    0,
                                    0);
                        } else {
                            params.setMargins(onViewX - pLeft - params.width,
                                    onViewY + (onViewHeight) / 2 - params.height / 2,
                                    0,
                                    0);
                        }
                        break;
                    case TOP:
                        lineParams.height = viewData.getExplainView().getContentViewData().getArrowLength();
                        lineParams.width = lineStroke;
                        lineView.setLayoutParams(lineParams);
                        layoutParams.width = params.width / 2;
                        layoutParams.height = params.height - arrowSize - lineParams.height - layoutParams.bottomMargin - layoutParams.topMargin;
                        tvContent.setLayoutParams(layoutParams);
                        if (viewData.getExplainView().getContentViewData().isAlwaysCenter()) {
                            params.setMargins(onViewX - pLeft + (onViewWidth + pLeft + pRight) / 2 - params.width / 2,
                                    onViewY - pTop - params.height,
                                    0,
                                    0);
                        } else {
                            params.setMargins(onViewX + (onViewWidth) / 2 - params.width / 2,
                                    onViewY - pTop - params.height,
                                    0,
                                    0);
                        }
                        break;
                    case RIGHT:
                        lineParams.width = viewData.getExplainView().getContentViewData().getArrowLength();
                        lineParams.height = lineStroke;
                        lineView.setLayoutParams(lineParams);
                        layoutParams.width = params.width - arrowSize - lineParams.width - layoutParams.leftMargin - layoutParams.rightMargin;
                        layoutParams.height = params.height / 2;
                        tvContent.setLayoutParams(layoutParams);
                        if (viewData.getExplainView().getContentViewData().isAlwaysCenter()) {
                            params.setMargins(onViewX + onViewWidth + pRight,
                                    onViewY - pTop + (onViewHeight + pTop + pBottom) / 2 - params.height / 2,
                                    0,
                                    0);
                        } else {
                            params.setMargins(onViewX + onViewWidth + pRight,
                                    onViewY + (onViewHeight) / 2 - params.height / 2,
                                    0,
                                    0);
                        }
                        break;
                    case BOTTOM:
                        lineParams.height = viewData.getExplainView().getContentViewData().getArrowLength();
                        lineParams.width = lineStroke;
                        lineView.setLayoutParams(lineParams);
                        layoutParams.width = params.width / 2;
                        layoutParams.height = params.height - arrowSize - lineParams.height - layoutParams.bottomMargin - layoutParams.topMargin;
                        tvContent.setLayoutParams(layoutParams);
                        if (viewData.getExplainView().getContentViewData().isAlwaysCenter()) {
                            params.setMargins(onViewX - pLeft + (onViewWidth + pLeft + pRight) / 2 - params.width / 2,
                                    onViewY + onViewHeight + pBottom,
                                    0,
                                    0);
                        } else {
                            params.setMargins(onViewX + (onViewWidth) / 2 - params.width / 2,
                                    onViewY + onViewHeight + pBottom,
                                    0,
                                    0);
                        }
                        break;
                    default:
                }

                vg.addView(view, params);
                viewList.add(view);
            } else if (viewData.getExplainView().getView() != null) {
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(onViewX + viewData.getExplainView().getView().getWidth(),
                        onViewY + viewData.getExplainView().getView().getHeight(),
                        onViewX,
                        onViewY);
                vg.addView(viewData.getExplainView().getView(), params);
                viewList.add(viewData.getExplainView().getView());
            }
        }
    }

    private Handler handler = new Handler();

    public void showGuideView() {
        handler.postDelayed(runnable, 200);
    }

    public void hideGuideView() {
        clearAllViews();
    }

    public void showAgain() {
        if (viewBuilder == null)
            return;
        viewCount = 0;
        if (viewBuilder.callback != null) {
            viewBuilder.callback.dismiss(viewCount, viewCount);
        }
        removeExplainViews();
        setOnViewInfos(viewBuilder.onViewDatas[viewCount]);
        addOtherLayout();
        addSkipButton(viewBuilder.skipButtonText);
    }

    public void refreshView(int position) {
        if (viewBuilder == null)
            return;
        viewCount = position;
        removeExplainViews();
        setOnViewInfos(viewBuilder.onViewDatas[viewCount]);
        addOtherLayout();
//        addSkipButton(viewBuilder.skipButtonText);
    }
}
