package com.example.stronghope.dragdeletelistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.ListView;


/**
 * StrongHope
 * Created by gongyasen on 2015/4/27.
 */
public class MyDragDeleteListView extends ListView{

    private float startX;
    private float startY;
    private int currentPosition;
    private View currentChild;
    private float initX;
    private float currentOffset;
    private OnRemoveListener removeListener;
    private Rect childRect = new Rect();
    private Paint textPaint = new Paint();
    private String leftText = "通过";
    private String rightText = "驳回";
    private int rightColor = 0xffff0000;
    private int leftColor = 0xff00ff00;
    private int unRemovedColor = Color.GRAY;
    private boolean isDraging = false;//是否正在拖拽item
    private float dragAvailableDistanceRatio = 3;//拖拽生效比例,width/ratio
    private MyItemRetranslateAnim myItemRetranslateAnim = new MyItemRetranslateAnim();
    private MyItemRemoveAnim myItemRemoveAnim = new MyItemRemoveAnim();
    private MyItemZoomYAnimation myLastItemZoomYAnimation;
    private int pinnedItemType = -1;

    /**
     * 设置不可拖拽的item类型(这种类型的Item不会响应拖拽)
     * @param pinnedItemType
     */
    public void setPinnedItemType(int pinnedItemType) {
        this.pinnedItemType = pinnedItemType;
    }

    public enum Direction{LEFT,RIGHT};

    public MyDragDeleteListView(Context context) {
        super(context);
        init();
    }

    public MyDragDeleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyDragDeleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(UIUtils.dip2px(getContext(),18));
        textPaint.setAntiAlias(true);
        myItemRetranslateAnim.setFillAfter(true);
        myItemRetranslateAnim.setDuration(300);
        myItemRetranslateAnim.setInterpolator(new OvershootInterpolator());
        myItemRemoveAnim.setFillAfter(true);
        myItemRemoveAnim.setDuration(300);
        myItemRemoveAnim.setInterpolator(new OvershootInterpolator());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int childIndex = currentPosition - getFirstVisiblePosition();
        if(getChildAt(childIndex) != null && getAdapter().getItemViewType(currentPosition) != pinnedItemType){
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                myItemRetranslateAnim.cancel();
                myItemRemoveAnim.cancel();
                if(myLastItemZoomYAnimation != null) myLastItemZoomYAnimation.cancel();
                initX = ev.getRawX();
                startX = ev.getRawX();
                startY = ev.getRawY();
                currentPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
//                if(BuildConfig.DEBUG)System.out.println("当前点击到的item的位置:"+currentPosition);
                break;
            case MotionEvent.ACTION_MOVE:
                float newX = ev.getRawX();
                float newY = ev.getRawY();
                float distanceX = newX - startX;
                float distanceY = newY - startY;
                if(Math.abs(distanceX) > Math.abs(distanceY)*5 && getAdapter().getItemViewType(currentPosition) != pinnedItemType){
                    isDraging = true;
                }
                if(isDraging){
                    int childIndex = currentPosition - getFirstVisiblePosition();
                    if(childIndex >= 0){
//                        System.out.println("当前孩子的索引:"+childIndex + ",当前position:"+currentPosition + ",当前第一条可见条目的位置:"+getFirstVisiblePosition());
                        currentChild = getChildAt(childIndex);
                        if(currentChild == null)return super.onTouchEvent(ev);
                        currentOffset = newX - initX;
                        currentChild.setPressed(false);
                        invalidate();
//                        currentChild.layout((int) (currentChild.getLeft() + distanceX), currentChild.getTop(), (int) (currentChild.getRight() + distanceX), currentChild.getBottom());
                    }
                    startX = newX;
                    startY = newY;

                    return true;
                }
                startX = newX;
                startY = newY;
                break;
            case MotionEvent.ACTION_UP:
                if(currentOffset != 0 && Math.abs(currentOffset) < getMeasuredWidth()/dragAvailableDistanceRatio){
                    startAnimation(myItemRetranslateAnim);
                }else if(Math.abs(currentOffset) > getMeasuredWidth()/dragAvailableDistanceRatio){
                    myItemRemoveAnim.setAnimationListener(new MyItemRemovedListener(currentOffset,currentPosition));
                    startAnimation(myItemRemoveAnim);
                }
                if(isDraging){//使拖拽时不响应点击
                    if(currentChild != null)currentChild.setPressed(false);
                    isDraging = false;
                    return true;
                }
                isDraging = false;

                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 设置Item被移除监听器
     * @param removeListener
     */
    public void setOnRemoveListener(OnRemoveListener removeListener){
        this.removeListener = removeListener;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if(currentChild != null && currentChild == child){
            drawChildBackground(canvas, child);
            canvas.save();
            canvas.translate(currentOffset, 0);
            boolean drawChildReturn = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return drawChildReturn;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * 设置左右两侧显示的文字
     * @param leftText
     * @param rightText
     */
    public void setLeftRightText(String leftText,String rightText){
        this.leftText = leftText;
        this.rightText = rightText;
    }

    /**
     * 设置左右两侧显示的背景色
     * @param leftColor
     * @param rightColor
     */
    public void setLeftRightColor(int leftColor,int rightColor){
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    /**
     * 设置还没有达到移除条目的距离时的颜色
     * @param unRemovedColor
     */
    public void setUnRemovedColor(int unRemovedColor){
        this.unRemovedColor = unRemovedColor;
    }

    /**
     * 绘制item背景
     * @param canvas
     * @param child
     */
    private void drawChildBackground(Canvas canvas, View child) {
        canvas.save();
        childRect.left = child.getLeft();
        childRect.right = child.getRight();
        childRect.top = child.getTop();
        childRect.bottom = child.getBottom();
        canvas.clipRect(childRect);
        float availableDistance = getMeasuredWidth() / dragAvailableDistanceRatio;
        if(currentOffset > 0){
            //计算文字大小
            textPaint.getTextBounds(leftText, 0,leftText.length(), childRect);
            int x;
            int y = child.getBottom() - (((child.getBottom() - child.getTop()) - (childRect.bottom - childRect.top))/2) - UIUtils.dip2px(getContext(),2);
            if(currentOffset > availableDistance){
                x = child.getLeft() + (int)availableDistance/2 + (int)(currentOffset - availableDistance);//文字x坐标
                canvas.drawColor(leftColor);
            }else{
                x = child.getLeft() + (int)availableDistance/2;//文字x坐标
                canvas.drawColor(unRemovedColor);
            }
            canvas.drawText(leftText, 0, leftText.length(), x, y, textPaint);
        }else{
            //计算文字大小
            textPaint.getTextBounds(rightText,0,rightText.length(),childRect);
            int x;
            int y = child.getBottom() - (((child.getBottom() - child.getTop()) - (childRect.bottom - childRect.top))/2) - UIUtils.dip2px(getContext(),2);
//            System.out.println("x:"+x+",y:"+y);
            if(currentOffset < -availableDistance){
//                x = child.getRight() - (childRect.right - childRect.left) - UIUtils.dip2px(getContext(), 15);//文字x坐标
                x = child.getRight() - (int)availableDistance/2 - (childRect.right - childRect.left) + (int)(currentOffset + availableDistance);//文字x坐标
                canvas.drawColor(rightColor);
            }else{
                x = child.getRight() - (int)availableDistance/2 - (childRect.right - childRect.left);//文字x坐标
                canvas.drawColor(unRemovedColor);
            }
            canvas.drawText(rightText, 0, rightText.length(), x, y, textPaint);
        }
        canvas.restore();
    }

    /**
     * item的被移除监听器
     */
    public interface OnRemoveListener{
        /**
         * item被移除
         * @param position 当前移除的位置
         * @param direction 移除的方向
         */
        void onRemoved(int position, Direction direction);
    }

    /**
     * 播放指定条目被移除的动画
     * @param position
     */
    public void removeItemAnim(int position,Animation.AnimationListener listener){
        View v = getChildAt(position - getFirstVisiblePosition());
        if(v == null)return;
        if(myLastItemZoomYAnimation != null)this.myLastItemZoomYAnimation.cancel();
        MyItemZoomYAnimation myItemZoomYAnimation = new MyItemZoomYAnimation();
        this.myLastItemZoomYAnimation = myItemZoomYAnimation;
        myItemZoomYAnimation.setView(v);
        myItemZoomYAnimation.setDuration(300);
        myItemZoomYAnimation.setAnimationListener(listener);
        v.startAnimation(myItemZoomYAnimation);
    }

    /**
     * 条目恢复动画
     */
    private class MyItemRetranslateAnim extends Animation{
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            currentOffset = currentOffset * (1 - interpolatedTime);
            if(currentOffset == 0)currentChild = null;
            invalidate();
        }

        @Override
        public void cancel() {
            super.cancel();
            currentChild = null;
            currentOffset = 0;
            currentPosition = 0;
            invalidate();
        }
    }

    /**
     * 条目移除动画
     */
    private class MyItemRemoveAnim extends Animation{
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if(currentOffset < 0){
                currentOffset = currentOffset - (getMeasuredWidth() - Math.abs(currentOffset)) * interpolatedTime;
            }else{
                currentOffset = currentOffset + (getMeasuredWidth() - Math.abs(currentOffset)) * interpolatedTime;
            }
            if(currentOffset == 0)currentChild = null;
            invalidate();
        }

        @Override
        public void cancel() {
            super.cancel();
            currentChild = null;
            currentOffset = 0;
            currentPosition = 0;
            invalidate();
        }
    }

    /**
     * item移除完毕监听器
     */
    private class MyItemRemovedListener implements Animation.AnimationListener{

        private final float initCurrentOffset;
        private final int position;

        public MyItemRemovedListener(float initCurrentOffset,int position){
            this.initCurrentOffset = initCurrentOffset;
            this.position = position;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
        @Override
        public void onAnimationEnd(Animation animation) {
            if(removeListener != null){
                if(initCurrentOffset > getMeasuredWidth()/dragAvailableDistanceRatio){
                    removeListener.onRemoved(this.position,Direction.RIGHT);
                }else if(initCurrentOffset < -getMeasuredWidth()/dragAvailableDistanceRatio){
                    removeListener.onRemoved(this.position,Direction.LEFT);
                }
            }
        }
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }


    /**
     * item Y轴缩放动画
     */
    private class MyItemZoomYAnimation extends Animation{
        private View v;
        private int initHeight;

        public void setView(View v){
            this.v = v;
            initHeight = v.getHeight();
        }

        @Override
        protected void applyTransformation(float interpolatedTime,Transformation t) {
            int height = (int) ((1-interpolatedTime)*initHeight);
            if(height == 0){
                cancel();
                return;
            }
            v.getLayoutParams().height = height;
            v.requestLayout();
        }

        @Override
        public void cancel() {
            super.cancel();
            currentChild = null;
            currentOffset = 0;
            currentPosition = 0;
            if(v == null)return;
            v.getLayoutParams().height = initHeight;
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    };

}
