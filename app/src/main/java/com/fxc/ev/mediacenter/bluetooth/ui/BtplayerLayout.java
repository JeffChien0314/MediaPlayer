package com.fxc.ev.mediacenter.bluetooth.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.bluetooth.client.MediaBrowserConnecter;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.util.Constants;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.util.MediaItemUtil;

import static android.view.MotionEvent.ACTION_DOWN;
import static com.fxc.ev.mediacenter.util.Constants.BLUETOOTH_DEVICE;
import static com.fxc.ev.mediacenter.util.Constants.STATE_FORWARD;
import static com.fxc.ev.mediacenter.util.Constants.STATE_REWIND;

public class BtplayerLayout extends RelativeLayout implements View.OnClickListener {
    private final String TAG = BtplayerLayout.class.getSimpleName();
    private final int FASTFORWARD = 1;
    private final int DO_NOTHING = 0;
    private final int REWIND = -1;
    private final int HIDE_VIEW_FASTFORWARD = 0;
    private final int HIDE_VIEW_REWIND = 1;
    private final int HIDE_VIEW_CONTROL = 2;
    private final int SHOW_VIEW_CONTROL = 4;
    private final int HIDE_VIEW = 3;
    private final int HIDE_TIMER = 5 * 1000;
    private final int HIDE_TIMER2 = 2 * 1000;
    private final int HIDE_TIMER3 = 500;

    private GestureDetector mGesture;
    private View mBtPlayerLayer;
    private MediaSeekBar mSeekbar;
    private ImageView mStart;
    private ImageView mNext;
    private ImageView mPrevious;
    private ImageView mFastforward;
    private ImageView mRewind;
    private LinearLayout mTopLayout;
    private int BT_LAYER_DOUBLE_TAP = DO_NOTHING;
    private OnTouchListener touchListener;
    private Context mContext;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
               /* case HIDE_VIEW_FASTFORWARD:
                    mFastforward.setVisibility(INVISIBLE);
                    break;
                case HIDE_VIEW_REWIND:
                    mRewind.setVisibility(INVISIBLE);
                    break;*/
                case HIDE_VIEW:
                    mStart.setVisibility(INVISIBLE);
                    mPrevious.setVisibility(INVISIBLE);
                    mNext.setVisibility(INVISIBLE);
                    mTopLayout.setVisibility(INVISIBLE);
                //    mFastforward.setVisibility(INVISIBLE);
              //      mRewind.setVisibility(INVISIBLE);
                    break;
                case SHOW_VIEW_CONTROL:
                    showControlBtn();
                    break;
                case HIDE_VIEW_CONTROL:
                    mStart.setVisibility(INVISIBLE);
                    mNext.setVisibility(INVISIBLE);
                    mPrevious.setVisibility(INVISIBLE);
                    break;
            }
        }
    };

    public BtplayerLayout(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    public BtplayerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtplayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        mBtPlayerLayer = inflate(context, R.layout.layout_media_play_bt, this);
        mTopLayout = findViewById(R.id.layout_top);
        mStart = findViewById(R.id.bt_start);
        mNext = findViewById(R.id.bt_next);
        mPrevious = findViewById(R.id.bt_previous);
       // mFastforward = findViewById(R.id.bt_fwd);
       // mRewind = findViewById(R.id.bt_rewind);
        mSeekbar = (MediaSeekBar) findViewById(R.id.progress);
        mSeekbar.setEnabled(false);

        MediaBrowserConnecter.getInstance(context).setSeekBar(mSeekbar);
     //   initGestureDetector(context);
        initListener(context);
        if (mContext instanceof Activity) {
            ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void initListener(Context context) {
        mNext.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mStart.setOnClickListener(this);

        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                BT_LAYER_DOUBLE_TAP = DO_NOTHING;
                Log.i(TAG, "onTouch: ");
                if (event.getAction() == ACTION_DOWN) {
                    checkBtnVisble();
                }
                /*if (MediaController.getInstance(context).currentSourceType == BLUETOOTH_DEVICE) {
                    float sx = mBtPlayerLayer.getX();
                    float cx = event.getX();
                    int ex = mBtPlayerLayer.getWidth();

                    if (((cx - sx) / (ex - sx)) < 0.4) {
                        BT_LAYER_DOUBLE_TAP = REWIND;
                        mGesture.onTouchEvent(event);
                    } else if (((cx - sx) / (ex - sx)) > 0.6) {
                        BT_LAYER_DOUBLE_TAP = FASTFORWARD;
                        mGesture.onTouchEvent(event);
                    }
                }*/

                return true;
            }
        };
        mBtPlayerLayer.setOnTouchListener(touchListener);
    }

    private void initGestureDetector(Context context) {
        mGesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.i(TAG, "onDoubleTapEvent:DOUBLE_TAP= " + BT_LAYER_DOUBLE_TAP);
                //  handler.sendEmptyMessage(HIDE_VIEW_CONTROL);
                switch (BT_LAYER_DOUBLE_TAP) {
                    case REWIND:
                        handler.removeMessages(HIDE_VIEW);
                        handler.removeMessages(HIDE_VIEW_CONTROL);
                        handler.removeMessages(SHOW_VIEW_CONTROL);
                        mFastforward.setVisibility(INVISIBLE);
                        mRewind.setVisibility(VISIBLE);
                        MediaController.getInstance(context).setPlayerState(STATE_REWIND, -1);
                        handler.sendEmptyMessageDelayed(HIDE_VIEW, HIDE_TIMER2);
                        break;
                    case FASTFORWARD:
                        handler.removeMessages(HIDE_VIEW);
                        handler.removeMessages(HIDE_VIEW_CONTROL);
                        handler.removeMessages(SHOW_VIEW_CONTROL);
                        mRewind.setVisibility(INVISIBLE);
                        mFastforward.setVisibility(VISIBLE);
                        MediaController.getInstance(context).setPlayerState(STATE_FORWARD, -1);
                        handler.sendEmptyMessageDelayed(HIDE_VIEW, HIDE_TIMER2);
                        break;
                }
                return super.onDoubleTapEvent(e);
            }
        });

    }

    /**
     * 收到播放状态改变广播后更新UI
     */
    public void updateStateButtonImg(int state) {
        switch (state) {
            case Constants.STATE_PLAY:
                mStart.setImageResource(R.drawable.icon_pause_normal);
                break;
            case Constants.STATE_PAUSE:
                mStart.setImageResource(R.drawable.icon_play_normal);
                break;
        }
    }

    /**
     * 收到播放状态改变广播后更新UI,有内容播放时需要500ms消失
     */
    public void showControlBtn() {
        Log.i(TAG, "showControlBtn: ");
        mStart.setVisibility(VISIBLE);
        mNext.setVisibility(VISIBLE);
        mPrevious.setVisibility(VISIBLE);
        mTopLayout.setVisibility(VISIBLE);
        handler.removeMessages(HIDE_VIEW);
        handler.sendEmptyMessageDelayed(HIDE_VIEW, HIDE_TIMER);
    }

    private void checkBtnVisble() {
        Log.i(TAG, "checkBtnVisble:1 ");
        handler.sendEmptyMessageDelayed(VISIBLE == mStart.getVisibility() ? HIDE_VIEW_CONTROL : SHOW_VIEW_CONTROL, HIDE_TIMER3);
    }

    /**
     * 收到播放状态改变广播后更新UI
     */
    public void updateMediaDetail(MediaItem item) {
        if (item == null) {
            ((TextView) mBtPlayerLayer.findViewById(R.id.title)).setText("");
            ((TextView) mBtPlayerLayer.findViewById(R.id.total)).setText("");
            mBtPlayerLayer.findViewById(R.id.surface_container).setBackground(null);
        } else {
            ((TextView) mBtPlayerLayer.findViewById(R.id.title)).setText(item.getTitle());
            ((TextView) mBtPlayerLayer.findViewById(R.id.total)).setText(MediaItemUtil.formatTime(item.getDuration()));
           /* if (null == item.getThumbBitmap()) return;
            Bitmap bm = item.getThumbBitmap();
            Drawable drawable = new BitmapDrawable(mContext.getResources(), bm);
            mBtPlayerLayer.findViewById(R.id.surface_container).setBackground(drawable);*/
        }
    }

    public void release() {
        ((MediaSeekBar) findViewById(R.id.progress)).disconnectController(mContext);
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick: v=" + v);
        int state = Constants.STATE_PLAY;
        switch (v.getId()) {
            case R.id.bt_next:
                state = Constants.STATE_NEXT;
                break;
            case R.id.bt_previous:
                state = Constants.STATE_PREVIOUS;
                break;
            case R.id.bt_start:
                break;
           /* case R.id.bt_rewind:
                state=STATE_REWIND;
                break;
                case R.id.S*/
        }
        MediaController.getInstance(mContext).setPlayerState(state, -1);
    }

}
