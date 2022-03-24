package com.fxc.ev.mediacenter.bt;

import android.bluetooth.BluetoothDevice;

public interface ConnectBlueCallBack {
    void onStartConnect();

    void onConnectSuccess(BluetoothDevice device/*, BluetoothSocket bluetoothSocket*/);

    void onConnectFail(BluetoothDevice device, String string);
}
