package com.fxc.ev.mediacenter.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.fxc.ev.mediacenter.bluetooth.ConnectBlueCallBack;
import com.fxc.ev.mediacenter.bluetooth.client.MediaBrowserConnecter;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MediaController {
    private final String TAG = MediaController.class.getSimpleName();
    private static MediaController mInstance;
    private static Context mContext;
    public int currentSourceType = Constants.BLUETOOTH_DEVICE;


    private MediaController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static MediaController getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MediaController.class) {
                mInstance = new MediaController(context);
            }
        }
        return mInstance;
    }

   /* public void start2Play(List<MediaInfo> mediaInfos, int postion) {
        if (currentSourceType == USB_DEVICE) {
            CSDMediaPlayer.getInstance(mContext).setUp((ArrayList<MediaInfo>) mediaInfos, true, postion);
            CSDMediaPlayer.getInstance(mContext).startPlayLogic();
        }
    }*/

    public MediaInfo getMeidaInfosByDevice(DeviceItem deviceInfo, int media_Type, boolean needPlay) {
        if (deviceInfo == null) {
            ArrayList<MediaItem> items = new ArrayList<>();
            return new MediaInfo(items, deviceInfo);
        }
        if (needPlay) currentSourceType = deviceInfo.getType();
        if (deviceInfo.getType() == Constants.BLUETOOTH_DEVICE) {
                ArrayList<MediaItem> items = new ArrayList<>();
                MediaInfo mediaInfo = new MediaInfo(items, deviceInfo);
                return mediaInfo;
            } else {
            return MediaItemUtil.getMediaInfos(media_Type, mContext, deviceInfo);
        }

    }

    /**
     * 给UI使用改变播放器状态
     *
     * @param state
     * @param value
     */
    public void setPlayerState(int state, int value) {
        Intent intent = new Intent(Constants.ACTION_CHANGE_STATE_RECEIVER);
        intent.putExtra(CSDMediaPlayer.STATE_EXTRA, state);
        intent.putExtra(CSDMediaPlayer.POS_EXTRA, value);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * 给UI使用抓取当前设备
     *
     * @return
     */
    public List<DeviceItem> getDevices() {
        return DeviceItemUtil.getInstance(mContext).getExternalDeviceInfoList();
    }

    public void setCurrentSourceType(int device_type) {
        currentSourceType = device_type;
    }

    public boolean isBtAudioActive() {
        return MediaBrowserConnecter.getInstance(mContext).isBtAudioActive();
    }
}
