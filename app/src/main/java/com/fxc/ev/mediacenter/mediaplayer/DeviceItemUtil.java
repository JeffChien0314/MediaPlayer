package com.fxc.ev.mediacenter.mediaplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.fxc.ev.mediacenter.bt.BtMusicManager;
import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.util.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sandra on 2022/2/18.
 */

public class DeviceItemUtil {
    public static final String ACTION_DEVICE_CHANGED = "DeviceItemUtil.deviceChanged";
    public static final String ACTION_DEVICE_OF_LIST_LOST = "DeviceItemUtildevice.lost";
    private StorageManager mStorageManager;
    private List<StorageVolume> volumes;
    public DeviceItem currentDevice;
    private List<DeviceItem> externalDeviceItems;
    private Context mContext;
    private static DeviceItemUtil mInstance;

    public DeviceItemUtil(Context context) {
        mContext = context;
    }

    public static DeviceItemUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DeviceItemUtil.class) {
                if (mInstance == null) {
                    mInstance = new DeviceItemUtil(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取所有外置存储器的目录
     *
     * @return
     */
    public List<DeviceItem> getExternalDeviceInfoList(Context context, boolean needNotify) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        volumes = mStorageManager.getStorageVolumes(); //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        externalDeviceItems = new ArrayList<>();
        if (volumes == null || volumes.size() == 0) {
            if (needNotify) broadCastDeviceChanged();
            return externalDeviceItems;
        }

        try {
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                if (volumes != null && volumes.get(i) != null) {
                    StorageVolume storageVolume = volumes.get(i);//获取每个挂载的StorageVolume
                    //通过反射调用getPath、isRemovable
                    String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                    boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                    String description = storageVolume.getDescription(context);
                    DeviceItem externalDeviceItem = new DeviceItem();
                //    if (isRemovableResult) {//Sandra@20220210 剔除内部存储
                        externalDeviceItem.setStoragePath(storagePath);
                        externalDeviceItem.setRemovableResult(true);
                        externalDeviceItem.setDescription(description);
                        externalDeviceItem.setResImage(R.drawable.icon_usb);//此處設置設備圖標icon_usb/icon_bt
                        externalDeviceItems.add(externalDeviceItem);
               //     }
                }

            }
        } catch (Exception e) {
            Log.d("jason", " e:" + e);
        }

        if (BtMusicManager.getInstance().isEnabled()) {
            for (BluetoothDevice device : BtMusicManager.getInstance().getBondedDevices()) {
                DeviceItem info = new DeviceItem();
                info.setDescription(device.getName() + (device.isConnected() ? "(已连接)" : ""));
                info.setBluetoothDevice(device);
                info.setType(Constants.BLUETOOTH_DEVICE);
                externalDeviceItems.add(info);
            }
        }
        if (needNotify) broadCastDeviceChanged();

        return externalDeviceItems;
    }

    /**
     * 获取所有外置存储器的目录
     *
     * @return
     */
    public List<DeviceItem> getExternalDeviceInfoList() {

        getExternalDeviceInfoList(mContext, false);

        return externalDeviceItems;
    }

    public DeviceItem getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(DeviceItem deviceItem) {
        currentDevice = deviceItem;
    }

    public boolean isDeviceExist(String StoragePath) {
        if (StoragePath == null) return false;
        boolean exist = false;
        if (externalDeviceItems != null && externalDeviceItems.size() > 0) {
            for (int i = 0; i < externalDeviceItems.size(); i++) {
                if (externalDeviceItems.get(i).getType() == Constants.USB_DEVICE) {
                    if (StoragePath.equals(externalDeviceItems.get(i).getStoragePath())) {
                        exist = true;
                    }
                } else {//蓝牙设备
                }
            }
                /*if (!exist){//不存在设备时，清除内存
                    List<MediaItem>  mediaItems = MediaItemUtil.getMusicInfos(context, deviceInfo.getStoragePath());
                    for (int i=0;i<mediaItems.size();i++){
                        Bitmap bitmap=mediaItems.get(i).getThumbBitmap();
                        if(bitmap!=null && !bitmap.isRecycled()){
                            bitmap.recycle();
                            bitmap = null;
                        }
                        System.gc();

                }}*/

        }
        return exist;
    }

    public DeviceItem getDeviceByStoragePath(String StoragePath) {
        if (externalDeviceItems != null && externalDeviceItems.size() > 0) {
            for (int i = 0; i < externalDeviceItems.size(); i++) {
                if (externalDeviceItems.get(i).getType() == Constants.USB_DEVICE) {
                    if (StoragePath.equals(externalDeviceItems.get(i).getStoragePath())) {
                        return externalDeviceItems.get(i);
                    }
                } else {//蓝牙设备
                }
            }

        }
        return null;
    }

    private void broadCastDeviceChanged() {//通知设备变更，client主动去取设备
        Intent intent = new Intent(ACTION_DEVICE_CHANGED);
        intent.setPackage("com.example.fxc.mediaplayer");
        mContext.sendBroadcast(intent);

    }
}
