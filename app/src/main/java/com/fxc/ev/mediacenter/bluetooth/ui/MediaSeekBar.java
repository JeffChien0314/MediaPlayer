package com.fxc.ev.mediacenter.bluetooth.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.bluetooth.client.MediaBrowserConnecter;
import com.fxc.ev.mediacenter.util.MediaItemUtil;

/**
 * SeekBar that can be used with a {@link MediaSessionCompat} to track and seek in playing
 * media.
 */

public class MediaSeekBar extends AppCompatSeekBar {
    private MediaControllerCompat mMediaController;
    private ControllerCallback mControllerCallback;
    private final int INIT_UPDATE = 0;
    private final int REGULAR_UPDATE = 1;
    private MediaMetadataCompat metadataCompat;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REGULAR_UPDATE:
                    if(null==mMediaController) break;
                    try {
                        final int progress = mMediaController.getPlaybackState() != null
                                ? (int) mMediaController.getPlaybackState().getPosition()
                                : 0;
                        setProgress(progress);
                        ((TextView) ((View) getParent()).findViewById(R.id.current)).setText(MediaItemUtil.formatTime(mMediaController.getPlaybackState().getPosition()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendMsg(REGULAR_UPDATE, 1000);
                    break;
                case INIT_UPDATE:
                    if(null==mMediaController) break;
                    metadataCompat = mMediaController.getMetadata();
                    if (null != mMediaController.getMetadata()) {
                        setMax((int) mMediaController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                    }
                    sendMsg(REGULAR_UPDATE, 0);
                    break;
            }
        }
    };

    /* private boolean mIsTracking = false;
    * private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
              mIsTracking = true;
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
              if (mMediaController != null) {
                  mMediaController.getTransportControls().seekTo(getProgress());
                  mIsTracking = false;
              }
          }
      };*/
    private ValueAnimator mProgressAnimator;

    public MediaSeekBar(Context context) {
        super(context);
        //   super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //  super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //  super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

   /* @Override
    public final void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // Prohibit adding seek listeners to this subclass.
        throw new UnsupportedOperationException("Cannot add listeners to a MediaSeekBar");
    }*/

    private void sendMsg(int what, int delayTime) {
        Message message = new Message();
        message.what = what;
        mHandler.sendMessageDelayed(message, delayTime);

    }

    public void setMediaController(final MediaControllerCompat mediaController) {
        if (mediaController != null) {
            mControllerCallback = new ControllerCallback();
            mediaController.registerCallback(mControllerCallback);
        } else if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
        }
        mMediaController = mediaController;
        sendMsg(INIT_UPDATE, 0);
    }

    public void disconnectController(Context context) {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
            mMediaController = null;
        }
        MediaBrowserConnecter.getInstance(context).release();
    }

    private class ControllerCallback
            extends MediaControllerCompat.Callback
            implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            final int max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;
            setMax(max);
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            // If the user is changing the slider, cancel the animation.
           /* if (mIsTracking) {
                valueAnimator.cancel();
                return;
            }*/

           /* final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            setProgress(animatedIntValue);*/
        }
    }
}
