package com.fxc.ev.mediacenter.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface ConnectBlueCallBack {
    void onStartConnect();

    void onConnectSuccess(BluetoothDevice device/*, BluetoothSocket bluetoothSocket*/);

    void onConnectFail(BluetoothDevice device, String string);
}
