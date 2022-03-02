package com.example.fxc.bt.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

public class MediaBroswerConnector {
    private final String TAG = MediaBroswerConnector.class.getSimpleName();
    public static final int STATE_PLAY = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_REPEAT_MODE_ONE = 2;
    public static final int STATE_REPEAT_MODE_ALL = 3;
    public static final int STATE_SHUFFLE_MODE_NONE = 4;
    public static final int STATE_SHUFFLE_MODE_ALL = 5;
    public static final int STATE_SKIP2NEXT = 6;
    public static final int STATE_SKIP2PREVIOUS = 7;
    public static final int STATE_SKIP2ITEM = 8;
    public static final int STATE_SEEKTO = 9;

    private MediaBrowserCompat mMediaBrowser;
    private MediaBrowserConnectionCallback mConnectionCallback;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mControllerCallback;
    private Context mContext;
    private String packageName = "com.android.bluetooth";
    private String className = "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService";
    private static MediaBroswerConnector mInstance;

    public static MediaBroswerConnector getInstance() {
        if (mInstance == null) {
            mInstance = new MediaBroswerConnector();
        }
        return mInstance;
    }

    public void initBroswer(Context context, MediaControllerCallback controllerCallback) {
        mContext = context;
        mControllerCallback = controllerCallback;
        mConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(packageName, className),
                mConnectionCallback, null);
        mMediaBrowser.connect();
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
                   /* if (mContext instanceof Activity) {
                        MediaControllerCompat.setMediaController((Activity) mContext, mMediaController);
                    }
*/
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

    public class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            mMediaBrowser.disconnect();
        }
    }

    public void setBTDeviceState(int state, long value) {
        switch (state) {
            case STATE_PLAY:
                mMediaController.getTransportControls().play();
                break;
            case STATE_PAUSE:
                mMediaController.getTransportControls().pause();
                break;
            case STATE_REPEAT_MODE_ONE:
            case STATE_REPEAT_MODE_ALL:
                mMediaController.getTransportControls().setRepeatMode(state - 1);//因为使用sdk定义会有case的重复，所以自定义相关case内容
                break;
            case STATE_SHUFFLE_MODE_ALL:
            case STATE_SHUFFLE_MODE_NONE:
                mMediaController.getTransportControls().setShuffleMode(state - 4);
                break;
            case STATE_SKIP2NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case STATE_SKIP2PREVIOUS:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            case STATE_SKIP2ITEM://还要确认点击事件怎么播放
                mMediaController.getTransportControls().skipToQueueItem(value);
                break;
            case STATE_SEEKTO://快进快退的位置
                mMediaController.getTransportControls().seekTo(value);
                break;
            //  mMediaController.getTransportControls().setShuffleMode();

        }

    }

    MediaControllerCompat.Callback controllerCallback =

            new MediaControllerCompat.Callback() {
                public void onSessionDestroyed() {
                    mMediaBrowser.disconnect();
                    //Session销毁
                    Log.i(TAG, "onSessionDestroyed: ");
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    //循环模式发生变化
                    Log.i(TAG, "onRepeatModeChanged: ");
                }

                @Override
                public void onShuffleModeChanged(int shuffleMode) {
                    //随机模式发生变化
                    Log.i(TAG, "onShuffleModeChanged: ");
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    //数据变化->修改音频展示信息？
                    Log.i(TAG, "onMetadataChanged: ");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    //播放状态变化
                    Log.i(TAG, "onPlaybackStateChanged: state=" + state.getPosition());
                    //根据状态变化改变seekbar位置
                }
            };

    MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {

        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.d(TAG, "onChildrenLoaded: ");
            if (children != null && children.size() != 0) {
                for (int i = 0; i < children.size(); i++) {
                    MediaBrowserCompat.MediaItem mediaItem = children.get(i);
                    Log.d(TAG, "" + mediaItem.getDescription().getTitle().toString());
                    // 将返回的音乐列表保存起来
                }
            }
        }
    };


}
