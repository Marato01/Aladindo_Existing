package com.reeman.delige.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.reeman.delige.utils.SizeUtils;


public class DrawView extends View {
    private String TAG = "  ";
    private int width, height;
    private int shadowSize = 0;
    private int shapeType = 1;
    private int onViewX, onViewY, onViewWidth, onViewHeight;
    private OnViewData[] viewDatas;

    public DrawView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getSize(100, widthMeasureSpec);
        height = getSize(100, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height); //保存自身尺寸，要不然scrollView一类的父View没办法判断给宽高
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation")
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        setBackgroundColor(Color.parseColor("#66000000"));
        paint.setColor(0xa0000000);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        if (viewDatas != null) {
            for (OnViewData viewData : viewDatas) {
                int[] location = new int[2];
                viewData.getView().getLocationOnScreen(location);
                onViewX = location[0] - viewData.getPaddingL();
                onViewY = location[1] - viewData.getPaddingT();
                onViewWidth = viewData.getView().getWidth() + viewData.getPaddingR() + viewData.getPaddingL();
                onViewHeight = viewData.getView().getHeight() + viewData.getPaddingB() + viewData.getPaddingT();

                if (shapeType == GuideView.RECTANGLE) {
                    if (shadowSize > 0) {
                        paint.setShadowLayer(shadowSize, onViewX + onViewWidth, 0, 0x00000000);
//                    canvas.drawRect(-onViewWidth,onViewY,0,onViewY+onViewHeight,paint);
                        RectF rectF = new RectF(-onViewWidth, onViewY, 0, onViewY + onViewHeight);
                        canvas.drawRoundRect(rectF, SizeUtils.dp2px(10), SizeUtils.dp2px(10), paint);

                    } else {
//                    canvas.drawRect(onViewX,onViewY,onViewX+onViewWidth,onViewY+onViewHeight,paint);
                        RectF rectF = new RectF(onViewX, onViewY, onViewX + onViewWidth, onViewY + onViewHeight);
                        canvas.drawRoundRect(rectF, SizeUtils.dp2px(10), SizeUtils.dp2px(10), paint);
                    }
                } else if (shapeType == GuideView.CIRCLE) {
                    if (shadowSize > 0) {
                        int circleW = Math.max(onViewWidth, onViewHeight);
                        float radius = circleW / 2f;
                        paint.setShadowLayer(shadowSize, onViewX + circleW, 0, 0x00000000);
                        canvas.drawCircle(-radius, onViewY + onViewHeight / 2, radius, paint);
                    } else {
                        canvas.drawCircle(onViewX + onViewWidth / 2, onViewY + onViewHeight / 2, onViewWidth > onViewHeight ? onViewWidth / 2 : onViewHeight / 2, paint);
                    }
                } else if (shapeType == GuideView.OVAL) {
                    if (shadowSize > 0) {
                        RectF rectF = new RectF(-onViewWidth, onViewY, 0, onViewY + onViewHeight);
                        paint.setShadowLayer(shadowSize, onViewX + onViewWidth, 0, 0x00000000);
                        canvas.drawOval(rectF, paint);
                    } else {
                        RectF rectF = new RectF(onViewX, onViewY, onViewX + onViewWidth, onViewY + onViewHeight);
                        canvas.drawOval(rectF, paint);
                    }
                }
            }
        }
        paint.setXfermode(null);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private int getSize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST: //最大取值
                mySize = size;
                break;
            case MeasureSpec.EXACTLY: //指定大小
                mySize = size;
                break;
            case MeasureSpec.UNSPECIFIED: //没有指定大小 listView 的 itemView 或者 ScrollView 的子View 这类超出屏幕范围带滚动的视图会出现
                mySize = size; //默认大小
                break;
            default:
                break;
        }
        return mySize;
    }

    public void setOnViewInfo(int onViewX, int onViewY, int onViewWidth, int onViewHeight) {
        this.onViewWidth = onViewWidth;
        this.onViewHeight = onViewHeight;
        this.onViewX = onViewX;
        this.onViewY = onViewY;
        invalidate();
    }

    public void setOnViewInfos(OnViewData[] viewData) {
        this.viewDatas = viewData;
        invalidate();
    }

    public void setShadowSize(int size) {
        this.shadowSize = size;
    }

    public void setShapeType(int type) {
        this.shapeType = type;
    }
}
