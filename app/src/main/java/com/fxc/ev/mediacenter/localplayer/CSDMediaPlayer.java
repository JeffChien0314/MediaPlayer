package com.fxc.ev.mediacenter.localplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
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

import moe.codeest.enviews.ENDownloadView;
import moe.codeest.enviews.ENPlayView;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static com.fxc.ev.mediacenter.util.Constants.*;

/**
 * Created by Jennifer on 2022/1/17.
 */

public class CSDMediaPlayer extends ListGSYVideoPlayer implements View.OnClickListener, View.OnTouchListener {
    private final String TAG = CSDMediaPlayer.class.getSimpleName();
    public static final String STATE_EXTRA = "state";
    public static final String POS_EXTRA = "pos";
    private final int FASTFORWARD = 1;//jennifer add for 快进快退功能
    private final int DO_NOTHING = 0;//jennifer add for 快进快退功能
    private final int REWIND = -1;//jennifer add for 快进快退功能
    private int USB_LAYER_DOUBLE_TAP = DO_NOTHING;
    private MediaInfo mediaInfo;
    private static CSDMediaPlayer mInstance;
    private ImageView mPrevious, mNext, mRewind, mFwd;
    private TextView mRewindTotalTime, mFwdTotalTime;//jennifer add for 快进快退功能
    private int playMode = 0;
    private int mDoubleFwdClickCount = 0;//jennifer add for 快进快退功能
    private int mDoubleRewindClickCount = 0;//jennifer add for 快进快退功能
    private boolean randomOpen = false;
    private OnTouchListener mTouchListener;
    private GestureDetector mGesture;//jennifer add for 快进快退功能
    private LinkedList<Integer> randomIndexList = new LinkedList<>();
    private boolean isDoubleTouch = false;//jennifer add for 快进快退功能
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
        mPrevious = findViewById(R.id.bt_previous);
        mNext = findViewById(R.id.bt_next);
        mRewind = findViewById(R.id.bt_rewind);
        mFwd = findViewById(R.id.bt_fwd);
        mRewindTotalTime = findViewById(R.id.rewind_content_display);
        mFwdTotalTime = findViewById(R.id.fwd_content_display);
        mFullscreenButton.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mRewind.setOnClickListener(this);
        mFwd.setOnClickListener(this);
        initGestureDetector(context);
        //jennifer add for 快进快退功能-->
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
                    if(event.getAction() == ACTION_UP){
                        //mDoubleClickCount=0;
                        startDismissControlViewTimer();
                    }

                return true;
            }
        };
        findViewById(R.id.surface_container).setOnTouchListener(mTouchListener);
    }
    //jennifer add for 快进快退功能<--


    /**
     * 设置播放URL
     *
     * @param mediaInfos    当前播放mediaInfo
     * @param position      需要播放的位置
     * @param cacheWithPlay 是否边播边缓存
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
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cacheWithPlay 是否边播边缓存
     * @return
     */
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position) {
        return setUp(url, cacheWithPlay, position, null, new HashMap<String, String>());
    }


    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
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
                    findViewById(R.id.surface_container).setBackground(null);
                    ImageView imageView = new ImageView(getActivityContext());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(mediaInfo.getMediaItems().get(mPlayPosition).getThumbBitmap());
                    mThumbImageViewLayout.removeAllViews();
                    setThumbImageView(imageView);
                } else {
                    Bitmap bm = mediaInfo.getMediaItems().get(mPlayPosition).getThumbBitmap();
                    Drawable drawable = new BitmapDrawable(mContext.getResources(), bm);
                    findViewById(R.id.surface_container).setBackground(drawable);
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
            to.findViewById(R.id.surface_container).setBackground(drawable);
        } else {
            findViewById(R.id.surface_container).setBackground(null);
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
    //jennifer add for 按钮的显示的消失-->
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
    }


    @Override
    protected void changeUiToPreparingShow() {
        Debuger.printfLog("changeUiToPreparingShow");

        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, GONE);
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
        if(isDoubleTouch){
            setViewShowState(mPrevious, INVISIBLE);
            setViewShowState(mNext, INVISIBLE);
            setViewShowState(mStartButton, INVISIBLE);
        }else{
            setViewShowState(mPrevious, VISIBLE);
            setViewShowState(mNext, VISIBLE);
            setViewShowState(mStartButton, VISIBLE);
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
        }else{
            setViewShowState(mPrevious, VISIBLE);
            setViewShowState(mNext, VISIBLE);
            setViewShowState(mStartButton, VISIBLE);
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
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
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
    }
    protected void changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear();
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
        setViewShowState(mRewind, INVISIBLE);
        setViewShowState(mFwd, INVISIBLE);
        setViewShowState(mRewindTotalTime, INVISIBLE);
        setViewShowState(mFwdTotalTime, INVISIBLE);
    }
   protected void changeUiToPlayingBufferingClear() {
       Debuger.printfLog("changeUiToPlayingBufferingClear");
       setViewShowState(mTopContainer, INVISIBLE);
       setViewShowState(mBottomContainer, INVISIBLE);
       setViewShowState(mStartButton, INVISIBLE);
       setViewShowState(mPrevious, INVISIBLE);
       setViewShowState(mNext, INVISIBLE);
       setViewShowState(mPrevious, INVISIBLE);
       setViewShowState(mNext, INVISIBLE);

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
    }
    //jennifer add for 按钮的显示的消失<--

    /**
     * 开始状态视频播放，prepare时不执行  addTextureView();
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
     * 播放下一集
     *
     * @return true表示还有下一集
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
        saveData(); //保存Playing歌曲信息
        broadCastStateChanged(ACTION_MEDIAITEM_CHANGED_BROADCAST, MEDIAITEM_CHANGED);
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
                imageView.setImageResource(R.drawable.icon_pause_normal);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_click_error_selector);
            } else {
                imageView.setImageResource(R.drawable.icon_play_normal);
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
                //Sandra@20220402 add cutDownBitmap用于缩减图片的大小，图片大于1024KB将不能正常传输，图片太小分辨率就太低
                //所以请尝试更改方法体内部的参数，以达到即适合显又尽量降低传输大小的效果。
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
            random = (int) (Math.random() * (mUriList.size()));//生成1到list.size()-1之间的随机数
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
        SharedPreferences sharedPreferences = getActivityContext().getSharedPreferences("SavePlayingStatus", Context.MODE_PRIVATE); //私有数据
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
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
    //jennifer add for 快进快退功能-->
    public void backWard() {
        if (CSDMediaPlayer.getInstance(mContext) != null) {
            int position = (int) (getGSYVideoManager().getCurrentPosition());
            if (position > 10000) {
                position -= 10000;
                mRewindTotalTime.setText((mDoubleRewindClickCount*10)+"秒");
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
                mFwdTotalTime.setText((mDoubleFwdClickCount*10)+"秒");
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
    //jennifer add for 快进快退功能<--
}
