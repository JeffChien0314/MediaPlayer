package com.example.fxc.mediaplayer;

import android.bluetooth.BluetoothDevice;

import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;

/**
 * Created by Sandra on 2022/2/10.
 */

public class DeviceInfo {

    private String storagePath;//设备路径
    private String description;//设备名字
    private int resImage;//设备图片
    private int type = USB_DEVICE;//设备类型 0:USB usb; 1:bluetooth
    private boolean isRemovableResult;
    private BluetoothDevice bluetoothDevice;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getResImage() {
        return resImage;
    }

    public void setResImage(int resImage) {
        this.resImage = resImage;
    }
    //設備圖標

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRemovableResult() {
        return isRemovableResult;
    }

    public void setRemovableResult(boolean removableResult) {
        isRemovableResult = removableResult;
    }
}
