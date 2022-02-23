package com.example.fxc.mediaplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.example.fxc.bt.BtMusicManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sandra on 2022/2/18.
 */

public class MediaDeviceManager {
    private static volatile MediaDeviceManager sInstance;
    private StorageManager mStorageManager;
    private List<StorageVolume> volumes;
    private DeviceInfo currentDevice;
    private List<DeviceInfo> externalDeviceInfos = new ArrayList<DeviceInfo>();

    public MediaDeviceManager() {
    }

    public static MediaDeviceManager getInstance() {
        if (sInstance == null) {
            synchronized (MediaDeviceManager.class) {
                if (sInstance == null) {
                    sInstance = new MediaDeviceManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取所有外置存储器的目录
     *
     * @return
     */
    public List<DeviceInfo> getExternalDeviceInfoList(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        volumes = mStorageManager.getStorageVolumes(); //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        externalDeviceInfos = new ArrayList<>();//最好是可以监测设备连接状态进行刷新
        try {
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolume storageVolume = volumes.get(i);//获取每个挂载的StorageVolume

                //通过反射调用getPath、isRemovable
                String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                String description = storageVolume.getDescription(context);
                Log.d("jason", " i=" + i + " ,storagePath=" + storagePath
                        + " ,isRemovableResult=" + isRemovableResult + " ,description=" + description);
                DeviceInfo externalDeviceInfo = new DeviceInfo();
                //   if (isRemovableResult){//Sandra@20220210 剔除内部存储
                externalDeviceInfo.setStoragePath(storagePath);
                externalDeviceInfo.setRemovableResult(isRemovableResult);
                externalDeviceInfo.setDescription(description);
                externalDeviceInfo.setResImage(R.drawable.icon_usb);//此處設置設備圖標icon_usb/icon_bt
                externalDeviceInfos.add(externalDeviceInfo);
                //  }

            }
        } catch (Exception e) {
            Log.d("jason", " e:" + e);
        }

        if (BtMusicManager.getInstance().isEnabled()) {
            for (BluetoothDevice device : BtMusicManager.getInstance().getBondedDevices()) {
                DeviceInfo info = new DeviceInfo();
                info.setDescription(device.getName() + (device.isConnected() ? "(已连接)" : ""));
                info.setBtDeviceAddress(BtMusicManager.getInstance().getBTDeviceAddress());
                info.setType(Constants.BLUETOOTH_DEVICE);
                externalDeviceInfos.add(info);
            }
        }
        if (externalDeviceInfos != null && externalDeviceInfos.size() > 0) {
            setCurrentDevice(externalDeviceInfos.get(0));
        }
        return externalDeviceInfos;
    }


    public DeviceInfo getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(DeviceInfo deviceInfo) {
        currentDevice = deviceInfo;
    }
}
