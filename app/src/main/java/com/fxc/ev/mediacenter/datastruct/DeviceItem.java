package com.fxc.ev.mediacenter.datastruct;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.fxc.ev.mediacenter.util.Constants;

/**
 * Created by Sandra on 2022/2/10.
 */

public class DeviceItem implements Parcelable {

    private String storagePath;//设备路径
    private String description;//设备名字
    private int resImage;//设备图片
    private int type = Constants.USB_DEVICE;//设备类型 0:USB usb; 1:bluetooth
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
