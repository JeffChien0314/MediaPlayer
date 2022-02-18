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

public class ExternalStorageDeviceUtil {
    private static volatile ExternalStorageDeviceUtil sInstance;
    private StorageManager mStorageManager;
    private List<StorageVolume> volumes;
    public ExternalStorageDeviceUtil() {
    }
    private List<ExternalDeviceInfo> externalDeviceInfos = new ArrayList<ExternalDeviceInfo>();
    public static ExternalStorageDeviceUtil getInstance() {
        if (sInstance == null) {
            synchronized (BtMusicManager.class) {
                if (sInstance == null) {
                    sInstance = new ExternalStorageDeviceUtil();
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
  public   List<ExternalDeviceInfo> getExternalDeviceInfoList(Context context) {

        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        volumes = mStorageManager.getStorageVolumes();
        try {
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolume storageVolume = volumes.get(i);//获取每个挂
                // 载的StorageVolume
                //通过反射调用getPath、isRemovable

                String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                String description = storageVolume.getDescription(context);
                Log.d("jason", " i=" + i + " ,storagePath=" + storagePath
                        + " ,isRemovableResult=" + isRemovableResult + " ,description=" + description);
                ExternalDeviceInfo externalDeviceInfo = new ExternalDeviceInfo();
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
            for(BluetoothDevice device:BtMusicManager.getInstance().getBondedDevices()){
                ExternalDeviceInfo info = new ExternalDeviceInfo();
                info.setDescription(device.getName()+(device.isConnected()?"(已连接)":""));
                info.setBtDeviceAddress(BtMusicManager.getInstance().getBTDeviceAddress());
                info.setType(Constants.BLUETOOTH_DEVICE);
                externalDeviceInfos.add(info);
            }

            // info.setBtDeviceUUID(B);
            //  info.setBluetoothDevice();
        }
        return externalDeviceInfos;
    }
}
