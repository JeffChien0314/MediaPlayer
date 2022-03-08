package com.example.fxc.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import static com.example.fxc.mediaplayer.CSDMediaPlayer.ACTION_CHANGE_STATE;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.POS_EXTRA;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.STATE_EXTRA;

/**
 *
 */
public class MediaController {
    private static MediaController mInstance;
    private static Context mContext;

    private MediaController(Context context) {
        mContext = context;
    }

    public static MediaController getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MediaController.class) {
                mInstance = new MediaController(context);
            }
        }
        return mInstance;
    }


    /**
     * 给UI使用设置播放器状态
     * @param state 状态
     * @param pos 位置
     */
    public void setPlayerState(int state, int pos) {
        Intent intent = new Intent(ACTION_CHANGE_STATE);
        intent.putExtra(STATE_EXTRA, state);
        intent.putExtra(POS_EXTRA, pos);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * 给UI使用抓取当前设备
     * @return
     */
    public List<DeviceInfo> getDevices() {
        return DeviceManager.getInstance(mContext).getExternalDeviceInfoList();
    }

}
