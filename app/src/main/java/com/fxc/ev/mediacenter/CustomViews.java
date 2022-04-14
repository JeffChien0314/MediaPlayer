package com.fxc.ev.mediacenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import static android.security.KeyStore.getApplicationContext;

/**
 * Created by Jennifer on 2022/4/6.
 */
public class CustomViews extends RelativeLayout {
    private ViewDragHelper mDragHelper;
    //private View mView;
    private LinearLayout mView;
    //private LinearLayout mView;
    private View player;
    private boolean isDragUp=true;
    private boolean isDraging=false;

    String TAG = CustomViews.class.getSimpleName();

    public CustomViews(Context context) {
        super(context);
        init();
    }

    public CustomViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomViews(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        /**
         * @params ViewGroup forParent 必须是一个ViewGroup
         * @params float sensitivity 灵敏度
         * @params Callback cb 回调
         */
        /*mListLinearLayout =(LinearLayout) findViewById(R.id.list_related);
        Log.i(TAG, "onTouchEvent1: "+mListLinearLayout);*/
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragCallback());

    }


    private class ViewDragCallback extends ViewDragHelper.Callback {
        /**
         * 尝试捕获子view，一定要返回true
         *
         * @param view      尝试捕获的view
         * @param pointerId 指示器id？
         *                  这里可以决定哪个子view可以拖动
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if(child == mView){
                Log.i(TAG, "onDragEvent4: "+mView);
                return true;
            }
            Log.i(TAG, "onDragEvent5: "+child);
            return false;
        }

        /**
         * 处理水平方向上的拖动
         *
         * @param child 被拖动到view
         * @param left  移动到达的x轴的距离
         * @param dx    建议的移动的x距离
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            System.out.println("left = " + left + ", dx = " + dx);
            // 两个if主要是为了让viewViewGroup里
            if (getPaddingLeft() > left) {
                return getPaddingLeft();
            }
            if (getWidth() - child.getWidth() < left) {
                return getWidth() - child.getWidth();
            }
            return left;
        }

        /**
         * 处理竖直方向上的拖动
         *
         * @param child 被拖动到view
         * @param top   移动到达的y轴的距离
         * @param dy    建议的移动的y距离
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Log.i(TAG, "onTouchEvent11: "+player.getHeight());
            isDraging=true;
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) player.getLayoutParams();
            if(player.getHeight() >=486) {
                layoutParams2.height = player.getHeight() + dy;
                player.setLayoutParams(layoutParams2);
            }
            if (getPaddingTop() > top) {
                return getPaddingTop();
            }
            if (getHeight() - child.getHeight() < top) {
                Log.i(TAG, "onTouchEvent12: "+(getHeight() - child.getHeight()));
                return getHeight() - child.getHeight();
            }
            return top;
        }

        /**
         * 当拖拽到状态改变时回调
         *
         * @params 新的状态
         */
        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:  // 正在被拖动
                    break;
                case ViewDragHelper.STATE_IDLE:  // view没有被拖拽或者 正在进行fling/snap
                    RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) player.getLayoutParams();// view没有被拖拽或者 正在进行fling/snap
                    if(isDraging) {
                        if (isDragUp) {
                            layoutParams2.height = 1440;
                            isDragUp = false;
                        } else {
                            layoutParams2.height = 486;
                            isDragUp = true;
                        }
                        isDraging=false;
                        player.setLayoutParams(layoutParams2);
                    }
                    break;
                case ViewDragHelper.STATE_SETTLING: // fling完毕后被放置到一个位置
                    break;
            }
            super.onViewDragStateChanged(state);
        }
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            invalidate();
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_DOWN:
                mDragHelper.cancel(); // 相当于调用 processTouchEvent收到ACTION_CANCEL
                break;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mView = (LinearLayout) findViewById(R.id.input_source_related);
        player=(View)findViewById(R.id.player);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }


}
