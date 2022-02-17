package com.example.fxc.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface ConnectBlueCallBack{
    void onStartConnect();
    void onConnectSuccess(BluetoothDevice device, BluetoothSocket bluetoothSocket);
    void onConnectFail(BluetoothDevice device, String string);
}
