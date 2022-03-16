package com.example.fxc.bt.client;

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

import com.example.fxc.mediaplayer.MediaInfo;
import com.example.fxc.mediaplayer.MediaItem;

import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.*;

public class MediaBrowserConnecter {
    private final String TAG = MediaBrowserConnecter.class.getSimpleName();
  /*  public static final int STATE_PLAY = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_REPEAT_MODE_ONE = 2;
    public static final int STATE_REPEAT_MODE_ALL = 3;
    public static final int STATE_SHUFFLE_MODE_NONE = 4;
    public static final int STATE_SHUFFLE_MODE_ALL = 5;
    public static final int STATE_SKIP2NEXT = 6;
    public static final int STATE_SKIP2PREVIOUS = 7;
    public static final int STATE_SKIP2ITEM = 8;
    public static final int STATE_SEEKTO = 9;*/

    private MediaBrowserCompat mMediaBrowser;
    private MediaBrowserConnectionCallback mConnectionCallback;
    private MediaControllerCompat mMediaController;
    //  private MediaControllerCallback mControllerCallback;
    private Context mContext;
    private String packageName = "com.android.bluetooth";
    private String className = "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService";
    private static MediaBrowserConnecter mInstance;
    private boolean isBtPlaying = false;
    private MediaItem currentBtItem;//需要通知UI显示内容
    private MediaInfo mediaInfo;

    public static MediaBrowserConnecter getInstance() {
        if (mInstance == null) {
            mInstance = new MediaBrowserConnecter();
        }
        return mInstance;
    }

    public void initBroswer(Context context/*, MediaControllerCallback controllerCallback*/) {
        mContext = context;
        //  mControllerCallback = controllerCallback;
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

    MediaControllerCompat.Callback mControllerCallback =

            new MediaControllerCompat.Callback() {
                public void onSessionDestroyed() {
                    mMediaBrowser.disconnect();
                    //Session销毁
                    Log.i(TAG, "onSessionDestroyed: ");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                    boolean playingState = state != null &&
                            state.getState() == PlaybackStateCompat.STATE_PLAYING;
                    if (isBtPlaying != playingState) {
                        isBtPlaying = playingState;
                        broadCastStateChanged(PLAYSTATE_CHANGED, isBtPlaying);
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
                    broadCastStateChanged(MEDIAITEM_CHANGED, item);
                }

                @Override
                public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
                    super.onQueueChanged(queue);
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    super.onRepeatModeChanged(repeatMode);
                    broadCastStateChanged(REPEATMODE_CHANGED, repeatMode);
                }

                @Override
                public void onShuffleModeChanged(int shuffleMode) {
                    super.onShuffleModeChanged(shuffleMode);
                    broadCastStateChanged(SHUFFLEMODE_CHANGED, shuffleMode);
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

    private void broadCastStateChanged(int extraName, Object value) {
        Intent intent = new Intent(ACTION_STATE_CHANGED_BROADCAST);
        intent.setPackage("com.example.fxc.mediaplayer");
        switch (extraName) {
            case PLAYSTATE_CHANGED:
                intent.putExtra(PLAYSTATE_CHANGED + "", (Boolean) value);
                break;
            case MEDIAITEM_CHANGED:  //目前第几首要计算，后续增加
                intent.putExtra(MEDIAITEM_CHANGED + "", currentBtItem);
                break;
            case REPEATMODE_CHANGED:
            case SHUFFLEMODE_CHANGED:
                intent.putExtra(extraName + "", (Integer) value);
                break;
        }
        Log.i(TAG, "broadCastStateChanged: extraName=" + extraName);
        mContext.sendBroadcast(intent);

    }

    public void setBTDeviceState(int state, long value) {
        switch (state) {
            case STATE_PLAY:
                mMediaController.getTransportControls().play();
                break;
            case STATE_PAUSE:
                mMediaController.getTransportControls().pause();
                break;
            case STATE_SINGLE_REPEAT:
            case STATE_ALL_REPEAT:
                mMediaController.getTransportControls().setRepeatMode(state - 1);//因为使用sdk定义会有case的重复，所以自定义相关case内容
                break;
            case STATE_RANDOM_OPEN:
            case STATE_RANDOM_CLOSE:
                mMediaController.getTransportControls().setShuffleMode(state - 4);
                break;
            case STATE_NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case STATE_PREVIOUS:
                mMediaController.getTransportControls().skipToPrevious();
                break;
          /*  case STATE_SKIP2ITEM://还要确认点击事件怎么播放
                mMediaController.getTransportControls().skipToQueueItem(value);
                break;*/
            case STATE_SEEKTO://快进快退的位置
                mMediaController.getTransportControls().seekTo(value);
                break;
            //  mMediaController.getTransportControls().setShuffleMode();

        }

    }


}
