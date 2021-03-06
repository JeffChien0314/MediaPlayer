package com.fxc.ev.mediacenter.localplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.util.MediaItemUtil;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import moe.codeest.enviews.ENDownloadView;
import moe.codeest.enviews.ENPlayView;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static com.fxc.ev.mediacenter.util.Constants.*;
import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * Created by Jennifer on 2022/1/17.
 */

public class CSDMediaPlayer extends ListGSYVideoPlayer implements View.OnClickListener, View.OnTouchListener {
    private final String TAG = CSDMediaPlayer.class.getSimpleName();
    public static final String STATE_EXTRA = "state";
    public static final String POS_EXTRA = "pos";
    private final int FASTFORWARD = 1;//jennifer add for ??????????????????
    private final int DO_NOTHING = 0;//jennifer add for ??????????????????
    private final int REWIND = -1;//jennifer add for ??????????????????
    private int USB_LAYER_DOUBLE_TAP = DO_NOTHING;
    private MediaInfo mediaInfo;
    private static CSDMediaPlayer mInstance;
    private TextView mNot_Playing;//Sandra@20220419 add according to UI SPEC
    private ImageView mAlbum_photo;//Sandra@20220419 add according to UI SPEC
    private ImageView mStart,mPrevious, mNext, mRewind, mFwd;
    private TextView mRewindTotalTime, mFwdTotalTime;//jennifer add for ??????????????????
    private int playMode = 0;
    private int mDoubleFwdClickCount = 0;//jennifer add for ??????????????????
    private int mDoubleRewindClickCount = 0;//jennifer add for ??????????????????
    private boolean randomOpen = false;
    private OnTouchListener mTouchListener;
    private GestureDetector mGesture;//jennifer add for ??????????????????
    private LinkedList<Integer> randomIndexList = new LinkedList<>();
    private boolean isDoubleTouch = false;//jennifer add for ??????????????????
    public CSDMediaPlayer(Context context) {
        super(context);
    }

    public CSDMediaPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    protected Context getActivityContext() {
        if (null == CommonUtil.getActivityContext(getContext()))
            return mContext;
        else
            return CommonUtil.getActivityContext(getContext());
    }


    public CSDMediaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static CSDMediaPlayer getInstance(Context context) {
        Log.i("CSDMediaPlayer", "CSDMediaPlayer: context=" + context);
        if (mInstance == null) {
            synchronized (CSDMediaPlayer.class) {
                mInstance = new CSDMediaPlayer(context, false);
            }
        }
        return mInstance;
    }


    @Override
    public void init(Context context) {
        super.init(context);
        mAlbum_photo=findViewById(R.id.album_photo);
        mNot_Playing =findViewById(R.id.Not_Playing);
        mPrevious = findViewById(R.id.bt_previous);
        mNext = findViewById(R.id.bt_next);
        mRewind = findViewById(R.id.bt_rewind);
        mFwd = findViewById(R.id.bt_fwd);
        mStart = findViewById(R.id.start);
        mRewindTotalTime = findViewById(R.id.rewind_content_display);
        mFwdTotalTime = findViewById(R.id.fwd_content_display);
        mFullscreenButton.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mRewind.setOnClickListener(this);
        mFwd.setOnClickListener(this);
        initGestureDetector(context);
        //jennifer add for ??????????????????-->
        mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch: ");
                if (event.getAction() == ACTION_DOWN) {
                    //checkBtnVisble();
                }
                float sx = findViewById(R.id.surface_container).getX();
                float cx = event.getX();
                int ex = findViewById(R.id.surface_container).getWidth();
                if (((cx - sx) / (ex - sx)) < 0.4) {
                    USB_LAYER_DOUBLE_TAP = REWIND;
                    mGesture.onTouchEvent(event);
                } else if (((cx - sx) / (ex - sx)) > 0.6) {
                    USB_LAYER_DOUBLE_TAP = FASTFORWARD;
                    mGesture.onTouchEvent(event);
                }else{
                    mGesture.onTouchEvent(event);
                }
//                showControlPanel();//Sandra@20220429 modify
                if(event.getAction() == ACTION_UP){
                    //mDoubleClickCount=0;
                    startDismissControlViewTimer();
                }

                return true;
            }
        };
        findViewById(R.id.surface_container).setOnTouchListener(mTouchListener);
        //Sandra@20220419 add according to UI SPEC-->
        if (getMediaInfo()!=null &&
               getMediaInfo().getMediaItems()!=null && getMediaInfo().getMediaItems().size()!=0){
            mPrevious.setEnabled(true);
            mNext.setEnabled(true);
            mStart.setEnabled(true);
            mNot_Playing.setText("");
        }else {
            Log.i(TAG, "init: Not_Playing");
            mNot_Playing.setText(R.string.Not_Playing);
            mNot_Playing.setVisibility(VISIBLE);
            mPrevious.setEnabled(false);
            mNext.setEnabled(false);
            mStart.setEnabled(false);
        }
        //<--Sandra@20220419 add according to UI SPEC
    }
    //jennifer add for ??????????????????<--
    //Sandra@20220429 add-->
   /* protected DismissControlViewTimerTask mDismissControlViewTimerTask;
    protected Timer mDismissControlViewTimer;
    protected void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        mDismissControlViewTimer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        mDismissControlViewTimer.schedule(mDismissControlViewTimerTask, mDismissControlTime);
    }
    //??????????????????????????????
    protected int mDismissControlTime = 4000;
    private class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getActivityContext() != null) {
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "run:hideAllWidget?????? ");
                                    hideAllWidget();
                                    setViewShowState(mLockScreen, GONE);
                                    if (mHideKey && mIfCurrentIsFullscreen && mShowVKey) {
                                        hideNavKey(mContext);
                                    }
                                }
                            }
                    );
                }
            }
        }
    }
    protected void cancelDismissControlViewTimer() {
        if (mDismissControlViewTimer != null) {
            mDismissControlViewTimer.cancel();
            mDismissControlViewTimer = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
            mDismissControlViewTimerTask = null;
        }

    }*/
        public void showControlPanel(){
            setViewShowState(mPrevious, VISIBLE);
            setViewShowState(mNext, VISIBLE);
            setViewShowState(mStartButton, VISIBLE);
            setViewShowState(mNot_Playing, VISIBLE);
            setViewShowState(mTopContainer, VISIBLE);
            setViewShowState(mBottomContainer, VISIBLE);
        }
        //Sandra@20220429 add<--
    /**
     * ????????????URL
     *
     * @param mediaInfos    ????????????mediaInfo
     * @param position      ?????????????????????
     * @param cacheWithPlay ?????????????????????
     * @return
     */
    public boolean setUp(MediaInfo mediaInfos, boolean cacheWithPlay, int position) {
        mediaInfo = mediaInfos;
        ArrayList<GSYVideoModel> models = new ArrayList<>();
        for (int i = 0; i < mediaInfos.getMediaItems().size(); i++) {
            models.add(mediaInfos.getMediaItems().get(i).getGsyVideoModel());
        }

        boolean result = setUp(models, cacheWithPlay, position, null, new HashMap<String, String>());
        return result;
    }

    /**
     * ????????????URL
     *
     * @param url           ??????url
     * @param position      ?????????????????????
     * @param cacheWithPlay ?????????????????????
     * @return
     */
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position) {
        return setUp(url, cacheWithPlay, position, null, new HashMap<String, String>());
    }


    /**
     * ????????????URL
     *
     * @param url           ??????url
     * @param cacheWithPlay ?????????????????????
     * @param position      ?????????????????????
     * @param cachePath     ????????????????????????M3U8??????HLS???????????????false
     * @param mapHeadData   http header
     * @param changeState   ?????????????????????surface
     * @return
     */
    protected boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
      if (url==null || url.size()==0){return false;}
        mUriList = url;
        mPlayPosition = position;
        mMapHeadData = mapHeadData;
        GSYVideoModel gsyVideoModel = url.get(position);
        boolean set = setUp(gsyVideoModel.getUrl(), cacheWithPlay, cachePath, gsyVideoModel.getTitle(), changeState);
        if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
            mTitleTextView.setText(gsyVideoModel.getTitle());
            String Title = gsyVideoModel.getTitle();
            SpannableString spanText = new SpannableString(Title);
            spanText.setSpan(new TextAppearanceSpan(getActivityContext(), R.style.text_artist_style), Title.indexOf("\n"), Title.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mTitleTextView.setText(spanText);
            if (mediaInfo.getMediaItems().get(mPlayPosition) != null)
                if (mediaInfo.getMediaItems().get(mPlayPosition).isIfVideo()) {
                   // findViewById(R.id.surface_container).setBackground(null);
                    findViewById(R.id.album_photo).setBackground(null);
                    ImageView imageView = new ImageView(getActivityContext());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(mediaInfo.getMediaItems().get(mPlayPosition).getThumbBitmap());
                    mThumbImageViewLayout.removeAllViews();
                    setThumbImageView(imageView);
                } else {
                    Bitmap bm = mediaInfo.getMediaItems().get(mPlayPosition).getThumbBitmap();
                    Drawable drawable = new BitmapDrawable(mContext.getResources(), bm);
                    //findViewById(R.id.surface_container).setBackground(drawable);
                    findViewById(R.id.album_photo).setBackground(drawable);
                }
        }
        return set;
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        CSDMediaPlayer sf = (CSDMediaPlayer) from;
        CSDMediaPlayer st = (CSDMediaPlayer) to;
        st.mPlayPosition = sf.mPlayPosition;
        st.mUriList = sf.mUriList;
        st.mediaInfo = sf.mediaInfo;
        if (!mediaInfo.getMediaItems().get(mPlayPosition).isIfVideo()) {
            Bitmap bm = mediaInfo.getMediaItems().get(mPlayPosition).getThumbBitmap();
            Drawable drawable = new BitmapDrawable(mContext.getResources(), bm);
          //  to.findViewById(R.id.surface_container).setBackground(drawable);
            to.findViewById(R.id.album_photo).setBackground(drawable);
        } else {
        //    findViewById(R.id.surface_container).setBackground(null);
            findViewById(R.id.album_photo).setBackground(null);
        }

    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            CSDMediaPlayer listGSYVideoPlayer = (CSDMediaPlayer) gsyBaseVideoPlayer;
            GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
                listGSYVideoPlayer.mTitleTextView.setText(gsyVideoModel.getTitle());
            }
        }
        return gsyBaseVideoPlayer;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        if (gsyVideoPlayer != null) {
            CSDMediaPlayer listGSYVideoPlayer = (CSDMediaPlayer) gsyVideoPlayer;
            GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
                mTitleTextView.setText(gsyVideoModel.getTitle());
            }
        }
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
    }

    @Override
    public void onCompletion() {
        releaseNetWorkState();
        if (mPlayPosition < (mUriList.size())) {
            return;
        }
        super.onCompletion();
    }

    @Override
    public void onAutoCompletion() {
        Log.i("main", "Jennifertest10=: ");
        if (playMode == 0) {
            Log.i("main", "Jennifertest11=: ");
            playNext();
            return;
        } else if (playMode == 1) {
            setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
            Log.i("main", "Jennifertest12=: ");
            startPlayLogic();
            return;
        }
        super.onAutoCompletion();
    }
    //jennifer add for ????????????????????????-->
    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        mDoubleFwdClickCount=0;
        mDoubleRewindClickCount=0;
        isDoubleTouch=false;
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
        setViewShowState(mRewindTotalTime, INVISIBLE);
        setViewShowState(mFwdTotalTime, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);

    }


    @Override
    protected void changeUiToPreparingShow() {
        Debuger.printfLog("changeUiToPreparingShow");
        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);//Sandra@20220429 modify
      /*  setViewShowState(mTopContainer, INVISIBLE);
       setViewShowState(mBottomContainer, INVISIBLE);//Sandra@20220429 modify*/
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, GONE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419
        if (isDoubleTouch == false) {
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
                if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                    ((ENDownloadView) mLoadingProgressBar).start();
                }
            }
        }
    }
    @Override
    protected void changeUiToPlayingShow() {

        Debuger.printfLog("changeUiToPlayingShow");
        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
       /* setViewShowState(mTopContainer, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);*/
        if(isDoubleTouch){
            setViewShowState(mPrevious, INVISIBLE);
            setViewShowState(mNext, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
            setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419
        }else{
            setViewShowState(mPrevious, VISIBLE);
            setViewShowState(mNext, VISIBLE);
            setViewShowState(mStartButton, VISIBLE);
            setViewShowState(mNot_Playing, VISIBLE);//Sandra@20220419
           /* setViewShowState(mPrevious, INVISIBLE);
            setViewShowState(mNext, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
            setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419*/
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ((ENDownloadView) mLoadingProgressBar).reset();
            }
            updateStartImage();
        }
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, (mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
    }

    @Override
    protected void changeUiToPauseShow() {
        Debuger.printfLog("changeUiToPauseShow");

        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        if(isDoubleTouch){
            setViewShowState(mPrevious, INVISIBLE);
            setViewShowState(mNext, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
            setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419
        }else{
            setViewShowState(mPrevious, VISIBLE);
            setViewShowState(mNext, VISIBLE);
            setViewShowState(mStartButton, VISIBLE);
            setViewShowState(mNot_Playing, VISIBLE);//Sandra@20220419
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ((ENDownloadView) mLoadingProgressBar).reset();
            }
            updateStartImage();
            updatePauseCover();
        }
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, (mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);



    }
    /* @Override
     protected void changeUiToPlayingBufferingShow() {
         super.changeUiToPlayingBufferingShow();
         Debuger.printfLog("changeUiToPlayingBufferingShow");
         setViewShowState(mPrevious, INVISIBLE);
         setViewShowState(mNext, INVISIBLE);
         setViewShowState(mRewind, INVISIBLE);
         setViewShowState(mFwd, INVISIBLE);
          setViewShowState(mNot_Playing, INVISIBLE);
     }*/
    @Override
    protected void changeUiToPlayingBufferingShow() {
        Debuger.printfLog("changeUiToPlayingBufferingShow");

        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419
        setViewShowState(mLockScreen, GONE);
        if (isDoubleTouch == false) {
            setViewShowState(mLoadingProgressBar, VISIBLE);
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
                if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                    ((ENDownloadView) mLoadingProgressBar).start();
                }
            }
        }else{
            setViewShowState(mLoadingProgressBar, INVISIBLE);
        }
    }
    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        Debuger.printfLog("changeUiToCompleteShow");
        setViewShowState(mPrevious, INVISIBLE);//Sandra@20220419
        setViewShowState(mNext, INVISIBLE);//Sandra@20220419
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419
        setViewShowState(mStartButton, INVISIBLE);//Sandra@20220419
        setViewShowState(mRewind, INVISIBLE);//Sandra@20220419
        setViewShowState(mFwd, INVISIBLE);//Sandra@20220419
        setViewShowState(mRewindTotalTime, INVISIBLE);
        mRewindTotalTime.setText(null);
        setViewShowState(mFwdTotalTime, INVISIBLE);
        mFwdTotalTime.setText(null);
        mDoubleFwdClickCount=0;
        mDoubleRewindClickCount=0;
    }

    @Override
    protected void changeUiToError() {
        super.changeUiToError();
        Debuger.printfLog("changeUiToError");
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
        setViewShowState(mRewindTotalTime, INVISIBLE);
        setViewShowState(mFwdTotalTime, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419 add
    }
    protected void changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear();
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
        setViewShowState(mRewindTotalTime, INVISIBLE);
        setViewShowState(mFwdTotalTime, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419 add
    }
    protected void changeUiToPlayingBufferingClear() {
        Debuger.printfLog("changeUiToPlayingBufferingClear");
        setViewShowState(mTopContainer, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419 add
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, VISIBLE);
        setViewShowState(mLockScreen, GONE);
        if(isDoubleTouch==false) {
            setViewShowState(mLoadingProgressBar, VISIBLE);
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
                if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                    ((ENDownloadView) mLoadingProgressBar).start();
                }
            }
            updateStartImage();
        }else{
            setViewShowState(mLoadingProgressBar, VISIBLE);
        }
    }
    protected void changeUiToClear(){
        super.changeUiToClear();
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
        setViewShowState(mRewindTotalTime, INVISIBLE);
        setViewShowState(mFwdTotalTime, INVISIBLE);
        setViewShowState(mNot_Playing, INVISIBLE);//Sandra@20220419 add
    }
    //jennifer add for ????????????????????????<--

    /**
     * ???????????????????????????prepare????????????  addTextureView();
     */
    @Override
    protected void prepareVideo() {
        super.prepareVideo();
        if (mHadPlay && mPlayPosition < (mUriList.size())) {
            setViewShowState(mLoadingProgressBar, VISIBLE);
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    protected void startPrepare() {
        if (getGSYVideoManager().listener() != null) {
            getGSYVideoManager().listener().onCompletion();
        }
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared");
            mVideoAllCallBack.onStartPrepared(mOriginUrl, mTitle, this);
        }
        getGSYVideoManager().setListener(this);
        getGSYVideoManager().setPlayTag(mPlayTag);
        getGSYVideoManager().setPlayPosition(mPlayPosition);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (getActivityContext() instanceof Activity)
            ((Activity) getActivityContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBackUpPlayingBufferState = -1;
        getGSYVideoManager().prepare(mUrl, (mMapHeadData == null) ? new HashMap<String, String>() : mMapHeadData, mLooping, mSpeed, mCache, mCachePath, mOverrideExtension);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }


    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        setViewShowState(mNot_Playing, VISIBLE);//Sandra@20220419 add

    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_media_play_usb;
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == mPrevious.getId()) {
            playPrevious();
            return;
        } else if (i == mNext.getId()) {
            playNext();
            return;
        } else if (i == mFullscreenButton.getId()) {
            if (mContext instanceof Activity) {
                startWindowFullscreen(mContext, true, true);
            }
            return;
        }
    }

    private boolean playPrevious() {
        if (mPlayPosition < 0 || mUriList.size() == 0) return false;
        if (randomOpen == false) {
            if (mPlayPosition == 0) {
                mPlayPosition = mUriList.size() - 1;
            } else if (mPlayPosition <= (mUriList.size() - 1)) {
                mPlayPosition--;
            }
        } else {
            int i;
            for (i = (randomIndexList).size() - 1; i > 0; i--) {
                if (mPlayPosition == (randomIndexList).get(i)) {
                    mPlayPosition = (randomIndexList).get(i - 1);
                    break;
                } else if (mPlayPosition == (randomIndexList).get(0)) {
                    mPlayPosition = (randomIndexList).get((randomIndexList).size() - 1);
                    i = (randomIndexList).size() - 1;
                    break;
                }
            }
        }
        GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
        mSaveChangeViewTIme = 0;
        setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
        startPlayLogic();
        return true;
    }

    /**
     * ???????????????
     *
     * @return true?????????????????????
     */
    public boolean playNext() {
        if (mPlayPosition < 0 || mUriList.size() == 0) return false;
        if (randomOpen == false) {
            if (mPlayPosition < (mUriList.size() - 1)) {
                mPlayPosition += 1;
            } else if (mPlayPosition >= (mUriList.size() - 1)) {
                mPlayPosition = 0;
            }
        } else {
            int i;
            //((ContentFragment) fragments.get(currentTab)).resetAnimation(currPosition);
            for (i = 0; i < (randomIndexList).size() - 1; i++) {
                if (mPlayPosition == (randomIndexList).get(i)) {
                    mPlayPosition = (randomIndexList).get(i + 1);
                    break;
                } else if (mPlayPosition == (randomIndexList).get((randomIndexList).size() - 1)) {
                    mPlayPosition = (randomIndexList).get(0);
                    i = 0;
                    break;
                }
            }
        }
        GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
        mSaveChangeViewTIme = 0;
        setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
        /*if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
            mTitleTextView.setText(gsyVideoModel.getTitle());
        }*/
        startPlayLogic();
        return true;
    }

    @Override
    public void startPlayLogic() {
        super.startPlayLogic();
        saveData(); //??????Playing????????????
        broadCastStateChanged(ACTION_MEDIAITEM_CHANGED_BROADCAST, MEDIAITEM_CHANGED);
        mPrevious.setEnabled(true);
        mNext.setEnabled(true);
        mStart.setEnabled(true);
        mNot_Playing.setText("");
    }

    @Override
    protected void updateStartImage() {
        if (mStartButton instanceof ENPlayView) {
            ENPlayView enPlayView = (ENPlayView) mStartButton;
            enPlayView.setDuration(500);
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                enPlayView.play();
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                enPlayView.pause();
            } else {
                enPlayView.pause();
            }
        } else if (mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
               // imageView.setImageResource(R.drawable.icon_pause_normal);
                imageView.setImageResource(R.drawable.icon_pause_bg);//Sandra@20220419 add
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_click_error_selector);
            } else {
              //  imageView.setImageResource(R.drawable.icon_play_normal);
                imageView.setImageResource(R.drawable.icon_play_bg);//Sandra@20220419 add
            }
        }
        broadCastStateChanged(ACTION_STATE_CHANGED_BROADCAST, PLAYSTATE_CHANGED);
    }


    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(MediaInfo mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

    public String getCurrentUri() {
        return mOriginUrl;
    }

    private void broadCastStateChanged(String action, int extraName) {
        Intent intent = new Intent(action);
        intent.setPackage(mContext.getPackageName());
        switch (extraName) {
            case PLAYSTATE_CHANGED:
                Log.i(TAG, "broadCastStateChanged: mCurrentState=" + mCurrentState);
                intent.putExtra(PLAYSTATE_CHANGED + "", mCurrentState);
                break;
            case MEDIAITEM_CHANGED:
                // mediaInfo.getMediaItems().get(mPlayPosition).setThumbBitmap(null);
                if (mediaInfo.getMediaItems().size()==0)return;
                MediaItem mediaItemOrignal=mediaInfo.getMediaItems().get(mPlayPosition);
                MediaItem mediaItem=new MediaItem(mediaItemOrignal.getId(),mediaItemOrignal.getTitle(),mediaItemOrignal.getAlbum(),mediaItemOrignal.getArtist(),
                        mediaItemOrignal.getDuration(),mediaItemOrignal.getThumbBitmap(),mediaItemOrignal.getGsyVideoModel(),mediaItemOrignal.isIfVideo(),mediaItemOrignal.getStoragePath() );
                try {
                    mediaItem.setThumbBitmap(MediaItemUtil.cutDownBitmap(mediaItem.getThumbBitmap()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //Sandra@20220402 add cutDownBitmap??????????????????????????????????????????1024KB??????????????????????????????????????????????????????
                //????????????????????????????????????????????????????????????????????????????????????????????????????????????
                intent.putExtra(MEDIAITEM_CHANGED + "", mediaItem);
                intent.putExtra(POS_EXTRA, mPlayPosition);
                break;
        }
        //   Log.i(TAG, "broadCastStateChanged: extraName=" + extraName);
        mContext.sendBroadcast(intent);

    }
    public LinkedList<Integer> getRandom() {
        int random;
        while (randomIndexList.size() < (mUriList.size())) {
            random = (int) (Math.random() * (mUriList.size()));//??????1???list.size()-1??????????????????
            if (!randomIndexList.contains(random)) {
                randomIndexList.add(random);
            }
        }
        Log.i("main", "Jennifertest90=: " + randomIndexList);
        return randomIndexList;
    }
    public void mediaControl(int state, long position) {
        switch (state) {
            case STATE_PLAY:
            case STATE_PAUSE:
                clickStartIcon();
                break;
            case STATE_NEXT:
                playNext();
                break;
            case STATE_PREVIOUS:
                playPrevious();
                break;
            case STATE_RANDOM_CLOSE:
                randomOpen = false;
                if (randomIndexList.size() > 0) {
                    randomIndexList.clear();
                }
                break;
            case STATE_RANDOM_OPEN:
                randomOpen = true;
                getRandom();
                break;
            case STATE_ALL_REPEAT:
                playMode = 0;
                break;
            case STATE_SINGLE_REPEAT:
                playMode = 1;
                break;

        }
        //updateStartImage();
    }
    public void saveData() {
        if (mediaInfo == null) {
            return;
        }
        SharedPreferences sharedPreferences = getActivityContext().getSharedPreferences("SavePlayingStatus", Context.MODE_PRIVATE); //????????????
        SharedPreferences.Editor editor = sharedPreferences.edit();//???????????????
        if (mediaInfo.getMediaItems().size()==0)return;
        MediaItem mediaItem = mediaInfo.getMediaItems().get(mPlayPosition);
        if (mediaItem.isIfVideo()) {
            editor.putInt("currentTab", 1);
        } else {
            editor.putInt("currentTab", 0);
        }
        if (mediaInfo.getDeviceItem() != null) {
            editor.putString("description", mediaInfo.getDeviceItem().getDescription());
            editor.putString("storagePath", mediaInfo.getDeviceItem().getStoragePath());
        }
        editor.putString("title", mediaItem.getTitle());
        editor.putLong("id", mediaItem.getId());
        editor.apply();
    }
    //jennifer add for ??????????????????-->
    public void backWard() {
        if (CSDMediaPlayer.getInstance(mContext) != null) {
            int position = (int) (getGSYVideoManager().getCurrentPosition());
            if (position > 10000) {
                position -= 10000;
                mRewindTotalTime.setText((mDoubleRewindClickCount*10)+"???");
                mRewindTotalTime.setVisibility(VISIBLE);
                mRewind.setVisibility(VISIBLE);
            } else {
                position = 0;
                mDoubleRewindClickCount=0;
                mRewindTotalTime.setText(null);
                mRewindTotalTime.setVisibility(INVISIBLE);
                mRewind.setVisibility(INVISIBLE);
            }
            getGSYVideoManager().seekTo(position);
        }
    }

    public void forWard() {
        if (CSDMediaPlayer.getInstance(mContext) != null) {
            int position = (int) (getGSYVideoManager().getCurrentPosition());

            if(position >= (int)getGSYVideoManager().getDuration()){
                position = (int)getGSYVideoManager().getDuration();
                mDoubleFwdClickCount=0;
                mFwdTotalTime.setText(null);
                mFwd.setVisibility(INVISIBLE);
                mFwdTotalTime.setVisibility(INVISIBLE);
            }else{
                position+=10000;
                mFwdTotalTime.setText((mDoubleFwdClickCount*10)+"???");
                mFwd.setVisibility(VISIBLE);
                mFwdTotalTime.setVisibility(VISIBLE);
            }
            getGSYVideoManager().seekTo(position);
        }
    }

    @Override
    protected void touchDoubleUp() {
        if (!mHadPlay) {
            return;
        } else {
            switch (USB_LAYER_DOUBLE_TAP) {
                case REWIND:
                    mFwd.setVisibility(INVISIBLE);
                    mFwdTotalTime.setVisibility(INVISIBLE);
                    backWard();
                    break;
                case FASTFORWARD:
                    mRewind.setVisibility(INVISIBLE);
                    mRewindTotalTime.setVisibility(INVISIBLE);
                    forWard();
                    break;
            }
        }
    }
    private void initGestureDetector(Context context) {
        mGesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if(USB_LAYER_DOUBLE_TAP == REWIND){
                    mDoubleRewindClickCount+=1;
                    mDoubleFwdClickCount=0;
                }else if(USB_LAYER_DOUBLE_TAP == FASTFORWARD){
                    mDoubleFwdClickCount+=1;
                    mDoubleRewindClickCount=0;
                }
                isDoubleTouch=true;
                Log.i(TAG, "onDoubleTapEvent:DOUBLE_TAP= " + USB_LAYER_DOUBLE_TAP);
                touchDoubleUp();
                //  handler.sendEmptyMessage(HIDE_VIEW_CONTROL);
                return super.onDoubleTapEvent(e);
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                isDoubleTouch=false;
                mDoubleRewindClickCount=0;
                mDoubleFwdClickCount=0;
                mFwd.setVisibility(INVISIBLE);
                mRewind.setVisibility(INVISIBLE);
                mFwdTotalTime.setVisibility(INVISIBLE);
                mRewindTotalTime.setVisibility(INVISIBLE);
                Log.i(TAG, "onSingleTapEvent:DOUBLE_TAP= " + USB_LAYER_DOUBLE_TAP);
                if (!mChangePosition && !mChangeVolume && !mBrightness) {
                    onClickUiToggle();
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }
    //jennifer add for ??????????????????<--
}
