package com.example.fxc.bt;

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

public class MediaControlHelper {
    String TAG = MediaControlHelper.class.getSimpleName();
    MediaBrowserCompat mMediaBrowser;
    MediaBrowserConnectionCallback mConnectionCallback;
    MediaControllerCompat mMediaController;
    MediaControllerCompat.Callback controllerCallback =

            new MediaControllerCompat.Callback() {
                public void onSessionDestroyed() {
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
                    //数据变化
                    Log.i(TAG, "onMetadataChanged: ");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    //播放状态变化
                    Log.i(TAG, "onPlaybackStateChanged: state=" + state);
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
    private Context mContext;

    private void initmedia(Context context) {
        mContext = context;
        mConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowser = new MediaBrowserCompat(context,
                new ComponentName("com.android.bluetooth",
                        "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService"),
                mConnectionCallback, null);
        mMediaBrowser.connect();
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        public MediaBrowserConnectionCallback() {
            super();
            Log.i(TAG, "MediaBrowserConnectionCallback: ");
        }

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
                    //    MediaControllerCompat.setMediaController(MainActivity.this, mMediaController);
                    mMediaController.registerCallback(controllerCallback);
                    Log.i(TAG, "onConnected: ok");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.i(TAG, "onConnected: 182");
                }
            }
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
}
