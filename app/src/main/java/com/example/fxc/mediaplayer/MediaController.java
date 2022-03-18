package com.example.fxc.mediaplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.fxc.bt.BtMusicManager;
import com.example.fxc.bt.ConnectBlueCallBack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.example.fxc.mediaplayer.CSDMediaPlayer.ACTION_CHANGE_STATE_RECEIVER;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.POS_EXTRA;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.STATE_EXTRA;
import static com.example.fxc.mediaplayer.Constants.BLUETOOTH_DEVICE;
import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;

/**
 *
 */
public class MediaController {
    private final String TAG = MediaController.class.getSimpleName();
    private static MediaController mInstance;
    private static Context mContext;
    public int currentSourceType = BLUETOOTH_DEVICE;


    private final ConnectBlueCallBack mConnectBlueCallBack = new ConnectBlueCallBack() {
        @Override
        public void onStartConnect() {
            Log.i(TAG, "onStartConnect: ");
            Toast.makeText(mContext, "start to connect the buletooth device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectSuccess(BluetoothDevice device) {
            Log.i(TAG, "onConnectSuccess: ");
            Toast.makeText(mContext, "Bluetooth device connect successfully", Toast.LENGTH_SHORT).show();
            //connect成功去更新设备列表
        }

        @Override
        public void onConnectFail(BluetoothDevice device, String string) {
            Log.i(TAG, "onConnectFail: ");
            Toast.makeText(mContext, "The bluetooth device  is unable to connect", Toast.LENGTH_SHORT).show();
        }
    };

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
        if (deviceInfo.getType() == BLUETOOTH_DEVICE) {
            if (deviceInfo.getBluetoothDevice().isConnected()) {
                //展示音乐列表，获取播放状态
                ArrayList<MediaItem> items = new ArrayList<>();
                MediaInfo mediaInfo = new MediaInfo(items, deviceInfo);
                return mediaInfo;
            } else {
                try {
                    if (deviceInfo.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_NONE) {
                        Method m = BluetoothDevice.class.getMethod("createBond");
                        m.invoke(deviceInfo.getBluetoothDevice());
                    } else if (deviceInfo.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                        BtMusicManager.getInstance().a2dpSinkConnect(deviceInfo.getBluetoothDevice(), mConnectBlueCallBack);//首次连接播放状态无需改变
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                }
                ArrayList<MediaItem> items = new ArrayList<>();
                MediaInfo mediaInfo = new MediaInfo(items, deviceInfo);
                return mediaInfo;
            }
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
        Intent intent = new Intent(ACTION_CHANGE_STATE_RECEIVER);
        intent.putExtra(STATE_EXTRA, state);
        intent.putExtra(POS_EXTRA, value);
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
}
