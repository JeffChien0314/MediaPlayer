package com.example.fxc.mediaplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.example.fxc.bt.BtMusicManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;

/**
 * Created by Sandra on 2022/2/18.
 */

public class MediaDeviceManager {
   // private static volatile MediaDeviceManager sInstance;
    private StorageManager mStorageManager;
    private List<StorageVolume> volumes;
    public  DeviceInfo currentDevice;
    private List<DeviceInfo> externalDeviceInfos = new ArrayList<DeviceInfo>();

    public MediaDeviceManager() {
    }

  /*  public static MediaDeviceManager getInstance() {
        if (sInstance == null) {
            synchronized (MediaDeviceManager.class) {
                if (sInstance == null) {
                    sInstance = new MediaDeviceManager();
                }
            }
        }
        return sInstance;
    }*/

    /**
     * 获取所有外置存储器的目录
     *
     * @return
     */
    public List<DeviceInfo> getExternalDeviceInfoList(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        volumes = mStorageManager.getStorageVolumes(); //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        externalDeviceInfos = new ArrayList<>();//最好是可以监测设备连接状态进行刷新
        if (volumes==null || volumes.size()==0){return externalDeviceInfos;}

        try {
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                if (volumes!=null && volumes.get(i)!=null){
                    StorageVolume storageVolume = volumes.get(i);//获取每个挂载的StorageVolume
                    //通过反射调用getPath、isRemovable
                    String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                    boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                    String description = storageVolume.getDescription(context);
                    DeviceInfo externalDeviceInfo = new DeviceInfo();
                    if (isRemovableResult){//Sandra@20220210 剔除内部存储
                        externalDeviceInfo.setStoragePath(storagePath);
                        externalDeviceInfo.setRemovableResult(isRemovableResult);
                        externalDeviceInfo.setDescription(description);
                        externalDeviceInfo.setResImage(R.drawable.icon_usb);//此處設置設備圖標icon_usb/icon_bt
                        externalDeviceInfos.add(externalDeviceInfo);
                    }
                }

            }
        } catch (Exception e) {
            Log.d("jason", " e:" + e);
        }

        if (BtMusicManager.getInstance().isEnabled()) {
            for (BluetoothDevice device : BtMusicManager.getInstance().getBondedDevices()) {
                DeviceInfo info = new DeviceInfo();
                info.setDescription(device.getName() + (device.isConnected() ? "(已连接)" : ""));
                info.setBluetoothDevice(device);
                info.setType(Constants.BLUETOOTH_DEVICE);
                externalDeviceInfos.add(info);
            }
        }
        return externalDeviceInfos;
    }


    public DeviceInfo getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(DeviceInfo deviceInfo) {
        currentDevice = deviceInfo;
    }

    public boolean ifExsitThisDeviceByStoragePath(String StoragePath) {
        if (StoragePath == null) return false;
        boolean exist = false;
        if (externalDeviceInfos != null && externalDeviceInfos.size() > 0) {
            for (int i = 0; i < externalDeviceInfos.size(); i++) {
                if (externalDeviceInfos.get(i).getType() == USB_DEVICE) {
                    if (StoragePath.equals(externalDeviceInfos.get(i).getStoragePath())) {
                        exist = true;
                    }
                } else {//蓝牙设备
                }
                }
                /*if (!exist){//不存在设备时，清除内存
                    List<MediaInfo>  mediaInfos = MediaUtil.getMusicInfos(context, deviceInfo.getStoragePath());
                    for (int i=0;i<mediaInfos.size();i++){
                        Bitmap bitmap=mediaInfos.get(i).getThumbBitmap();
                        if(bitmap!=null && !bitmap.isRecycled()){
                            bitmap.recycle();
                            bitmap = null;
                        }
                        System.gc();

                }}*/

        }
       return exist;
    }
    public DeviceInfo getDeviceByStoragePath(String StoragePath){
        if (externalDeviceInfos != null && externalDeviceInfos.size() > 0) {
            for (int i = 0; i < externalDeviceInfos.size(); i++) {
                if (externalDeviceInfos.get(i).getType() == USB_DEVICE) {
                    if (StoragePath.equals(externalDeviceInfos.get(i).getStoragePath())) {
                       return externalDeviceInfos.get(i);
                    }
                } else {//蓝牙设备
                }
            }

        }
        return null;
    }
}
