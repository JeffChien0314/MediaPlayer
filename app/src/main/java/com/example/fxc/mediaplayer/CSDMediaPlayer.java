package com.example.fxc.mediaplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.fxc.MainActivity;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.codeest.enviews.ENDownloadView;
import moe.codeest.enviews.ENPlayView;

/**
 * Created by Jennifer on 2022/1/17.
 */

public class CSDMediaPlayer extends ListGSYVideoPlayer {
    private final String TAG = CSDMediaPlayer.class.getSimpleName();
    public static final String ACTION_STATE_CHANGED = "CSDMediaPlayer.stateChanged";
    public static final String ACTION_CHANGE_STATE = "CSDMediaPlayer.changestate";
    public static final String STATE_EXTRA = "state";
    public static final String POS_EXTRA = "pos";
    public static final int PLAYSTATE_CHANGED = 1;
    public static final int MEDIAITEM_CHANGED = 2;
    public static final int STATE_PLAY = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_NEXT = 2;
    public static final int STATE_PREVIOUS = 3;
    public static final int STATE_SEEKTO = 4;
    public static final int STATE_RANDOM = 5;
    /* public static final int STATE_RANDOM_FALSE = 6;*/
    public static final int STATE_SHUFFLE = 6;

    private onAutoCompletionListener mListener;
    private MediaInfo mediaInfo;
    private static CSDMediaPlayer mInstance;
    private ImageView mPrevious, mNext, mRandom;

    public CSDMediaPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CSDMediaPlayer(Context context) {
        super(context);
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
            Log.i("CSDMediaPlayer", "CSDMediaPlayer: context=" + context);
            mInstance = new CSDMediaPlayer(context);
        }
        return mInstance;
    }

    public void setOnAutoCompletionListener(onAutoCompletionListener listener) {
        mListener = listener;
    }


    @Override
    protected void init(Context context) {
        super.init(context);
        mPrevious = findViewById(R.id.bt_previous);
        mNext = findViewById(R.id.bt_next);
        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
    }

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


    /*

     */
/**
 * 设置播放URL
 *
 * @param url           播放url
 * @param position      需要播放的位置
 * @param cacheWithPlay 是否边播边缓存
 * @return
 *//*

    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, int playmode) {
        return setUp(url, cacheWithPlay, position, null, new HashMap<String, String>());
    }

    */
/**
 * 设置播放URL
 *
 * @param url           播放url
 * @param cacheWithPlay 是否边播边缓存
 * @param position      需要播放的位置
 * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
 * @return
 *//*

    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath) {
        return setUp(url, cacheWithPlay, position, cachePath, new HashMap<String, String>());
    }

    */
/**
 * 设置播放URL
 *
 * @param url           播放url
 * @param cacheWithPlay 是否边播边缓存
 * @param position      需要播放的位置
 * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
 * @param mapHeadData   http header
 * @return
 *//*

    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp(url, cacheWithPlay, position, cachePath, mapHeadData, true);
    }
*/

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
        if (MainActivity.playMode == 0 || MainActivity.playMode == 1) {
            mListener.completion();
            return;
        }
        super.onAutoCompletion();
    }

    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
    }


    @Override
    protected void changeUiToPreparingShow() {
        super.changeUiToPreparingShow();
        Debuger.printfLog("changeUiToPreparingShow");
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);

    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        Debuger.printfLog("changeUiToPlayingShow");
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        updateStartImage();
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        Debuger.printfLog("changeUiToPauseShow");
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        updateStartImage();
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
        Debuger.printfLog("changeUiToPlayingBufferingShow");
        setViewShowState(mPrevious, INVISIBLE);
        setViewShowState(mNext, INVISIBLE);
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        Debuger.printfLog("changeUiToCompleteShow");
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        updateStartImage();
    }

    @Override
    protected void changeUiToError() {
        super.changeUiToError();
        Debuger.printfLog("changeUiToError");
        setViewShowState(mPrevious, VISIBLE);
        setViewShowState(mNext, VISIBLE);
        updateStartImage();
    }

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
        updateStartImage();
    }

    public interface onAutoCompletionListener {
        public void completion();

    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_media_play_control;
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
        }
    }

    private boolean playPrevious() {
        if (mPlayPosition < 0 || mUriList.size() == 0) return false;

        if (mPlayPosition == 0) {
            mPlayPosition = mUriList.size() - 1;
        } else if (mPlayPosition <= (mUriList.size() - 1)) {
            mPlayPosition--;
        }
        GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
        mSaveChangeViewTIme = 0;
        setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
        if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
            mTitleTextView.setText(gsyVideoModel.getTitle());
        }
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
        if (mPlayPosition < (mUriList.size() - 1)) {
            mPlayPosition += 1;
        } else if (mPlayPosition >= (mUriList.size() - 1)) {
            mPlayPosition = 0;
        }
        GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
        mSaveChangeViewTIme = 0;
        setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
        if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
            mTitleTextView.setText(gsyVideoModel.getTitle());
        }
        startPlayLogic();
        return true;
    }

    @Override
    public void startPlayLogic() {
        super.startPlayLogic();
        broadCastStateChanged(MEDIAITEM_CHANGED);
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
        broadCastStateChanged(PLAYSTATE_CHANGED);
    }


    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public String getCurrentUri() {
        /*if (MediaInfo != null && MediaInfo.getMediaItems() != null && mPlayPosition > 0 && MediaInfo.getMediaItems().size() - 1 < mPlayPosition) {
            return MediaInfo.getMediaItems().get(mPlayPosition).getGsyVideoModel().getUrl();
        }*/
        return mOriginUrl;
    }

    private void broadCastStateChanged(int extraName) {
        Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.setPackage("com.example.fxc.mediaplayer");
        switch (extraName) {
            case PLAYSTATE_CHANGED:
                intent.putExtra(PLAYSTATE_CHANGED + "", mCurrentState);
                break;
            case MEDIAITEM_CHANGED:
                intent.putExtra(MEDIAITEM_CHANGED + "", mediaInfo.getMediaItems().get(mPlayPosition));
                intent.putExtra(POS_EXTRA, mPlayPosition);
                break;
        }
        Log.i(TAG, "broadCastStateChanged: extraName=" + extraName);
        mContext.sendBroadcast(intent);

    }

    public void mediaControl(int state, long position) {
        switch (state) {
            case STATE_PLAY:
                getGSYVideoManager().start();
                break;
            case STATE_PAUSE:
                getGSYVideoManager().pause();
                break;
            case STATE_NEXT:
                playNext();
                break;
            case STATE_PREVIOUS:
                playPrevious();
                break;
            case STATE_RANDOM:
            case STATE_SHUFFLE:
                break;

        }
        updateStartImage();
    }
}
