package com.fxc.ev.mediacenter.bt.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.fxc.ev.mediacenter.mediaplayer.MediaItem;
import com.fxc.ev.mediacenter.mediaplayer.MediaSeekBar;
import com.fxc.ev.mediacenter.mediaplayer.Constants;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE;

public class MediaBrowserConnecter {
    private final String TAG = MediaBrowserConnecter.class.getSimpleName();

    private MediaBrowserCompat mMediaBrowser;
    private MediaBrowserConnectionCallback mConnectionCallback;
    private MediaControllerCompat mMediaController;
    private Context mContext;
    private String packageName = "com.android.bluetooth";
    private String className = "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService";
    private static MediaBrowserConnecter mInstance;
    private boolean isBtPlaying = false;
    private MediaItem currentBtItem;//需要通知UI显示内容
    // private List<MediaBrowserCompat.MediaItem> items;
    private ArrayList<MediaItem> items;
    private MediaSeekBar mSeekbar;
    private Object lock = new Object();

    public MediaBrowserConnecter(Context context) {
        mContext = context;
    }

    public static MediaBrowserConnecter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MediaBrowserConnecter(context);
        }
        return mInstance;
    }

    public void initBroswer(/*, MediaControllerCallback controllerCallback*/) {
        // synchronized (lock) {
        //  mContext = context;
        //  mControllerCallback = controllerCallback;
        mConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowser = new MediaBrowserCompat(mContext,
                new ComponentName(packageName, className),
                mConnectionCallback, null);
        mMediaBrowser.connect();
        //    }
    }


    public MediaControllerCompat getMediaController() {
        return mMediaController;
    }

    public void setSeekBar(MediaSeekBar seekBar) {
        synchronized (lock) {
            mSeekbar = seekBar;
            if (null != mMediaController)
                mSeekbar.setMediaController(mMediaController);
        }
    }

    /**
     * mMediaController创建成功后主动bind Seekbar
     */
    private void bindUI() {
        synchronized (lock) {
            if (null != mSeekbar) {
                mSeekbar.setMediaController(mMediaController);
                broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, remapDefine(Constants.REPEATMODE_CHANGED, mMediaController.getRepeatMode()));
                broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, remapDefine(Constants.SHUFFLEMODE_CHANGED, mMediaController.getShuffleMode()));
                broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ? Constants.STATE_PLAY : Constants.STATE_PAUSE);

            }
        }
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            Log.i(TAG, "onConnected: ");
            if (mMediaBrowser.isConnected()) {
                Log.d(TAG, "onConnected: " + "连接成功了");
                String mediaId = mMediaBrowser.getRoot();
                Log.i(TAG, "onConnected: mediaid=" + mediaId);
                // 必须先解除订阅再重新订阅
                mMediaBrowser.unsubscribe(mediaId);
                mMediaBrowser.subscribe(mediaId, subscriptionCallback);

                try {
                    mMediaController = new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                    bindUI();
                    mMediaController.registerCallback(mControllerCallback);
                    Log.i(TAG, "onConnected: ok");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.i(TAG, "onConnected: 182");
                }
            }
        }

        public MediaBrowserConnectionCallback() {
            super();
            Log.i(TAG, "MediaBrowserConnectionCallback: ");
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            Log.i(TAG, "onConnectionSuspended: ");
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            Log.i(TAG, "onConnectionFailed: ");
        }
    }


    MediaControllerCompat.Callback mControllerCallback =

            new MediaControllerCompat.Callback() {
                public void onSessionDestroyed() {
                    mMediaBrowser.disconnect();       //Session销毁
                    Log.i(TAG, "onSessionDestroyed: ");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                    boolean playingState = state != null &&
                            state.getState() == PlaybackStateCompat.STATE_PLAYING;
                    if (isBtPlaying != playingState) {
                        isBtPlaying = playingState;
                        broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, isBtPlaying ? Constants.STATE_PLAY : Constants.STATE_PAUSE);
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (metadata == null) {
                        return;
                    }
                    MediaItem item = new MediaItem(metadata.getLong(METADATA_KEY_MEDIA_ID),
                            metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                            metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM),
                            metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                            metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION),
                            metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON), null, false, null);
                    currentBtItem = item;
                    broadCastStateChanged(Constants.ACTION_MEDIAITEM_CHANGED_BROADCAST, Constants.MEDIAITEM_CHANGED, -1);
                }

                @Override
                public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
                    super.onQueueChanged(queue);
                    //重新获取列表
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    // super.onRepeatModeChanged(repeatMode);
                    //  int value = remapDefine(REPEATMODE_CHANGED, repeatMode);

                    broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, remapDefine(Constants.REPEATMODE_CHANGED, repeatMode));
                }

                @Override
                public void onShuffleModeChanged(int shuffleMode) {
                    //  super.onShuffleModeChanged(shuffleMode);
                    //  int value = remapDefine(SHUFFLEMODE_CHANGED, shuffleMode);
                    broadCastStateChanged(Constants.ACTION_STATE_CHANGED_BROADCAST, Constants.PLAYSTATE_CHANGED, remapDefine(Constants.SHUFFLEMODE_CHANGED, shuffleMode));
                }
            };


    MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {

        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.d(TAG, "onChildrenLoaded: ");

            if (children != null && children.size() != 0) {
                items = new ArrayList<>();
                for (int i = 0; i < children.size(); i++) {
                    MediaBrowserCompat.MediaItem mediaItem = children.get(i);
                    Log.d(TAG, "" + mediaItem.getDescription().getTitle().toString());
                    Log.i(TAG, "onChildrenLoaded: ");
                    // 将返回的音乐列表保存起来
                   /* MediaItem item = new MediaItem(-1l,
                            mediaItem.getDescription().getTitle() + "", "", mediaItem.getDescription().getDescription() + "", -1, mediaItem.getDescription().getIconBitmap(), null, false, "");
                    items.add(item);*/
                }
            }
        }
    };

    private void broadCastStateChanged(String action, int extraName, int value) {
        Intent intent = new Intent(action);
        intent.setPackage(mContext.getPackageName());
        switch (extraName) {
            case Constants.PLAYSTATE_CHANGED:
                intent.putExtra(Constants.PLAYSTATE_CHANGED + "", value);
                break;
            case Constants.MEDIAITEM_CHANGED:  //目前第几首要计算，后续增加
                intent.putExtra(Constants.MEDIAITEM_CHANGED + "", currentBtItem);
                break;
           /* case PLAYSTATE_INIT:
                intent.putExtra()*/
        }
        Log.i(TAG, "broadCastStateChanged: extraName=" + extraName);
        mContext.sendBroadcast(intent);

    }

    private int remapDefine(int mode, int value) {//转换成本地统一定义
        Log.i(TAG, "remapDefine: mode=" + mode + "value=" + value);
        switch (mode) {
            case Constants.SHUFFLEMODE_CHANGED:
                if (SHUFFLE_MODE_NONE == value)
                    return Constants.STATE_RANDOM_CLOSE;
                else if (SHUFFLE_MODE_ALL == value)
                    return Constants.STATE_RANDOM_OPEN;
                break;
            case Constants.REPEATMODE_CHANGED:
                if (value == REPEAT_MODE_ONE)
                    return Constants.STATE_SINGLE_REPEAT;
                else if (value == REPEAT_MODE_ALL) {
                    return Constants.STATE_ALL_REPEAT;
                }
                break;
            case Constants.STATE_SINGLE_REPEAT:
                return REPEAT_MODE_ONE;
            case Constants.STATE_ALL_REPEAT:
                return REPEAT_MODE_ALL;
            case Constants.STATE_RANDOM_OPEN:
                return SHUFFLE_MODE_ALL;
            case Constants.STATE_RANDOM_CLOSE:
                return SHUFFLE_MODE_NONE;
        }
        return -1;
    }

    public void setBTDeviceState(int state, long value) {
        switch (state) {
            case Constants.STATE_PLAY:
                /*mMediaController.getTransportControls().play();
                break;*/
            case Constants.STATE_PAUSE:
                if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                mMediaController.getTransportControls().pause();
                } else {
                    mMediaController.getTransportControls().play();
                }

                break;
            case Constants.STATE_SINGLE_REPEAT:
            case Constants.STATE_ALL_REPEAT:
                mMediaController.getTransportControls().setRepeatMode(remapDefine(state, -1));//因为使用sdk定义会有case的重复，所以自定义相关case内容
                break;
            case Constants.STATE_RANDOM_OPEN:
            case Constants.STATE_RANDOM_CLOSE:
                mMediaController.getTransportControls().setShuffleMode(remapDefine(state, -1));
                break;
            case Constants.STATE_NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case Constants.STATE_PREVIOUS:
                mMediaController.getTransportControls().skipToPrevious();
                break;
          /*  case STATE_SKIP2ITEM://还要确认点击事件怎么播放
                mMediaController.getTransportControls().skipToQueueItem(value);
                break;*/
            case Constants.STATE_SEEKTO://快进快退的位置
                mMediaController.getTransportControls().seekTo(value);
                break;
            //  mMediaController.getTransportControls().setShuffleMode();

        }

    }


}
